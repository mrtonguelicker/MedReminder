package com.example.medreminder.activities;

import android.os.Bundle;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medreminder.R;
import androidx.appcompat.app.AlertDialog;

public class LockScreenAlarmActivity extends AppCompatActivity{
    private String medicationId;
    private String medicationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        setContentView(R.layout.activity_lock_screen_alarm);

        medicationId = getIntent().getStringExtra("medication_id");
        medicationName = getIntent().getStringExtra("medication_name");

        // display medication name
        // wire up take button - log as taken, decrement pill count
        // wire up snooze button  - schedule a new alarm in 5 mins
        // wire up dismiss button - log as missed
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
