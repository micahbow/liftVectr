package com.example.liftvectr;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.liftvectr.data.Exercise;
import com.example.liftvectr.database.ExerciseViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ExerciseHistory extends AppCompatActivity {
    private ExerciseViewModel exerciseViewModel;
    private ListView exerciseList;
    private List<Exercise> savedExercises;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_history);

        exerciseList = (ListView) findViewById(R.id.exerciseList);

        //Nav Bar
        // Initialize and Assign Variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        // Set Create Exercise Selected
        bottomNavigationView.setSelectedItemId(R.id.view);
        // Perform ItemSelectedListener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.create:
                        startActivity(new Intent(getApplicationContext()
                                , MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.view:
                        return true;
                }
                return false;
            }
        });

        //Populating Exercises
        exerciseViewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);

        exerciseViewModel.getAllExercises().observe(this, exercises -> {
            savedExercises = exerciseViewModel.getAllExercises().getValue();
            ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1
                    , generateExerciseHistoryListHeaders(savedExercises));
            exerciseList.setAdapter(adapter2);
        });

        exerciseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String config = "default";
                Exercise selectedExercise = savedExercises.get(position);
                transitionToChartDisplayActivity(config, selectedExercise);
            }
        });
    }

    public List<String> generateExerciseHistoryListHeaders(List<Exercise> exercises) {
        List<String> exerciseListHeaders = new ArrayList<String>();
        for (int i =0; i < exercises.size(); i++) {
            exerciseListHeaders.add( (String) exercises.get(i).getType()
                    + " " + (String) exercises.get(i).getDate().toString());
        }
        return exerciseListHeaders;
    }

    public void transitionToChartDisplayActivity(String config, Exercise selectedExercise)
    {
        Intent intent = new Intent(this, ChartDisplay.class);
        intent.putExtra("exercise", selectedExercise);
        intent.putExtra("config", config);
        startActivity(intent);
    }
}