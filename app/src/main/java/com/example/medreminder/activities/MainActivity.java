package com.example.medreminder.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;

import com.example.medreminder.R;
import com.example.medreminder.fragments.HistoryFragment;
import com.example.medreminder.fragments.MedicationsFragment;
import com.example.medreminder.fragments.SettingsFragment;
import com.example.medreminder.fragments.TodayFragment;
import com.example.medreminder.models.DoseLog;
import com.example.medreminder.models.Medication;
import com.example.medreminder.models.SharedPreferencesHelper;
import com.example.medreminder.services.AlarmScheduler;
import com.example.medreminder.services.NotificationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private int currentSelectedItemId = R.id.nav_today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply dark mode before setContentView
        SharedPreferencesHelper prefs = new SharedPreferencesHelper(this);
        if (prefs.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == currentSelectedItemId) {
                return true;
            }

            Fragment selectedFragment = null;

            if (id == R.id.nav_today) {
                selectedFragment = new TodayFragment();
            } else if (id == R.id.nav_medications) {
                selectedFragment = new MedicationsFragment();
            } else if (id == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
            } else if (id == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment, true);
                currentSelectedItemId = id;
                return true;
            }

            return false;
        });

        if (savedInstanceState == null) {
            loadFragment(new TodayFragment(), false);
            bottomNav.setSelectedItemId(R.id.nav_today);
            currentSelectedItemId = R.id.nav_today;
        } else {
            // Activity recreated (e.g. theme change) — stay on the current tab
            currentSelectedItemId = bottomNav.getSelectedItemId();
        }
    }

    private void loadFragment(Fragment fragment, boolean animate) {
        if (animate) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_fade_in,
                            R.anim.slide_fade_out
                    )
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        NotificationHelper.createNotificationChannels(this);
        AlarmScheduler.rescheduleAllAlarms(this);
        requestNotificationPermission();
        seedDemoDataIfNeeded();
    }

    // TODO: Remove this method before release — temporary demo data
    private void seedDemoDataIfNeeded() {
        SharedPreferences sp = getSharedPreferences("demo_seed", MODE_PRIVATE);
        if (sp.getBoolean("seeded_v2", false)) return;

        SharedPreferencesHelper prefs = new SharedPreferencesHelper(this);

        // Clear existing data so demo starts clean
        prefs.clearAllData();

        // Create medications
        Medication lisinopril = new Medication("med_1", "Lisinopril", "10mg",
                "Once Daily", 8, 0, 15, 5, true);
        Medication metforminAM = new Medication("med_2", "Metformin", "500mg",
                "Twice Daily", 8, 0, 45, 10, true);
        Medication metforminPM = new Medication("med_3", "Metformin", "500mg",
                "Twice Daily", 20, 0, 45, 10, true);
        Medication vitaminD = new Medication("med_4", "Vitamin D", "2000 IU",
                "Once Daily", 9, 0, 5, 5, true);

        prefs.saveMedication(lisinopril);
        prefs.saveMedication(metforminAM);
        prefs.saveMedication(metforminPM);
        prefs.saveMedication(vitaminD);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        // Seed dose logs for the past 7 days
        for (int daysAgo = 7; daysAgo >= 1; daysAgo--) {
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo);
            String date = sdf.format(cal.getTime());

            // Set timestamp to morning
            cal.set(Calendar.HOUR_OF_DAY, 8);
            cal.set(Calendar.MINUTE, 15);
            long morningTs = cal.getTimeInMillis();

            cal.set(Calendar.HOUR_OF_DAY, 8);
            cal.set(Calendar.MINUTE, 10);
            long morning2Ts = cal.getTimeInMillis();

            cal.set(Calendar.HOUR_OF_DAY, 20);
            cal.set(Calendar.MINUTE, 5);
            long eveningTs = cal.getTimeInMillis();

            cal.set(Calendar.HOUR_OF_DAY, 9);
            cal.set(Calendar.MINUTE, 0);
            long vitTs = cal.getTimeInMillis();

            if (daysAgo <= 5) {
                // Lisinopril — taken every day for past 5 days
                prefs.saveDoseLog(new DoseLog("med_1", "Lisinopril", date, "taken", morningTs));
            }

            if (daysAgo <= 6) {
                // Metformin AM — taken
                prefs.saveDoseLog(new DoseLog("med_2", "Metformin", date, "taken", morning2Ts));
            }

            if (daysAgo <= 4) {
                // Metformin PM — taken most days, missed on day 3
                if (daysAgo == 3) {
                    prefs.saveDoseLog(new DoseLog("med_3", "Metformin", date, "missed", eveningTs));
                } else {
                    prefs.saveDoseLog(new DoseLog("med_3", "Metformin", date, "taken", eveningTs));
                }
            }

            if (daysAgo <= 5) {
                // Vitamin D — missed on day 1 and 5
                if (daysAgo == 1 || daysAgo == 5) {
                    prefs.saveDoseLog(new DoseLog("med_4", "Vitamin D", date, "missed", vitTs));
                } else {
                    prefs.saveDoseLog(new DoseLog("med_4", "Vitamin D", date, "taken", vitTs));
                }
            }
        }

        // Also seed today's data (partial — some taken, some pending)
        cal.setTimeInMillis(System.currentTimeMillis());
        String today = sdf.format(cal.getTime());
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 15);
        prefs.saveDoseLog(new DoseLog("med_1", "Lisinopril", today, "taken", cal.getTimeInMillis()));
        cal.set(Calendar.MINUTE, 10);
        prefs.saveDoseLog(new DoseLog("med_2", "Metformin", today, "taken", cal.getTimeInMillis()));

        sp.edit().putBoolean("seeded_v2", true).apply();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }
    }
}