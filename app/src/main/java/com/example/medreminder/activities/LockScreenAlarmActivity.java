package com.example.medreminder.activities;

import android.app.NotificationManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medreminder.R;
import com.example.medreminder.models.DoseLog;
import com.example.medreminder.models.Medication;
import com.example.medreminder.models.SharedPreferencesHelper;
import com.example.medreminder.services.AlarmScheduler;
import com.example.medreminder.services.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LockScreenAlarmActivity extends AppCompatActivity {
    private String medicationId;
    private String medicationName;
    private String medicationDosage;
    private int snoozeCount;
    private SharedPreferencesHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        setContentView(R.layout.activity_lock_screen_alarm);

        prefsHelper = new SharedPreferencesHelper(this);

        medicationId = getIntent().getStringExtra("medication_id");
        medicationName = getIntent().getStringExtra("medication_name");
        medicationDosage = getIntent().getStringExtra("medication_dosage");
        snoozeCount = getIntent().getIntExtra("snooze_count", 0);

        // Display medication info
        TextView tvMedName = findViewById(R.id.tv_med_name);
        TextView tvDosage = findViewById(R.id.tv_dosage);
        TextView tvAlarmTime = findViewById(R.id.tv_alarm_time);

        tvMedName.setText(medicationName != null ? medicationName : "Medication");
        tvDosage.setText(medicationDosage != null ? medicationDosage : "");
        tvAlarmTime.setText(new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date()));

        // Wire up Take button
        Button btnTake = findViewById(R.id.btn_take_now);
        btnTake.setOnClickListener(v -> {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            DoseLog log = new DoseLog(medicationId, medicationName, today, "taken", System.currentTimeMillis());
            prefsHelper.saveDoseLog(log);

            Medication med = findMedicationById(medicationId);
            if (med != null) {
                med.setPillCount(med.getPillCount() - 1);
                prefsHelper.saveMedication(med);
            }

            NotificationHelper.cancelNotification(this, medicationId);
            finish();
        });

        // Wire up Snooze button
        Button btnSnooze = findViewById(R.id.btn_snooze);
        btnSnooze.setOnClickListener(v -> {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            DoseLog log = new DoseLog(medicationId, medicationName, today, "snoozed", System.currentTimeMillis());
            prefsHelper.saveDoseLog(log);

            Medication med = findMedicationById(medicationId);
            if (med != null) {
                AlarmScheduler.scheduleSnoozeAlarm(this, med, snoozeCount + 1);
            }

            NotificationHelper.cancelNotification(this, medicationId);
            finish();
        });

        // Wire up Miss button
        Button btnMiss = findViewById(R.id.btn_miss);
        btnMiss.setOnClickListener(v -> {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            DoseLog log = new DoseLog(medicationId, medicationName, today, "missed", System.currentTimeMillis());
            prefsHelper.saveDoseLog(log);

            NotificationHelper.cancelNotification(this, medicationId);
            finish();
        });
    }

    private Medication findMedicationById(String id) {
        List<Medication> medications = prefsHelper.getAllMedications();
        for (Medication med : medications) {
            if (med.getId().equals(id)) return med;
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Dismiss alarm?")
                .setMessage("Are you sure you want to dismiss the alarm?")
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", null)
                .show();
    }
}
