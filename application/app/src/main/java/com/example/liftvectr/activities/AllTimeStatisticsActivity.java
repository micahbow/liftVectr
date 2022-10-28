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
import android.view.ViewGroup;
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
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class AllTimeStatisticsActivity extends AppCompatActivity {
    private ExerciseViewModel exerciseViewModel;
    private List<Exercise> savedExercises;
    private Spinner exerciseSpinner;
    private EditText weightInput;
    private Button exerciseBtn;
    private ArrayList<String> availableTypes = new ArrayList<String>();
    private ArrayList<String> exerciseTypes = new ArrayList<String>();
    private String initString = "Please select an available exercise type.";
    private LineChart avgForceVWeight;
    private LineChart avgForceTimeChart;
    private boolean updatedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_time_statistics);
        exerciseSpinner = findViewById(R.id.availableExercises);
        avgForceVWeight = findViewById(R.id.AvgForceVWeight_chart);
        avgForceVWeight.setVisibility(View.INVISIBLE);
        weightInput = (EditText) findViewById(R.id.editWeight);
        exerciseBtn = (Button) findViewById(R.id.button);
        avgForceTimeChart = (LineChart) findViewById(R.id.avgForceTime);
        updatedData = false;

        exerciseTypes.add(initString);
        exerciseTypes.add("Bench Press");
        exerciseTypes.add("Squat");
        exerciseTypes.add("Deadlift");

        //Initial adapter for exercise types
        ArrayAdapter initAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
                exerciseTypes);
        initAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        exerciseSpinner.setAdapter(initAdapter);

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


        //Populating Exercises
        exerciseViewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);
        exerciseViewModel.getAllExercises().observe(this, exercises -> {
            savedExercises = exerciseViewModel.getAllExercises().getValue();
            availableTypes = getAvailableTypes(savedExercises);

            availableTypes.add(0, initString);
            // Adapter updates once exercises from the db loads
            ArrayAdapter updatedAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
                    availableTypes);
            exerciseSpinner.setAdapter(updatedAdapter);

        });


        exerciseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(exerciseBtn.getText().equals("Find Exercises")) {
                    if (exerciseSpinner.getCount() == 1) {
                        setToastText("No available exercises. Go exercise.");
                        //avgForceVWeight.setVisibility(View.INVISIBLE);
                    }

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
                    if (!savedExercises.isEmpty()) {
                        int numMatches = 0;
                        List<Exercise> exerciseMatches = new ArrayList<Exercise>();
                        List<Float> avgForces = new ArrayList<Float>();
                        List<Date> dates = new ArrayList<Date>();

                        List<Float> tempPlotting = new ArrayList<Float>();

                        for (int i = 0; i < savedExercises.size(); i++) {
                            if (savedExercises.get(i).getType().equals(exerciseType) &&
                                    savedExercises.get(i).getWeight() == weight) {
                                exerciseMatches.add(savedExercises.get(i));
                                numMatches++;
                            }
                        }

                        if (numMatches != 0) {
                            for (int i = exerciseMatches.size() - 1; i >= 0; i--) {
                                Float force = new Float(exerciseMatches.get(i).getAvgForce());
                                avgForces.add(force);
                                dates.add(exerciseMatches.get(i).getDate());
                            }

                            for (int i = 0; i < exerciseMatches.size(); i++) {
                                Float temp = new Float(i);
                                tempPlotting.add(temp);
                            }

                            displaySingleLineChart(avgForceTimeChart,
                                    tempPlotting,
                                    avgForces,
                                    "Date", "Average Force");
                        } else {
                            setToastText("No exercises matching this criteria were found!");
                            return;
                        }

                        avgForceVWeight.setVisibility(View.VISIBLE);
                        ArrayList<Float> weightsTotal = new ArrayList<Float>();
                        ArrayList<Float> avgForcesTotal = new ArrayList<Float>();
                        Exercise currExercise;

                        // Map: [Weight, (sum of avgForce, # of same weight entries)]
                        Map<Float, ArrayList<Float>> dataMap = new TreeMap<Float, ArrayList<Float>>() {
                        };

                        for (int i = 0; i < savedExercises.size(); i++) {
                            currExercise = savedExercises.get(i);
                            ArrayList<Float> avgForceEntry = new ArrayList<Float>();    // 0: sum of avgForce, 1: # of entries
                            avgForceEntry.add(0f);
                            avgForceEntry.add(0f);

                            if (exerciseType.equals(currExercise.getType())) {
                                if (!dataMap.containsKey(currExercise.getWeight())) {
                                    avgForceEntry.set(0, currExercise.getAvgForce());
                                    avgForceEntry.set(1, 1f);
                                    dataMap.put(currExercise.getWeight(), avgForceEntry);
                                } else {
                                    Float currWeight = currExercise.getWeight();
                                    Float currSum = dataMap.get(currWeight).get(0);
                                    Float currEntries = dataMap.get(currWeight).get(1);

                                    avgForceEntry.set(0, currExercise.getAvgForce() + currSum);
                                    avgForceEntry.set(1, currEntries + 1);
                                    dataMap.put(currWeight, avgForceEntry);
                                }
                            }
                        }
                        for (Map.Entry<Float, ArrayList<Float>> entry : dataMap.entrySet()) {
                            Float key = entry.getKey();
                            ArrayList<Float> value = entry.getValue();
                            weightsTotal.add(key);
                            avgForcesTotal.add(value.get(0) / value.get(1));
                            Log.i("AllTimeStats", String.format("Type: %s, Weight: %f, Avg Force: %f", exerciseType, key, value.get(0) / value.get(1)));
                        }

                        //Plot avg force vs weight for all exercises of selected type
                        displaySingleLineChart(avgForceVWeight, weightsTotal, avgForcesTotal,
                                "Average Force (N)", "Average Force vs Weight");

                    }
                }
            }
        });
    }

    private ArrayList<String> getAvailableTypes(List<Exercise> exercises) {
        ArrayList<String> types = new ArrayList<String>();
        for (int i=0; i < exercises.size(); i++) {
            String currType = exercises.get(i).getType();
            if (!types.contains(currType)) {
                types.add(currType);
            }
        }
        return types;
    }

    public void setToastText(String Text) {
        runOnUiThread(() -> Toast.makeText(AllTimeStatisticsActivity.this, Text, Toast.LENGTH_SHORT).show());
    }
}