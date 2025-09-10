package com.example.meditrack;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class EditMedicationActivity extends AppCompatActivity {

    private int medId;
    private int selectedHour = -1;
    private int selectedMinute = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_medication);

        EditText txtMedicationName = findViewById(R.id.txtMedicationName);
        EditText txtDosage = findViewById(R.id.txtDosage);
        EditText txtTime = findViewById(R.id.txtTime);
        EditText txtFrequency = findViewById(R.id.txtFrequency);
        Button btnUpdateMedication = findViewById(R.id.btnUpdateMedication);

        // Build DB
        AppDatabase db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class, "medication-db"
        ).allowMainThreadQueries().build();

        // Get medId from Intent
        medId = getIntent().getIntExtra("medId", -1);
        if (medId == -1) {
            Toast.makeText(this, "Error: No medication ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load medication
        Medication med = db.medicationDao().getById(medId);
        if (med == null) {
            Toast.makeText(this, "Medication not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Pre-fill fields
        txtMedicationName.setText(med.name);
        txtDosage.setText(med.dosage);

        // Extract saved time if available
        String rawTime = med.time.split(" ")[0]; // e.g. "08:00"
        txtTime.setText(rawTime);

        try {
            String[] parts = rawTime.split(":");
            selectedHour = Integer.parseInt(parts[0]);
            selectedMinute = Integer.parseInt(parts[1]);
        } catch (Exception ignored) {}

        // Pre-fill frequency (extract number from "every Xh")
        int frequencyHours = 24;
        if (med.time.contains("every")) {
            try {
                String freqStr = med.time.replaceAll("\\D+", "");
                frequencyHours = Integer.parseInt(freqStr);
            } catch (Exception ignored) {}
        }
        txtFrequency.setText(String.valueOf(frequencyHours));

        // Time picker
        txtTime.setFocusable(false);
        txtTime.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int hour = (selectedHour == -1) ? c.get(Calendar.HOUR_OF_DAY) : selectedHour;
            int minute = (selectedMinute == -1) ? c.get(Calendar.MINUTE) : selectedMinute;

            TimePickerDialog timePicker = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minuteOfHour) -> {
                        selectedHour = hourOfDay;
                        selectedMinute = minuteOfHour;
                        String formatted = String.format("%02d:%02d", hourOfDay, minuteOfHour);
                        txtTime.setText(formatted);
                    },
                    hour, minute, true
            );
            timePicker.show();
        });

        // Save changes
        int finalFrequencyHours = frequencyHours;
        btnUpdateMedication.setOnClickListener(v -> {
            String name = txtMedicationName.getText().toString();
            String dosage = txtDosage.getText().toString();
            String freqText = txtFrequency.getText().toString();

            if (name.isEmpty() || dosage.isEmpty() || selectedHour == -1 || selectedMinute == -1) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int freq = finalFrequencyHours;
            if (!freqText.isEmpty()) {
                try {
                    freq = Integer.parseInt(freqText);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid frequency, using once per day", Toast.LENGTH_SHORT).show();
                }
            }

            // Update DB
            med.name = name;
            med.dosage = dosage;
            med.time = String.format("%02d:%02d (every %dh)", selectedHour, selectedMinute, freq);

            db.medicationDao().update(med);

            // Cancel old WorkManager job
            WorkManager.getInstance(this).cancelAllWorkByTag("med_" + medId);

            // Schedule new one
            scheduleReminder(medId, name, dosage, freq);

            Toast.makeText(this, "Medication updated", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void scheduleReminder(int medId, String medName, String dosage, int frequencyHours) {
        Data inputData = new Data.Builder()
                .putInt("medId", medId)
                .putString("medName", medName)
                .putString("dosage", dosage)
                .build();

        PeriodicWorkRequest reminderRequest =
                new PeriodicWorkRequest.Builder(ReminderWorker.class, frequencyHours, TimeUnit.HOURS)
                        .setInputData(inputData)
                        .addTag("med_" + medId)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "med_" + medId,
                ExistingPeriodicWorkPolicy.REPLACE,
                reminderRequest
        );
    }
}
