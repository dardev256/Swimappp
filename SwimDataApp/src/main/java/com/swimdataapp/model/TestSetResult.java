// TestSetResult.java
package com.swimdataapp.model;

import java.time.LocalDate;

public abstract class TestSetResult {
  private String swimmerName;
  private LocalDate date;

  public TestSetResult(String swimmerName, LocalDate date) {
    this.swimmerName = swimmerName;
    this.date = date;
  }

  public String getSwimmerName() {
    return swimmerName;
  }

  public LocalDate getDate() {
    return date;
  }

  public abstract double getPrimaryMetric();
}
