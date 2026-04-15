package com.example.medreminder.services;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.medreminder.models.DoseLog;
import com.example.medreminder.models.Medication;
import com.example.medreminder.models.SharedPreferencesHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicationAlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_TAKE = "com.example.medreminder.ACTION_TAKE";
    public static final String ACTION_SNOOZE = "com.example.medreminder.ACTION_SNOOZE";
    public static final String ACTION_MISS = "com.example.medreminder.ACTION_MISS";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String medicationId = intent.getStringExtra("medication_id");

        if (medicationId == null) return;

        SharedPreferencesHelper prefs = new SharedPreferencesHelper(context);

        if (ACTION_TAKE.equals(action)) {
            handleTake(context, prefs, medicationId);
        } else if (ACTION_SNOOZE.equals(action)) {
            int snoozeCount = intent.getIntExtra("snooze_count", 0);
            handleSnooze(context, prefs, medicationId, snoozeCount);
        } else if (ACTION_MISS.equals(action)) {
            handleMiss(context, prefs, medicationId);
        } else {
            // Alarm trigger — show notification
            handleAlarmTrigger(context, prefs, intent);
        }
    }

    private void handleAlarmTrigger(Context context, SharedPreferencesHelper prefs, Intent intent) {
        String medicationId = intent.getStringExtra("medication_id");
        String medicationName = intent.getStringExtra("medication_name");
        String medicationDosage = intent.getStringExtra("medication_dosage");
        int snoozeCount = intent.getIntExtra("snooze_count", 0);

        // Check quiet hours
        if (prefs.isQuietHours()) {
            // Reschedule for when quiet hours end
            Medication med = findMedicationById(prefs, medicationId);
            if (med != null) {
                AlarmScheduler.scheduleAlarm(context, med);
            }
            return;
        }

        // Build a temporary Medication for the notification
        Medication med = findMedicationById(prefs, medicationId);
        if (med == null) {
            // Medication may have been deleted — use intent extras as fallback
            med = new Medication();
            med.setId(medicationId);
            med.setName(medicationName != null ? medicationName : "Medication");
            med.setDosage(medicationDosage != null ? medicationDosage : "");
        }

        NotificationHelper.showMedicationNotification(context, med, snoozeCount);

        // Reschedule next daily alarm (tomorrow)
        AlarmScheduler.scheduleAlarm(context, med);
    }

    private void handleTake(Context context, SharedPreferencesHelper prefs, String medicationId) {
        Medication med = findMedicationById(prefs, medicationId);
        if (med == null) return;

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DoseLog log = new DoseLog(medicationId, med.getName(), today, "taken", System.currentTimeMillis());
        prefs.saveDoseLog(log);

        med.setPillCount(med.getPillCount() - 1);
        prefs.saveMedication(med);

        NotificationHelper.cancelNotification(context, medicationId);
    }

    private void handleSnooze(Context context, SharedPreferencesHelper prefs, String medicationId, int snoozeCount) {
        Medication med = findMedicationById(prefs, medicationId);
        if (med == null) return;

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DoseLog log = new DoseLog(medicationId, med.getName(), today, "snoozed", System.currentTimeMillis());
        prefs.saveDoseLog(log);

        AlarmScheduler.scheduleSnoozeAlarm(context, med, snoozeCount + 1);

        NotificationHelper.cancelNotification(context, medicationId);
    }

    private void handleMiss(Context context, SharedPreferencesHelper prefs, String medicationId) {
        Medication med = findMedicationById(prefs, medicationId);
        if (med == null) return;

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DoseLog log = new DoseLog(medicationId, med.getName(), today, "missed", System.currentTimeMillis());
        prefs.saveDoseLog(log);

        NotificationHelper.cancelNotification(context, medicationId);
    }

    private Medication findMedicationById(SharedPreferencesHelper prefs, String id) {
        List<Medication> medications = prefs.getAllMedications();
        for (Medication med : medications) {
            if (med.getId().equals(id)) return med;
        }
        return null;
    }
}
