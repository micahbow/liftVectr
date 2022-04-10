package com.example.liftvectr;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.BLUETOOTH_CONNECT;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Calendar;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_BLUETOOTH_ID = 1;
    private static final int REQUEST_BLUETOOTH_ADMIN_ID = 2;
    private static final int REQUEST_BLUETOOTH_CONNECT_ID = 3;
    private static final int REQUEST_LOCATION_ID = 4;


    private BluetoothAdapter bluetoothAdapter;
    private static final String TAG = "MainActivity";

    private Button exerciseBtn;
    private Button viewChartBtn;
    private Button ConnectBTDeviceBtn;
    private Button PairNewDeviceBtn;
    private Button TurnBluetoothOn;
    private Spinner exerciseSpinner;
    private TextView x_accel, y_accel, z_accel;
    private TextView x_gyro, y_gyro, z_gyro;

    private Exercise newExercise;

    private boolean exerciseOngoing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectBTDeviceBtn = (Button) findViewById(R.id.connect_device_button);
        PairNewDeviceBtn = (Button) findViewById(R.id.pair_device_button);
        TurnBluetoothOn = (Button) findViewById(R.id.turn_bluetooth_on);
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

        //Bluetooth initialization
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        exerciseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (exerciseBtn.getText().equals("Start Exercise")) {
                    exerciseBtn.setText("Stop Exercise");
                    viewChartBtn.setVisibility(View.INVISIBLE);
                    exerciseOngoing = true;

                    newExercise = new Exercise(exerciseSpinner.getSelectedItem().toString(), Calendar.getInstance().getTime());

                    // Fill exercise with fake bluetooth data
                    newExercise.addDataSample(new IMUData(0.2f, 1.0f, 0.43f, 0.0f, 0.1f, 0.0f, -9.0f, -1.0f, -1.0f, 1));
                    newExercise.addDataSample(new IMUData(0.23f, 0.95f, 0.41f, 0.3f, 0.2f, 0.3f, -8.4f,-1.0f, -0.8f, 2));
                    newExercise.addDataSample(new IMUData(0.28f, 1.10f, 0.39f, 0.5f, 0.1f, 0.5f, -9.3f,-0.9f, -1.0f, 3));
                    newExercise.addDataSample(new IMUData(0.25f, 1.03f, 0.43f, 0.3f, 0.3f, 0.9f, -7.2f,-1.0f, -0.9f, 4));
                    newExercise.addDataSample(new IMUData(0.29f, 0.93f, 0.45f, 0.0f, 0.2f, 1.1f, -6.3f,-0.9f, -1.0f, 5));
                    newExercise.addDataSample(new IMUData(0.24f, 0.98f, 0.49f, 0.3f, 0.1f, 3.3f, -6.8f,-1.0f, -1.3f, 6));
                    newExercise.addDataSample(new IMUData(0.22f, 1.01f, 0.46f, 0.5f, 0.0f, 3.0f, -9.5f,-0.8f, -1.0f, 7));
                    newExercise.addDataSample(new IMUData(0.21f, 1.03f, 0.42f, 0.3f, 0.0f, 2.1f, -10.0f,-1.0f, -1.2f, 8));
                    newExercise.addDataSample(new IMUData(0.24f, 0.94f, 0.40f, 0.0f, 0.1f, 1.2f, -9.1f, -0.9f, -1.0f, 9));
                    newExercise.addDataSample(new IMUData(0.26f, 0.99f, 0.43f, 0.3f, 0.2f, 0.4f, -9.3f,-1.2f, -1.1f, 10));

                    // We'd display live data within the UI's table using this function
                    displayData(new IMUData(1.215f, 3.983f, 0.015f, 3.947f, 5.543f, 0.132f, -1.0f, -3.2f, -9.3f, 1));

                } else {
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

        ConnectBTDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        PairNewDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        TurnBluetoothOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isBluetoothEnabled()) {
                    Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    bluetoothActivityResultLauncher.launch(enableBT);
                } else {
                    Log.d(TAG, "Bluetooth already enabled");
                }
            }
        });

    }

    @Override
    protected void onStart(){
        super.onStart();

        // Requests Bluetooth perms at start
        if (ActivityCompat.checkSelfPermission(MainActivity.this, BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Bluetooth permission has not been granted.
            bluetoothPermissionCheck(REQUEST_BLUETOOTH_CONNECT_ID);
        }

    }
    public void transitionToChartDisplayActivity(String config) {
        Intent intent = new Intent(this, ChartDisplay.class);
        intent.putExtra("exercise", newExercise);
        intent.putExtra("config", config);
        startActivity(intent);
    }

    private BluetoothManager getBluetoothManager() {
        return Objects.requireNonNull((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE),"cannot get BluetoothManager");
    }

    private boolean isBluetoothEnabled() {
        bluetoothAdapter = getBluetoothManager().getAdapter();
        if(bluetoothAdapter == null) return false;

        return bluetoothAdapter.isEnabled();
    }

    public void displayData(IMUData sample) {
        x_accel.setText(Float.toString(sample.x_lin_acc));
        y_accel.setText(Float.toString(sample.y_lin_acc));
        z_accel.setText(Float.toString(sample.z_lin_acc));
        x_gyro.setText(Float.toString(sample.x_ang_vel));
        y_gyro.setText(Float.toString(sample.y_ang_vel));
        z_gyro.setText(Float.toString(sample.z_ang_vel));
    }

    private ActivityResultLauncher<Intent> bluetoothActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "Action completed.");

                    } else {
                        Log.d(TAG, "Cancelled...");
                    }
                }
            }
    );

    private void bluetoothPermissionCheck(int requestedID) {

        switch (requestedID) {
            case REQUEST_BLUETOOTH_ID:
                ActivityCompat.requestPermissions(this, new String[]{BLUETOOTH}, REQUEST_BLUETOOTH_ID);
                break;
            case REQUEST_BLUETOOTH_ADMIN_ID:
                ActivityCompat.requestPermissions(this, new String[]{BLUETOOTH_ADMIN}, REQUEST_BLUETOOTH_ADMIN_ID);
                break;
            case REQUEST_BLUETOOTH_CONNECT_ID:
                ActivityCompat.requestPermissions(this, new String[]{BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT_ID);
                break;
            case REQUEST_LOCATION_ID:
                ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, REQUEST_LOCATION_ID);
                break;
            default:
                Log.d("Invalid Permission", "Invalid Permission ID Provided");
                break;
        }

    }

}

