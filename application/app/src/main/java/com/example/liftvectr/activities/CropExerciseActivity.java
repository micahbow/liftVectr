package com.example.liftvectr.activities;

import static com.example.liftvectr.database.Converters.IMUDataArrayListToJson;

import static com.example.liftvectr.util.ChartDisplay.displayIMUDataChart;
import static com.example.liftvectr.util.StatisticsLib.averageForce;
import static com.example.liftvectr.util.StatisticsLib.getForceValues;
import static com.example.liftvectr.util.StatisticsLib.getTimeValues;
import static com.example.liftvectr.util.StatisticsLib.zeroOutliers;
import static com.example.liftvectr.util.StatisticsLib.peakForce;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.liftvectr.R;
import com.example.liftvectr.data.Exercise;
import com.example.liftvectr.data.IMUData;
import com.example.liftvectr.database.ExerciseViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.highlight.Highlight;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

public class CropExerciseActivity extends AppCompatActivity {

    private LineChart IMUAccLineChart;
    private Button cancelButton;
    private Button saveButton;

    private ExerciseViewModel exerciseViewModel;
    private Exercise exercise;

    private Highlight[] highlighted;
    private float highlightedX;

    private ArrayList<IMUData> exerciseData;
    private ArrayList<IMUData> adjustedExerciseData;

    // Global PyObject instance
    PyObject pyObjMadgwick;
    PyObject pyObjWeightStandards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_exercise);

        // Start Py
        if(!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        // Specify the python file we want to use functions from
        Python py = Python.getInstance();
        pyObjMadgwick = py.getModule("madgwick_filtering");
        pyObjWeightStandards = py.getModule("weight_standards");

        cancelButton = (Button) findViewById(R.id.cancel_button);
        saveButton = (Button) findViewById(R.id.save_button);
        IMUAccLineChart = (LineChart) findViewById(R.id.line_chart);

        exerciseViewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);

        // Accessing the exercise/config data sent over from ExerciseHistoryActivity
        Intent intent = getIntent();
        exercise = (Exercise) intent.getSerializableExtra("exercise");

        displayIMUDataChart(exercise, IMUAccLineChart, "a_only", "Recorded IMU Data", false);
        // User needs to be able to adjust the exercise passed in from the intent
        IMUAccLineChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                highlighted = IMUAccLineChart.getHighlighted();
                if(highlighted != null)
                {
                    Log.i("CropExercise","Point Selected!");
                    cropExercise();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transitionToAddExerciseActivity();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calculate stats from the adjustedExercise W/ StatLib
                exercise.setForceVsTimeXValues(getTimeValues(exercise.getData()));
                exercise.setForceVsTimeYValues(getForceValues(exercise));
                exercise.setAvgForce(averageForce(exercise.getData(), exercise.getForceVsTimeYValues()
                ));
                exercise.setPeakForce(peakForce(exercise.getForceVsTimeYValues(), exercise.getType()));


                SharedPreferences sharedPref =
                        getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);

                if (sharedPref.getString("userSex", null) == null ||
                        sharedPref.getInt("userWeight", -1) == -1) {
                    System.out.println("Do not have user's sex/weight data, can't calculate weight " +
                            "standard stats");
                } else {
                    String userSex = sharedPref.getString("userSex", null);
                    int userWeight = sharedPref.getInt("userWeight", -1);

                    PyObject weightStandardsJson = pyObjWeightStandards.callAttr(
                            "calc_all_weight_standards",
                            exercise.getType(),
                            userSex.toLowerCase(),
                            userWeight, // body weight
                            exercise.getWeight()); // lift weight

                    if (weightStandardsJson != null) {
                        System.out.println(weightStandardsJson.toString());
                        try {
                            JSONObject dObj = new JSONObject(weightStandardsJson.toString());
                            exercise.setWilksScore(Float.parseFloat(dObj.getString("wilksScore")));
                            exercise.setWilks2Score(Float.parseFloat(dObj.getString("wilks2Score")));
                            exercise.setDotsScore(Float.parseFloat(dObj.getString("dotsScore")));
                            exercise.setBwRatio(Float.parseFloat(dObj.getString("bwRatio")));
                            exercise.setSkillLevel(dObj.getString("skillLevel"));
                            exercise.setPercentile(dObj.getString("percentile"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // calculate stats from python magdwick script
                PyObject dataObjectJson = pyObjMadgwick.callAttr("process_imu_data", IMUDataArrayListToJson(exercise.getData()));

                // null if we can't get reference frame (user must not move at the start)
                if (!(dataObjectJson == null)) {
                    Type listType = new TypeToken<ArrayList<ArrayList<Float>>>(){}.getType();
                    System.out.println(dataObjectJson.toString());
                    ArrayList<ArrayList<Float>> dObj = new Gson().fromJson(dataObjectJson.toString(),listType);
                    ArrayList<ArrayList<Float>> xyzAng = new ArrayList<ArrayList<Float>>();
                    ArrayList<ArrayList<Float>> vhbVel = new ArrayList<ArrayList<Float>>();
                    ArrayList<ArrayList<Float>> vhbDisp = new ArrayList<ArrayList<Float>>();
                    for(int i = 0; i < 3; i++) {
                        xyzAng.add(new ArrayList<Float>(Arrays.asList(dObj.get(i).toArray(new Float[] {}))));
                    }
                    exercise.setXyzAngles(xyzAng);
                    for(int i = 3; i < 6; i++) {
                        vhbVel.add(new ArrayList<Float>(Arrays.asList(dObj.get(i).toArray(new Float[] {}))));
                    }
                    exercise.setVhbVelocity(zeroOutliers(vhbVel,100));
                    for(int i = 6; i < 9; i++) {
                        vhbDisp.add(new ArrayList<Float>(Arrays.asList(dObj.get(i).toArray(new Float[] {}))));
                    }
                    exercise.setVhbPosition(zeroOutliers(vhbDisp,5));
                    exercise.setPosDeviation(dObj.get(9));
                    exercise.setTimeArray(dObj.get(10));
                    exercise.setAveragePError(dObj.get(11).get(0));
                    exercise.setIntegratedPE(dObj.get(12).get(0));
                    exercise.setAccurateFlag(dObj.get(13).get(0) == 0.0? true:false);
                    float totalBulkVelocity = 0;
                    for(int i = 0; i<vhbVel.get(2).size(); i++){
                        totalBulkVelocity += vhbVel.get(2).get(i);
                    }
                    float averageVelocity = totalBulkVelocity / vhbVel.get(2).size();
                    exercise.setCalories(exercise.getAvgForce() * averageVelocity / exercise.getTimeArray().get(exercise.getTimeArray().size() -1));
                }
                else {
                    System.out.println("False Start Detected!");
                }
                exerciseViewModel.saveExercise(exercise);
                transitionToExerciseHistoryActivity();
            }
        });
    }

    public void transitionToAddExerciseActivity()
    {
        Intent intent = new Intent(this, AddExerciseActivity.class);
        startActivity(intent);
    }

    public void transitionToExerciseHistoryActivity()
    {
        Intent intent = new Intent(this, ExerciseHistoryActivity.class);
        startActivity(intent);
    }

    public void cropExercise()
    {
        // Time value
        highlightedX = highlighted[0].getX();
        exerciseData = exercise.getData();
        adjustedExerciseData = new ArrayList<>();

        for(int i = 0; i < exerciseData.size(); i++)
        {
            if(exerciseData.get(i).micros <= highlightedX) {
                adjustedExerciseData.add(exerciseData.get(i));
            }
            else { break; }
        }
        exercise.setData(adjustedExerciseData);
    }
}