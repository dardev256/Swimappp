package com.swimdataapp.controller;

import com.swimdataapp.data.DataManager;
import com.swimdataapp.model.IMXPoint;
import com.swimdataapp.model.MeetResult;
import com.swimdataapp.model.Swimmer;
import com.swimdataapp.services.TimeStandardService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.Objects;

public class DashboardController {

  @FXML
  private ComboBox<Swimmer> swimmerSelector;
  @FXML
  private Label nameLabel, ageLabel, rosterGroupLabel, imxScoreLabel, imxLevelLabel;
  @FXML
  private LineChart<String, Number> imxHistoryChart;
  @FXML
  private TableView<MeetResult> bestTimesTable;
  @FXML
  private TableColumn<MeetResult, String> eventColumn;
  @FXML
  private TableColumn<MeetResult, String> timeColumn;
  @FXML
  private TableColumn<MeetResult, LocalDate> dateColumn;
  @FXML
  private TableColumn<MeetResult, String> standardColumn;
  @FXML
  private TabPane dashboardTabPane;
  @FXML
  private VBox imxDetailsTabContent;
  @FXML
  private VBox testSetAnalysisTabContent;

  private final DataManager dataManager = DataManager.getInstance();
  private final TimeStandardService timeStandardService = new TimeStandardService();

  @FXML
  public void initialize() {
    swimmerSelector.setItems(dataManager.getSwimmers());
    swimmerSelector.valueProperty().bindBidirectional(dataManager.selectedSwimmerProperty());

    // The individual tabs will listen to selectedSwimmerProperty changes
    // No need to call updateDashboard directly from here anymore
    // dataManager.selectedSwimmerProperty().addListener((obs, oldV, newV) -> {
    //   if (newV != null) {
    //     updateDashboard(newV);
    //   }
    // });

    setupDashboardTable();

    // Load content into tabs
    try {
      // Load IMX Individual View
      FXMLLoader imxLoader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/swimdataapp/view/IMXIndividualView.fxml")));
      imxDetailsTabContent.getChildren().add(imxLoader.load());

      // Load Test Set Analysis View
      FXMLLoader testSetLoader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/swimdataapp/view/TestSetAnalysis.fxml")));
      testSetAnalysisTabContent.getChildren().add(testSetLoader.load());

    } catch (IOException e) {
      // Log or handle the exception appropriately
      System.err.println("Failed to load tab content: " + e.getMessage());
      e.printStackTrace();
    }

    // Initial update for the overview tab
    if (dataManager.getSelectedSwimmer() != null) {
      updateDashboard(dataManager.getSelectedSwimmer());
    } else if (!dataManager.getSwimmers().isEmpty()) {
      updateDashboard(dataManager.getSwimmers().get(0));
    }
  }

  private void setupDashboardTable() {
    eventColumn.setCellValueFactory(new PropertyValueFactory<>("eventName"));
    timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
    dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
    standardColumn.setCellValueFactory(cellData -> {
      MeetResult result = cellData.getValue();
      Swimmer swimmer = dataManager.getSelectedSwimmer();
      if (swimmer == null)
        return null;
      String standard = timeStandardService.getStandardForTime(result.getEventName(), swimmer.ageProperty().get(),
          swimmer.getGender(), result.getTimeInSeconds());
      return new javafx.beans.property.SimpleStringProperty(standard);
    });
  }

  private void updateDashboard(Swimmer swimmer) {
    nameLabel.setText(swimmer.getName());
    ageLabel.setText(swimmer.ageProperty().asString().get());
    rosterGroupLabel.setText(swimmer.rosterGroupProperty().get());
    imxScoreLabel.setText(swimmer.imxScoreProperty().asString().get());
    imxLevelLabel.setText(swimmer.imxLevelProperty().get());

    updateBestTimesTable(swimmer);
    updateIMXHistoryChart(swimmer);
  }

  private void updateBestTimesTable(Swimmer swimmer) {
    Map<String, MeetResult> bestTimesMap = swimmer.getEventHistory().stream()
        .collect(Collectors.groupingBy(
            MeetResult::getEventName,
            Collectors.minBy(Comparator.comparing(MeetResult::getTimeInSeconds))))
        .values().stream()
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toMap(MeetResult::getEventName, result -> result));

    bestTimesTable.setItems(FXCollections.observableArrayList(bestTimesMap.values()));
    bestTimesTable.refresh();
  }

  private void updateIMXHistoryChart(Swimmer swimmer) {
    imxHistoryChart.getData().clear();
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("IMX Score");

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
    for (IMXPoint point : swimmer.getImxHistory()) {
      series.getData().add(new XYChart.Data<>(point.getDate().format(formatter), point.getScore()));
    }
    imxHistoryChart.getData().add(series);
  }
}
