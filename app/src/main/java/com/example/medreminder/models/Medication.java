package com.example.medreminder.models;

public class Medication {
    private String id;
    private String name;
    private String dosage;
    private String frequency;
    private int timeHour;
    private int timeMinute;
    private int pillCount;
    private int refillThreshold;
    private boolean isActive;

    public Medication() {};

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public void setTimeHour(int timeHour) {
        this.timeHour = timeHour;
    }

    public void setTimeMinute(int timeMinute) {
        this.timeMinute = timeMinute;
    }

    public void setPillCount(int pillCount) {
        this.pillCount = pillCount;
    }

    public void setRefillThreshold(int refillThreshold) {
        this.refillThreshold = refillThreshold;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDosage() {
        return dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public int getTimeHour() {
        return timeHour;
    }

    public int getTimeMinute() {
        return timeMinute;
    }

    public int getPillCount() {
        return pillCount;
    }

    public int getRefillThreshold() {
        return refillThreshold;
    }

    public boolean isActive() {
        return isActive;
    }

    public Medication(String id, String name, String dosage, String frequency, int timeHour, int timeMinute, int pilCount, int refillThreshold, boolean isActive) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.timeHour = timeHour;
        this.timeMinute = timeMinute;
        this.pillCount = pilCount;
        this.refillThreshold = refillThreshold;
        this.isActive = isActive;
    }

    public boolean needsRefill() {
        return (pillCount <= refillThreshold);
    }
}
