package com.example.medreminder.models;

import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesHelper {
    private static final String PREFS_NAME = "medreminder_prefs";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Gson gson;

    public void saveMedication(Medication medication) {
        // TODO: load existing list, save the new medication to the list, save, exit
    }

    public List<Medication> getAllMedications() {
        // TODO: load JSON string from prefs and convert it to a regular array list
        return new ArrayList<>();
    }

    public void deleteMedicaiton(String id) {
        // TODO: load list, remove by id, save, exit
    }

    public void saveDoseLog(DoseLog doseLog) {
        // TODO: load existing logs, add new logs (append it), save, exit
    }

    public List<DoseLog> getDoseLogForDate(String date) {
        // TODO: load ALL logs, filter by whatever date the user enters, find it in the existing log, return matching
        return new ArrayList<>();
    }

    public String geTodayStatus(String medicationId) {
        // TODO: get today's date, find latest log for this med by date and time, return status
        return null;
    }

    public void saveSetting(boolean darkMode, boolean quietHoursEnabled, int quietStart, int quietEnd, int snoozeDuration) {
        // TODO: save each setting to prefs
    }

    public boolean isDarkMode() {
        // TODO: return dark mode preferences; whatever the user saved in the last session
        return false;
    }

    public boolean isQuietHours() {
        // TODO: check if time right now falls under quiet hours
        return false;
    }

    public void saveEmergencyContact(String name, String number) {
        // TODO: save emergency name and number to prefs
    }

    public String getEmergencyContactName() {
        // TODO: return contact name from prefs
        return null;
    }

    public String getEmergencyContactNumber() {
        // TODO: return saved contact number from prefs
        return null;
    }
}
