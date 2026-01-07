package com.swimdataapp.controller;

import com.swimdataapp.data.DataManager;
import com.swimdataapp.model.Swimmer;
import com.swimdataapp.services.FileParsingService;
import com.swimdataapp.services.IMXService;
import com.swimdataapp.services.TestSetManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainViewController {

  private static final Logger LOGGER = Logger.getLogger(MainViewController.class.getName());

  @FXML
  private VBox navRail;
  @FXML
  private StackPane contentStackPane;

  private final FileParsingService fileParsingService = new FileParsingService();
  private final TestSetManager testSetManager = new TestSetManager();
  private final IMXService imxService = new IMXService();
  private final DataManager dataManager = DataManager.getInstance();

  private Pane dashboardView, comparisonView, testSetView, imxView;
  private TestSetAnalysisController testSetAnalysisController;

  private volatile boolean isLoading = false;

  @FXML
  public void initialize() throws IOException {
    loadViews();
    contentStackPane.getChildren().addAll(dashboardView, comparisonView, testSetView, imxView);
    showSwimmerDashboard();
  }

  private void loadViews() throws IOException {
    try {
      dashboardView = FXMLLoader
          .load(Objects.requireNonNull(getClass().getResource("/com/swimdataapp/view/DashboardView.fxml")));
      comparisonView = FXMLLoader
          .load(Objects.requireNonNull(getClass().getResource("/com/swimdataapp/view/ComparisonView.fxml")));

      FXMLLoader testSetLoader = new FXMLLoader(
          Objects.requireNonNull(getClass().getResource("/com/swimdataapp/view/TestSetAnalysis.fxml")));
      testSetView = testSetLoader.load();
      testSetAnalysisController = testSetLoader.getController();

      imxView = FXMLLoader
          .load(Objects.requireNonNull(getClass().getResource("/com/swimdataapp/view/IMXDashboard.fxml")));
    } catch (NullPointerException e) {
      LOGGER.log(Level.SEVERE, "Could not load FXML resource: " + e.getMessage(), e);
      showErrorAlert("Resource Error", "Could not load application views. Check that all FXML files are present.");
      throw e;
    }
  }

  /**
   * Shows file chooser and loads Excel data on a background thread.
   */
  @FXML
  private void handleLoadFile() {
    if (isLoading) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle("Load in Progress");
      alert.setHeaderText("Data Load Already Running");
      alert.setContentText("Data is already being loaded. Please wait for it to complete.");
      alert.showAndWait();
      return;
    }

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open Swim Data File");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
    File selectedFile = fileChooser.showOpenDialog(null);

    if (selectedFile != null) {
      Button loadButton = findLoadButton();
      if (loadButton == null) {
        LOGGER.warning("Load Data button not found in navigation rail");
        return;
      }

      isLoading = true;
      loadButton.setDisable(true);

      Task<List<Swimmer>> loadDataTask = createLoadDataTask(selectedFile);
      setupTaskHandlers(loadDataTask, loadButton);

      new Thread(loadDataTask).start();
    }
  }

  /**
   * Create the background task for loading and processing swim data.
   */
  private Task<List<Swimmer>> createLoadDataTask(File file) {
    return new Task<>() {
      @Override
      protected List<Swimmer> call() throws Exception {
        List<Swimmer> swimmers = fileParsingService.parseUnifiedFile(file.getAbsolutePath());

        for (Swimmer swimmer : swimmers) {
          // Calculate IMX profile and update swimmer
          var profile = imxService.calculateIMXProfile(swimmer);
          if (profile != null) {
            swimmer.setImxScore(profile.getTotalScore());
            swimmer.setImxLevel(profile.getLevel());
          }

          // Calculate IMX history and personal bests
          swimmer.getImxHistory().setAll(imxService.calculateIMXHistory(swimmer));
          swimmer.calculatePersonalBests();
        }

        testSetManager.loadTestSetsFromFile(file.getAbsolutePath());
        return swimmers;
      }
    };
  }

  /**
   * Setup success and failure handlers for the load data task.
   */
  private void setupTaskHandlers(Task<List<Swimmer>> task, Button loadButton) {
    task.setOnSucceeded(event -> {
      List<Swimmer> swimmers = task.getValue();
      Platform.runLater(() -> {
        dataManager.setSwimmers(swimmers);
        testSetAnalysisController.setTestSetData(testSetManager.getAllResults());
        isLoading = false;
        loadButton.setDisable(false);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Data Loaded Successfully");
        alert.setContentText("Loaded " + swimmers.size() + " swimmers.");
        alert.showAndWait();
      });
    });

    task.setOnFailed(event -> {
      Throwable ex = task.getException();
      LOGGER.log(Level.SEVERE, "Error loading file", ex);
      Platform.runLater(() -> {
        isLoading = false;
        loadButton.setDisable(false);
        showErrorAlert("Error Loading File",
            "Could not load the data file.\nError: " + (ex != null ? ex.getMessage() : "Unknown error"));
      });
    });

    task.setOnCancelled(event -> {
      Platform.runLater(() -> {
        isLoading = false;
        loadButton.setDisable(false);
      });
    });
  }

  /**
   * Find the Load Data button in the navigation rail.
   */
  private Button findLoadButton() {
    for (Node node : navRail.getChildren()) {
      if (node instanceof Button button) {
        if ("Load Data".equals(button.getText())) {
          return button;
        }
      }
    }
    return null;
  }

  /**
   * Show an error alert dialog.
   */
  private void showErrorAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  /**
   * Helper method to manage view visibility.
   */
  private void showView(Pane viewToShow) {
    for (Node child : contentStackPane.getChildren()) {
      child.setVisible(child == viewToShow);
    }
  }

  /**
   * Set the active navigation button styling.
   */
  private void setActiveNav(Button activeButton) {
    navRail.getChildren().forEach(node -> {
      if (node instanceof Button button) {
        button.getStyleClass().remove("nav-button-selected");
      }
    });
    activeButton.getStyleClass().add("nav-button-selected");
  }

  @FXML
  private void showSwimmerDashboard() {
    showView(dashboardView);
    setActiveNav((Button) navRail.lookup("#dashboardNavButton"));
  }

  @FXML
  private void showComparison() {
    showView(comparisonView);
    setActiveNav((Button) navRail.lookup("#compareNavButton"));
  }

  @FXML
  private void showTestSetDashboard() {
    showView(testSetView);
    setActiveNav((Button) navRail.lookup("#testSetNavButton"));
  }

  @FXML
  private void showIMXDashboard() {
    showView(imxView);
    setActiveNav((Button) navRail.lookup("#imxNavButton"));
  }
}
