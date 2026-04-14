package com.example.medreminder.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.medreminder.R;
import com.example.medreminder.fragments.HistoryFragment;
import com.example.medreminder.fragments.MedicationsFragment;
import com.example.medreminder.fragments.SettingsFragments;
import com.example.medreminder.fragments.TodayFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private int currentSelectedItemId = R.id.nav_today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            loadFragment(new TodayFragment(), false);
            bottomNav.setSelectedItemId(R.id.nav_today);
            currentSelectedItemId = R.id.nav_today;
        }

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
                selectedFragment = new SettingsFragments();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment, true);
                currentSelectedItemId = id;
                return true;
            }

            return false;
        });
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
        // put reminder service here
    }
}