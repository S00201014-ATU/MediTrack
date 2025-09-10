package com.example.meditrack;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<History> historyList;

    public HistoryAdapter(List<History> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        History history = historyList.get(position);

        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(history.timestamp));

        holder.txtMedName.setText(history.name + " - " + history.dosage);
        holder.txtStatus.setText(history.status);
        holder.txtDate.setText(date);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtMedName, txtStatus, txtDate;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMedName = itemView.findViewById(R.id.txtMedName);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtDate = itemView.findViewById(R.id.txtDate);
        }
    }
}
