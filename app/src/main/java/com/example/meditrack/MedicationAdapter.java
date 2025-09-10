package com.example.meditrack;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.Calendar;
import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {

    private List<Medication> medications;
    private Context context;

    public MedicationAdapter(List<Medication> medications, Context context) {
        this.medications = medications;
        this.context = context;
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        Medication med = medications.get(position);

        holder.txtMedName.setText(med.name);
        holder.txtMedDetails.setText(med.dosage + " at " + med.time);

        holder.btnDelete.setOnClickListener(v -> {
            // Cancel alarms
            cancelReminders(med.id, med.time);

            // Delete from database
            AppDatabase db = Room.databaseBuilder(
                    context,
                    AppDatabase.class, "medication-db"
            ).allowMainThreadQueries().build();
            db.medicationDao().deleteById(med.id);

            // Remove from list and refresh
            medications.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, medications.size());
        });
    }

    @Override
    public int getItemCount() {
        return medications.size();
    }

    static class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView txtMedName, txtMedDetails;
        Button btnDelete;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMedName = itemView.findViewById(R.id.txtMedName);
            txtMedDetails = itemView.findViewById(R.id.txtMedDetails);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private void cancelReminders(int medId, String time) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Extract frequency from time string (e.g. "08:00 (every 8h)")
        int frequencyHours = 24;
        if (time.contains("every")) {
            try {
                frequencyHours = Integer.parseInt(time.replaceAll("\\D+", ""));
            } catch (Exception ignored) {}
        }

        for (int t = 0; t < 24; t += frequencyHours) {
            Intent reminderIntent = new Intent(context, ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    medId * 100 + t,
                    reminderIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
        }
    }
}
