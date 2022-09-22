package com.example.liftvectr.activities;

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
    private TextView weight;
    private TextView peakForceTextbox;
    private TextView averageForceTextbox;

    private LineChart forceVsTimeChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_statistics);

        returnBtn = (Button) findViewById(R.id.return_button);
        exerciseType = (TextView) findViewById(R.id.exercise_type);
        forceVsTimeChart = (LineChart) findViewById(R.id.line_chart);
        peakForceTextbox = (TextView) findViewById(R.id.peakForceTextbox);
        averageForceTextbox = (TextView) findViewById(R.id.averageForceTextbox);

        // Accessing the exercise/config data sent over from ExerciseHistoryActivity
        Intent intent = getIntent();
        Exercise exercise = (Exercise) intent.getSerializableExtra("exercise");

        displaySingleLineChart(forceVsTimeChart,
                exercise.getForceVsTimeXValues(),
                exercise.getForceVsTimeYValues(),
                "Force (N)", "Force vs Time");
        peakForceTextbox.setText(String.format("Peak Force: %.2f", exercise.getPeakForce()));
        averageForceTextbox.setText(String.format("Average Force: %.2f", exercise.getAvgForce()));
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