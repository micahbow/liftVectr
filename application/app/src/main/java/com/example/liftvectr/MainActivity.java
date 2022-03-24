package com.example.liftvectr;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button exerciseBtn;
    private Spinner exerciseSpinner;
    private TextView x_accel, y_accel, z_accel;
    private TextView x_gyro, y_gyro, z_gyro;

    private boolean exerciseOngoing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        exerciseBtn = (Button) findViewById(R.id.button);
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

        // Testing the displayData function
        IMUData fakeData = new IMUData(1.215, 3.983, 0.015, 3.947, 5.543, 0.132);

        exerciseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(exerciseBtn.getText().equals("Start Exercise")) {
                    exerciseBtn.setText("Stop Exercise");
                    exerciseOngoing = true;
                    displayData(fakeData);
                }
                else {
                    exerciseBtn.setText("Start Exercise");
                    exerciseOngoing = false;
                }
            }
        });
    }

    public void displayData(IMUData sample) {
        x_accel.setText(Double.toString(sample.x_lin_acc));
        y_accel.setText(Double.toString(sample.y_lin_acc));
        z_accel.setText(Double.toString(sample.z_lin_acc));
        x_gyro.setText(Double.toString(sample.x_ang_vel));
        y_gyro.setText(Double.toString(sample.y_ang_vel));
        z_gyro.setText(Double.toString(sample.z_ang_vel));
    }
}