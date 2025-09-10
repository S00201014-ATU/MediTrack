package com.example.meditrack;

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
import androidx.work.WorkManager;

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
        holder.txtStock.setText("Stock: " + med.stock);

        // Delete button
        holder.btnDelete.setOnClickListener(v -> {
            WorkManager.getInstance(context).cancelAllWorkByTag("med_" + med.id);

            AppDatabase db = Room.databaseBuilder(
                    context,
                    AppDatabase.class, "medication-db"
            ).fallbackToDestructiveMigration().allowMainThreadQueries().build();
            db.medicationDao().deleteById(med.id);

            medications.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, medications.size());
        });

        // Edit button
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditMedicationActivity.class);
            intent.putExtra("medId", med.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return medications.size();
    }

    static class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView txtMedName, txtMedDetails, txtStock;
        Button btnDelete, btnEdit;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMedName = itemView.findViewById(R.id.txtMedName);
            txtMedDetails = itemView.findViewById(R.id.txtMedDetails);
            txtStock = itemView.findViewById(R.id.txtMedStock); // NEW
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}
