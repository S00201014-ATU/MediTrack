package com.example.meditrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the button
        Button btnAddMedication = findViewById(R.id.btnAddMedication);

        //Show a message when the button is clicked
        btnAddMedication.setOnClickListener(v ->
                Toast.makeText(this, "Add medication clicked", Toast.LENGTH_SHORT).show()
        );

        btnAddMedication.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddMedicationActivity.class);
            startActivity(intent);
        });
    }
}