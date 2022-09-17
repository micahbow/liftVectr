package com.example.liftvectr.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.liftvectr.R;
import com.example.liftvectr.data.Exercise;
import com.example.liftvectr.database.ExerciseViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class ExerciseHistoryActivity extends AppCompatActivity {
    private ExerciseViewModel exerciseViewModel;
    private ListView exerciseList;
    private List<Exercise> savedExercises;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_history);

        exerciseList = (ListView) findViewById(R.id.exerciseList);

        // Initialize and Assign Variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set Create Exercise Selected
        bottomNavigationView.setSelectedItemId(R.id.exercise_history_page);

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
                        return true;
                    case R.id.all_time_statistics_page:
                        startActivity(new Intent(getApplicationContext(), AllTimeStatisticsActivity.class));
                        overridePendingTransition(0, 0);
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