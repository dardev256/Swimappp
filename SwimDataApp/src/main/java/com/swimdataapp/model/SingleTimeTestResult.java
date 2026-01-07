// SingleTimeTestResult.java
package com.swimdataapp.model;

import java.time.LocalDate;

public class SingleTimeTestResult extends TestSetResult {
  private double timeInSeconds;

  public SingleTimeTestResult(String swimmerName, LocalDate date, double timeInSeconds) {
    super(swimmerName, date);
    this.timeInSeconds = timeInSeconds;
  }

  @Override
  public double getPrimaryMetric() {
    return timeInSeconds;
  }
}
