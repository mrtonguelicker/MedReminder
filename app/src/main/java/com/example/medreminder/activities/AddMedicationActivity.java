package com.example.medreminder.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medreminder.R;
public class AddMedicationActivity extends AppCompatActivity{

    private boolean isEditing = false;
    private String medicationId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        // check if editing existing saved meds (Ibrahim)
        // if getIntent() has a medication id extra, load that medication and pre fill the form about details (Ibrahim)

        // TODO: setup form fields (Ibrahim)
        // TODO: setup Save button (Ibrahim)
        // TODO: setup barcode scan button - just the button not the actual functionality (Ibrahim)
    }

    private void validateAndSave() {
        // TODO: validate the user inputs making sure all inputs are of correct type, save via SharedPreferenceHelper, finish() (Ibrahim)
    }
}
