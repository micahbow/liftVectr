package com.example.liftvectr;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
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
import java.util.List;

public class ExerciseHistory extends AppCompatActivity {
    private Spinner exerciseListSpinner;
    private ExerciseViewModel exerciseViewModel;
    private ListView exerciseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_history);
        //exerciseListSpinner = (Spinner) findViewById(R.id.spinner3);
        exerciseList = (ListView) findViewById(R.id.exerciseList);
        //exerciseList.addHeaderView("Exercise History");

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

        /*
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.exercises_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exerciseListSpinner.setAdapter(adapter);

        exerciseListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                String selectedExercise = (String) exerciseListSpinner.getSelectedItem();
                Log.i("ExerciseHistory: ", selectedExercise);

                switch (selectedExercise) {
                    case "Bench Press":
                        break;
                    case "Squat":
                        break;
                    case "Deadlift":
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.i("ExerciseHistory: ", "Nothing is selected.");
            }
        });
         */

        //Populating Exercises
        exerciseViewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);
        exerciseViewModel.getAllExercises().observe(this, exercises -> {
            System.out.println("An exercise has been added or deleted! Refresh the ui with the list of exercises here!");

            LiveData<List<Exercise>> savedExercises = exerciseViewModel.getAllExercises();
            List<String> exerciseData = new ArrayList<String>();
            for (int i =0; i < savedExercises.getValue().size(); i++) {
                exerciseData.add( (String) savedExercises.getValue().get(i).getType()
                        + " " + (String) savedExercises.getValue().get(i).getDate().toString() );
            }

            ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1
            , exerciseData);
            exerciseList.setAdapter(adapter2);

        });
    }
}