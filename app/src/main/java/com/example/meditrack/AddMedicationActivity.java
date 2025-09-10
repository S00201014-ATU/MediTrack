package com.example.meditrack;

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

import java.util.concurrent.TimeUnit;

public class AddMedicationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        EditText txtMedicationName = findViewById(R.id.txtMedicationName);
        EditText txtDosage = findViewById(R.id.txtDosage);
        EditText txtTime = findViewById(R.id.txtTime);   // Still here, but currently unused by WorkManager
        EditText txtFrequency = findViewById(R.id.txtFrequency);
        Button btnSaveMedication = findViewById(R.id.btnSaveMedication);

        // Build database
        AppDatabase db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class, "medication-db"
        ).allowMainThreadQueries().build();

        btnSaveMedication.setOnClickListener(v -> {
            String name = txtMedicationName.getText().toString();
            String dosage = txtDosage.getText().toString();
            String time = txtTime.getText().toString();   // kept for UI, but not driving reminders yet
            String freqText = txtFrequency.getText().toString();

            if (name.isEmpty() || dosage.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse frequency (default once per day)
            int frequencyHours = 24;
            if (!freqText.isEmpty()) {
                try {
                    frequencyHours = Integer.parseInt(freqText);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid frequency, using once per day", Toast.LENGTH_SHORT).show();
                }
            }

            // Save into database and get ID
            Medication med = new Medication();
            med.name = name;
            med.dosage = dosage;
            med.time = time + " (every " + frequencyHours + "h)";
            long medId = db.medicationDao().insert(med); // returns new row ID
            int medIdInt = (int) medId;

            // Schedule reminder with WorkManager
            scheduleReminder(medIdInt, name, dosage, frequencyHours);

            Toast.makeText(this,
                    "Medication saved with reminders every " + frequencyHours + " hours",
                    Toast.LENGTH_SHORT).show();
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
                        .addTag("med_" + medId) // tag for cancellation later
                        .build();

        // Unique work ensures persistence across reboot and avoids duplicates
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "med_" + medId,
                ExistingPeriodicWorkPolicy.REPLACE,
                reminderRequest
        );
    }
}
