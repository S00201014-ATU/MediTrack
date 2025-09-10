package com.example.meditrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.room.Room;
import androidx.work.WorkManager;

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
        ).fallbackToDestructiveMigration().allowMainThreadQueries().build();

        // Save log to History
        History history = new History();
        history.medicationId = medId;
        history.name = medName;
        history.dosage = dosage;
        history.status = action.equals("ACTION_TAKEN") ? "Taken" : "Missed";
        history.timestamp = System.currentTimeMillis();

        db.historyDao().insert(history);

        // If Taken, reduce stock
        if (action.equals("ACTION_TAKEN")) {
            Medication med = db.medicationDao().getById(medId);
            if (med != null) {
                med.stock = Math.max(0, med.stock - 1); // avoid negative
                db.medicationDao().update(med);

                if (med.stock == 0) {
                    // Cancel reminders
                    WorkManager.getInstance(context).cancelAllWorkByTag("med_" + medId);

                    // Notify user
                    Toast.makeText(context,
                            medName + " is out of stock. Reminders cancelled.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        // Show quick feedback for both Taken and Missed
        String msg = medName + " marked as " + history.status;
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
