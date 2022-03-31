package com.example.liftvectr.database;

import androidx.room.*;
import java.util.*;

@Dao
public interface ExerciseEntryDao {
    @Insert
    void insertAll(ExerciseEntry... exerciseEntries);

    @Delete
    void delete(ExerciseEntry exerciseEntry);

    @Query("SELECT * FROM exerciseEntry")
    List<ExerciseEntry> getAll();
}
