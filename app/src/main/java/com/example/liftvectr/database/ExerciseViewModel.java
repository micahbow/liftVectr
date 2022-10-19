package com.example.liftvectr.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.liftvectr.data.Exercise;

import java.util.List;

// References ExerciseRepository and keeps an up-to-date list of all exercises
public class ExerciseViewModel extends AndroidViewModel {
    private ExerciseRepository repository;

    // Using LiveData + ViewModel to store the exercises we retrieve from the database
    // allows us to put an observer on the data (instead of polling)
    private LiveData<List<Exercise>> allExercises;

    public ExerciseViewModel(@NonNull Application application) {
        super(application);
        repository = new ExerciseRepository(application);
        allExercises = repository.getAllExercises();
    }

    public void saveExercise(Exercise exercise) {
        repository.insert(exercise);
    }

    public void deleteExercise(Exercise exercise) {
        repository.delete(exercise);
    }

    public void deleteAllExercises() {
        repository.deleteAllExercises();
    }

    public LiveData<List<Exercise>> getAllExercises() {
        return allExercises;
    }
}