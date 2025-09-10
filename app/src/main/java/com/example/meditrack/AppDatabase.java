package com.example.meditrack;

import androidx.room.Database;
import androidx.room.RoomDatabase;

// Database holding the Medication table
@Database(entities = {Medication.class, History.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MedicationDao medicationDao();
    public abstract HistoryDao historyDao();
}

