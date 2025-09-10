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

public class AddMedicationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        EditText txtMedicationName = findViewById(R.id.txtMedicationName);
        EditText txtDosage = findViewById(R.id.txtDosage);
        EditText txtTime = findViewById(R.id.txtTime);
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

            if (name.isEmpty() || dosage.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save into database
            Medication med = new Medication();
            med.name = name;
            med.dosage = dosage;
            med.time = time;
            db.medicationDao().insert(med);

            // --- Schedule reminder (currently 5 seconds later for testing) ---
            long triggerTime = System.currentTimeMillis() + 5000; // TODO: replace with parsed time

            Intent reminderIntent = new Intent(this, ReminderReceiver.class);
            reminderIntent.putExtra("name", name);
            reminderIntent.putExtra("dosage", dosage);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    (int) System.currentTimeMillis(), // unique request code
                    reminderIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }

            Toast.makeText(this, "Medication saved with reminder", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
