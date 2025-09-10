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

public class AddMedicationActivity extends AppCompatActivity {

    private int selectedHour = -1;
    private int selectedMinute = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        EditText txtMedicationName = findViewById(R.id.txtMedicationName);
        EditText txtDosage = findViewById(R.id.txtDosage);
        EditText txtTime = findViewById(R.id.txtTime);
        EditText txtFrequency = findViewById(R.id.txtFrequency);
        EditText txtStock = findViewById(R.id.txtStock); // NEW field
        Button btnSaveMedication = findViewById(R.id.btnSaveMedication);

        // Disable manual typing into time field
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

        // Build database
        AppDatabase db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class, "medication-db"
        ).fallbackToDestructiveMigration().allowMainThreadQueries().build();

        btnSaveMedication.setOnClickListener(v -> {
            String name = txtMedicationName.getText().toString();
            String dosage = txtDosage.getText().toString();
            String freqText = txtFrequency.getText().toString();
            String stockText = txtStock.getText().toString();

            if (name.isEmpty() || dosage.isEmpty() || selectedHour == -1 || selectedMinute == -1) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
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

            // Parse stock (default 0)
            int stock = 0;
            if (!stockText.isEmpty()) {
                try {
                    stock = Integer.parseInt(stockText);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid stock, using 0", Toast.LENGTH_SHORT).show();
                }
            }

            // Save into database and get ID
            Medication med = new Medication();
            med.name = name;
            med.dosage = dosage;
            med.time = String.format("%02d:%02d (every %dh)", selectedHour, selectedMinute, frequencyHours);
            med.stock = stock; // NEW
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
                        .addTag("med_" + medId)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "med_" + medId,
                ExistingPeriodicWorkPolicy.REPLACE,
                reminderRequest
        );
    }
}
