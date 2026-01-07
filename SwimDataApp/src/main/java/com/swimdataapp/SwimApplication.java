package com.swimdataapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class SwimApplication extends Application {
  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(SwimApplication.class.getResource("view/MainView.fxml"));
    Scene scene = new Scene(fxmlLoader.load(), 1200, 800);

    String css = Objects.requireNonNull(this.getClass().getResource("view/style.css")).toExternalForm();
    scene.getStylesheets().add(css);

    stage.setTitle("Swim Data App");
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
