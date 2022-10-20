package com.example.liftvectr.activities;

import static com.example.liftvectr.util.ChartDisplay.displaySingleLineChart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.liftvectr.R;
import com.example.liftvectr.data.Exercise;
import com.example.liftvectr.database.ExerciseViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class AllTimeStatisticsActivity extends AppCompatActivity {
    private ExerciseViewModel exerciseViewModel;
    private List<Exercise> savedExercises;
    private Spinner availableExercises;
    private ArrayList<String> availableTypes = new ArrayList<String>();
    private ArrayList<String> exerciseTypes = new ArrayList<String>();
    private String initString = "Please select an available exercise type.";
    private LineChart avgForceVWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_time_statistics);
        availableExercises = findViewById(R.id.availableExercises);
        avgForceVWeight = findViewById(R.id.AvgForceVWeight_chart);
        avgForceVWeight.setVisibility(View.INVISIBLE);

        exerciseTypes.add(initString);
        exerciseTypes.add("Bench Press");
        exerciseTypes.add("Squat");
        exerciseTypes.add("Deadlift");

        //Initial adapter for exercise types
        ArrayAdapter initAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
                exerciseTypes);
        initAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        availableExercises.setAdapter(initAdapter);

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
            availableExercises.setAdapter(updatedAdapter);

        });

        availableExercises.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedType = availableExercises.getSelectedItem().toString();
                if (availableExercises.getCount() == 1) {
                    setToastText("No available exercises. Go exercise.");
                    avgForceVWeight.setVisibility(View.INVISIBLE);
                }
                else if (availableExercises.getItemAtPosition(position) == initString){
                    avgForceVWeight.setVisibility(View.INVISIBLE);
                }
                else {
                    avgForceVWeight.setVisibility(View.VISIBLE);
                    ArrayList<Float> weights = new ArrayList<Float>();
                    ArrayList<Float> avgForces = new ArrayList<Float>();
                    Exercise currExercise;

                    // Map: [Weight, (sum of avgForce, # of same weight entries)]
                    Map<Float, ArrayList<Float>> dataMap = new TreeMap<Float, ArrayList<Float>>() {
                    };

                    for (int i=0; i < savedExercises.size(); i++) {
                        currExercise = savedExercises.get(i);
                        ArrayList<Float> avgForceEntry = new ArrayList<Float>();    // 0: sum of avgForce, 1: # of entries
                        avgForceEntry.add(0f);
                        avgForceEntry.add(0f);

                        if (selectedType.equals(currExercise.getType())) {
                            if (!dataMap.containsKey(currExercise.getWeight())) {
                                avgForceEntry.set(0,currExercise.getAvgForce());
                                avgForceEntry.set(1, 1f);
                                dataMap.put(currExercise.getWeight(),avgForceEntry);
                            }
                            else {
                                Float currWeight = currExercise.getWeight();
                                Float currSum = dataMap.get(currWeight).get(0);
                                Float currEntries = dataMap.get(currWeight).get(1);

                                avgForceEntry.set(0,currExercise.getAvgForce() + currSum);
                                avgForceEntry.set(1, currEntries + 1);
                                dataMap.put(currWeight, avgForceEntry);
                            }
                        }
                    }
                    for (Map.Entry<Float, ArrayList<Float>> entry : dataMap.entrySet()) {
                        Float key = entry.getKey();
                        ArrayList<Float> value= entry.getValue();
                        weights.add(key);
                        avgForces.add(value.get(0) / value.get(1));
                        Log.i("AllTimeStats", String.format("Weight: %f, Avg Force: %f", key, value.get(0) / value.get(1)));
                    };

                    //Plot avg force vs weight for all exercises of selected type
                    displaySingleLineChart(avgForceVWeight, weights, avgForces,
                            "Average Force (N)", "Average Force vs Weight");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {Log.i("deviceListSpinner", "Nothing selected.");
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