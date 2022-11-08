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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
            ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, R.layout.row
                    , generateExerciseList(savedExercises));
            exerciseList.setAdapter(adapter2);
        });

        exerciseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String config = "default";
                Exercise selectedExercise = savedExercises.get(position);
                transitionToExerciseStatisticsActivity(config, selectedExercise);
            }
        });
    }

    public List<String> generateExerciseList(List<Exercise> exercises) {
        List<String> exerciseListHeaders = new ArrayList<String>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

        for (int i =0; i < exercises.size(); i++) {
            Exercise currExercise = exercises.get(i);
            String currDate = dateFormat.format(currExercise.getDate()).toString();
            exerciseListHeaders.add( String.format("%s: %s - %s",currExercise.getType(),
                    currExercise.getWeight(), currDate) );
        }
        return exerciseListHeaders;
    }

    public void transitionToExerciseStatisticsActivity(String config, Exercise selectedExercise)
    {
        Intent intent = new Intent(this, ExerciseStatisticsActivity.class);
        intent.putExtra("exercise", selectedExercise);
        startActivity(intent);
    }
}