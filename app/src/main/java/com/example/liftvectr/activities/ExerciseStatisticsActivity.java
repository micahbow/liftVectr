package com.example.liftvectr.activities;

import static com.example.liftvectr.util.ChartDisplay.displayIMUDataChart;
import static com.example.liftvectr.util.ChartDisplay.displaySingleLineChart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.liftvectr.R;
import com.example.liftvectr.data.Exercise;
import com.github.mikephil.charting.charts.LineChart;

public class ExerciseStatisticsActivity extends AppCompatActivity {

    private Button returnBtn;

    private TextView exerciseType;
    private TextView exerciseWeight;
    private TextView exerciseDate;
    private TextView peakForceTextbox;
    private TextView averageForceTextbox;

    private LineChart IMUAccLineChart;
    private LineChart IMUGyroLineChart;
    private LineChart forceVsTimeChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_statistics);

        returnBtn = (Button) findViewById(R.id.return_button);
        exerciseType = (TextView) findViewById(R.id.exercise_type);
        exerciseDate = (TextView) findViewById(R.id.exercise_date);
        exerciseWeight = (TextView) findViewById(R.id.exercise_weight);

        IMUAccLineChart = (LineChart) findViewById(R.id.acc_line_chart);
        IMUGyroLineChart = (LineChart) findViewById(R.id.gyro_line_chart);
        forceVsTimeChart = (LineChart) findViewById(R.id.FvT_line_chart);
        peakForceTextbox = (TextView) findViewById(R.id.peakForceTextbox);
        averageForceTextbox = (TextView) findViewById(R.id.averageForceTextbox);

        // Accessing the exercise/config data sent over from ExerciseHistoryActivity
        Intent intent = getIntent();
        Exercise exercise = (Exercise) intent.getSerializableExtra("exercise");

        // Display text boxes
        exerciseType.setText("Exercise Type: " + exercise.getType());
        exerciseDate.setText("Date: " + exercise.getDate().toString());
        exerciseWeight.setText("Weight: " + exercise.getWeight());
        peakForceTextbox.setText(String.format("Peak Force: %.2f", exercise.getPeakForce()));
        averageForceTextbox.setText(String.format("Average Force: %.2f", exercise.getAvgForce()));

        // Display charts
        displayIMUDataChart(exercise, IMUAccLineChart, "a_only", "Linear Acceleration vs Time");
        displayIMUDataChart(exercise, IMUGyroLineChart, "g_only", "Angular Velocity vs Time");
        displaySingleLineChart(forceVsTimeChart,
                exercise.getForceVsTimeXValues(),
                exercise.getForceVsTimeYValues(),
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