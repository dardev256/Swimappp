package com.swimdataapp.util;

/**
 * Utility class for parsing swim times in various formats.
 * Centralizes time parsing logic to avoid duplication across the codebase.
 */
public class TimeParsingUtil {

  /**
   * Parse a time string into seconds.
   * 
   * Supported formats:
   * - "MM:SS.HH" (minutes:seconds.hundredths)
   * - "MM:SS" (minutes:seconds)
   * - "SS.HH" (seconds.hundredths)
   * - "SS" (seconds only)
   * 
   * @param timeStr the time string to parse
   * @return time in seconds as a double, or 0.0 if parsing fails
   */
  public static double parseTimeString(String timeStr) {
    if (timeStr == null || timeStr.trim().isEmpty() || "-".equals(timeStr.trim())) {
      return 0.0;
    }

    try {
      // Split into major (minutes/seconds) and minor (hundredths) parts
      String[] majorMinorSplit = timeStr.split("\\.");
      String majorPart = majorMinorSplit[0];
      double hundredths = (majorMinorSplit.length > 1)
          ? Double.parseDouble("0." + majorMinorSplit[1])
          : 0.0;

      // Split major part into minutes and seconds
      String[] minSecSplit = majorPart.split(":");

      if (minSecSplit.length == 2) {
        // Format MM:SS
        return Integer.parseInt(minSecSplit[0]) * 60.0 + Integer.parseInt(minSecSplit[1]) + hundredths;
      } else if (minSecSplit.length == 1) {
        // Format SS
        return Integer.parseInt(minSecSplit[0]) + hundredths;
      }
    } catch (NumberFormatException e) {
      System.err.println("Could not parse time format: " + timeStr);
      return 0.0;
    }

    return 0.0;
  }

  /**
   * Convert seconds back to a formatted time string (MM:SS.HH).
   * 
   * @param seconds the time in seconds
   * @return formatted time string
   */
  public static String formatSecondsToTimeString(double seconds) {
    if (seconds <= 0) {
      return "0:00.00";
    }

    int minutes = (int) (seconds / 60);
    int secs = (int) (seconds % 60);
    int hundredths = (int) ((seconds % 1) * 100);

    return String.format("%d:%02d.%02d", minutes, secs, hundredths);
  }
}
