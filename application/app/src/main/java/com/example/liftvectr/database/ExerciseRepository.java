package com.example.liftvectr.database;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.liftvectr.data.Exercise;

import java.util.List;

// The centralized access point for the exercise database
// Calls database CRUD functions asynchronously
// https://developer.android.com/training/data-storage/room/async-queries
public class ExerciseRepository {
    private ExerciseDao exerciseDao;
    private LiveData<List<Exercise>> allExercises;

    public ExerciseRepository(Application application) {
        ExerciseDatabase database = ExerciseDatabase.getInstance(application);
        exerciseDao = database.ExerciseDao();
        allExercises = exerciseDao.getAllExercises();
    }

    public void insert(Exercise exercise) {
        new SaveExerciseAsyncTask(exerciseDao).execute(exercise);
    }

    public void delete(Exercise exercise) {
        new DeleteExerciseAsyncTask(exerciseDao).execute();
    }

    public void deleteAllExercises() {
        new DeleteAllExercisesAsyncTask(exerciseDao).execute();
    }

    public LiveData<List<Exercise>> getAllExercises() {
        return allExercises;
    }

    private static class SaveExerciseAsyncTask extends AsyncTask<Exercise, Void, Void> {
        private ExerciseDao dao;

        private SaveExerciseAsyncTask(ExerciseDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Exercise... exercises) {
            dao.insert(exercises[0]);
            return null;
        }
    }

    private static class DeleteExerciseAsyncTask extends AsyncTask<Exercise, Void, Void> {
        private ExerciseDao dao;

        private DeleteExerciseAsyncTask(ExerciseDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Exercise... exercises) {
            dao.delete(exercises[0]);
            return null;
        }
    }

    private static class DeleteAllExercisesAsyncTask extends AsyncTask<Void, Void, Void> {
        private ExerciseDao dao;

        private DeleteAllExercisesAsyncTask(ExerciseDao dao) {
            this.dao = dao;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            // on below line calling method
            // to delete all courses.
            dao.deleteAllExercises();
            return null;
        }
    }
}
