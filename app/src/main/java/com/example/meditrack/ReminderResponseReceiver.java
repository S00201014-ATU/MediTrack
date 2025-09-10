package com.example.meditrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class ReminderResponseReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int medId = intent.getIntExtra("medId", -1);
        String medName = intent.getStringExtra("medName");
        String dosage = intent.getStringExtra("dosage");

        if (action == null || medId == -1) return;

        if ("ACTION_SNOOZE".equals(action)) {
            // Schedule the reminder again in 10 minutes
            Data data = new Data.Builder()
                    .putInt("medId", medId)
                    .putString("medName", medName)
                    .putString("dosage", dosage)
                    .build();

            OneTimeWorkRequest snoozeWork = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                    .setInitialDelay(10, TimeUnit.MINUTES)
                    .setInputData(data)
                    .build();

            WorkManager.getInstance(context).enqueue(snoozeWork);

            Toast.makeText(context, "Snoozed for 10 minutes", Toast.LENGTH_SHORT).show();
        }
    }
}
