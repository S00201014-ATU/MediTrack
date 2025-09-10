package com.example.meditrack;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class History {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int medicationId;
    public String name;
    public String dosage;
    public String status; // "Taken" or "Missed"
    public long timestamp;
}
