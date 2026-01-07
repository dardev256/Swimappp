package com.swimdataapp.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Model class representing a swimmer with their personal information,
 * event history, and IMX profile data.
 */
public class Swimmer {
  private final StringProperty name;
  private final StringProperty gender;
  private final StringProperty rosterGroup;
  private final ObjectProperty<LocalDate> dateOfBirth;
  private final IntegerProperty age;
  private final ObservableList<MeetResult> eventHistory;
  private final StringProperty imxLevel;
  private final IntegerProperty imxScore;
  private final ObservableList<IMXPoint> imxHistory;

  private Map<String, MeetResult> personalBests;

  /**
   * Create a new Swimmer with the given name.
   * Initializes all properties with default values.
   */
  public Swimmer(String name) {
    this.name = new SimpleStringProperty(name);
    this.gender = new SimpleStringProperty("Male"); // Default to Male
    this.rosterGroup = new SimpleStringProperty("N/A");
    this.dateOfBirth = new SimpleObjectProperty<>();
    this.age = new SimpleIntegerProperty(0);
    this.eventHistory = FXCollections.observableArrayList();
    this.imxLevel = new SimpleStringProperty("N/A");
    this.imxScore = new SimpleIntegerProperty(0);
    this.imxHistory = FXCollections.observableArrayList();

    // Listen for date of birth changes and update age automatically
    this.dateOfBirth.addListener((obs, oldVal, newVal) -> {
      if (newVal != null) {
        this.age.set(Period.between(newVal, LocalDate.now()).getYears());
      } else {
        this.age.set(0);
      }
    });
  }

  /**
   * Calculate personal bests for each event.
   * Groups event history by event name and finds the fastest time for each.
   */
  public void calculatePersonalBests() {
    personalBests = eventHistory.stream()
        .collect(Collectors.toMap(
            MeetResult::getEventName,
            r -> r,
            (r1, r2) -> r1.getTimeInSeconds() < r2.getTimeInSeconds() ? r1 : r2));
  }

  /**
   * Get all personal best times for this swimmer.
   * Lazily calculates if not already done.
   */
  public Collection<MeetResult> getPersonalBests() {
    if (personalBests == null) {
      calculatePersonalBests();
    }
    return personalBests.values();
  }

  // ===== PROPERTIES =====

  public StringProperty nameProperty() {
    return name;
  }

  public StringProperty genderProperty() {
    return gender;
  }

  public StringProperty rosterGroupProperty() {
    return rosterGroup;
  }

  public IntegerProperty ageProperty() {
    return age;
  }

  public StringProperty imxLevelProperty() {
    return imxLevel;
  }

  public IntegerProperty imxScoreProperty() {
    return imxScore;
  }

  // ===== OBSERVABLE COLLECTIONS =====

  public ObservableList<MeetResult> getEventHistory() {
    return eventHistory;
  }

  public ObservableList<IMXPoint> getImxHistory() {
    return imxHistory;
  }

  // ===== GETTERS =====

  public String getName() {
    return name.get();
  }

  public String getGender() {
    return gender.get();
  }

  public String getRosterGroup() {
    return rosterGroup.get();
  }

  public LocalDate getDateOfBirth() {
    return dateOfBirth.get();
  }

  public int getAge() {
    return age.get();
  }

  public String getImxLevel() {
    return imxLevel.get();
  }

  public int getImxScore() {
    return imxScore.get();
  }

  // ===== SETTERS =====

  public void setRosterGroup(String rosterGroup) {
    this.rosterGroup.set(rosterGroup);
  }

  public void setGender(String gender) {
    this.gender.set(gender);
  }

  public void setDateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth.set(dateOfBirth);
  }

  public void setImxLevel(String level) {
    this.imxLevel.set(level);
  }

  public void setImxScore(int score) {
    this.imxScore.set(score);
  }

  @Override
  public String toString() {
    return name.get();
  }
}
