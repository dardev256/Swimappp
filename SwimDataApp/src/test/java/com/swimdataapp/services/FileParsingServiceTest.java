package com.swimdataapp.services;

import com.swimdataapp.data.DataManager;
import com.swimdataapp.model.NKBRecord;
import com.swimdataapp.model.Swimmer;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;

import static org.junit.jupiter.api.Assertions.*;

class FileParsingServiceTest {

    private FileParsingService fileParsingService;
    private File tempExcelFile;
    private DataManager dataManager;

    @BeforeEach
    void setUp() {
        // Disable logging for cleaner test output
        LogManager.getLogManager().reset();
        fileParsingService = new FileParsingService();
        dataManager = DataManager.getInstance();
        dataManager.getSwimmers().clear(); // Clear any previous data
        dataManager.getNkbRecords().clear(); // Clear any previous data
    }

    @AfterEach
    void tearDown() {
        if (tempExcelFile != null && tempExcelFile.exists()) {
            tempExcelFile.delete();
        }
    }

    private void createTempExcelFile(boolean includeRoster, boolean includeMeetResults, boolean includeNKBRecords) throws IOException {
        tempExcelFile = File.createTempFile("testSwimData", ".xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            if (includeRoster) {
                Sheet rosterSheet = workbook.createSheet("Roster");
                Row header = rosterSheet.createRow(0);
                header.createCell(0).setCellValue("Name");
                header.createCell(1).setCellValue("DOB");
                header.createCell(2).setCellValue("RosterGroup");
                header.createCell(3).setCellValue("Gender");

                Row dataRow = rosterSheet.createRow(1);
                dataRow.createCell(0).setCellValue("John Doe");
                CellStyle dateStyle = workbook.createCellStyle();
                dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-dd"));
                Cell dobCell = dataRow.createCell(1);
                dobCell.setCellValue(LocalDate.of(2005, 1, 1));
                dobCell.setCellStyle(dateStyle);
                dataRow.createCell(2).setCellValue("Squad A");
                dataRow.createCell(3).setCellValue("Male");

                Row dataRow2 = rosterSheet.createRow(2);
                dataRow2.createCell(0).setCellValue("Jane Doe");
                Cell dobCell2 = dataRow2.createCell(1);
                dobCell2.setCellValue(LocalDate.of(2006, 2, 15));
                dobCell2.setCellStyle(dateStyle);
                dataRow2.createCell(2).setCellValue("Squad B");
                dataRow2.createCell(3).setCellValue("Female");
            }

            if (includeMeetResults) {
                Sheet resultsSheet = workbook.createSheet("Meet Results");
                Row header = resultsSheet.createRow(0);
                header.createCell(0).setCellValue("Name");
                header.createCell(1).setCellValue("Event");
                header.createCell(2).setCellValue("Time");
                header.createCell(3).setCellValue("Date");

                Row dataRow = resultsSheet.createRow(1);
                dataRow.createCell(0).setCellValue("John Doe");
                dataRow.createCell(1).setCellValue("100 Freestyle");
                dataRow.createCell(2).setCellValue("00:55.12"); // HH:MM.SS format
                CellStyle dateStyle = workbook.createCellStyle();
                dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-dd"));
                Cell dateCell = dataRow.createCell(3);
                dateCell.setCellValue(LocalDate.of(2023, 10, 26));
                dateCell.setCellStyle(dateStyle);

                Row dataRow2 = resultsSheet.createRow(2);
                dataRow2.createCell(0).setCellValue("Jane Doe");
                dataRow2.createCell(1).setCellValue("50 Butterfly");
                dataRow2.createCell(2).setCellValue(28.50); // numeric seconds
                Cell dateCell2 = dataRow2.createCell(3);
                dateCell2.setCellValue(LocalDate.of(2023, 11, 1));
                dateCell2.setCellStyle(dateStyle);
            }

            if (includeNKBRecords) {
                Sheet nkbSheet = workbook.createSheet("NKB Records");
                Row header = nkbSheet.createRow(0);
                header.createCell(0).setCellValue("Age Group");
                header.createCell(1).setCellValue("Gender");
                header.createCell(2).setCellValue("Course");
                header.createCell(3).setCellValue("Event Name");
                header.createCell(4).setCellValue("Record Time");

                Row dataRow = nkbSheet.createRow(1);
                dataRow.createCell(0).setCellValue("13-14");
                dataRow.createCell(1).setCellValue("Boys");
                dataRow.createCell(2).setCellValue("SCM");
                dataRow.createCell(3).setCellValue("100 Freestyle");
                dataRow.createCell(4).setCellValue(50.0);

                Row dataRow2 = nkbSheet.createRow(2);
                dataRow2.createCell(0).setCellValue("13-14");
                dataRow2.createCell(1).setCellValue("Girls");
                dataRow2.createCell(2).setCellValue("SCM");
                dataRow2.createCell(3).setCellValue("50 Butterfly");
                dataRow2.createCell(4).setCellValue(25.0);
            }

            try (FileOutputStream fos = new FileOutputStream(tempExcelFile)) {
                workbook.write(fos);
            }
        }
    }

    @Test
    @DisplayName("Should parse a complete Excel file with Roster, Meet Results, and NKB Records")
    void parseUnifiedFile_completeFile_success() throws IOException {
        createTempExcelFile(true, true, true);
        List<Swimmer> swimmers = fileParsingService.parseUnifiedFile(tempExcelFile.getAbsolutePath());

        assertNotNull(swimmers);
        assertEquals(2, swimmers.size());

        // Verify John Doe
        Swimmer john = swimmers.stream().filter(s -> s.getName().equals("John Doe")).findFirst().orElse(null);
        assertNotNull(john);
        assertEquals(LocalDate.of(2005, 1, 1), john.getDateOfBirth());
        assertEquals("Male", john.getGender());
        assertEquals("Squad A", john.getRosterGroup());
        assertEquals(1, john.getEventHistory().size());
        assertEquals("00:55.12", john.getEventHistory().get(0).getTime()); // Time as string

        // Verify Jane Doe
        Swimmer jane = swimmers.stream().filter(s -> s.getName().equals("Jane Doe")).findFirst().orElse(null);
        assertNotNull(jane);
        assertEquals(LocalDate.of(2006, 2, 15), jane.getDateOfBirth());
        assertEquals("Female", jane.getGender());
        assertEquals("Squad B", jane.getRosterGroup());
        assertEquals(1, jane.getEventHistory().size());
        assertEquals("28.5", jane.getEventHistory().get(0).getTime()); // POI DataFormatter outputs "28.5" for 28.50

        // Verify NKB Records
        List<NKBRecord> nkbRecords = dataManager.getNkbRecords();
        assertNotNull(nkbRecords);
        assertEquals(2, nkbRecords.size());
        assertEquals("13-14", nkbRecords.get(0).getAgeGroup());
        assertEquals("Boys", nkbRecords.get(0).getGender());
        assertEquals("SCM", nkbRecords.get(0).getCourse());
        assertEquals("100 Freestyle", nkbRecords.get(0).getEventName());
        assertEquals(50.0, nkbRecords.get(0).getRecordTime());
    }

    @Test
    @DisplayName("Should handle Excel file with missing Roster sheet gracefully")
    void parseUnifiedFile_missingRosterSheet_noSwimmersLoaded() throws IOException {
        createTempExcelFile(false, true, true); // No Roster sheet
        List<Swimmer> swimmers = fileParsingService.parseUnifiedFile(tempExcelFile.getAbsolutePath());

        assertNotNull(swimmers);
        assertTrue(swimmers.isEmpty()); // No swimmers should be loaded if roster is missing
        // NKB Records should still be loaded even if Roster is missing
        assertEquals(2, dataManager.getNkbRecords().size());
    }

    @Test
    @DisplayName("Should handle Excel file with missing Meet Results sheet gracefully")
    void parseUnifiedFile_missingMeetResultsSheet_swimmersLoadedWithoutResults() throws IOException {
        createTempExcelFile(true, false, true); // No Meet Results sheet
        List<Swimmer> swimmers = fileParsingService.parseUnifiedFile(tempExcelFile.getAbsolutePath());

        assertNotNull(swimmers);
        assertEquals(2, swimmers.size());
        assertTrue(swimmers.stream().allMatch(s -> s.getEventHistory().isEmpty())); // Swimmers loaded, no results
        assertEquals(2, dataManager.getNkbRecords().size()); // NKB Records should still be loaded
    }

    @Test
    @DisplayName("Should handle Excel file with missing NKB Records sheet gracefully")
    void parseUnifiedFile_missingNKBRecordsSheet_swimmersAndResultsLoadedWithoutNKB() throws IOException {
        createTempExcelFile(true, true, false); // No NKB Records sheet
        List<Swimmer> swimmers = fileParsingService.parseUnifiedFile(tempExcelFile.getAbsolutePath());

        assertNotNull(swimmers);
        assertEquals(2, swimmers.size()); // Swimmers and results loaded
        assertTrue(dataManager.getNkbRecords().isEmpty()); // NKB Records should be empty
    }

    @Test
    @DisplayName("Should handle an empty Excel file")
    void parseUnifiedFile_emptyFile_noDataLoaded() throws IOException {
        tempExcelFile = File.createTempFile("emptyTestFile", ".xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // Create an empty workbook
            try (FileOutputStream fos = new FileOutputStream(tempExcelFile)) {
                workbook.write(fos);
            }
        }
        List<Swimmer> swimmers = fileParsingService.parseUnifiedFile(tempExcelFile.getAbsolutePath());
        assertNotNull(swimmers);
        assertTrue(swimmers.isEmpty());
        assertTrue(dataManager.getNkbRecords().isEmpty());
    }

    @Test
    @DisplayName("Should handle invalid file path")
    void parseUnifiedFile_invalidFilePath_throwsIOException() {
        String invalidPath = "non_existent_file.xlsx";
        IOException exception = assertThrows(IOException.class, () -> fileParsingService.parseUnifiedFile(invalidPath));
        assertTrue(exception.getMessage().contains(invalidPath));
    }
}
