package com.swimdataapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.util.Objects;

public class IMXDashboardController {

  @FXML
  private StackPane contentStackPane;
  @FXML
  private ToggleGroup viewToggle;
  @FXML
  private RadioButton summaryRadio;

  private Pane individualView;
  private Pane summaryView;

  @FXML
  public void initialize() {
    try {
      individualView = FXMLLoader
          .load(Objects.requireNonNull(getClass().getResource("/com/swimdataapp/view/IMXIndividualView.fxml")));

      summaryView = FXMLLoader
          .load(Objects.requireNonNull(getClass().getResource("/com/swimdataapp/view/IMXSummaryView.fxml")));

      contentStackPane.getChildren().addAll(individualView, summaryView);

    } catch (IOException e) {
      e.printStackTrace();
    }

    viewToggle.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
      if (summaryRadio.isSelected()) {
        showView(summaryView);
      } else {
        showView(individualView);
      }

    });

    showView(individualView);
  }

  // Helper method to manage view visibility
  private void showView(Pane viewToShow) {
    for (Node child : contentStackPane.getChildren()) {
      child.setVisible(child == viewToShow);
    }
  }
}
