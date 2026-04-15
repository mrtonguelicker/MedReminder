package com.example.medreminder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medreminder.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.img_logo);
        TextView appName = findViewById(R.id.tv_app_name);

        // 🔥 Initial state
        logo.setScaleX(0.7f);
        logo.setScaleY(0.7f);

        // 🔥 Logo animation (fade + bounce scale)
        logo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1200)
                .setInterpolator(new OvershootInterpolator())
                .start();

        // 🔥 App name fade in (slightly delayed)
        appName.setAlpha(0f);
        appName.animate()
                .alpha(1f)
                .setStartDelay(600)
                .setDuration(800)
                .start();

        // 🔥 Navigate
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, SPLASH_DELAY);
    }
}