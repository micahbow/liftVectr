package com.example.liftvectr;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final ScanCallback scanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            int bondstate = device.getBondState();
            BluetoothGatt gatt = device.connectGatt(getApplicationContext(), false, bluetoothGattCallback, TRANSPORT_LE);

            final List<BluetoothGattService> services = gatt.getServices();
            Log.i("BLEnote", String.format(Locale.ENGLISH,"discovered %d services for '%s'", services.size(), getName()));

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            // Ignore for now
            Log.e("BLEerr","Batch Result!");
        }

        @Override
        public void onScanFailed(int errorCode) {
            // Ignore for now
            Log.e("BLEerr","Scan Failed!");
        }
    };

    private Button exerciseBtn;
    private Button viewChartBtn;
    private Spinner exerciseSpinner;
    private TextView x_accel, y_accel, z_accel;
    private TextView x_gyro, y_gyro, z_gyro;

    private Exercise newExercise;

    private boolean exerciseOngoing = false;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        //--------------------START BLUETOOTH PORTION--------------------//

        //CHECK PERMS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, ACCESS_COARSE_LOCATION_REQUEST);
                Log.e("BLEerr","No perms!");
                while(true); //infinite loop
            }
        }

        //FILTER SETTINGS
        UUID WATCH_SERVICE_UUID = UUID.fromString("0000181C-0000-1000-8000-00805f9b34fb");
        UUID[] serviceUUIDs = new UUID[]{BLP_SERVICE_UUID};
        List<ScanFilter> filters = null;
        filters = new ArrayList<>();
        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(WATCH_SERVICE_UUID))
                .build();
        filters.add(filter); //1-element

        //SCAN SETTINGS
        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setReportDelay(0L)
                .build();

        BluetoothAdapter BLEadapter = BluetoothAdapter.getDefaultAdapter(); //need to check in future
        BluetoothLeScanner scanner = BLEadapter.getBluetoothLeScanner();

        if (scanner != null) {
            scanner.startScan(filters, scanSettings, scanCallback);
        }  else {Log.e("BLEerr","NULL SCANNER");}

        exerciseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(exerciseBtn.getText().equals("Start Exercise")) {
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
};