package com.swimdataapp.services;

import com.swimdataapp.model.SingleTimeTestResult;
import com.swimdataapp.model.TestSetResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSetManager {

  private final Map<String, List<TestSetResult>> allResults = new HashMap<>();

  public void loadTestSetsFromFile(String filePath) throws IOException {
    allResults.clear();
    try (FileInputStream file = new FileInputStream(new File(filePath));
        Workbook workbook = new XSSFWorkbook(file)) {

      Sheet sheet = workbook.getSheet("Test Sets");
      if (sheet != null) {
        parseTestSetSheet(sheet);
      }
    }
  }

  private void parseTestSetSheet(Sheet sheet) {
    DataFormatter dataFormatter = new DataFormatter();
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Start from the second row (index 1) to skip the header
    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
      Row row = sheet.getRow(i);
      if (row == null)
        continue;

      Cell nameCell = row.getCell(0);
      Cell testNameCell = row.getCell(1);
      Cell dateCell = row.getCell(2);
      Cell resultCell = row.getCell(3);

      if (nameCell == null || testNameCell == null || dateCell == null || resultCell == null) {
        continue; // Skip incomplete rows
      }

      String swimmerName = dataFormatter.formatCellValue(nameCell).trim();
      String testName = dataFormatter.formatCellValue(testNameCell).trim();
      LocalDate date = null;
      double resultValue = 0.0;

      // --- Robust Date Parsing ---
      try {
        if (dateCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dateCell)) {
          date = dateCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
          date = LocalDate.parse(dataFormatter.formatCellValue(dateCell).trim(), dateFormatter);
        }
      } catch (Exception e) {
        System.err.println("Could not parse date in Test Sets sheet at row " + (i + 1) + ". Skipping row.");
        continue;
      }

      // --- Robust Result Parsing ---
      try {
        if (resultCell.getCellType() == CellType.NUMERIC) {
          resultValue = resultCell.getNumericCellValue();
        } else {
          resultValue = Double.parseDouble(dataFormatter.formatCellValue(resultCell).trim());
        }
      } catch (NumberFormatException e) {
        System.err.println("Could not parse result value in Test Sets sheet at row " + (i + 1) + ". Skipping row.");
        continue;
      }

      if (!swimmerName.isEmpty() && !testName.isEmpty()) {
        TestSetResult result = new SingleTimeTestResult(swimmerName, date, resultValue);
        allResults.computeIfAbsent(testName, k -> new ArrayList<>()).add(result);
      }
    }
  }

  public Map<String, List<TestSetResult>> getAllResults() {
    return allResults;
  }
}
