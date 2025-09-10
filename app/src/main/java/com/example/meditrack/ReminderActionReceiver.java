package com.example.meditrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.room.Room;

public class ReminderActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int medId = intent.getIntExtra("medId", -1);
        String medName = intent.getStringExtra("medName");
        String dosage = intent.getStringExtra("dosage");

        if (action == null || medId == -1) return;

        // Build database
        AppDatabase db = Room.databaseBuilder(
                context,
                AppDatabase.class, "medication-db"
        ).allowMainThreadQueries().build();

        // Save log to History
        History history = new History();
        history.medicationId = medId;
        history.name = medName;
        history.dosage = dosage;
        history.status = action.equals("ACTION_TAKEN") ? "Taken" : "Missed";
        history.timestamp = System.currentTimeMillis();

        db.historyDao().insert(history);

        // Show quick feedback
        String msg = medName + " marked as " + history.status;
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
