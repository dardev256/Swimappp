package com.swimdataapp.services;

import com.swimdataapp.model.TestSetResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;

import static org.junit.jupiter.api.Assertions.*;

class TestSetManagerTest {

    private TestSetManager testSetManager;
    private File tempExcelFile;

    @BeforeEach
    void setUp() {
        LogManager.getLogManager().reset(); // Disable logging for cleaner test output
        testSetManager = new TestSetManager();
    }

    @AfterEach
    void tearDown() {
        if (tempExcelFile != null && tempExcelFile.exists()) {
            tempExcelFile.delete();
        }
    }

    private void createTestSetExcelFile(boolean includeTestSetsSheet, boolean validData) throws IOException {
        tempExcelFile = File.createTempFile("testSetData", ".xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            if (includeTestSetsSheet) {
                Sheet testSetSheet = workbook.createSheet("Test Sets");
                Row header = testSetSheet.createRow(0);
                header.createCell(0).setCellValue("Swimmer Name");
                header.createCell(1).setCellValue("Test Name");
                header.createCell(2).setCellValue("Date");
                header.createCell(3).setCellValue("Result");

                if (validData) {
                    Row dataRow1 = testSetSheet.createRow(1);
                    dataRow1.createCell(0).setCellValue("John Doe");
                    dataRow1.createCell(1).setCellValue("Distance Swim");
                    CellStyle dateStyle = workbook.createCellStyle();
                    dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-dd"));
                    Cell dateCell1 = dataRow1.createCell(2);
                    dateCell1.setCellValue(LocalDate.of(2023, 5, 10));
                    dateCell1.setCellStyle(dateStyle);
                    dataRow1.createCell(3).setCellValue(900.5); // 900.5 seconds

                    Row dataRow2 = testSetSheet.createRow(2);
                    dataRow2.createCell(0).setCellValue("Jane Doe");
                    dataRow2.createCell(1).setCellValue("Sprint Test");
                    Cell dateCell2 = dataRow2.createCell(2);
                    dateCell2.setCellValue(LocalDate.of(2023, 5, 10));
                    dateCell2.setCellStyle(dateStyle);
                    dataRow2.createCell(3).setCellValue(120.0); // 120.0 seconds

                    Row dataRow3 = testSetSheet.createRow(3); // Another result for John Doe, same test
                    dataRow3.createCell(0).setCellValue("John Doe");
                    dataRow3.createCell(1).setCellValue("Distance Swim");
                    Cell dateCell3 = dataRow3.createCell(2);
                    dateCell3.setCellValue(LocalDate.of(2023, 6, 15));
                    dateCell3.setCellStyle(dateStyle);
                    dataRow3.createCell(3).setCellValue(890.0); // Improved time
                } else { // Invalid data for robustness test
                    Row dataRowInvalidDate = testSetSheet.createRow(1);
                    dataRowInvalidDate.createCell(0).setCellValue("Invalid Date Swimmer");
                    dataRowInvalidDate.createCell(1).setCellValue("Sprint Test");
                    dataRowInvalidDate.createCell(2).setCellValue("Not a Date"); // Invalid Date
                    dataRowInvalidDate.createCell(3).setCellValue(60.0);

                    Row dataRowInvalidResult = testSetSheet.createRow(2);
                    dataRowInvalidResult.createCell(0).setCellValue("Invalid Result Swimmer");
                    dataRowInvalidResult.createCell(1).setCellValue("Sprint Test");
                    CellStyle dateStyle = workbook.createCellStyle();
                    dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-dd"));
                    Cell dateCell = dataRowInvalidResult.createCell(2);
                    dateCell.setCellValue(LocalDate.of(2023, 1, 1));
                    dateCell.setCellStyle(dateStyle);
                    dataRowInvalidResult.createCell(3).setCellValue("Not a Number"); // Invalid Result
                }
            }

            try (FileOutputStream fos = new FileOutputStream(tempExcelFile)) {
                workbook.write(fos);
            }
        }
    }

    @Test
    @DisplayName("Should load test sets from a complete and valid Excel file")
    void loadTestSetsFromFile_completeFile_success() throws IOException {
        createTestSetExcelFile(true, true);
        testSetManager.loadTestSetsFromFile(tempExcelFile.getAbsolutePath());

        Map<String, List<TestSetResult>> results = testSetManager.getAllResults();
        assertNotNull(results);
        assertEquals(2, results.size()); // "Distance Swim" and "Sprint Test"

        // Verify "Distance Swim" results for John Doe
        List<TestSetResult> distanceSwimResults = results.get("Distance Swim");
        assertNotNull(distanceSwimResults);
        assertEquals(2, distanceSwimResults.size());
        assertEquals("John Doe", distanceSwimResults.get(0).getSwimmerName());
        assertEquals(LocalDate.of(2023, 5, 10), distanceSwimResults.get(0).getDate());
        assertEquals(900.5, distanceSwimResults.get(0).getPrimaryMetric());

        // Verify "Sprint Test" results for Jane Doe
        List<TestSetResult> sprintTestResults = results.get("Sprint Test");
        assertNotNull(sprintTestResults);
        assertEquals(1, sprintTestResults.size());
        assertEquals("Jane Doe", sprintTestResults.get(0).getSwimmerName());
        assertEquals(LocalDate.of(2023, 5, 10), sprintTestResults.get(0).getDate());
        assertEquals(120.0, sprintTestResults.get(0).getPrimaryMetric());
    }

    @Test
    @DisplayName("Should handle Excel file with missing 'Test Sets' sheet")
    void loadTestSetsFromFile_missingSheet_noResultsLoaded() throws IOException {
        createTestSetExcelFile(false, true); // No "Test Sets" sheet
        testSetManager.loadTestSetsFromFile(tempExcelFile.getAbsolutePath());

        Map<String, List<TestSetResult>> results = testSetManager.getAllResults();
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should handle invalid data in 'Test Sets' sheet gracefully")
    void loadTestSetsFromFile_invalidData_skipsInvalidRows() throws IOException {
        createTestSetExcelFile(true, false); // Invalid data in "Test Sets" sheet
        testSetManager.loadTestSetsFromFile(tempExcelFile.getAbsolutePath());

        Map<String, List<TestSetResult>> results = testSetManager.getAllResults();
        assertNotNull(results);
        assertTrue(results.isEmpty()); // Both rows had invalid data and should be skipped
    }

    @Test
    @DisplayName("Should handle an empty Excel file")
    void loadTestSetsFromFile_emptyFile_noResultsLoaded() throws IOException {
        tempExcelFile = File.createTempFile("emptyTestSetFile", ".xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            try (FileOutputStream fos = new FileOutputStream(tempExcelFile)) {
                workbook.write(fos);
            }
        }
        testSetManager.loadTestSetsFromFile(tempExcelFile.getAbsolutePath());

        Map<String, List<TestSetResult>> results = testSetManager.getAllResults();
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should handle invalid file path")
    void loadTestSetsFromFile_invalidFilePath_throwsIOException() {
        String invalidPath = "non_existent_testset_file.xlsx";
        IOException exception = assertThrows(IOException.class, () -> testSetManager.loadTestSetsFromFile(invalidPath));
        assertTrue(exception.getMessage().contains(invalidPath));
    }
}
