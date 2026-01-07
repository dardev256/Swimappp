package com.swimdataapp.services;

import com.swimdataapp.model.MeetResult;
import com.swimdataapp.model.Swimmer;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileParsingService {

  private static final Logger LOGGER = Logger.getLogger(FileParsingService.class.getName());
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
   * Parse a unified Excel file containing Roster and Meet Results sheets.
   * 
   * Expected file structure: -- Side note I did not include gender but it is
   * going to be something i go in the future.
   * Sheet 1 "Roster": Name (col 0), DOB (col 1), Gender (col 2), RosterGroup (col
   * 3)
   * Sheet 2 "Meet Results": Name (col 0), Event (col 1), Time (col 2), Date (col
   * 3)
   */
  public List<Swimmer> parseUnifiedFile(String filePath) throws IOException {
    Map<String, Swimmer> swimmerMap = new HashMap<>();

    try (FileInputStream file = new FileInputStream(new File(filePath));
        Workbook workbook = new XSSFWorkbook(file)) {

      DataFormatter dataFormatter = new DataFormatter();

      // Step 1: Parse Roster Sheet
      Sheet rosterSheet = workbook.getSheet("Roster");
      if (rosterSheet != null) {
        parseRosterSheet(rosterSheet, swimmerMap, dataFormatter);
      } else {
        LOGGER.warning("Roster sheet not found in workbook");
      }

      // Step 2: Parse Meet Results Sheet
      Sheet resultsSheet = workbook.getSheet("Meet Results");
      if (resultsSheet != null) {
        parseMeetResultsSheet(resultsSheet, swimmerMap, dataFormatter);
      } else {
        LOGGER.warning("Meet Results sheet not found in workbook");
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to parse Excel file: " + filePath, e);
      throw e;
    }

    LOGGER.info("Successfully parsed " + swimmerMap.size() + " swimmers.");
    return new ArrayList<>(swimmerMap.values());
  }

  /**
   * Parse the Roster sheet containing swimmer information.
   * Columns: Name (0), DOB (1), Gender (2), RosterGroup (3)
   */
  private void parseRosterSheet(Sheet sheet, Map<String, Swimmer> swimmerMap, DataFormatter dataFormatter) {
    LOGGER.info("Parsing Roster sheet...");

    for (Row row : sheet) {
      if (row.getRowNum() == 0) {
        continue; // Skip header row
      }

      Cell nameCell = row.getCell(0);
      if (nameCell == null || dataFormatter.formatCellValue(nameCell).trim().isEmpty()) {
        continue;
      }

      String name = dataFormatter.formatCellValue(nameCell).trim();
      Swimmer swimmer = swimmerMap.computeIfAbsent(name, Swimmer::new);

      // Parse Date of Birth (Column 1)
      Cell dobCell = row.getCell(1);
      if (dobCell != null) {
        try {
          if (dobCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dobCell)) {
            swimmer.setDateOfBirth(dobCell.getLocalDateTimeCellValue().toLocalDate());
          } else {
            String dobStr = dataFormatter.formatCellValue(dobCell).trim();
            if (!dobStr.isEmpty()) {
              swimmer.setDateOfBirth(LocalDate.parse(dobStr, DATE_FORMATTER));
            }
          }
        } catch (DateTimeParseException e) {
          LOGGER.log(Level.WARNING, "Could not parse date of birth for " + name, e);
        }
      }

      // Parse Roster Group (Column 2)
      Cell rosterGroupCell = row.getCell(2);
      if (rosterGroupCell != null) {
        String rosterGroup = dataFormatter.formatCellValue(rosterGroupCell).trim();
        if (!rosterGroup.isEmpty()) {
          swimmer.setRosterGroup(rosterGroup);
        }
      }

      // FIXED: Parse Gender (Column 3)
      Cell genderCell = row.getCell(3);
      if (genderCell != null) {
        String gender = dataFormatter.formatCellValue(genderCell).trim();
        if (!gender.isEmpty()) {
          swimmer.setGender(gender);
          LOGGER.fine("Set gender for " + name + ": " + gender);
        }
      }
    }
  }

  /**
   * Parse the Meet Results sheet containing race times.
   * Columns: Name (0), Event (1), Time (2), Date (3)
   */
  private void parseMeetResultsSheet(Sheet sheet, Map<String, Swimmer> swimmerMap, DataFormatter dataFormatter) {
    LOGGER.info("Parsing Meet Results sheet...");

    for (Row row : sheet) {
      if (row.getRowNum() == 0) {
        continue; // Skip header row
      }

      try {
        String swimmerName = dataFormatter.formatCellValue(row.getCell(0)).trim();
        if (swimmerName.isEmpty()) {
          continue;
        }

        Swimmer swimmer = swimmerMap.get(swimmerName);
        if (swimmer == null) {
          LOGGER.fine("Swimmer not found in roster: " + swimmerName);
          continue;
        }

        String eventName = dataFormatter.formatCellValue(row.getCell(1)).trim();
        String time = parseTimeFromCell(row.getCell(2), dataFormatter);
        LocalDate date = parseDateFromCell(row.getCell(3), dataFormatter);

        if (!eventName.isEmpty() && !time.isEmpty() && date != null) {
          swimmer.getEventHistory().add(new MeetResult(eventName, time, date));
        }
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Error parsing meet result at row " + (row.getRowNum() + 1), e);
      }
    }
  }

  /**
   * Parse time from a cell, handling both numeric and string formats.
   */
  private String parseTimeFromCell(Cell cell, DataFormatter dataFormatter) {
    if (cell == null) {
      return "";
    }

    try {
      if (cell.getCellType() == CellType.NUMERIC) {
        // Handle both plain numbers and Excel time format
        if (DateUtil.isCellDateFormatted(cell)) {
          double excelTimeValue = cell.getNumericCellValue();
          double totalSeconds = excelTimeValue * 24 * 60 * 60;
          long minutes = (long) (totalSeconds / 60);
          long seconds = (long) (totalSeconds % 60);
          long hundredths = Math.round((totalSeconds - (minutes * 60) - seconds) * 100);
          return String.format("%02d:%02d.%02d", minutes, seconds, hundredths);
        } else {
          return String.valueOf((int) cell.getNumericCellValue());
        }
      } else {
        return dataFormatter.formatCellValue(cell).trim();
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Could not parse time from cell", e);
      return "";
    }
  }

  /**
   * Parse date from a cell, handling both numeric date format and string format.
   */
  private LocalDate parseDateFromCell(Cell cell, DataFormatter dataFormatter) {
    if (cell == null) {
      return null;
    }

    try {
      if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
        return cell.getLocalDateTimeCellValue().toLocalDate();
      } else {
        String dateStr = dataFormatter.formatCellValue(cell).trim();
        if (!dateStr.isEmpty()) {
          return LocalDate.parse(dateStr, DATE_FORMATTER);
        }
      }
    } catch (DateTimeParseException e) {
      LOGGER.log(Level.WARNING, "Could not parse date: " + dataFormatter.formatCellValue(cell), e);
    }
    return null;
  }
}
