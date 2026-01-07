// IMXEvent.java
package com.swimdataapp.model;

import javafx.beans.property.*;

public class IMXEvent {
  private final StringProperty eventName;
  private final StringProperty time;
  private final IntegerProperty points;

  public IMXEvent(String eventName, String time, int points) {
    this.eventName = new SimpleStringProperty(eventName);
    this.time = new SimpleStringProperty(time);
    this.points = new SimpleIntegerProperty(points);
  }

  public StringProperty eventNameProperty() {
    return eventName;
  }

  public StringProperty timeProperty() {
    return time;
  }

  public IntegerProperty pointsProperty() {
    return points;
  }
}
