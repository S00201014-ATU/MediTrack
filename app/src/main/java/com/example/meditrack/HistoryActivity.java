package com.example.meditrack;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        RecyclerView recyclerView = findViewById(R.id.recyclerHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AppDatabase db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class, "medication-db"
        ).fallbackToDestructiveMigration().allowMainThreadQueries().fallbackToDestructiveMigration().build();

        List<History> historyList = db.historyDao().getAll();
        HistoryAdapter adapter = new HistoryAdapter(historyList);
        recyclerView.setAdapter(adapter);
    }
}
