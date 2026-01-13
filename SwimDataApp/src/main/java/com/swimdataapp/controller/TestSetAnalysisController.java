package com.swimdataapp.controller;

import com.swimdataapp.data.DataManager;
import com.swimdataapp.model.Swimmer;
import com.swimdataapp.model.TestSetResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class TestSetAnalysisController {

  @FXML
  private ComboBox<String> testSetSelector;
  @FXML
  private VBox contentPane;

  private Map<String, List<TestSetResult>> allResults;
  private final DataManager dataManager = DataManager.getInstance();

  /**
   * Map of test set names to their distance in hundreds (e.g., "Distance Swim" ->
   * 15.0 for 1500m)
   */
  private static final Map<String, Double> TEST_SET_DISTANCES = Map.of(
      "Distance Swim", 15.0, // 1500m
      "Sprint Test", 2.0, // 200m
      "Threshold Test", 10.0 // 1000m
  );

  @FXML
  public void initialize() {
    testSetSelector.getSelectionModel().selectedItemProperty()
        .addListener((obs, oldV, newV) -> updateView());
    
    // Listen to changes in the globally selected swimmer
    dataManager.selectedSwimmerProperty().addListener((obs, oldV, newV) -> updateView());
  }

  /**
   * Set test set data from the TestSetManager.
   */
  public void setTestSetData(Map<String, List<TestSetResult>> results) {
    this.allResults = results;
    if (results != null && !results.isEmpty()) {
      testSetSelector.setItems(
          FXCollections.observableArrayList(allResults.keySet().stream().sorted().collect(Collectors.toList())));
      if (!testSetSelector.getItems().isEmpty()) {
        testSetSelector.getSelectionModel().selectFirst();
      }
    }
    updateView();
  }

  private void updateView() {
    Swimmer selectedSwimmer = dataManager.getSelectedSwimmer();
    if (allResults == null || testSetSelector.getValue() == null || selectedSwimmer == null) {
      contentPane.getChildren().clear();
      return;
    }

    String selectedTest = testSetSelector.getValue();
    List<TestSetResult> resultsForTest = allResults.getOrDefault(selectedTest, new ArrayList<>());

    displayIndividualView(resultsForTest, selectedSwimmer.getName(), selectedTest);
  }

  /**
   * Display individual swimmer performance for a test set.
   */
  private void displayIndividualView(List<TestSetResult> results, String swimmerName, String testName) {
    contentPane.getChildren().clear();

    List<TestSetResult> swimmerResults = results.stream()
        .filter(r -> r.getSwimmerName().equals(swimmerName))
        .sorted(Comparator.comparing(TestSetResult::getDate))
        .collect(Collectors.toList());

    if (swimmerResults.isEmpty()) {
      Label noDataLabel = new Label("No data available for this swimmer in this test set.");
      noDataLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #a9a9a9;");
      contentPane.getChildren().add(noDataLabel);
      return;
    }

    // Create performance chart
    LineChart<String, Number> chart = createPerformanceChart(swimmerName, testName, swimmerResults);

    VBox layout = new VBox(20, chart);
    layout.setStyle("-fx-padding: 10;");

    // Add appropriate table based on test type
    if ("Distance Swim".equals(testName)) {
      layout.getChildren().add(createPaceTable(swimmerResults, testName));
    } else {
      layout.getChildren().add(createSimpleResultsTable(swimmerResults));
    }

    contentPane.getChildren().add(layout);
  }

  /**
   * Create a performance chart for the test set.
   */
  private LineChart<String, Number> createPerformanceChart(String swimmerName, String testName,
      List<TestSetResult> results) {
    LineChart<String, Number> chart = new LineChart<>(new javafx.scene.chart.CategoryAxis(),
        new javafx.scene.chart.NumberAxis());
    chart.setTitle(swimmerName + " - Progress for " + testName);
    chart.setStyle("-fx-font-size: 12;");

    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Performance (seconds)");

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    for (TestSetResult result : results) {
      series.getData().add(new XYChart.Data<>(result.getDate().format(formatter), result.getPrimaryMetric()));
    }
    chart.getData().add(series);
    chart.setMinHeight(300);

    return chart;
  }

  /**
   * Create a simple results table for non-distance test sets.
   */
  private TableView<TestSetResult> createSimpleResultsTable(List<TestSetResult> swimmerResults) {
    TableView<TestSetResult> table = new TableView<>(FXCollections.observableArrayList(swimmerResults));

    TableColumn<TestSetResult, LocalDate> dateCol = new TableColumn<>("Date");
    dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
    dateCol.setPrefWidth(150);

    TableColumn<TestSetResult, Double> timeCol = new TableColumn<>("Time (s)");
    timeCol.setCellValueFactory(new PropertyValueFactory<>("primaryMetric"));
    timeCol.setPrefWidth(150);

    table.getColumns().addAll(dateCol, timeCol);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    table.setMinHeight(200);

    return table;
  }

  /**
   * value.
   * Calculate pace per 100m based on test set distance configuration.
   */
  private TableView<Map.Entry<TestSetResult, String>> createPaceTable(List<TestSetResult> swimmerResults,
      String testName) {

    double distanceInHundreds = TEST_SET_DISTANCES.getOrDefault(testName, 15.0);

    // Validate distance is positive to prevent division by zero
    if (distanceInHundreds <= 0) {
      distanceInHundreds = 15.0; // Default fallback
    }

    TableView<Map.Entry<TestSetResult, String>> table = new TableView<>();

    TableColumn<Map.Entry<TestSetResult, String>, String> dateCol = new TableColumn<>("Date");
    dateCol.setCellValueFactory(cellData -> new SimpleStringProperty(
        cellData.getValue().getKey().getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
    dateCol.setPrefWidth(120);

    TableColumn<Map.Entry<TestSetResult, String>, String> timeCol = new TableColumn<>("Total Time (s)");
    timeCol.setCellValueFactory(cellData -> new SimpleStringProperty(
        String.format("%.2f", cellData.getValue().getKey().getPrimaryMetric())));
    timeCol.setPrefWidth(140);

    TableColumn<Map.Entry<TestSetResult, String>, String> paceCol = new TableColumn<>("Avg 100 Pace (s)");
    paceCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue()));
    paceCol.setPrefWidth(140);

    table.getColumns().addAll(dateCol, timeCol, paceCol);

    // Calculate pace for each result
    Map<TestSetResult, String> paceData = new LinkedHashMap<>();
    final double finalDistance = distanceInHundreds;
    for (TestSetResult result : swimmerResults) {
      double pace = result.getPrimaryMetric() / finalDistance;
      paceData.put(result, String.format("%.2f", pace));
    }

    table.setItems(FXCollections.observableArrayList(paceData.entrySet()));
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    table.setMinHeight(200);

    return table;
  }
}
