package com.swimdataapp.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class IMXProfile {
  private String swimmerName;
  private final int totalScore;
  private final String level;
  private final int scoreForCurrentLevel;
  private final int scoreForNextLevel;
  private final ObservableList<IMXEvent> contributingEvents;

  /**
   * Constructor for temporary profile creation during calculation.
   */
  public IMXProfile(int totalScore, String level, int scoreForCurrentLevel, int scoreForNextLevel) {
    this.totalScore = totalScore;
    this.level = level;
    this.scoreForCurrentLevel = scoreForCurrentLevel;
    this.scoreForNextLevel = scoreForNextLevel;
    this.contributingEvents = FXCollections.observableArrayList();
  }

  /**
   * Constructor for final profile creation with contributing events.
   * This is the primary constructor used after calculations are complete.
   */
  public IMXProfile(int totalScore, String level, int scoreForCurrentLevel, int scoreForNextLevel,
      ObservableList<IMXEvent> events) {
    this.totalScore = totalScore;
    this.level = level;
    this.scoreForCurrentLevel = scoreForCurrentLevel;
    this.scoreForNextLevel = scoreForNextLevel;
    this.contributingEvents = events != null ? events : FXCollections.observableArrayList();
  }

  // Getters and Setters
  public String getSwimmerName() {
    return swimmerName;
  }

  public void setSwimmerName(String swimmerName) {
    this.swimmerName = swimmerName;
  }

  public int getTotalScore() {
    return totalScore;
  }

  public String getLevel() {
    return level;
  }

  public int getScoreForCurrentLevel() {
    return scoreForCurrentLevel;
  }

  public int getScoreForNextLevel() {
    return scoreForNextLevel;
  }

  public ObservableList<IMXEvent> getContributingEvents() {
    return contributingEvents;
  }
}
