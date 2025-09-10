package com.example.meditrack;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddMedicationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        EditText txtMedicationName = findViewById(R.id.txtMedicationName);
        EditText txtDosage = findViewById(R.id.txtDosage);
        EditText txtTime = findViewById(R.id.txtTime);
        Button btnSaveMedication = findViewById(R.id.btnSaveMedication);

        // When Save is clicked, show entered details in a message
        btnSaveMedication.setOnClickListener(v -> {
            String name = txtMedicationName.getText().toString();
            String dosage = txtDosage.getText().toString();
            String time = txtTime.getText().toString();

            String message = "Saved: " + name + " - " + dosage + " at " + time;
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            // For now, just close the screen
            finish();
        });
    }
}
