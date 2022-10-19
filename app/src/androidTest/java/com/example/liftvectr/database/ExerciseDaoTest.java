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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import java.util.ArrayList;
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

    private float testAvgForce;
    private float testPeakForce;
    private ArrayList<Float> testForceVsTimeXValues;
    private ArrayList<Float> testForceVsTimeYValues;

    private final float DELTA = 0.001f;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void initDb() {
        Context context = ApplicationProvider.getApplicationContext();
        mockExerciseDatabase = Room.inMemoryDatabaseBuilder(context, ExerciseDatabase.class)
                .allowMainThreadQueries()
                .build();
        mockExerciseDao = mockExerciseDatabase.ExerciseDao();

        testIMUData = new IMUData(1.23f, 4.56f, 7.89f, 1.23f, 4.56f, 7.89f, 1);
        testExercise = new Exercise("Bench Press", 250, new Date(12345678));
        testExercise.addDataSample(testIMUData);

        // Post recording analysis
        testForceVsTimeXValues = new ArrayList<Float>(Arrays.asList(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f , 9f, 10f));
        testForceVsTimeYValues = new ArrayList<Float>(Arrays.asList(1.23f, 3.45f, 6.78f, 9.12f, 12.13f, 0.01f, 0.09f, 193.21f, 1.123f, 0.91f));
        testAvgForce = 12.39f;
        testPeakForce = 21.90f;

        testExercise.setAvgForce(testAvgForce);
        testExercise.setPeakForce(testPeakForce);
        testExercise.setForceVsTimeXValues(testForceVsTimeXValues);
        testExercise.setForceVsTimeYValues(testForceVsTimeXValues);

        testIMUData2 = new IMUData(9.12f, 3.87f, 11.32f, 1.30f, 3.39f, 0.01f, 2);
        testExercise2 = new Exercise("Squat", 190, new Date(87654321));
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
        assertEquals(savedExercise.getWeight(), testExercise.getWeight(), DELTA);
        assertEquals(savedExercise.getDate(), testExercise.getDate());
        assertEquals(savedExercise.getData(), testExercise.getData());

        assertEquals(savedExercise.getAvgForce(), testExercise.getAvgForce(), DELTA);
        assertEquals(savedExercise.getPeakForce(), testExercise.getPeakForce(), DELTA);
        assertEquals(savedExercise.getForceVsTimeXValues(), testExercise.getForceVsTimeXValues());
        assertEquals(savedExercise.getForceVsTimeYValues(), testExercise.getForceVsTimeYValues());
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
        assertEquals(allSavedExercises.size(), 0);
    }

    @Test
    public void testGetAllExercises() throws Exception {
        mockExerciseDao.insert(testExercise);
        mockExerciseDao.insert(testExercise2);

        List<Exercise> allSavedExercises = LiveDataTestUtil.getValue(mockExerciseDao.getAllExercises());

        // Exercises should be returned in descending order (most recent exercise first)
        assertEquals(allSavedExercises.get(0).getType(), testExercise2.getType());
        assertEquals(allSavedExercises.get(0).getWeight(), testExercise2.getWeight(), DELTA);
        assertEquals(allSavedExercises.get(0).getDate(), testExercise2.getDate());
        assertEquals(allSavedExercises.get(0).getData(), testExercise2.getData());

        assertEquals(allSavedExercises.get(1).getType(), testExercise.getType());
        assertEquals(allSavedExercises.get(1).getWeight(), testExercise.getWeight(), DELTA);
        assertEquals(allSavedExercises.get(1).getDate(), testExercise.getDate());
        assertEquals(allSavedExercises.get(1).getData(), testExercise.getData());
    }
}