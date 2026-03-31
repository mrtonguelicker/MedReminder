package com.example.medreminder.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medreminder.R;
import com.example.medreminder.models.SharedPreferencesHelper;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferencesHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefsHelper = new SharedPreferencesHelper(this);

        // TODO: dark mode toggle
        // TODO: emergency contact name + number fields (Ibrahim)
        // TODO: quiet hours start and end time pickers
        // TODO: snooze duration setting
        // TODO: clear all data button with confirmation dialog
    }

}
