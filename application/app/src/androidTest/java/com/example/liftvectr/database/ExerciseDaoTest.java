package com.example.liftvectr.database;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.liftvectr.data.Exercise;
import com.example.liftvectr.data.IMUData;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ExerciseDaoTest {

    private ExerciseDatabase mockExerciseDatabase;
    private ExerciseDao mockExerciseDao;

    private Exercise testExercise;
    private IMUData testIMUData;

    private Exercise testExercise2;
    private IMUData testIMUData2;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void initDb() {
        Context context = ApplicationProvider.getApplicationContext();
        mockExerciseDatabase = Room.inMemoryDatabaseBuilder(context, ExerciseDatabase.class)
                .allowMainThreadQueries()
                .build();
        mockExerciseDao = mockExerciseDatabase.ExerciseDao();

        testIMUData = new IMUData(1.23f, 4.56f, 7.89f, 1.23f, 4.56f, 7.89f, 1.23f, 4.56f, 7.89f, 1);
        testExercise = new Exercise("Bench Press", new Date(12345678));
        testExercise.addDataSample(testIMUData);

        testIMUData2 = new IMUData(9.12f, 3.87f, 11.32f, 1.30f, 3.39f, 0.01f, 0.71f, 6.13f, 8.90f, 2);
        testExercise2 = new Exercise("Squat", new Date(87654321));
        testExercise.addDataSample(testIMUData2);
    }

    @After
    public void closeDb() {
        mockExerciseDatabase.close();
    }

    @Test
    public void testSaveExercise() throws Exception {
        mockExerciseDao.insert(testExercise);
        List<Exercise> allSavedExercises = LiveDataTestUtil.getValue(mockExerciseDao.getAllExercises());

        Exercise savedExercise = allSavedExercises.get(0);

        assertEquals(savedExercise.getType(), testExercise.getType());
        assertEquals(savedExercise.getDate(), testExercise.getDate());
        assertEquals(savedExercise.getData(), testExercise.getData());
    }

    @Test
    public void testDeleteExercise() throws Exception {
        mockExerciseDao.insert(testExercise);
        mockExerciseDao.insert(testExercise2);

        mockExerciseDao.delete(testExercise);
        List<Exercise> allSavedExercises = LiveDataTestUtil.getValue(mockExerciseDao.getAllExercises());
        assertEquals(allSavedExercises.size(), 1);

        mockExerciseDao.delete(testExercise2);
        allSavedExercises = LiveDataTestUtil.getValue(mockExerciseDao.getAllExercises());
        assertEquals(allSavedExercises.size(), 0);
    }

    @Test
    public void testDeleteAllExercises() throws Exception {
        mockExerciseDao.insert(testExercise);
        mockExerciseDao.insert(testExercise2);

        mockExerciseDao.deleteAllExercises();

        List<Exercise> allSavedExercises = LiveDataTestUtil.getValue(mockExerciseDao.getAllExercises());
        allSavedExercises = LiveDataTestUtil.getValue(mockExerciseDao.getAllExercises());
        assertEquals(allSavedExercises.size(), 0);
    }

    @Test
    public void testGetAllExercises() throws Exception {
        mockExerciseDao.insert(testExercise);
        mockExerciseDao.insert(testExercise2);

        List<Exercise> allSavedExercises = LiveDataTestUtil.getValue(mockExerciseDao.getAllExercises());

        // Exercises should be returned in descending order (most recent exercise first)
        assertEquals(allSavedExercises.get(0).getType(), testExercise2.getType());
        assertEquals(allSavedExercises.get(0).getDate(), testExercise2.getDate());
        assertEquals(allSavedExercises.get(0).getData(), testExercise2.getData());

        assertEquals(allSavedExercises.get(1).getType(), testExercise.getType());
        assertEquals(allSavedExercises.get(1).getDate(), testExercise.getDate());
        assertEquals(allSavedExercises.get(1).getData(), testExercise.getData());
    }
}