package com.example.liftvectr.activities;

import static com.example.liftvectr.util.ChartDisplay.displaySingleLineChart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.liftvectr.R;
import com.example.liftvectr.data.Exercise;
import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class ExerciseStatisticsActivity extends AppCompatActivity {

    private Button returnBtn;
    private TextView exerciseType;
    private TextView weight;

    private LineChart forceVsTimeChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_statistics);

        returnBtn = (Button) findViewById(R.id.return_button);
        exerciseType = (TextView) findViewById(R.id.exercise_type);
        forceVsTimeChart = (LineChart) findViewById(R.id.line_chart);

        // Accessing the exercise/config data sent over from ExerciseHistoryActivity
        Intent intent = getIntent();
        Exercise exercise = (Exercise) intent.getSerializableExtra("exercise");

        // Normally call this with the exercise variable we have above
        // Currently calling this with fake data
        Exercise fakeExercise = new Exercise("Squat", 250, new Date());
        fakeExercise.setForceVsTimeXValues(new ArrayList<>(Arrays.asList(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f)));
        fakeExercise.setForceVsTimeYValues(new ArrayList<>(Arrays.asList(0.1f, 0.2f, 0.3f, 5.92f, 21.1f, 23.3f, 16.0f, 4.0f, 2.39f, 0.5f)));
        displaySingleLineChart(forceVsTimeChart,
                fakeExercise.getForceVsTimeXValues(),
                fakeExercise.getForceVsTimeYValues(),
                "Force (N)", "Force vs Time");

        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transitionToExerciseHistoryActivity();
            }
        });
    }

    public void transitionToExerciseHistoryActivity()
    {
        Intent intent = new Intent(this, ExerciseHistoryActivity.class);
        startActivity(intent);
    }
}