package com.swimdataapp.controller;

import com.swimdataapp.data.DataManager;
import com.swimdataapp.model.IMXProfile;
import com.swimdataapp.model.Swimmer;
import com.swimdataapp.services.IMXService;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TeamIMXSummaryController {

  @FXML
  private TableView<IMXProfile> summaryTable;
  @FXML
  private TableColumn<IMXProfile, String> nameCol;
  @FXML
  private TableColumn<IMXProfile, Integer> scoreCol;
  @FXML
  private TableColumn<IMXProfile, String> levelCol;

  private final DataManager dataManager = DataManager.getInstance();
  private final IMXService imxService = new IMXService();

  @FXML
  public void initialize() {
    setupTable();

    dataManager.getSwimmers().addListener((ListChangeListener<Swimmer>) c -> updateSummaryView());
    updateSummaryView();
  }

  private void setupTable() {
    nameCol.setCellValueFactory(new PropertyValueFactory<>("swimmerName"));
    scoreCol.setCellValueFactory(new PropertyValueFactory<>("totalScore"));
    levelCol.setCellValueFactory(new PropertyValueFactory<>("level"));
  }

  private void updateSummaryView() {
    if (dataManager.getSwimmers().isEmpty()) {
      summaryTable.setItems(FXCollections.emptyObservableList());
      return;
    }

    List<IMXProfile> profiles = dataManager.getSwimmers().stream()
        .map(swimmer -> {
          IMXProfile p = imxService.calculateIMXProfile(swimmer);
          if (p != null)
            p.setSwimmerName(swimmer.getName());
          return p;
        })
        .filter(Objects::nonNull)
        .sorted(Comparator.comparingInt(IMXProfile::getTotalScore).reversed())
        .collect(Collectors.toList());

    summaryTable.setItems(FXCollections.observableArrayList(profiles));
  }
}
