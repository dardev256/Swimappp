module com.swimdataapp {
  requires javafx.controls;
  requires javafx.fxml;
  requires org.apache.poi.ooxml;
  requires java.logging;

  opens com.swimdataapp.controller to javafx.fxml;
  opens com.swimdataapp.model to javafx.base;
  opens com.swimdataapp.data to javafx.fxml;
  opens com.swimdataapp.util to javafx.fxml;

  exports com.swimdataapp;
  exports com.swimdataapp.data;
  exports com.swimdataapp.util;
}
