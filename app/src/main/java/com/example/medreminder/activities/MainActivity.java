package com.example.medreminder.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medreminder.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
public class MainActivity extends AppCompatActivity{
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setcContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        // wire up bottom nav to swap fragments
        // tab1 - today view
        // tab2 - medications (list of saved medications) (Ibrahim)
        // tab3 - history
        // tab4 - settings
    }

    @Override
    protected void onStart() {
        super.onStart();
        // put reminder service here
    }
}
