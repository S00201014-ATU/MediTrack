package com.example.meditrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        Button btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(v -> {
            // Save flag so we don’t show this again
            SharedPreferences prefs = getSharedPreferences("MediTrackPrefs", MODE_PRIVATE);
            prefs.edit().putBoolean("firstRun", false).apply();

            // Go to AddMedication screen
            Intent intent = new Intent(WelcomeActivity.this, AddMedicationActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
