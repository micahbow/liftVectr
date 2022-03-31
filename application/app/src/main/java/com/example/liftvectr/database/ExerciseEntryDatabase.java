package com.example.liftvectr.database;

import androidx.room.*;
import java.util.*;

@Database(entities = {ExerciseEntry.class}, version = 1)
public abstract class ExerciseEntryDatabase extends RoomDatabase{
    public abstract ExerciseEntryDao exerciseEntryDao();
}
