package com.swimdataapp.controller;

import com.swimdataapp.data.DataManager;
import com.swimdataapp.model.IMXEvent;
import com.swimdataapp.model.IMXProfile;
import com.swimdataapp.model.Swimmer;
import com.swimdataapp.services.IMXService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class IMXIndividualController {

  @FXML
  private ComboBox<Swimmer> swimmerSelector;
  @FXML
  private Label imxScoreLabel;
  @FXML
  private ProgressBar progressToNextLevelBar;
  @FXML
  private Label progressLabel;
  @FXML
  private TableView<IMXEvent> imxEventsTable;
  @FXML
  private TableColumn<IMXEvent, String> eventCol;
  @FXML
  private TableColumn<IMXEvent, String> timeCol;
  @FXML
  private TableColumn<IMXEvent, Number> pointsCol;

  private final DataManager dataManager = DataManager.getInstance();
  private final IMXService imxService = new IMXService();

  @FXML
  public void initialize() {
    swimmerSelector.setItems(dataManager.getSwimmers());

    swimmerSelector.valueProperty().bindBidirectional(dataManager.selectedSwimmerProperty());

    // This listener correctly updates the view whenever the central selected
    // swimmer changes.
    dataManager.selectedSwimmerProperty().addListener((obs, oldV, newV) -> {
      if (newV != null) {
        displayIndividualData(newV);
      } else {
        clearIndividualView();
      }
    });

    setupTable();

    // Initial data load for the first swimmer
    if (dataManager.getSelectedSwimmer() != null) {
      displayIndividualData(dataManager.getSelectedSwimmer());
    } else {
      clearIndividualView();
    }
  }

  private void setupTable() {
    eventCol.setCellValueFactory(new PropertyValueFactory<>("eventName"));
    timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
    pointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));
  }

  private void displayIndividualData(Swimmer swimmer) {
    IMXProfile profile = imxService.calculateIMXProfile(swimmer);
    if (profile == null) {
      clearIndividualView();
      return;
    }

    imxScoreLabel.setText(profile.getTotalScore() + " points (" + profile.getLevel() + ")");

    double progress = 0.0;
    int currentScore = profile.getTotalScore();
    int scoreForCurrent = profile.getScoreForCurrentLevel();
    int scoreForNext = profile.getScoreForNextLevel();

    if (profile.getLevel().equals("Unranked")) {
      if (scoreForNext > 0) {
        progress = (double) currentScore / scoreForNext;
        int pointsNeeded = scoreForNext - currentScore;
        progressLabel.setText(
            String.format("%,d points to reach %s", pointsNeeded, imxService.getNextLevelName(profile.getLevel())));
      }
    } else if (scoreForNext <= 0 || (currentScore >= scoreForNext && profile.getLevel().equals("Level 5"))) {
      progressLabel.setText("Highest Level Achieved!");
      progress = 1.0;
    } else {
      int levelRange = scoreForNext - scoreForCurrent;
      int progressInLevel = currentScore - scoreForCurrent;

      if (levelRange > 0) {
        progress = (double) progressInLevel / levelRange;
      }

      int pointsNeeded = scoreForNext - currentScore;
      progressLabel.setText(
          String.format("%,d points to reach %s", pointsNeeded, imxService.getNextLevelName(profile.getLevel())));
    }

    progressToNextLevelBar.setProgress(progress);
    imxEventsTable.setItems(profile.getContributingEvents());
  }

  private void clearIndividualView() {
    imxScoreLabel.setText("N/A");
    progressLabel.setText("Select a swimmer to view their IMX profile.");
    progressToNextLevelBar.setProgress(0.0);
    imxEventsTable.setItems(FXCollections.emptyObservableList());
  }
}
