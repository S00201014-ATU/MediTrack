package com.example.meditrack;

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

            Toast.makeText(this, "Medication saved", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
