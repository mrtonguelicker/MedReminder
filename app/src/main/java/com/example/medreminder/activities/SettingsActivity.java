package com.example.medreminder.activities;

import android.app.TimePickerDialog;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medreminder.R;
import com.example.medreminder.models.SharedPreferencesHelper;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferencesHelper prefsHelper;
    private EditText etEmergencyName, etEmergencyPhone;

    private EditText etSnoozeDuration;
    private EditText etQuietStart, etQuietEnd;
    private SwitchCompat switchDarkMode;
    private TextView btnClearData;
    private int startHour, endHour;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefsHelper = new SharedPreferencesHelper(this);
        etEmergencyName = findViewById(R.id.etEmergencyName);
        etEmergencyPhone = findViewById(R.id.et_emergency_number);

        etSnoozeDuration = findViewById(R.id.et_snooze_duration);
        etQuietStart = findViewById(R.id.et_quiet_start);
        etQuietEnd = findViewById(R.id.et_quiet_end);
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        btnClearData = findViewById(R.id.btn_clear_data);

        // Load saved data
        etEmergencyName.setText(prefsHelper.getEmergencyContactName());
        etEmergencyPhone.setText(prefsHelper.getEmergencyContactNumber());
        etSnoozeDuration.setText(String.valueOf(prefsHelper.getSnoozeDuration()));
        startHour = prefsHelper.getQuietStart();
        endHour = prefsHelper.getQuietEnd();

        // Save button
        etEmergencyName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveEmergencyContact();
            }
        });
        etEmergencyPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveEmergencyContact();
            }
        });
        etSnoozeDuration.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveSnooze();
            }
        });
        etQuietStart.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveStartTime();
            }
        });

        etQuietEnd.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveEndTime();
            }
        });

        // Show saved values in UI
        etQuietStart.setText(String.valueOf(startHour));
        etQuietEnd.setText(String.valueOf(endHour));


        switchDarkMode.setChecked(prefsHelper.isDarkMode());

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsHelper.setDarkMode(isChecked);

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            recreate();
        });

        btnClearData.setOnClickListener(v -> showClearDataDialog());

        // TODO: dark mode toggle DONE !!!!
        // TODO: emergency contact name + number fields (Ibrahim) DONE!!!!!
        // TODO: quiet hours start and end time pickers  DONE !!!!
        // TODO: snooze duration setting DONE!!!!
        // TODO: clear all data button with confirmation dialog DONE!!!!
    }

    private void saveEmergencyContact() {
        String name = etEmergencyName.getText().toString().trim();
        String phone = etEmergencyPhone.getText().toString().trim();


        prefsHelper.saveEmergencyContact(name, phone);

        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
    }
    private void saveSnooze() {
        String snoozeStr = etSnoozeDuration.getText().toString().trim();

        if (snoozeStr.isEmpty()) return;

        try {
            int snooze = Integer.parseInt(snoozeStr);

            if (snooze <= 0) return; // prevent invalid values

            prefsHelper.setSnoozeDuration(snooze);

        } catch (NumberFormatException e) {
            // ignore invalid input
        }
    }
    private void saveStartTime() {
        String value = etQuietStart.getText().toString().trim();

        if (value.isEmpty()) return;

        try {
            int hour = Integer.parseInt(value);

            if (hour < 0 || hour > 23) return;

            startHour = hour;
            prefsHelper.setQuietStart(hour);

        } catch (NumberFormatException ignored) {}
    }
    private void saveEndTime() {
        String value = etQuietEnd.getText().toString().trim();

        if (value.isEmpty()) return;

        try {
            int hour = Integer.parseInt(value);

            if (hour < 0 || hour > 23) return;

            endHour = hour;
            prefsHelper.setQuietEnd(hour);

        } catch (NumberFormatException ignored) {}
    }

    private void showClearDataDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Data")
                .setMessage("This will delete all medications, settings, and saved history. This cannot be undone.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    prefsHelper.clearAllData();

                    Toast.makeText(this, "All data cleared", Toast.LENGTH_SHORT).show();

                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
