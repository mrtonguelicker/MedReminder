package com.example.medreminder.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medreminder.R;
import com.example.medreminder.models.Medication;
import com.example.medreminder.models.OpenFoodResponse;
import com.example.medreminder.models.SharedPreferencesHelper;
import com.example.medreminder.services.AlarmScheduler;
import com.example.medreminder.services.ApiClient;
import com.example.medreminder.services.ApiService;
import com.example.medreminder.models.DrugResponse;
import com.example.medreminder.services.OpenFoodApiClient;
import com.example.medreminder.services.OpenFoodApiService;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        // EDIT MODE
        if (getIntent() != null && getIntent().hasExtra("medication_id")) {
            medicationId = getIntent().getStringExtra("medication_id");
            isEditing = true;
        }

        btnSave.setOnClickListener(v -> validateAndSave());

        // ONLY ONE CLICK LISTENER (FIXED)
        btnScan.setOnClickListener(v -> startBarcodeScan());
    }

    // =========================
    // BARCODE SCANNER
    // =========================
    private void startBarcodeScan() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan medication barcode");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);

        barcodeLauncher.launch(options);
    }

    // SINGLE CLEAN LAUNCHER (FIXED)
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {

                if (result.getContents() == null) {
                    Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                    return;
                }

                String barcode = result.getContents();

                if (barcode.length() < 5) {
                    Toast.makeText(this, "Unsupported barcode", Toast.LENGTH_SHORT).show();
                    return;
                }

                fetchDrugInfo(barcode);
            });

    // =========================
    // DRUG LOOKUP
    // =========================

    private void fetchDrugInfo(String barcode) {

        // 🔥 STEP 1: Try OpenFoodFacts (BEST MATCH RATE)
        OpenFoodApiService foodApi =
                OpenFoodApiClient.getClient().create(OpenFoodApiService.class);

        foodApi.getProduct(barcode).enqueue(new Callback<OpenFoodResponse>() {
            @Override
            public void onResponse(Call<OpenFoodResponse> call,
                                   Response<OpenFoodResponse> response) {

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().product != null
                        && response.body().product.product_name != null) {

                    // ✅ SUCCESS (most cases)
                    etName.setText(response.body().product.product_name);

                    if (response.body().product.quantity != null) {
                        etDosage.setText(response.body().product.quantity);
                    }

                    Toast.makeText(AddMedicationActivity.this,
                            "Auto-filled from barcode", Toast.LENGTH_SHORT).show();

                } else {
                    // 🔁 FALLBACK → OpenFDA
                    fetchFromOpenFDA(barcode);
                }
            }

            @Override
            public void onFailure(Call<OpenFoodResponse> call, Throwable t) {
                // 🔁 FALLBACK → OpenFDA
                fetchFromOpenFDA(barcode);
            }
        });
    }
    private void fetchFromOpenFDA(String barcode) {

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        Call<DrugResponse> call =
                apiService.getDrugInfo("openfda.upc:" + barcode, 1);

        call.enqueue(new Callback<DrugResponse>() {
            @Override
            public void onResponse(Call<DrugResponse> call, Response<DrugResponse> response) {

                if (!response.isSuccessful()
                        || response.body() == null
                        || response.body().results == null
                        || response.body().results.isEmpty()) {

                    fallbackBarcode();
                    return;
                }

                DrugResponse.Result result = response.body().results.get(0);

                if (result.openfda != null
                        && result.openfda.brand_name != null
                        && !result.openfda.brand_name.isEmpty()) {

                    etName.setText(result.openfda.brand_name.get(0));
                }

                if (result.dosage_form != null
                        && !result.dosage_form.isEmpty()) {

                    etDosage.setText(result.dosage_form.get(0));
                }
            }

            @Override
            public void onFailure(Call<DrugResponse> call, Throwable t) {
                fallbackBarcode();
            }
        });
    }

    private void fallbackBarcode() {
        etName.setText("Unknown Medication");
        Toast.makeText(this,
                "No data found. Please enter manually.",
                Toast.LENGTH_SHORT).show();
    }

    // =========================
    // SPINNER
    // =========================
    private void setupFrequencySpinner() {
        String[] frequencyOptions = {
                "Once Daily",
                "Twice Daily",
                "Three Times Daily",
                "Weekly",
                "As Needed"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                frequencyOptions
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapter);
    }

    // =========================
    // SAVE
    // =========================
    private void validateAndSave() {

        String name = etName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String frequency = spinnerFrequency.getSelectedItem().toString();
        String pillCountStr = etPillCount.getText().toString().trim();
        String refillThresholdStr = etRefillThreshold.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(dosage)
                || TextUtils.isEmpty(pillCountStr) || TextUtils.isEmpty(refillThresholdStr)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int pillCount, refillThreshold;

        try {
            pillCount = Integer.parseInt(pillCountStr);
            refillThreshold = Integer.parseInt(refillThresholdStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        int timeHour, timeMinute;

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
        AlarmScheduler.scheduleAlarm(this, medication);

        Toast.makeText(this, "Medication saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}