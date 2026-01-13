package com.swimdataapp.model;

import java.time.LocalDate;
import java.util.Objects;

public class NKBRecord {
    private String ageGroup;
    private String gender; // Boys or Girls
    private String course; // SCM or LCM
    private String eventName;
    private double recordTime;

    public NKBRecord(String ageGroup, String gender, String course, String eventName, double recordTime) {
        this.ageGroup = ageGroup;
        this.gender = gender;
        this.course = course;
        this.eventName = eventName;
        this.recordTime = recordTime;
    }

    // Getters
    public String getAgeGroup() {
        return ageGroup;
    }

    public String getGender() {
        return gender;
    }

    public String getCourse() {
        return course;
    }

    public String getEventName() {
        return eventName;
    }

    public double getRecordTime() {
        return recordTime;
    }

    // You might want to add setters or make this immutable
    // For now, only getters are sufficient as records will be loaded once.

    @Override
    public String toString() {
        return "NKBRecord{"
                + "ageGroup='" + ageGroup + "'" +
                ", gender='" + gender + "'" +
                ", course='" + course + "'" +
                ", eventName='" + eventName + "'" +
                ", recordTime=" + recordTime +
                '}';
    }

    // Optionally, add equals and hashCode if NKBRecord objects will be used in collections
    // where equality is important.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NKBRecord nkbRecord = (NKBRecord) o;
        return Double.compare(nkbRecord.recordTime, recordTime) == 0 &&
                ageGroup.equals(nkbRecord.ageGroup) &&
                gender.equals(nkbRecord.gender) &&
                course.equals(nkbRecord.course) &&
                eventName.equals(nkbRecord.eventName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ageGroup, gender, course, eventName, recordTime);
    }
}
