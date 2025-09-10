package com.example.meditrack;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Medication table in the database
@Entity
public class Medication {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String dosage;
    public String time;
}
