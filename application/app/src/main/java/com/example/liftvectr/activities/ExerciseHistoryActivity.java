package com.example.liftvectr.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    private TextView popupDescription;
    private Spinner userSexSpinner;
    private EditText userWeightEditText;
    private Button submitButton;

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

        checkForUserDetails();
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

    public void checkForUserDetails() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        if (sharedPref.getString("userSex", null) == null ||
                sharedPref.getInt("userWeight", -1) == -1) {
            createNewUserDetailsDialog();
        }
    }

    public void createNewUserDetailsDialog() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View userDetailsPopupView = getLayoutInflater().inflate(R.layout.popup, null);

        userWeightEditText = (EditText) userDetailsPopupView.findViewById(R.id.userWeight);
        submitButton = (Button) userDetailsPopupView.findViewById(R.id.submit);
        popupDescription = (TextView) userDetailsPopupView.findViewById(R.id.popupDescription);
        popupDescription.setText("We ask that you provide some starting information to allow the app " +
                "to better analyze your weightlifting.");

        userSexSpinner = (Spinner) userDetailsPopupView.findViewById(R.id.userSex);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sex, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSexSpinner.setAdapter(adapter);

        dialogBuilder.setView(userDetailsPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userSex;
                int userWeight;

                if (userSexSpinner.getSelectedItem().toString() != null &&
                        userWeightEditText.getText() != null &&
                        !userWeightEditText.getText().toString().isEmpty()) {
                    userSex = userSexSpinner.getSelectedItem().toString();
                    userWeight = Integer.parseInt(userWeightEditText.getText().toString());
                } else {
                    return;
                }

                if (!userSex.isEmpty() && userSex != null && userWeight > 0 && userWeight < 1000) {
                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    System.out.println(userSex);
                    System.out.println(userWeight);

                    editor.putString("userSex", userSex);
                    editor.putInt("userWeight", userWeight);
                    editor.apply();
                    dialog.cancel();
                }
            }
        });
    }
}