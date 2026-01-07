package com.swimdataapp.services;

import java.util.Map;

public class TimeStandardService {

  private static final Map<String, Double> AAA_TIMES_15_16_BOYS_SCM = Map.ofEntries(
      Map.entry("50 Freestyle", 25.39),
      Map.entry("100 Freestyle", 55.49),
      Map.entry("200 Freestyle", 120.49),
      Map.entry("400 Freestyle", 254.99),
      Map.entry("100 Backstroke", 62.09),
      Map.entry("200 Backstroke", 134.39),
      Map.entry("100 Breaststroke", 70.89),
      Map.entry("200 Breaststroke", 154.29),
      Map.entry("100 Butterfly", 60.19),
      Map.entry("200 Butterfly", 132.89),
      Map.entry("200 IM", 135.29),
      Map.entry("400 IM", 286.99));

  public String getStandardForTime(String eventName, int age, String gender, double timeInSeconds) {
    // age groups, genders, and courses.
    if (age >= 15 && age <= 16 && "Male".equalsIgnoreCase(gender)) {
      Double standard = AAA_TIMES_15_16_BOYS_SCM.get(eventName);
      if (standard != null && timeInSeconds <= standard) {
        return "AAA";
      }
    }
    return ""; // Return empty if no standard is met or defined
  }
}
