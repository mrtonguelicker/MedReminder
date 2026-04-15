package com.example.medreminder.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SharedPreferencesHelper {

    private static final String PREFS_NAME = "medreminder_prefs";

    private static final String KEY_MEDICATIONS = "key_medications";
    private static final String KEY_DOSE_LOGS = "key_dose_logs";
    private static final String KEY_EMERGENCY_CONTACT_NAME = "key_emergency_contact_name";
    private static final String KEY_EMERGENCY_CONTACT_NUMBER = "key_emergency_contact_number";

    private static final String KEY_DARK_MODE = "key_dark_mode";
    private static final String KEY_QUIET_HOURS_ENABLED = "key_quiet_hours_enabled";
    private static final String KEY_QUIET_START = "key_quiet_start";
    private static final String KEY_QUIET_END = "key_quiet_end";
    private static final String KEY_SNOOZE_DURATION = "key_snooze_duration";
    private static final String KEY_ESCALATION_ENABLED = "key_escalation_enabled";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final Gson gson;

    public SharedPreferencesHelper(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        gson = new Gson();
    }

    public void saveMedication(Medication medication) {
        List<Medication> medications = getAllMedications();
        boolean updated = false;

        for (int i = 0; i < medications.size(); i++) {
            if (medications.get(i).getId().equals(medication.getId())) {
                medications.set(i, medication);
                updated = true;
                break;
            }
        }

        if (!updated) {
            medications.add(medication);
        }

        editor.putString(KEY_MEDICATIONS, gson.toJson(medications)).apply();
    }

    public List<Medication> getAllMedications() {
        String json = prefs.getString(KEY_MEDICATIONS, null);
        if (json == null) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<ArrayList<Medication>>() {}.getType();
        List<Medication> medications = gson.fromJson(json, type);
        return medications != null ? medications : new ArrayList<>();
    }

    public void deleteMedication(String id) {
        List<Medication> medications = getAllMedications();
        medications.removeIf(medication -> medication.getId().equals(id));
        editor.putString(KEY_MEDICATIONS, gson.toJson(medications)).apply();
    }

    public void saveDoseLog(DoseLog doseLog) {
        List<DoseLog> logs = getAllDoseLogs();
        logs.add(doseLog);
        editor.putString(KEY_DOSE_LOGS, gson.toJson(logs)).apply();
    }

    public List<DoseLog> getAllDoseLogs() {
        String json = prefs.getString(KEY_DOSE_LOGS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<DoseLog>>() {}.getType();
        List<DoseLog> logs = gson.fromJson(json, type);
        return logs != null ? logs : new ArrayList<>();
    }

    public List<DoseLog> getDoseLogsForDate(String date) {
        List<DoseLog> allLogs = getAllDoseLogs();
        List<DoseLog> filtered = new ArrayList<>();
        for (DoseLog log : allLogs) {
            if (log.getDate().equals(date)) {
                filtered.add(log);
            }
        }
        return filtered;
    }

    public String getTodayStatus(String medicationId) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        List<DoseLog> todayLogs = getDoseLogsForDate(today);

        for (int i = todayLogs.size() - 1; i >= 0; i--) {
            if (todayLogs.get(i).getMedicationId().equals(medicationId)) {
                return todayLogs.get(i).getStatus();
            }
        }
        return null;
    }

    public DoseLog getTodayDoseLog(String medicationId) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        List<DoseLog> todayLogs = getDoseLogsForDate(today);

        for (int i = todayLogs.size() - 1; i >= 0; i--) {
            if (todayLogs.get(i).getMedicationId().equals(medicationId)) {
                return todayLogs.get(i);
            }
        }
        return null;
    }

    public void saveSettings(boolean darkMode, boolean quietHoursEnabled, int quietStart, int quietEnd, int snoozeDuration) {
        editor.putBoolean(KEY_DARK_MODE, darkMode);
        editor.putBoolean(KEY_QUIET_HOURS_ENABLED, quietHoursEnabled);
        editor.putInt(KEY_QUIET_START, quietStart);
        editor.putInt(KEY_QUIET_END, quietEnd);
        editor.putInt(KEY_SNOOZE_DURATION, snoozeDuration);
        editor.apply();
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkMode(boolean enabled) {
        editor.putBoolean(KEY_DARK_MODE, enabled);
        editor.apply();
    }

    public boolean isQuietHours() {
        if (!prefs.getBoolean(KEY_QUIET_HOURS_ENABLED, false)) return false;

        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int start = prefs.getInt(KEY_QUIET_START, 22);
        int end = prefs.getInt(KEY_QUIET_END, 7);

        if (start <= end) {
            return currentHour >= start && currentHour < end;
        } else {
            return currentHour >= start || currentHour < end;
        }
    }

    public void setQuietStart(int hour) {
        editor.putInt(KEY_QUIET_START, hour);
        editor.apply();
    }

    public int getQuietStart() {
        return prefs.getInt(KEY_QUIET_START, 22); // default 10 PM
    }

    public void setQuietEnd(int hour) {
        editor.putInt(KEY_QUIET_END, hour);
        editor.apply();
    }

    public int getQuietEnd() {
        return prefs.getInt(KEY_QUIET_END, 7); // default 7 AM
    }

    public int getSnoozeDuration() {
        return prefs.getInt(KEY_SNOOZE_DURATION, 5); // default = 5 mins
    }

    public void setSnoozeDuration(int minutes) {
        editor.putInt(KEY_SNOOZE_DURATION, minutes);
        editor.apply();
    }

    public boolean isEscalationEnabled() {
        return prefs.getBoolean(KEY_ESCALATION_ENABLED, false);
    }

    public void setEscalationEnabled(boolean enabled) {
        editor.putBoolean(KEY_ESCALATION_ENABLED, enabled);
        editor.apply();
    }

    public void saveEmergencyContact(String name, String number) {
        editor.putString(KEY_EMERGENCY_CONTACT_NAME, name);
        editor.putString(KEY_EMERGENCY_CONTACT_NUMBER, number);
        editor.apply();
    }

    public String getEmergencyContactName() {
        return prefs.getString(KEY_EMERGENCY_CONTACT_NAME, "");
    }

    public String getEmergencyContactNumber() {
        return prefs.getString(KEY_EMERGENCY_CONTACT_NUMBER, "");
    }

    public void clearAllData() {
        editor.clear();
        editor.apply();
    }
}
