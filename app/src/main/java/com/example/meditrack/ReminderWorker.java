package com.example.meditrack;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ReminderWorker extends Worker {

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get data passed into this Worker
        Data input = getInputData();
        String medName = input.getString("medName");
        String dosage = input.getString("dosage");
        int medId = input.getInt("medId", -1);

        showNotification(medName, dosage, medId);

        return Result.success();
    }

    private void showNotification(String medName, String dosage, int medId) {
        Context context = getApplicationContext();
        String channelId = "medication_reminder_channel";

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create channel (only once on API 26+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Medication Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Intent for "Taken"
        Intent takenIntent = new Intent(context, ReminderActionReceiver.class);
        takenIntent.setAction("ACTION_TAKEN");
        takenIntent.putExtra("medId", medId);
        takenIntent.putExtra("medName", medName);
        takenIntent.putExtra("dosage", dosage);

        PendingIntent takenPendingIntent = PendingIntent.getBroadcast(
                context,
                medId, // unique per med
                takenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent for "Missed"
        Intent missedIntent = new Intent(context, ReminderActionReceiver.class);
        missedIntent.setAction("ACTION_MISSED");
        missedIntent.putExtra("medId", medId);
        missedIntent.putExtra("medName", medName);
        missedIntent.putExtra("dosage", dosage);

        PendingIntent missedPendingIntent = PendingIntent.getBroadcast(
                context,
                medId + 500, // offset for uniqueness
                missedIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent for "Snooze"
        Intent snoozeIntent = new Intent(context, ReminderResponseReceiver.class);
        snoozeIntent.setAction("ACTION_SNOOZE");
        snoozeIntent.putExtra("medId", medId);
        snoozeIntent.putExtra("medName", medName);
        snoozeIntent.putExtra("dosage", dosage);

        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                medId + 1000, // unique offset
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_popup_reminder) // system reminder icon
                .setContentTitle("Medication Reminder")
                .setContentText(medName + " - " + dosage)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(android.R.drawable.checkbox_on_background, "Taken", takenPendingIntent)
                .addAction(android.R.drawable.ic_delete, "Missed", missedPendingIntent)
                .addAction(android.R.drawable.ic_media_pause, "Snooze", snoozePendingIntent);

        notificationManager.notify(medId, builder.build());
    }
}
