package com.example.medreminder.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medreminder.utils.TimeUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medreminder.R;
import com.example.medreminder.adapters.MedicationAdapter;
import com.example.medreminder.models.DrugResponse;
import com.example.medreminder.models.Medication;
import com.example.medreminder.models.SharedPreferencesHelper;
import com.example.medreminder.services.AlarmScheduler;
import com.example.medreminder.services.ApiClient;
import com.example.medreminder.services.ApiService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedicationsFragment extends Fragment {

    private static final String[] FREQUENCY_OPTIONS = {
            "Once Daily", "Twice Daily", "Three Times Daily", "Weekly", "As Needed"
    };

    private RecyclerView recyclerView;
    private MedicationAdapter adapter;
    private List<Medication> medicationList;
    private SharedPreferencesHelper sharedPreferencesHelper;
    private TextView tvTotal;
    private FloatingActionButton fabAdd;

    // For barcode scanning in the add dialog
    private EditText pendingNameField;
    private EditText pendingDosageField;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() == null) {
                    Toast.makeText(requireContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
                    return;
                }
                String barcode = result.getContents();
                if (barcode.length() < 5) {
                    Toast.makeText(requireContext(), "Unsupported barcode", Toast.LENGTH_SHORT).show();
                    return;
                }
                fetchDrugInfo(barcode);
            });

    public MedicationsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_medications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rv_medications);
        tvTotal = view.findViewById(R.id.tv_total);
        fabAdd = view.findViewById(R.id.fab_add);

        sharedPreferencesHelper = new SharedPreferencesHelper(requireContext());

        medicationList = new ArrayList<>(sharedPreferencesHelper.getAllMedications());

        adapter = new MedicationAdapter(medicationList, new MedicationAdapter.OnMedicationActionListener() {
            @Override
            public void onEditClick(int position) {
                showEditDialog(position);
            }

            @Override
            public void onDeleteClick(int position) {
                Medication medication = medicationList.get(position);
                AlarmScheduler.cancelAlarm(requireContext(), medication);
                sharedPreferencesHelper.deleteMedication(medication.getId());
                medicationList.remove(position);
                adapter.notifyItemRemoved(position);
                updateTotalCount();
                Toast.makeText(requireContext(), "Medication deleted", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showAddDialog());

        updateTotalCount();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sharedPreferencesHelper != null && medicationList != null && adapter != null) {
            medicationList.clear();
            medicationList.addAll(sharedPreferencesHelper.getAllMedications());
            adapter.notifyDataSetChanged();
            updateTotalCount();
        }
    }

    private void updateTotalCount() {
        if (tvTotal != null && medicationList != null) {
            tvTotal.setText(medicationList.size() + " total");
        }
    }

    // ==========================================
    // ADD MEDICATION DIALOG
    // ==========================================
    private void showAddDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_medication, null);

        EditText etName = dialogView.findViewById(R.id.add_name);
        EditText etDosage = dialogView.findViewById(R.id.add_dosage);
        Spinner spinnerFreq = dialogView.findViewById(R.id.add_frequency);
        EditText etTime = dialogView.findViewById(R.id.add_time);
        EditText etPillCount = dialogView.findViewById(R.id.add_pill_count);
        EditText etRefill = dialogView.findViewById(R.id.add_refill_threshold);
        View btnScan = dialogView.findViewById(R.id.btn_scan_barcode);

        // Store references for barcode callback
        pendingNameField = etName;
        pendingDosageField = etDosage;

        // Frequency spinner
        ArrayAdapter<String> freqAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, FREQUENCY_OPTIONS);
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFreq.setAdapter(freqAdapter);

        // Time field setup
        etTime.setText(TimeUtils.format12Hour(8, 0));
        setupTimeField(etTime);

        // Barcode scan
        btnScan.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan medication barcode");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            barcodeLauncher.launch(options);
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Add Medication")
                .setView(dialogView)
                .create();

        dialog.show();

        // Rounded corners
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.dialog_rounded_bg);
        }

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btn_add).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String dosage = etDosage.getText().toString().trim();
            String pillStr = etPillCount.getText().toString().trim();
            String refillStr = etRefill.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(dosage)
                    || TextUtils.isEmpty(pillStr) || TextUtils.isEmpty(refillStr)) {
                Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int pillCount, refillThreshold;
            try {
                pillCount = Integer.parseInt(pillStr);
                refillThreshold = Integer.parseInt(refillStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                return;
            }

            int[] time = TimeUtils.parse(etTime.getText().toString());
            if (time == null) {
                Toast.makeText(requireContext(), "Invalid time format", Toast.LENGTH_SHORT).show();
                return;
            }

            String frequency = spinnerFreq.getSelectedItem().toString();
            String id = String.valueOf(System.currentTimeMillis());

            Medication medication = new Medication(id, name, dosage, frequency,
                    time[0], time[1], pillCount, refillThreshold, true);

            sharedPreferencesHelper.saveMedication(medication);
            AlarmScheduler.scheduleAlarm(requireContext(), medication);

            medicationList.add(medication);
            adapter.notifyItemInserted(medicationList.size() - 1);
            updateTotalCount();

            dialog.dismiss();
            Toast.makeText(requireContext(), "Medication added", Toast.LENGTH_SHORT).show();
        });
    }

    // ==========================================
    // EDIT MEDICATION DIALOG
    // ==========================================
    private void showEditDialog(int position) {
        Medication med = medicationList.get(position);

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_medication, null);

        EditText etName = dialogView.findViewById(R.id.edit_name);
        EditText etDosage = dialogView.findViewById(R.id.edit_dosage);
        Spinner spinnerFreq = dialogView.findViewById(R.id.edit_frequency);
        EditText etTime = dialogView.findViewById(R.id.edit_time);
        EditText etPillCount = dialogView.findViewById(R.id.edit_pill_count);
        EditText etRefill = dialogView.findViewById(R.id.edit_refill_threshold);

        // Frequency spinner
        ArrayAdapter<String> freqAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, FREQUENCY_OPTIONS);
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFreq.setAdapter(freqAdapter);

        // Pre-fill values
        etName.setText(med.getName());
        etDosage.setText(med.getDosage());
        etTime.setText(TimeUtils.format12Hour(med.getTimeHour(), med.getTimeMinute()));
        setupTimeField(etTime);
        etPillCount.setText(String.valueOf(med.getPillCount()));
        etRefill.setText(String.valueOf(med.getRefillThreshold()));

        for (int i = 0; i < FREQUENCY_OPTIONS.length; i++) {
            if (FREQUENCY_OPTIONS[i].equals(med.getFrequency())) {
                spinnerFreq.setSelection(i);
                break;
            }
        }

        AlertDialog editDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Edit Medication")
                .setView(dialogView)
                .create();

        editDialog.show();

        // Rounded corners
        Window window = editDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.dialog_rounded_bg);
        }

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> editDialog.dismiss());

        dialogView.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String dosage = etDosage.getText().toString().trim();
            String pillStr = etPillCount.getText().toString().trim();
            String refillStr = etRefill.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(dosage)
                    || TextUtils.isEmpty(pillStr) || TextUtils.isEmpty(refillStr)) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int pillCount, refillThreshold;
            try {
                pillCount = Integer.parseInt(pillStr);
                refillThreshold = Integer.parseInt(refillStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                return;
            }

            int[] time = TimeUtils.parse(etTime.getText().toString());
            if (time == null) {
                Toast.makeText(requireContext(), "Invalid time format", Toast.LENGTH_SHORT).show();
                return;
            }

            String frequency = spinnerFreq.getSelectedItem().toString();

            // Confirmation dialog
            AlertDialog confirmDialog = new AlertDialog.Builder(requireContext())
                    .setTitle("Save changes?")
                    .setMessage("Are you sure you want to update " + name + "?")
                    .setPositiveButton("Save", (d, w) -> {
                        med.setName(name);
                        med.setDosage(dosage);
                        med.setFrequency(frequency);
                        med.setTimeHour(time[0]);
                        med.setTimeMinute(time[1]);
                        med.setPillCount(pillCount);
                        med.setRefillThreshold(refillThreshold);

                        sharedPreferencesHelper.saveMedication(med);
                        AlarmScheduler.scheduleAlarm(requireContext(), med);

                        medicationList.set(position, med);
                        adapter.notifyItemChanged(position);

                        editDialog.dismiss();
                        Toast.makeText(requireContext(), "Medication updated", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .create();

            confirmDialog.show();

            if (confirmDialog.getWindow() != null) {
                confirmDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_bg);
            }
            confirmDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFFd4183d);
            confirmDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(0xFF717182);
        });
    }

    // ==========================================
    // BARCODE DRUG LOOKUP
    // ==========================================
    private void fetchDrugInfo(String barcode) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String query = "openfda.upc:" + barcode;

        apiService.getDrugInfo(query).enqueue(new Callback<DrugResponse>() {
            @Override
            public void onResponse(Call<DrugResponse> call, Response<DrugResponse> response) {
                if (!response.isSuccessful() || response.body() == null
                        || response.body().results == null || response.body().results.isEmpty()) {
                    fallbackBarcode();
                    return;
                }

                DrugResponse.Result result = response.body().results.get(0);

                if (pendingNameField != null && result.openfda != null
                        && result.openfda.brand_name != null && !result.openfda.brand_name.isEmpty()) {
                    pendingNameField.setText(result.openfda.brand_name.get(0));
                } else if (pendingNameField != null) {
                    pendingNameField.setText("Unknown Medication");
                }

                if (pendingDosageField != null && result.dosage_form != null
                        && !result.dosage_form.isEmpty()) {
                    pendingDosageField.setText(result.dosage_form.get(0));
                }

                Toast.makeText(requireContext(), "Drug info loaded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<DrugResponse> call, Throwable t) {
                fallbackBarcode();
            }
        });
    }

    private void fallbackBarcode() {
        if (pendingNameField != null) {
            pendingNameField.setText("Unknown Medication");
        }
        Toast.makeText(requireContext(), "No drug info found. Please enter manually.", Toast.LENGTH_SHORT).show();
    }

    // ==========================================
    // TIME FIELD HELPER
    // ==========================================
    private void showCustomTimePicker(EditText etTime, TextView tvError) {
        View pickerView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_time_picker, null);

        NumberPicker pickerHour = pickerView.findViewById(R.id.picker_hour);
        NumberPicker pickerMinute = pickerView.findViewById(R.id.picker_minute);
        NumberPicker pickerAmPm = pickerView.findViewById(R.id.picker_ampm);

        // Hour: 1-12
        pickerHour.setMinValue(1);
        pickerHour.setMaxValue(12);
        pickerHour.setWrapSelectorWheel(true);
        String[] hourLabels = new String[12];
        for (int i = 0; i < 12; i++) {
            hourLabels[i] = String.format(Locale.getDefault(), "%02d", i + 1);
        }
        pickerHour.setDisplayedValues(hourLabels);

        // Minute: 00-59
        pickerMinute.setMinValue(0);
        pickerMinute.setMaxValue(59);
        pickerMinute.setWrapSelectorWheel(true);
        String[] minuteLabels = new String[60];
        for (int i = 0; i < 60; i++) {
            minuteLabels[i] = String.format(Locale.getDefault(), "%02d", i);
        }
        pickerMinute.setDisplayedValues(minuteLabels);

        // AM/PM
        pickerAmPm.setMinValue(0);
        pickerAmPm.setMaxValue(1);
        pickerAmPm.setDisplayedValues(new String[]{"AM", "PM"});
        pickerAmPm.setWrapSelectorWheel(true);

        // Set current value
        int[] current = TimeUtils.parse(etTime.getText().toString());
        int h24 = current != null ? current[0] : 8;
        int min = current != null ? current[1] : 0;

        int h12 = h24 % 12;
        if (h12 == 0) h12 = 12;
        pickerHour.setValue(h12);
        pickerMinute.setValue(min);
        pickerAmPm.setValue(h24 >= 12 ? 1 : 0);

        AlertDialog pickerDialog = new AlertDialog.Builder(requireContext())
                .setView(pickerView)
                .setPositiveButton("OK", (d, w) -> {
                    int hour = pickerHour.getValue();
                    int minute = pickerMinute.getValue();
                    boolean isPM = pickerAmPm.getValue() == 1;

                    // Convert to 24h
                    int hour24 = hour;
                    if (isPM && hour != 12) hour24 += 12;
                    if (!isPM && hour == 12) hour24 = 0;

                    etTime.setText(TimeUtils.format12Hour(hour24, minute));
                    if (tvError != null) tvError.setVisibility(View.GONE);
                    etTime.setBackgroundResource(R.drawable.edit_text_box);
                })
                .setNegativeButton("Cancel", null)
                .create();

        pickerDialog.show();

        Window pickerWindow = pickerDialog.getWindow();
        if (pickerWindow != null) {
            pickerWindow.setBackgroundDrawableResource(R.drawable.dialog_rounded_bg);
        }
    }

    @android.annotation.SuppressLint("ClickableViewAccessibility")
    private void setupTimeField(EditText etTime) {
        // Find the error text view (sibling in parent)
        ViewGroup parent = (ViewGroup) etTime.getParent();
        TextView tvError = parent.findViewById(R.id.tv_time_error);

        // Auto-format on focus loss, show error for invalid input
        etTime.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String text = etTime.getText().toString().trim();
                if (text.isEmpty()) {
                    // Empty is fine, just reset
                    if (tvError != null) tvError.setVisibility(View.GONE);
                    etTime.setBackgroundResource(R.drawable.edit_text_box);
                    return;
                }
                int[] parsed = TimeUtils.parse(text);
                if (parsed != null) {
                    etTime.setText(TimeUtils.format12Hour(parsed[0], parsed[1]));
                    if (tvError != null) tvError.setVisibility(View.GONE);
                    etTime.setBackgroundResource(R.drawable.edit_text_box);
                } else {
                    if (tvError != null) tvError.setVisibility(View.VISIBLE);
                    etTime.setBackgroundResource(R.drawable.edit_text_box_error);
                }
            }
        });

        // Tapping the clock drawable opens custom time picker
        etTime.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (etTime.getCompoundDrawables()[2] != null) {
                    int drawableStart = etTime.getWidth() - etTime.getPaddingEnd()
                            - etTime.getCompoundDrawables()[2].getIntrinsicWidth();
                    if (event.getX() >= drawableStart) {
                        showCustomTimePicker(etTime, tvError);
                        return true;
                    }
                }
            }
            return false;
        });
    }
}
