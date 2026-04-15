package com.example.medreminder.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.medreminder.R;
import com.example.medreminder.models.Medication;
import com.example.medreminder.models.SharedPreferencesHelper;
import com.example.medreminder.utils.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private SharedPreferencesHelper prefsHelper;
    private EditText etEmergencyName, etEmergencyPhone;
    private EditText etSnoozeDuration;
    private EditText etQuietStart, etQuietEnd;
    private SwitchCompat switchDarkMode, switchEscalation;

    public SettingsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefsHelper = new SharedPreferencesHelper(requireContext());

        etEmergencyName = view.findViewById(R.id.etEmergencyName);
        etEmergencyPhone = view.findViewById(R.id.et_emergency_number);
        etSnoozeDuration = view.findViewById(R.id.et_snooze_duration);
        etQuietStart = view.findViewById(R.id.et_quiet_start);
        etQuietEnd = view.findViewById(R.id.et_quiet_end);
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        switchEscalation = view.findViewById(R.id.switch_escalation);
        View btnClearData = view.findViewById(R.id.btn_clear_data);
        Button btnShareMeds = view.findViewById(R.id.btn_share_meds);

        // Load saved data
        etEmergencyName.setText(prefsHelper.getEmergencyContactName());
        etEmergencyPhone.setText(prefsHelper.getEmergencyContactNumber());
        etSnoozeDuration.setText(String.valueOf(prefsHelper.getSnoozeDuration()));

        int startHour = prefsHelper.getQuietStart();
        int endHour = prefsHelper.getQuietEnd();
        etQuietStart.setText(TimeUtils.format12Hour(startHour, 0));
        etQuietEnd.setText(TimeUtils.format12Hour(endHour, 0));

        // Save on focus loss
        etEmergencyName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) saveEmergencyContact();
        });
        etEmergencyPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) saveEmergencyContact();
        });
        etSnoozeDuration.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) saveSnooze();
        });

        // Quiet hours — open custom time picker on click
        etQuietStart.setOnClickListener(v -> showQuietTimePicker(etQuietStart, true));
        etQuietEnd.setOnClickListener(v -> showQuietTimePicker(etQuietEnd, false));

        // Dark mode
        switchDarkMode.setChecked(prefsHelper.isDarkMode());
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsHelper.setDarkMode(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Escalation
        switchEscalation.setChecked(prefsHelper.isEscalationEnabled());
        switchEscalation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsHelper.setEscalationEnabled(isChecked);
        });

        // Clear data
        btnClearData.setOnClickListener(v -> showClearDataDialog());

        // Share
        btnShareMeds.setOnClickListener(v -> shareMedicationList());
    }

    private void saveEmergencyContact() {
        String name = etEmergencyName.getText().toString().trim();
        String phone = etEmergencyPhone.getText().toString().trim();
        prefsHelper.saveEmergencyContact(name, phone);
    }

    private void saveSnooze() {
        String snoozeStr = etSnoozeDuration.getText().toString().trim();
        if (snoozeStr.isEmpty()) return;
        try {
            int snooze = Integer.parseInt(snoozeStr);
            if (snooze > 0) prefsHelper.setSnoozeDuration(snooze);
        } catch (NumberFormatException ignored) {}
    }

    private void showQuietTimePicker(EditText target, boolean isStart) {
        int currentHour = isStart ? prefsHelper.getQuietStart() : prefsHelper.getQuietEnd();

        View pickerView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_time_picker, null);

        android.widget.NumberPicker pickerHour = pickerView.findViewById(R.id.picker_hour);
        android.widget.NumberPicker pickerMinute = pickerView.findViewById(R.id.picker_minute);
        android.widget.NumberPicker pickerAmPm = pickerView.findViewById(R.id.picker_ampm);

        pickerHour.setMinValue(1);
        pickerHour.setMaxValue(12);
        pickerHour.setWrapSelectorWheel(true);
        String[] hourLabels = new String[12];
        for (int i = 0; i < 12; i++) hourLabels[i] = String.format(Locale.getDefault(), "%02d", i + 1);
        pickerHour.setDisplayedValues(hourLabels);

        pickerMinute.setMinValue(0);
        pickerMinute.setMaxValue(59);
        pickerMinute.setWrapSelectorWheel(true);
        String[] minuteLabels = new String[60];
        for (int i = 0; i < 60; i++) minuteLabels[i] = String.format(Locale.getDefault(), "%02d", i);
        pickerMinute.setDisplayedValues(minuteLabels);

        pickerAmPm.setMinValue(0);
        pickerAmPm.setMaxValue(1);
        pickerAmPm.setDisplayedValues(new String[]{"AM", "PM"});
        pickerAmPm.setWrapSelectorWheel(true);

        int h12 = currentHour % 12;
        if (h12 == 0) h12 = 12;
        pickerHour.setValue(h12);
        pickerMinute.setValue(0);
        pickerAmPm.setValue(currentHour >= 12 ? 1 : 0);

        AlertDialog pickerDialog = new AlertDialog.Builder(requireContext())
                .setView(pickerView)
                .setPositiveButton("OK", (d, w) -> {
                    int hour = pickerHour.getValue();
                    int minute = pickerMinute.getValue();
                    boolean isPM = pickerAmPm.getValue() == 1;

                    int hour24 = hour;
                    if (isPM && hour != 12) hour24 += 12;
                    if (!isPM && hour == 12) hour24 = 0;

                    target.setText(TimeUtils.format12Hour(hour24, minute));

                    if (isStart) {
                        prefsHelper.setQuietStart(hour24);
                    } else {
                        prefsHelper.setQuietEnd(hour24);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        pickerDialog.show();
        if (pickerDialog.getWindow() != null) {
            pickerDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_bg);
        }
    }

    private void showClearDataDialog() {
        AlertDialog clearDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Clear All Data")
                .setMessage("This will delete all medications, settings, and saved history. This cannot be undone.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    prefsHelper.clearAllData();
                    Toast.makeText(requireContext(), "All data cleared", Toast.LENGTH_SHORT).show();
                    etEmergencyName.setText("");
                    etEmergencyPhone.setText("");
                    etSnoozeDuration.setText("5");
                    etQuietStart.setText(TimeUtils.format12Hour(22, 0));
                    etQuietEnd.setText(TimeUtils.format12Hour(7, 0));
                    switchDarkMode.setChecked(false);
                    switchEscalation.setChecked(false);
                })
                .setNegativeButton("Cancel", null)
                .create();

        clearDialog.show();
        if (clearDialog.getWindow() != null) {
            clearDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_bg);
        }
        clearDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFFd4183d);
        clearDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(0xFF717182);
    }

    private void shareMedicationList() {
        List<Medication> meds = prefsHelper.getAllMedications();
        if (meds.isEmpty()) {
            Toast.makeText(requireContext(), "No medications to share", Toast.LENGTH_SHORT).show();
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
}
