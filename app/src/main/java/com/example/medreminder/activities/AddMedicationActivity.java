package com.example.medreminder.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medreminder.R;
import com.example.medreminder.models.Medication;
import com.example.medreminder.models.SharedPreferencesHelper;

public class AddMedicationActivity extends AppCompatActivity {

    private boolean isEditing = false;
    private String medicationId = null;

    private EditText etName;
    private EditText etDosage;
    private EditText etPillCount;
    private EditText etRefillThreshold;
    private Spinner spinnerFrequency;
    private TimePicker timePicker;
    private Button btnSave;
    private Button btnScan;

    private SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        sharedPreferencesHelper = new SharedPreferencesHelper(this);

        etName = findViewById(R.id.et_name);
        etDosage = findViewById(R.id.et_dosage);
        etPillCount = findViewById(R.id.et_pill_count);
        etRefillThreshold = findViewById(R.id.et_refill_threshold);
        spinnerFrequency = findViewById(R.id.spinner_frequency);
        timePicker = findViewById(R.id.time_picker);
        btnSave = findViewById(R.id.btn_save);
        btnScan = findViewById(R.id.btn_scan);

        setupFrequencySpinner();

        // check if editing existing saved meds (Ibrahim)
        if (getIntent() != null && getIntent().hasExtra("medication_id")) {
            medicationId = getIntent().getStringExtra("medication_id");
            isEditing = true;

            // if getIntent() has a medication id extra, load that medication and pre fill the form about details (Ibrahim)
            // TODO: load medication from SharedPreferencesHelper and pre-fill fields
        }

        // TODO: setup form fields (Ibrahim)

        // TODO: setup Save button (Ibrahim)
        btnSave.setOnClickListener(v -> validateAndSave());

        // TODO: setup barcode scan button - just the button not the actual functionality (Ibrahim)
        btnScan.setOnClickListener(v ->
                Toast.makeText(this, "Barcode scanning coming soon", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupFrequencySpinner() {
        String[] frequencyOptions = {"Once Daily", "Twice Daily", "Three Times Daily", "Weekly", "As Needed"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                frequencyOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapter);
    }

    private void validateAndSave() {
        String name = etName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String frequency = spinnerFrequency.getSelectedItem().toString();
        String pillCountStr = etPillCount.getText().toString().trim();
        String refillThresholdStr = etRefillThreshold.getText().toString().trim();

        // TODO: validate the user inputs making sure all inputs are of correct type, save via SharedPreferenceHelper, finish() (Ibrahim)
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(dosage)
                || TextUtils.isEmpty(pillCountStr) || TextUtils.isEmpty(refillThresholdStr)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int pillCount;
        int refillThreshold;

        try {
            pillCount = Integer.parseInt(pillCountStr);
            refillThreshold = Integer.parseInt(refillThresholdStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numeric values", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pillCount < 0 || refillThreshold < 0) {
            Toast.makeText(this, "Values cannot be negative", Toast.LENGTH_SHORT).show();
            return;
        }

        int timeHour;
        int timeMinute;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            timeHour = timePicker.getHour();
            timeMinute = timePicker.getMinute();
        } else {
            timeHour = timePicker.getCurrentHour();
            timeMinute = timePicker.getCurrentMinute();
        }

        String id = (isEditing && medicationId != null)
                ? medicationId
                : String.valueOf(System.currentTimeMillis());

        Medication medication = new Medication(
                id,
                name,
                dosage,
                frequency,
                timeHour,
                timeMinute,
                pillCount,
                refillThreshold,
                true
        );

        sharedPreferencesHelper.saveMedication(medication);

        Toast.makeText(this, "Medication saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}