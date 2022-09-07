package com.example.liftvectr;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class ExerciseHistory extends AppCompatActivity {
    private Spinner exerciseListSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_history);

        exerciseListSpinner = (Spinner) findViewById(R.id.spinner3);
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
    }
}