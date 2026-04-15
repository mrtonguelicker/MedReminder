package com.example.medreminder.activities;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medreminder.R;
import com.example.medreminder.models.SharedPreferencesHelper;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.medreminder.models.Medication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferencesHelper prefsHelper;
    private EditText etEmergencyName, etEmergencyPhone;

    private EditText etSnoozeDuration;
    private EditText etQuietStart, etQuietEnd;
    private SwitchCompat switchDarkMode;
    private SwitchCompat switchEscalation;
    private View btnClearData;
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
        switchEscalation = findViewById(R.id.switch_escalation);

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

        // Escalation switch
        switchEscalation.setChecked(prefsHelper.isEscalationEnabled());
        switchEscalation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsHelper.setEscalationEnabled(isChecked);
        });

        // Share medication list
        Button btnShareMeds = findViewById(R.id.btn_share_meds);
        btnShareMeds.setOnClickListener(v -> shareMedicationList());
    }

    private void shareMedicationList() {
        List<Medication> meds = prefsHelper.getAllMedications();
        if (meds.isEmpty()) {
            Toast.makeText(this, "No medications to share", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Medication List\n");
        sb.append("Generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date())).append("\n\n");

        for (Medication med : meds) {
            sb.append("- ").append(med.getName())
                    .append(" (").append(med.getDosage()).append(")")
                    .append(" | ").append(med.getFrequency())
                    .append(" at ").append(String.format("%02d:%02d", med.getTimeHour(), med.getTimeMinute()))
                    .append(" | Pills: ").append(med.getPillCount())
                    .append("\n");
        }

        String contactName = prefsHelper.getEmergencyContactName();
        if (!contactName.isEmpty()) {
            sb.append("\nEmergency Contact: ").append(contactName)
                    .append(" (").append(prefsHelper.getEmergencyContactNumber()).append(")");
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Medication List");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, "Share via"));
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
