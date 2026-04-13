package com.example.medreminder.models;

public class CalendarDay {
    public int dayNumber;
    public String date;
    public String status;

    public CalendarDay(int dayNumber, String date, String status) {
        this.dayNumber = dayNumber;
        this.date = date;
        this.status = status;
    }
}