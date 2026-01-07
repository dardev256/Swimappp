// IMXPoint.java
package com.swimdataapp.model;

import java.time.LocalDate;

public class IMXPoint {
  private final LocalDate date;
  private final int score;

  public IMXPoint(LocalDate date, int score) {
    this.date = date;
    this.score = score;
  }

  public LocalDate getDate() {
    return date;
  }

  public int getScore() {
    return score;
  }
}
