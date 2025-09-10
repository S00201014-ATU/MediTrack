package com.example.meditrack;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int medId = intent.getIntExtra("medId", -1);
        String name = intent.getStringExtra("name");
        String dosage = intent.getStringExtra("dosage");

        NotificationHelper.createNotificationChannel(context);

        // Intent for "Taken"
        Intent takenIntent = new Intent(context, ReminderResponseReceiver.class);
        takenIntent.putExtra("medId", medId);
        takenIntent.putExtra("name", name);
        takenIntent.putExtra("dosage", dosage);
        takenIntent.putExtra("status", "Taken");

        PendingIntent takenPendingIntent = PendingIntent.getBroadcast(
                context,
                medId * 10,
                takenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent for "Missed"
        Intent missedIntent = new Intent(context, ReminderResponseReceiver.class);
        missedIntent.putExtra("medId", medId);
        missedIntent.putExtra("name", name);
        missedIntent.putExtra("dosage", dosage);
        missedIntent.putExtra("status", "Missed");

        PendingIntent missedPendingIntent = PendingIntent.getBroadcast(
                context,
                medId * 20,
                missedIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification with action buttons
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Medication Reminder")
                .setContentText("Time to take " + name + " - " + dosage)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(0, "Taken", takenPendingIntent)
                .addAction(0, "Missed", missedPendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
