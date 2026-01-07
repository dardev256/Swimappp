package com.swimdataapp.services;

import com.swimdataapp.model.IMXEvent;
import com.swimdataapp.model.IMXPoint;
import com.swimdataapp.model.IMXProfile;
import com.swimdataapp.model.MeetResult;
import com.swimdataapp.model.Swimmer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class IMXService {
  private static final Map<String, Integer> IMX_LEVELS = new LinkedHashMap<>();
  static {
    IMX_LEVELS.put("Level 1", 1000);
    IMX_LEVELS.put("Level 2", 1500);
    IMX_LEVELS.put("Level 3", 2000);
    IMX_LEVELS.put("Level 4", 2700);
    IMX_LEVELS.put("Level 5", 3500);
  }

  private static final Map<String, List<String>> IMX_EVENTS_BY_AGE_GROUP = Map.of(
      "13-14", List.of("200 Freestyle", "100 Backstroke", "100 Breaststroke", "100 Butterfly", "200 IM"),
      "15-16", List.of("400 Freestyle", "200 Backstroke", "200 Breaststroke", "200 Butterfly", "400 IM"));

  // Just to inform NKB is the club that work for as a swim coach
  private static final Map<String, Double> NKB_RECORDS = Map.ofEntries(
      Map.entry("13-14 Boys SCM 200 Freestyle", 112.92),
      Map.entry("13-14 Boys SCM 100 Backstroke", 59.81),
      Map.entry("13-14 Boys SCM 100 Breaststroke", 67.50),
      Map.entry("13-14 Boys SCM 100 Butterfly", 58.75),
      Map.entry("13-14 Boys SCM 200 IM", 123.01),
      Map.entry("13-14 Girls SCM 200 Freestyle", 118.45),
      Map.entry("13-14 Girls SCM 100 Backstroke", 65.90),
      Map.entry("13-14 Girls SCM 100 Breaststroke", 73.30),
      Map.entry("13-14 Girls SCM 100 Butterfly", 64.50),
      Map.entry("13-14 Girls SCM 200 IM", 130.50),
      Map.entry("15-16 Boys SCM 400 Freestyle", 230.99),
      Map.entry("15-16 Boys SCM 200 Backstroke", 123.70),
      Map.entry("15-16 Boys SCM 200 Breaststroke", 140.50),
      Map.entry("15-16 Boys SCM 200 Butterfly", 121.80),
      Map.entry("15-16 Boys SCM 400 IM", 266.65),
      Map.entry("15-16 Girls SCM 400 Freestyle", 247.50),
      Map.entry("15-16 Girls SCM 200 Backstroke", 130.85),
      Map.entry("15-16 Girls SCM 200 Breaststroke", 152.40),
      Map.entry("15-16 Girls SCM 200 Butterfly", 133.20),
      Map.entry("15-16 Girls SCM 400 IM", 278.72));

  /**
   * Calculate the IMX profile for a swimmer based on their event history.
   * Returns null if swimmer is null.
   */
  public IMXProfile calculateIMXProfile(Swimmer swimmer) {
    if (swimmer == null) {
      return null;
    }

    // Get best times for each event using improved stream operation
    Map<String, MeetResult> bests = swimmer.getEventHistory().stream()
        .collect(Collectors.toMap(
            MeetResult::getEventName,
            r -> r,
            (r1, r2) -> r1.getTimeInSeconds() < r2.getTimeInSeconds() ? r1 : r2));

    int totalScore = 0;
    String ageGroup = determineAgeGroup(swimmer.ageProperty().get());
    List<String> requiredEvents = IMX_EVENTS_BY_AGE_GROUP.getOrDefault(ageGroup, new ArrayList<>());
    ObservableList<IMXEvent> contributingEvents = FXCollections.observableArrayList();

    // Calculate points for each event
    for (String eventName : requiredEvents) {
      MeetResult bestResult = bests.get(eventName);
      if (bestResult != null) {
        int points = calculatePointsForEvent(bestResult, swimmer);
        totalScore += points;
        contributingEvents.add(new IMXEvent(eventName, bestResult.getTime(), points));
      } else {
        contributingEvents.add(new IMXEvent(eventName, "N/A", 0));
      }
    }

    String level = determineLevel(totalScore);
    int scoreForCurrent = getScoreForLevel(level);
    int scoreForNext = getNextLevelScore(level);

    return new IMXProfile(totalScore, level, scoreForCurrent, scoreForNext, contributingEvents);
  }

  /**
   * Calculate IMX history based on meet results chronologically.
   * Returns a list of IMX points over time.
   */
  public List<IMXPoint> calculateIMXHistory(Swimmer swimmer) {
    if (swimmer == null || swimmer.getEventHistory().isEmpty()) {
      return new ArrayList<>();
    }

    // Group results by date
    Map<LocalDate, List<MeetResult>> resultsByDate = swimmer.getEventHistory().stream()
        .collect(Collectors.groupingBy(MeetResult::getDate));

    List<LocalDate> sortedDates = new ArrayList<>(resultsByDate.keySet());
    Collections.sort(sortedDates);

    List<IMXPoint> history = new ArrayList<>();
    Map<String, MeetResult> currentBests = new HashMap<>();

    for (LocalDate date : sortedDates) {
      // Update current bests with new results from this date
      for (MeetResult newResult : resultsByDate.get(date)) {
        currentBests.merge(newResult.getEventName(), newResult,
            (existing, newer) -> newer.getTimeInSeconds() < existing.getTimeInSeconds() ? newer : existing);
      }

      int scoreOnDate = calculateScoreFromBestResults(currentBests, swimmer);
      if (scoreOnDate > 0) {
        history.add(new IMXPoint(date, scoreOnDate));
      }
    }
    return history;
  }

  /**
   * Calculate points for a single event based on national record.
   * Uses the formula: 1000 * (recordTime / swimmerTime)^3
   */
  private int calculatePointsForEvent(MeetResult result, Swimmer swimmer) {
    double swimmerTime = result.getTimeInSeconds();

    // Return 0 if time is invalid or zero
    if (swimmerTime <= 0) {
      return 0;
    }

    String ageGroup = determineAgeGroup(swimmer.ageProperty().get());
    String gender = "Male".equalsIgnoreCase(swimmer.getGender()) ? "Boys" : "Girls";
    String recordKey = ageGroup + " " + gender + " SCM " + result.getEventName();
    Double recordTime = NKB_RECORDS.get(recordKey);

    if (recordTime != null && recordTime > 0) {
      return (int) (1000 * Math.pow(recordTime / swimmerTime, 3));
    }
    return 0;
  }

  /**
   * Calculate total IMX score from a map of best results.
   */
  private int calculateScoreFromBestResults(Map<String, MeetResult> bestResults, Swimmer swimmer) {
    String ageGroup = determineAgeGroup(swimmer.ageProperty().get());
    List<String> requiredEvents = IMX_EVENTS_BY_AGE_GROUP.get(ageGroup);
    if (requiredEvents == null) {
      return 0;
    }

    int totalScore = 0;
    for (String eventName : requiredEvents) {
      MeetResult bestResult = bestResults.get(eventName);
      if (bestResult != null) {
        totalScore += calculatePointsForEvent(bestResult, swimmer);
      }
    }
    return totalScore;
  }

  /**
   * Determine age group from age.
   */
  private String determineAgeGroup(int age) {
    if (age <= 10) {
      return "10 & Under";
    }
    if (age <= 12) {
      return "11-12";
    }
    if (age <= 14) {
      return "13-14";
    }
    if (age <= 16) {
      return "15-16";
    }
    return "17 & Over";
  }

  /**
   * Determine IMX level based on total score.
   */
  private String determineLevel(int totalScore) {
    String achievedLevel = "Unranked";
    for (Map.Entry<String, Integer> levelEntry : IMX_LEVELS.entrySet()) {
      if (totalScore >= levelEntry.getValue()) {
        achievedLevel = levelEntry.getKey();
      } else {
        break;
      }
    }
    return achievedLevel;
  }

  /**
   * Get the minimum score required to achieve the given level.
   */
  private int getScoreForLevel(String level) {
    if ("Unranked".equals(level)) {
      return 0;
    }
    return IMX_LEVELS.getOrDefault(level, 0);
  }

  /**
   * Get the minimum score required for the next level.
   */
  private int getNextLevelScore(String currentLevel) {
    return switch (currentLevel) {
      case "Level 4" -> IMX_LEVELS.get("Level 5");
      case "Level 3" -> IMX_LEVELS.get("Level 4");
      case "Level 2" -> IMX_LEVELS.get("Level 3");
      case "Level 1" -> IMX_LEVELS.get("Level 2");
      case "Unranked" -> IMX_LEVELS.get("Level 1");
      default -> 0; // Highest level (Level 5) reached
    };
  }

  /**
   * Get the next level name from the current level.
   */
  public String getNextLevelName(String currentLevel) {
    return switch (currentLevel) {
      case "Level 4" -> "Level 5";
      case "Level 3" -> "Level 4";
      case "Level 2" -> "Level 3";
      case "Level 1" -> "Level 2";
      case "Unranked" -> "Level 1";
      default -> "Max";
    };
  }
}
