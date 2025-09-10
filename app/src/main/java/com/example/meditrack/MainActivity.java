package com.example.meditrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MedicationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnAddMedication = findViewById(R.id.btnAddMedication);
        RecyclerView recyclerView = findViewById(R.id.recyclerMedications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AppDatabase db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class, "medication-db"
        ).allowMainThreadQueries().build();

        // Load saved medications
        List<Medication> medications = db.medicationDao().getAll();
        adapter = new MedicationAdapter(medications);
        recyclerView.setAdapter(adapter);

        btnAddMedication.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddMedicationActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list after returning from AddMedicationActivity
        AppDatabase db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class, "medication-db"
        ).allowMainThreadQueries().build();

        List<Medication> medications = db.medicationDao().getAll();
        adapter = new MedicationAdapter(medications);
        RecyclerView recyclerView = findViewById(R.id.recyclerMedications);
        recyclerView.setAdapter(adapter);
    }
}
