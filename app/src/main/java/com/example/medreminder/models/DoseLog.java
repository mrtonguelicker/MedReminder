package com.example.medreminder.models;

public class DoseLog {
    private String medicationId;
    private String medicationName;
    private String date;
    private String status;
    private long timestamp;

    public DoseLog() {}

    public String getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(String medicationId) {
        this.medicationId = medicationId;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public DoseLog(String medicationId, String medicationName, String date, String status, long timestamp) {
        this.medicationId = medicationId;
        this.medicationName = medicationName;
        this.date = date;
        this.status = status;
        this.timestamp = timestamp;
    }
}


