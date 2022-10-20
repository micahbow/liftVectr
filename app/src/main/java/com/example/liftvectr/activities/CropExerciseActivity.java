package com.example.liftvectr.activities;

import static com.example.liftvectr.util.ChartDisplay.displayIMUDataChart;
import static com.example.liftvectr.util.StatisticsLib.averageForce;
import static com.example.liftvectr.util.StatisticsLib.getForceValues;
import static com.example.liftvectr.util.StatisticsLib.getTimeValues;
import static com.example.liftvectr.util.StatisticsLib.peakForce;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.liftvectr.R;
import com.example.liftvectr.data.Exercise;
import com.example.liftvectr.data.IMUData;
import com.example.liftvectr.database.ExerciseViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.highlight.Highlight;

import java.util.ArrayList;

public class CropExerciseActivity extends AppCompatActivity {

    private LineChart IMUAccLineChart;
    private Button cancelButton;
    private Button saveButton;

    private ExerciseViewModel exerciseViewModel;
    private Exercise exercise;

    private Highlight[] highlighted;
    private float highlightedX;

    private ArrayList<IMUData> exerciseData;
    private ArrayList<IMUData> adjustedExerciseData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_exercise);

        cancelButton = (Button) findViewById(R.id.cancel_button);
        saveButton = (Button) findViewById(R.id.save_button);
        IMUAccLineChart = (LineChart) findViewById(R.id.line_chart);

        exerciseViewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);

        // Accessing the exercise/config data sent over from ExerciseHistoryActivity
        Intent intent = getIntent();
        exercise = (Exercise) intent.getSerializableExtra("exercise");

        displayIMUDataChart(exercise, IMUAccLineChart, "a_only", "Recorded IMU Data");
        // User needs to be able to adjust the exercise passed in from the intent
        IMUAccLineChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                highlighted = IMUAccLineChart.getHighlighted();
                if(highlighted != null)
                {
                    cropExercise();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transitionToAddExerciseActivity();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calculate stats from the adjustedExercise W/ StatLib
                exercise.setForceVsTimeXValues(getTimeValues(exercise.getData()));
                exercise.setForceVsTimeYValues(getForceValues(exercise));
                exercise.setAvgForce(averageForce(exercise.getData(), exercise.getForceVsTimeYValues()
                ));
                exercise.setPeakForce(peakForce(exercise.getForceVsTimeYValues()));

                exerciseViewModel.saveExercise(exercise);
                transitionToExerciseHistoryActivity();
            }
        });
    }

    public void transitionToAddExerciseActivity()
    {
        Intent intent = new Intent(this, AddExerciseActivity.class);
        startActivity(intent);
    }

    public void transitionToExerciseHistoryActivity()
    {
        Intent intent = new Intent(this, ExerciseHistoryActivity.class);
        startActivity(intent);
    }

    public void cropExercise()
    {
        highlightedX = highlighted[0].getX();
        exerciseData = exercise.getData();
        adjustedExerciseData = new ArrayList<>();
        for(int i = 0; i < (int) highlightedX; i++)
        {
            adjustedExerciseData.add(exerciseData.get(i));
        }
        exercise.setData(adjustedExerciseData);
    }
}