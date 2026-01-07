package com.swimdataapp.model;

import com.swimdataapp.util.TimeParsingUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;

public class MeetResult {
  private final StringProperty eventName;
  private final StringProperty time;
  private final ObjectProperty<LocalDate> date;

  public MeetResult(String eventName, String time, LocalDate date) {
    this.eventName = new SimpleStringProperty(eventName);
    this.time = new SimpleStringProperty(time);
    this.date = new SimpleObjectProperty<>(date);
  }

  // Properties
  public StringProperty eventNameProperty() {
    return eventName;
  }

  public StringProperty timeProperty() {
    return time;
  }

  public ObjectProperty<LocalDate> dateProperty() {
    return date;
  }

  // Getters
  public String getEventName() {
    return eventName.get();
  }

  public String getTime() {
    return time.get();
  }

  public LocalDate getDate() {
    return date.get();
  }

  /**
   * Convert the time string to seconds using the centralized utility.
   */
  public double getTimeInSeconds() {
    String timeStr = time.get();
    return TimeParsingUtil.parseTimeString(timeStr);
  }
}
