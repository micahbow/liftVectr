package com.example.liftvectr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private Button exerciseBtn;
    private Button viewChartBtn;
    private Button ConnectDeviceBtBtn;
    private Button PairDeviceBtBtn;
    private Spinner exerciseSpinner;
    private TextView x_accel, y_accel, z_accel;
    private TextView x_gyro, y_gyro, z_gyro;

    private Exercise newExercise;

    private boolean exerciseOngoing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectDeviceBtBtn = (Button) findViewById(R.id.connect_device_button);
        PairDeviceBtBtn = (Button) findViewById(R.id.pair_device_button);
        exerciseBtn = (Button) findViewById(R.id.button);
        viewChartBtn = (Button) findViewById(R.id.view_chart_button);
        x_accel = (TextView) findViewById(R.id.x_a);
        y_accel = (TextView) findViewById(R.id.y_a);
        z_accel = (TextView) findViewById(R.id.z_a);
        x_gyro = (TextView) findViewById(R.id.x_g);
        y_gyro = (TextView) findViewById(R.id.y_g);
        z_gyro = (TextView) findViewById(R.id.z_g);

        exerciseSpinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.exercises_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        exerciseSpinner.setAdapter(adapter);

        exerciseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(exerciseBtn.getText().equals("Start Exercise")) {
                    exerciseBtn.setText("Stop Exercise");
                    viewChartBtn.setVisibility(View.INVISIBLE);
                    exerciseOngoing = true;

                    newExercise = new Exercise(exerciseSpinner.getSelectedItem().toString(), Calendar.getInstance().getTime());

                    // Fill exercise with fake bluetooth data
                    newExercise.addDataSample(new IMUData(0.2f, 1.0f, 0.43f, 0.0f, 0.0f, 0.0f, 1));
                    newExercise.addDataSample(new IMUData(0.23f, 0.95f, 0.41f, 0.0f, 0.0f, 0.0f, 2));
                    newExercise.addDataSample(new IMUData(0.28f, 1.10f, 0.39f, 0.0f, 0.0f, 0.0f, 3));
                    newExercise.addDataSample(new IMUData(0.25f, 1.03f, 0.43f, 0.0f, 0.0f, 0.0f, 4));
                    newExercise.addDataSample(new IMUData(0.29f, 0.93f, 0.45f, 0.0f, 0.0f, 0.0f, 5));
                    newExercise.addDataSample(new IMUData(0.24f, 0.98f, 0.49f, 0.0f, 0.0f, 0.0f, 6));
                    newExercise.addDataSample(new IMUData(0.22f, 1.01f, 0.46f, 0.0f, 0.0f, 0.0f, 7));
                    newExercise.addDataSample(new IMUData(0.21f, 1.03f, 0.42f, 0.0f, 0.0f, 0.0f, 8));
                    newExercise.addDataSample(new IMUData(0.24f, 0.94f, 0.40f, 0.0f, 0.0f, 0.0f, 9));
                    newExercise.addDataSample(new IMUData(0.26f, 0.99f, 0.43f, 0.0f, 0.0f, 0.0f, 10));

                    // We'd display live data within the UI's table using this function
                    displayData(new IMUData(1.215f, 3.983f, 0.015f, 3.947f, 5.543f, 0.132f, 1));

                }
                else {
                    exerciseBtn.setText("Start Exercise");
                    viewChartBtn.setVisibility(View.VISIBLE);
                    exerciseOngoing = false;
                }
            }

        });

        viewChartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chartConfig = "default";
                transitionToChartDisplayActivity(chartConfig);
            }
        });

        ConnectDeviceBtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        PairDeviceBtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public void transitionToChartDisplayActivity(String config)
    {
        Intent intent = new Intent(this, ChartDisplay.class);
        intent.putExtra("exercise", newExercise);
        intent.putExtra("config", config);
        startActivity(intent);
    }

    public void displayData(IMUData sample) {
        x_accel.setText(Float.toString(sample.x_lin_acc));
        y_accel.setText(Float.toString(sample.y_lin_acc));
        z_accel.setText(Float.toString(sample.z_lin_acc));
        x_gyro.setText(Float.toString(sample.x_ang_vel));
        y_gyro.setText(Float.toString(sample.y_ang_vel));
        z_gyro.setText(Float.toString(sample.z_ang_vel));
    }
}