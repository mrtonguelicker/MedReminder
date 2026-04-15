package com.example.medreminder.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.medreminder.R;
import com.example.medreminder.activities.LockScreenAlarmActivity;
import com.example.medreminder.models.Medication;
import com.example.medreminder.models.SharedPreferencesHelper;

public class NotificationHelper {

    public static final String CHANNEL_NORMAL = "med_reminder_channel";
    public static final String CHANNEL_ESCALATED = "med_reminder_escalated";

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);

            NotificationChannel normal = new NotificationChannel(
                    CHANNEL_NORMAL,
                    "Medication Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            normal.setDescription("Reminders to take your medications");
            normal.enableVibration(true);
            manager.createNotificationChannel(normal);

            NotificationChannel escalated = new NotificationChannel(
                    CHANNEL_ESCALATED,
                    "Urgent Medication Reminders",
                    NotificationManager.IMPORTANCE_MAX
            );
            escalated.setDescription("Escalated reminders for snoozed medications");
            escalated.enableVibration(true);
            escalated.setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    null
            );
            manager.createNotificationChannel(escalated);
        }
    }

    public static void showMedicationNotification(Context context, Medication medication, int snoozeCount) {
        String medicationId = medication.getId();
        int notificationId = medicationId.hashCode();

        // Full-screen intent for lock screen activity
        Intent fullScreenIntent = new Intent(context, LockScreenAlarmActivity.class);
        fullScreenIntent.putExtra("medication_id", medicationId);
        fullScreenIntent.putExtra("medication_name", medication.getName());
        fullScreenIntent.putExtra("medication_dosage", medication.getDosage());
        fullScreenIntent.putExtra("snooze_count", snoozeCount);
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context, notificationId, fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Action: Take
        Intent takeIntent = new Intent(context, MedicationAlarmReceiver.class);
        takeIntent.setAction(MedicationAlarmReceiver.ACTION_TAKE);
        takeIntent.putExtra("medication_id", medicationId);
        takeIntent.putExtra("snooze_count", snoozeCount);
        PendingIntent takePending = PendingIntent.getBroadcast(
                context, notificationId + 1, takeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Action: Snooze
        Intent snoozeIntent = new Intent(context, MedicationAlarmReceiver.class);
        snoozeIntent.setAction(MedicationAlarmReceiver.ACTION_SNOOZE);
        snoozeIntent.putExtra("medication_id", medicationId);
        snoozeIntent.putExtra("snooze_count", snoozeCount);
        PendingIntent snoozePending = PendingIntent.getBroadcast(
                context, notificationId + 2, snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Action: Miss
        Intent missIntent = new Intent(context, MedicationAlarmReceiver.class);
        missIntent.setAction(MedicationAlarmReceiver.ACTION_MISS);
        missIntent.putExtra("medication_id", medicationId);
        missIntent.putExtra("snooze_count", snoozeCount);
        PendingIntent missPending = PendingIntent.getBroadcast(
                context, notificationId + 3, missIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Choose channel based on escalation
        SharedPreferencesHelper prefs = new SharedPreferencesHelper(context);
        boolean escalationEnabled = prefs.isEscalationEnabled();
        String channel = (escalationEnabled && snoozeCount >= 2) ? CHANNEL_ESCALATED : CHANNEL_NORMAL;

        String timeText = String.format("%02d:%02d", medication.getTimeHour(), medication.getTimeMinute());
        String contentText = medication.getDosage() + " at " + timeText;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel)
                .setSmallIcon(R.drawable.ic_notification_med)
                .setContentTitle("Time to take " + medication.getName())
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .addAction(0, "Take", takePending)
                .addAction(0, "Snooze", snoozePending)
                .addAction(0, "Miss", missPending);

        // Escalation behavior
        if (escalationEnabled && snoozeCount >= 1) {
            builder.setVibrate(new long[]{0, 500, 200, 500, 200, 500});
        }
        if (escalationEnabled && snoozeCount >= 2) {
            builder.setOngoing(true);
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notificationId, builder.build());
    }

    public static void cancelNotification(Context context, String medicationId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(medicationId.hashCode());
    }
}
