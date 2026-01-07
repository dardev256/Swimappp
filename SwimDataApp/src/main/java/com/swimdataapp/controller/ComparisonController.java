package com.swimdataapp.controller;

import com.swimdataapp.data.DataManager;
import com.swimdataapp.model.IMXPoint;
import com.swimdataapp.model.MeetResult;
import com.swimdataapp.model.Swimmer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ComparisonController {

  @FXML
  private ComboBox<Swimmer> swimmerASelector;
  @FXML
  private ComboBox<Swimmer> swimmerBSelector;
  @FXML
  private TableView<String> pbComparisonTable;
  @FXML
  private TableColumn<String, String> eventColumn;
  @FXML
  private TableColumn<String, String> swimmerAColumn;
  @FXML
  private TableColumn<String, String> swimmerBColumn;
  @FXML
  private LineChart<String, Number> imxComparisonChart;

  private final DataManager dataManager = DataManager.getInstance();

  @FXML
  public void initialize() {
    swimmerASelector.setItems(dataManager.getSwimmers());
    swimmerBSelector.setItems(dataManager.getSwimmers());

    setupTableColumns();

    swimmerASelector.valueProperty().addListener((obs, oldV, newV) -> updateComparison());
    swimmerBSelector.valueProperty().addListener((obs, oldV, newV) -> updateComparison());
  }

  private void setupTableColumns() {
    eventColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
    swimmerAColumn.setCellValueFactory(
        cellData -> new SimpleStringProperty(getBestTime(swimmerASelector.getValue(), cellData.getValue())));
    swimmerBColumn.setCellValueFactory(
        cellData -> new SimpleStringProperty(getBestTime(swimmerBSelector.getValue(), cellData.getValue())));

    setCellFactory(swimmerAColumn, swimmerBColumn);
    setCellFactory(swimmerBColumn, swimmerAColumn);
  }

  private void updateComparison() {
    Swimmer swimmerA = swimmerASelector.getValue();
    Swimmer swimmerB = swimmerBSelector.getValue();

    if (swimmerA == null || swimmerB == null) {
      pbComparisonTable.setItems(FXCollections.emptyObservableList());
      imxComparisonChart.getData().clear();
      return;
    }

    updatePbTable(swimmerA, swimmerB);
    updateImxChart(swimmerA, swimmerB);
  }

  private void updatePbTable(Swimmer swimmerA, Swimmer swimmerB) {
    Set<String> allEvents = new HashSet<>();
    swimmerA.getEventHistory().forEach(r -> allEvents.add(r.getEventName()));
    swimmerB.getEventHistory().forEach(r -> allEvents.add(r.getEventName()));

    pbComparisonTable
        .setItems(FXCollections.observableArrayList(allEvents.stream().sorted().collect(Collectors.toList())));
    pbComparisonTable.refresh();
  }

  private void updateImxChart(Swimmer swimmerA, Swimmer swimmerB) {
    imxComparisonChart.getData().clear();
    imxComparisonChart.getData().add(createImxSeries(swimmerA));
    imxComparisonChart.getData().add(createImxSeries(swimmerB));
  }

  private XYChart.Series<String, Number> createImxSeries(Swimmer swimmer) {
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName(swimmer.getName());
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
    for (IMXPoint point : swimmer.getImxHistory()) {
      series.getData().add(new XYChart.Data<>(point.getDate().format(formatter), point.getScore()));
    }
    return series;
  }

  private String getBestTime(Swimmer swimmer, String event) {
    if (swimmer == null)
      return "-";
    return swimmer.getEventHistory().stream()
        .filter(r -> r.getEventName().equals(event))
        .min(Comparator.comparing(MeetResult::getTimeInSeconds))
        .map(MeetResult::getTime)
        .orElse("-");
  }

  private void setCellFactory(TableColumn<String, String> targetCol, TableColumn<String, String> otherCol) {
    targetCol.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(item);
        if (empty || item == null) {
          setStyle("");
        } else {
          int rowIndex = getIndex();
          if (rowIndex >= 0 && rowIndex < getTableView().getItems().size()) {
            String otherItem = otherCol.getCellData(rowIndex);
            if (!"-".equals(item) && !"-".equals(otherItem)) {
              double myTime = parseTime(item);
              double otherTime = parseTime(otherItem);
              if (myTime < otherTime) {
                setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;"); // Green for faster
              } else {
                setStyle("");
              }
            } else {
              setStyle("");
            }
          }
        }
      }
    });
  }

  private double parseTime(String timeStr) {
    if (timeStr == null || timeStr.equals("-"))
      return Double.MAX_VALUE;
    return new MeetResult("", timeStr, null).getTimeInSeconds();
  }
}
