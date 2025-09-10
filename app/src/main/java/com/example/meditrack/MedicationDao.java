package com.example.meditrack;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MedicationDao {

    // Add a new medication
    @Insert
    void insert(Medication medication);

    // Get all medications
    @Query("SELECT * FROM Medication")
    List<Medication> getAll();
}
