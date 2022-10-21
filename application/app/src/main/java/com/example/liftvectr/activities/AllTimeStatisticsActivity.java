package com.example.liftvectr.activities;

import static com.example.liftvectr.util.ChartDisplay.displaySingleLineChart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.liftvectr.R;
import com.example.liftvectr.data.Exercise;
import com.example.liftvectr.database.ExerciseViewModel;
import com.example.liftvectr.data.IMUData;
import com.example.liftvectr.util.ReadRunnable;
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;

public class AllTimeStatisticsActivity extends AppCompatActivity {
    private Spinner exerciseSpinner;
    private EditText weightInput;
    private Button exerciseBtn;

    private ExerciseViewModel exerciseViewModel;
    private ListView exerciseList;
    private List<Exercise> savedExercises;

    private LineChart avgForceTimeChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_time_statistics);

        // Initialize and Assign Variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set Create Exercise Selected
        bottomNavigationView.setSelectedItemId(R.id.all_time_statistics_page);

        // Perform ItemSelectedListener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.create_exercise_page:
                        startActivity(new Intent(getApplicationContext(), AddExerciseActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.exercise_history_page:
                        startActivity(new Intent(getApplicationContext(), ExerciseHistoryActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.all_time_statistics_page:
                        return true;
                }
                return false;
            }
        });

        exerciseSpinner = (Spinner) findViewById(R.id.spinner);
        weightInput = (EditText) findViewById(R.id.editWeight);
        exerciseBtn = (Button) findViewById(R.id.button);
//        avgForceTimeChart = (LineChart) findViewById(R.id.avgForceTime);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.exercises_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinners

        exerciseSpinner.setAdapter(adapter);

        // Populating Exercises
        exerciseViewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);
        exerciseViewModel.getAllExercises().observe(this, exercises -> {
            savedExercises = exerciseViewModel.getAllExercises().getValue();
        });


        exerciseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(exerciseBtn.getText().equals("Find Exercises")) {
                    if (weightInput.getText().toString().isEmpty()) {
                        setToastText("Please enter your exercise weight.");
                        return;
                    }

                    float weight = Float.parseFloat(weightInput.getText().toString());
                    if (weight < 0 || weight > 1500) {
                        setToastText("We wouldn't advise lifting this much");
                        return;
                    }

                    String exerciseType = exerciseSpinner.getSelectedItem().toString();

                    // If database contains saved exercises, look for any matching input criteria
                    if(!savedExercises.isEmpty()) {
                        int numMatches = 0;
                        List<Exercise> exerciseMatches = new ArrayList<Exercise>();
                        List<Float> avgForces = new ArrayList<Float>();
                        List<Date> dates = new ArrayList<Date>();

                        List<Float> tempPlotting = new ArrayList<Float>();

                        for(int i = 0; i < savedExercises.size(); i++) {
                            if(savedExercises.get(i).getType().equals(exerciseType) &&
                                    savedExercises.get(i).getWeight() == weight) {
                                exerciseMatches.add(savedExercises.get(i));
                                numMatches++;
                            }
                        }

                        if(numMatches != 0) {
                            for(int i = exerciseMatches.size()-1; i >= 0; i--) {
                                Float force = new Float(exerciseMatches.get(i).getAvgForce());
                                avgForces.add(force);
                                dates.add(exerciseMatches.get(i).getDate());
                            }

                            for(int i = 0; i < exerciseMatches.size(); i++) {
                                Float temp = new Float(i);
                                tempPlotting.add(temp);
                            }

                            avgForceTimeChart = (LineChart) findViewById(R.id.avgForceTime);

                            displaySingleLineChart(avgForceTimeChart,
                                    tempPlotting,
                                    avgForces,
                                    "Date", "Average Force");
                        }
                        else {
                            setToastText("No exercises matching this criteria were found!");
                            return;
                        }

                    }
                }
            }
        });

    }

    public void setToastText(String Text) {
        runOnUiThread(() -> Toast.makeText(AllTimeStatisticsActivity.this, Text, Toast.LENGTH_SHORT).show());
    }

}