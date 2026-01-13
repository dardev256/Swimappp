package com.swimdataapp.services;

import com.swimdataapp.data.DataManager;
import com.swimdataapp.model.IMXPoint;
import com.swimdataapp.model.IMXProfile;
import com.swimdataapp.model.MeetResult;
import com.swimdataapp.model.NKBRecord;
import com.swimdataapp.model.Swimmer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;

import static org.junit.jupiter.api.Assertions.*;

class IMXServiceTest {

    private IMXService imxService;
    private DataManager dataManager;

    @BeforeEach
    void setUp() {
        LogManager.getLogManager().reset(); // Disable logging for cleaner test output
        imxService = new IMXService();
        dataManager = DataManager.getInstance();
        dataManager.getSwimmers().clear();
        dataManager.getNkbRecords().clear();

        // Setup mock NKB Records for testing
        List<NKBRecord> mockNKBRecords = new ArrayList<>();
        mockNKBRecords.add(new NKBRecord("13-14", "Boys", "SCM", "200 Freestyle", 112.92));
        mockNKBRecords.add(new NKBRecord("13-14", "Boys", "SCM", "100 Backstroke", 59.81));
        mockNKBRecords.add(new NKBRecord("13-14", "Boys", "SCM", "100 Breaststroke", 67.50));
        mockNKBRecords.add(new NKBRecord("13-14", "Boys", "SCM", "100 Butterfly", 58.75));
        mockNKBRecords.add(new NKBRecord("13-14", "Boys", "SCM", "200 IM", 123.01));

        mockNKBRecords.add(new NKBRecord("13-14", "Girls", "SCM", "200 Freestyle", 118.45));
        mockNKBRecords.add(new NKBRecord("13-14", "Girls", "SCM", "100 Backstroke", 65.90));
        mockNKBRecords.add(new NKBRecord("13-14", "Girls", "SCM", "100 Breaststroke", 73.30));
        mockNKBRecords.add(new NKBRecord("13-14", "Girls", "SCM", "100 Butterfly", 64.50));
        mockNKBRecords.add(new NKBRecord("13-14", "Girls", "SCM", "200 IM", 130.50));

        mockNKBRecords.add(new NKBRecord("15-16", "Boys", "SCM", "400 Freestyle", 230.99));
        mockNKBRecords.add(new NKBRecord("15-16", "Boys", "SCM", "200 Backstroke", 123.70));
        mockNKBRecords.add(new NKBRecord("15-16", "Boys", "SCM", "200 Breaststroke", 140.50));
        mockNKBRecords.add(new NKBRecord("15-16", "Boys", "SCM", "200 Butterfly", 121.80));
        mockNKBRecords.add(new NKBRecord("15-16", "Boys", "SCM", "400 IM", 266.65));

        mockNKBRecords.add(new NKBRecord("15-16", "Girls", "SCM", "400 Freestyle", 247.50));
        mockNKBRecords.add(new NKBRecord("15-16", "Girls", "SCM", "200 Backstroke", 130.85));
        mockNKBRecords.add(new NKBRecord("15-16", "Girls", "SCM", "200 Breaststroke", 152.40));
        mockNKBRecords.add(new NKBRecord("15-16", "Girls", "SCM", "200 Butterfly", 133.20));
        mockNKBRecords.add(new NKBRecord("15-16", "Girls", "SCM", "400 IM", 278.72));
        
        dataManager.setNkbRecords(mockNKBRecords);
    }

    private Swimmer createTestSwimmer(String name, LocalDate dob, String gender, String rosterGroup, List<MeetResult> results) {
        Swimmer swimmer = new Swimmer(name);
        swimmer.setDateOfBirth(dob);
        swimmer.setGender(gender);
        swimmer.setRosterGroup(rosterGroup);
        results.forEach(r -> swimmer.getEventHistory().add(r));
        // Manually trigger age calculation as it usually happens on data load
 
        return swimmer;
    }

    @Test
    @DisplayName("Should calculate IMX profile for a swimmer with all required events (Boys 13-14)")
    void calculateIMXProfile_boys13_14_complete() {
        List<MeetResult> results = new ArrayList<>();
        results.add(new MeetResult("400 Freestyle", "4:00.00", LocalDate.of(2023, 1, 1))); // SCM 240.00s
        results.add(new MeetResult("200 Backstroke", "2:05.00", LocalDate.of(2023, 1, 1))); // SCM 125.00s
        results.add(new MeetResult("200 Breaststroke", "2:20.00", LocalDate.of(2023, 1, 1))); // SCM 140.00s
        results.add(new MeetResult("200 Butterfly", "2:00.00", LocalDate.of(2023, 1, 1))); // SCM 120.00s
        results.add(new MeetResult("400 IM", "4:20.00", LocalDate.of(2023, 1, 1))); // SCM 260.00s

        Swimmer swimmer = createTestSwimmer("Test Boy", LocalDate.of(2009, 6, 15), "Male", "Squad A", results); // Age 14 (in 2023)

        IMXProfile profile = imxService.calculateIMXProfile(swimmer);
        assertNotNull(profile);
        assertTrue(profile.getTotalScore() > 0);
        assertEquals(5, profile.getContributingEvents().size());
        assertFalse(profile.getLevel().equals("Unranked"));
    }

    @Test
    @DisplayName("Should calculate IMX profile for a swimmer with all required events (Girls 13-14)")
    void calculateIMXProfile_girls13_14_complete() {
        List<MeetResult> results = new ArrayList<>();
        results.add(new MeetResult("400 Freestyle", "4:15.00", LocalDate.of(2023, 1, 1))); // SCM 255.00s
        results.add(new MeetResult("200 Backstroke", "2:15.00", LocalDate.of(2023, 1, 1))); // SCM 135.00s
        results.add(new MeetResult("200 Breaststroke", "2:30.00", LocalDate.of(2023, 1, 1))); // SCM 150.00s
        results.add(new MeetResult("200 Butterfly", "2:10.00", LocalDate.of(2023, 1, 1))); // SCM 130.00s
        results.add(new MeetResult("400 IM", "4:40.00", LocalDate.of(2023, 1, 1))); // SCM 280.00s

        Swimmer swimmer = createTestSwimmer("Test Girl", LocalDate.of(2009, 6, 15), "Female", "Squad A", results); // Age 14 (in 2023)

        IMXProfile profile = imxService.calculateIMXProfile(swimmer);
        assertNotNull(profile);
        assertTrue(profile.getTotalScore() > 0);
        assertEquals(5, profile.getContributingEvents().size());
        assertFalse(profile.getLevel().equals("Unranked"));
    }


    @Test
    @DisplayName("Should calculate IMX profile for a swimmer missing some events")
    void calculateIMXProfile_missingEvents() {
        List<MeetResult> results = new ArrayList<>();
        results.add(new MeetResult("400 Freestyle", "4:00.00", LocalDate.of(2023, 1, 1)));
        results.add(new MeetResult("200 Backstroke", "2:05.00", LocalDate.of(2023, 1, 1)));

        Swimmer swimmer = createTestSwimmer("Partial Boy", LocalDate.of(2009, 6, 15), "Male", "Squad A", results);

        IMXProfile profile = imxService.calculateIMXProfile(swimmer);
        assertNotNull(profile);
        assertTrue(profile.getTotalScore() > 0); // Should have points for completed events
        assertEquals(5, profile.getContributingEvents().size());
        assertTrue(profile.getContributingEvents().stream().anyMatch(e -> e.timeProperty().get().equals("N/A"))); // Missing events
        assertEquals("Unranked", profile.getLevel());
    }

    @Test
    @DisplayName("Should calculate IMX history correctly over time")
    void calculateIMXHistory_progression() {
        List<MeetResult> results = new ArrayList<>();
        // Early results
        results.add(new MeetResult("400 Freestyle", "4:10.00", LocalDate.of(2023, 1, 15))); // SCM 250s
        results.add(new MeetResult("200 Backstroke", "2:10.00", LocalDate.of(2023, 1, 15))); // SCM 130s

        // Later, improved results for same events and new events
        results.add(new MeetResult("400 Freestyle", "4:05.00", LocalDate.of(2023, 3, 10))); // SCM 245s
        results.add(new MeetResult("200 Breaststroke", "2:25.00", LocalDate.of(2023, 3, 10))); // SCM 145s

        Swimmer swimmer = createTestSwimmer("History Boy", LocalDate.of(2009, 6, 15), "Male", "Squad A", results);

        List<IMXPoint> history = imxService.calculateIMXHistory(swimmer);
        assertNotNull(history);
        assertFalse(history.isEmpty());
        // Expect at least two entries (Jan 15 and Mar 10)
        assertTrue(history.size() >= 2);
        // Ensure scores are non-decreasing
        for (int i = 0; i < history.size() - 1; i++) {
            assertTrue(history.get(i + 1).getScore() >= history.get(i).getScore());
        }
    }

    @Test
    @DisplayName("Should return 0 points for invalid or zero swimmer time")
    void calculatePointsForEvent_invalidSwimmerTime() {
        MeetResult invalidResult = new MeetResult("200 Freestyle", "0", LocalDate.of(2023, 1, 1)); // Time in seconds would be 0
        Swimmer swimmer = createTestSwimmer("Invalid Time Swimmer", LocalDate.of(2009, 6, 15), "Male", "Squad A", new ArrayList<>());

        int points = imxService.calculatePointsForEvent(invalidResult, swimmer);
        assertEquals(0, points);
    }

    @Test
    @DisplayName("Should return 0 points if no matching NKB record is found")
    void calculatePointsForEvent_noMatchingRecord() {
        MeetResult result = new MeetResult("Non Existent Event", "1:50.00", LocalDate.of(2023, 1, 1));
        Swimmer swimmer = createTestSwimmer("No Record Swimmer", LocalDate.of(2009, 6, 15), "Male", "Squad A", new ArrayList<>());

        int points = imxService.calculatePointsForEvent(result, swimmer);
        assertEquals(0, points);
    }

    @Test
    @DisplayName("Should correctly determine age group")
    void determineAgeGroup_test() {
        assertEquals("10 & Under", imxService.determineAgeGroup(9));
        assertEquals("10 & Under", imxService.determineAgeGroup(10));
        assertEquals("11-12", imxService.determineAgeGroup(11));
        assertEquals("11-12", imxService.determineAgeGroup(12));
        assertEquals("13-14", imxService.determineAgeGroup(13));
        assertEquals("13-14", imxService.determineAgeGroup(14));
        assertEquals("15-16", imxService.determineAgeGroup(15));
        assertEquals("15-16", imxService.determineAgeGroup(16));
        assertEquals("17 & Over", imxService.determineAgeGroup(17));
        assertEquals("17 & Over", imxService.determineAgeGroup(20));
    }
}
