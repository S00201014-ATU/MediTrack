package com.example.meditrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.room.Room;

public class ReminderResponseReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int medId = intent.getIntExtra("medId", -1);
        String name = intent.getStringExtra("name");
        String dosage = intent.getStringExtra("dosage");
        String status = intent.getStringExtra("status");

        // Save response to history
        AppDatabase db = Room.databaseBuilder(
                context,
                AppDatabase.class, "medication-db"
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build();

        History history = new History();
        history.medicationId = medId;
        history.name = name;
        history.dosage = dosage;
        history.status = status;
        history.timestamp = System.currentTimeMillis();

        db.historyDao().insert(history);

        Toast.makeText(context, "Logged as " + status, Toast.LENGTH_SHORT).show();
    }
}
