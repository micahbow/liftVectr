package com.example.liftvectr.database;

import androidx.annotation.NonNull;
import androidx.room.*;

import com.example.liftvectr.Exercise;
import com.example.liftvectr.IMUData;

import java.util.*;
import java.text.*;

@Entity
public class ExerciseEntry {
    @PrimaryKey
    @NonNull
    public Date date;

    @ColumnInfo(name = "exerciseName")
    public String exerciseName;

    @ColumnInfo(name = "exerciseData")
    public ArrayList<IMUData> exerciseData;

    public ExerciseEntry(Exercise exercise) {
        this.date = exercise.getDate();
        this.exerciseName = exercise.getType();
        this.exerciseData = exercise.getExerciseData();
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date _date) {
        this.date = _date;
    }

}