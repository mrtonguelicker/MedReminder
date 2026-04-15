package com.example.medreminder.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.medreminder.models.Medication;
import com.example.medreminder.models.SharedPreferencesHelper;

import java.util.Calendar;
import java.util.List;

public class AlarmScheduler {

    public static void scheduleAlarm(Context context, Medication medication) {
        if (!medication.isActive()) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // temporary log
                android.util.Log.d("AlarmScheduler", "Cannot schedule exact alarms - permission denied");
                return;
            }
        }

        Intent intent = new Intent(context, MedicationAlarmReceiver.class);
        intent.putExtra("medication_id", medication.getId());
        intent.putExtra("medication_name", medication.getName());
        intent.putExtra("medication_dosage", medication.getDosage());
        intent.putExtra("snooze_count", 0);

        int requestCode = medication.getId().hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, medication.getTimeHour());
        calendar.set(Calendar.MINUTE, medication.getTimeMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If the time has already passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        //temporary log
        android.util.Log.d("AlarmScheduler", "Scheduling alarm for " + medication.getName() + " at " + calendar.getTimeInMillis());

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }

    public static void scheduleSnoozeAlarm(Context context, Medication medication, int snoozeCount) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) return;
        }

        SharedPreferencesHelper prefs = new SharedPreferencesHelper(context);
        int snoozeDuration = prefs.getSnoozeDuration();

        Intent intent = new Intent(context, MedicationAlarmReceiver.class);
        intent.putExtra("medication_id", medication.getId());
        intent.putExtra("medication_name", medication.getName());
        intent.putExtra("medication_dosage", medication.getDosage());
        intent.putExtra("snooze_count", snoozeCount);

        int requestCode = medication.getId().hashCode() + 10000;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = System.currentTimeMillis() + (snoozeDuration * 60L * 1000L);

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
        );
    }

    public static void cancelAlarm(Context context, Medication medication) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, MedicationAlarmReceiver.class);

        // Cancel daily alarm
        PendingIntent dailyPending = PendingIntent.getBroadcast(
                context, medication.getId().hashCode(), intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (dailyPending != null) {
            alarmManager.cancel(dailyPending);
            dailyPending.cancel();
        }

        // Cancel snooze alarm
        PendingIntent snoozePending = PendingIntent.getBroadcast(
                context, medication.getId().hashCode() + 10000, intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (snoozePending != null) {
            alarmManager.cancel(snoozePending);
            snoozePending.cancel();
        }
    }

    public static void rescheduleAllAlarms(Context context) {
        SharedPreferencesHelper prefs = new SharedPreferencesHelper(context);
        List<Medication> medications = prefs.getAllMedications();

        for (Medication med : medications) {
            if (med.isActive()) {
                scheduleAlarm(context, med);
            }
        }
    }
}
