package com.example.meditrack;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
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
        ).fallbackToDestructiveMigration().allowMainThreadQueries().build();

        // Save log to History
        History history = new History();
        history.medicationId = medId;
        history.dosage = dosage;
        history.timestamp = System.currentTimeMillis();

        if ("ACTION_TAKEN".equals(action)) {
            history.status = "Taken";

            // Decrement stock
            Medication medication = db.medicationDao().getById(medId); // ✅ fixed here
            if (medication != null) {
                medication.stock -= 1;
                db.medicationDao().update(medication);

                // Low stock warning (≤ 3 and > 0)
                if (medication.stock <= 3 && medication.stock > 0) {
                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "medication_reminder_channel")
                            .setSmallIcon(android.R.drawable.stat_sys_warning)
                            .setContentTitle("Low Stock Warning")
                            .setContentText(medication.name + " is running low (" + medication.stock + " left).")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true);

                    notificationManager.notify(medId + 2000, builder.build());
                }

                // Out of stock notification
                if (medication.stock <= 0) {
                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "medication_reminder_channel")
                            .setSmallIcon(android.R.drawable.stat_notify_error)
                            .setContentTitle("Out of Stock")
                            .setContentText(medication.name + " has run out of stock.")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true);

                    notificationManager.notify(medId + 3000, builder.build());
                }
            }
        } else if ("ACTION_MISSED".equals(action)) {
            history.status = "Missed";
        }

        db.historyDao().insert(history);
    }
}
