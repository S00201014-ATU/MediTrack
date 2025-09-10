package com.example.meditrack;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MedicationDao {

    // Add a new medication
    @Insert
    long insert(Medication medication);

    // Get all medications
    @Query("SELECT * FROM Medication")
    List<Medication> getAll();

    @Query("DELETE FROM Medication WHERE id = :medId")
    void deleteById(int medId);
}
