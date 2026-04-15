package com.example.medreminder.fragments;

import android.app.TimePickerDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

        // Time picker
        final int[] selectedHour = {8};
        final int[] selectedMinute = {0};
        etTime.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour[0], selectedMinute[0]));
        etTime.setOnClickListener(v -> {
            new TimePickerDialog(requireContext(), (view, h, m) -> {
                selectedHour[0] = h;
                selectedMinute[0] = m;
                etTime.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
            }, selectedHour[0], selectedMinute[0], true).show();
        });

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

            String frequency = spinnerFreq.getSelectedItem().toString();
            String id = String.valueOf(System.currentTimeMillis());

            Medication medication = new Medication(id, name, dosage, frequency,
                    selectedHour[0], selectedMinute[0], pillCount, refillThreshold, true);

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
        EditText etTimeHour = dialogView.findViewById(R.id.edit_time_hour);
        EditText etTimeMinute = dialogView.findViewById(R.id.edit_time_minute);
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
        etTimeHour.setText(String.valueOf(med.getTimeHour()));
        etTimeMinute.setText(String.valueOf(med.getTimeMinute()));
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
            String hourStr = etTimeHour.getText().toString().trim();
            String minuteStr = etTimeMinute.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(dosage)
                    || TextUtils.isEmpty(pillStr) || TextUtils.isEmpty(refillStr)
                    || TextUtils.isEmpty(hourStr) || TextUtils.isEmpty(minuteStr)) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int pillCount, refillThreshold, hour, minute;
            try {
                pillCount = Integer.parseInt(pillStr);
                refillThreshold = Integer.parseInt(refillStr);
                hour = Integer.parseInt(hourStr);
                minute = Integer.parseInt(minuteStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                return;
            }

            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                Toast.makeText(requireContext(), "Invalid time", Toast.LENGTH_SHORT).show();
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
                        med.setTimeHour(hour);
                        med.setTimeMinute(minute);
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
            confirmDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFFD32F2F);
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
}
