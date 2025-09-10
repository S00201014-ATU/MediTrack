package com.example.meditrack;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.util.Calendar;

public class AddMedicationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        EditText txtMedicationName = findViewById(R.id.txtMedicationName);
        EditText txtDosage = findViewById(R.id.txtDosage);
        EditText txtTime = findViewById(R.id.txtTime);
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
            String time = txtTime.getText().toString();
            String freqText = txtFrequency.getText().toString();

            if (name.isEmpty() || dosage.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate time format HH:mm
            if (!time.matches("\\d{2}:\\d{2}")) {
                Toast.makeText(this, "Enter time in HH:mm format", Toast.LENGTH_SHORT).show();
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

            // Parse time into hours and minutes
            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // Save into database and get ID
            Medication med = new Medication();
            med.name = name;
            med.dosage = dosage;
            med.time = time + " (every " + frequencyHours + "h)";
            long medId = db.medicationDao().insert(med); // returns new row ID
            int medIdInt = (int) medId;

            // Schedule repeating reminders
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            // Schedule reminders across 24 hours, repeating daily
            for (int t = 0; t < 24; t += frequencyHours) {
                Calendar reminderTime = (Calendar) calendar.clone();
                reminderTime.add(Calendar.HOUR_OF_DAY, t);

                Intent reminderIntent = new Intent(this, ReminderReceiver.class);
                reminderIntent.putExtra("name", name);
                reminderIntent.putExtra("dosage", dosage);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        medIdInt * 100 + t, // unique code per med and slot
                        reminderIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                if (alarmManager != null) {
                    alarmManager.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            reminderTime.getTimeInMillis(),
                            AlarmManager.INTERVAL_DAY,
                            pendingIntent
                    );
                }
            }

            Toast.makeText(this,
                    "Medication saved with reminders every " + frequencyHours + " hours (daily)",
                    Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
