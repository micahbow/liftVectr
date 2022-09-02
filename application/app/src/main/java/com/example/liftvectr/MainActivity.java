package com.example.liftvectr;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ederdoski.simpleble.interfaces.BleCallback;
import com.ederdoski.simpleble.models.BluetoothLE;
import com.ederdoski.simpleble.utils.BluetoothLEHelper;
import com.example.liftvectr.data.Exercise;
import com.example.liftvectr.data.IMUData;
import com.example.liftvectr.database.ExerciseViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private Button exerciseBtn;
    private Button viewChartBtn;
    private Spinner exerciseSpinner;
    private TextView x_accel, y_accel, z_accel;
    private TextView x_gyro, y_gyro, z_gyro;
    private TextView bluetoothConnected;

    private ExerciseViewModel exerciseViewModel;
    private List<Exercise> displayedExercises;

    private Exercise newExercise;

    private final String SERVICE_UUID = "0000181C-0000-1000-8000-00805f9b34fb";
    private final String CHAR_UUID = "00002ADA-0000-1000-8000-00805f9b34fb";

    BluetoothLEHelper ble;
   // private MacAddress MAC_ADDRESS = new MacAddress("1C:35:7E:C5:F9:3B");
    private boolean exerciseOngoing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        exerciseViewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);

        // To clear the database on app startup, uncomment this!
        //exerciseViewModel.deleteAllExercises();

        exerciseBtn = (Button) findViewById(R.id.button);
        viewChartBtn = (Button) findViewById(R.id.view_chart_button);
        x_accel = (TextView) findViewById(R.id.x_a);
        y_accel = (TextView) findViewById(R.id.y_a);
        z_accel = (TextView) findViewById(R.id.z_a);
        x_gyro = (TextView) findViewById(R.id.x_g);
        y_gyro = (TextView) findViewById(R.id.y_g);
        z_gyro = (TextView) findViewById(R.id.z_g);
        bluetoothConnected = (TextView) findViewById(R.id.bluetooth_status);

        exerciseSpinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.exercises_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        exerciseSpinner.setAdapter(adapter);

        bluetoothConnected.setText("Not Connected");
        // ============ BLUETOOTH =============
        ble = new BluetoothLEHelper(this);
        boolean scanFinished = false;


        //while (!deviceFound) {
            //System.out.println("WAITING FOR DEVICE");
        askForPermissions(this);
            if (ble.isReadyForScan()) {
                    Handler mHandler = new Handler();
                    ble.scanLeDevice(true);

//                  System.out.println("Scan period: " + ble.getScanPeriod());
//
                    mHandler.postDelayed(() -> {
                        //--The scan is over, you should recover the found devices.
                        Log.v("Devices found: ", String.valueOf(ble.getListDevices()));
                        findMatchingDevice();
                    }, ble.getScanPeriod());

                    //findMatchingDevice();

            }
            else {
                System.out.println("PERMISSIONS!!!");
            }



        //}

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

                    // Save a new fake exercise to the db
                    exerciseViewModel.saveExercise(newExercise);

                    if (ble.isConnected()) {
                        System.out.println("READING!!!");
                        for(int i = 0; i < 1000000; i++) {
                            ble.read(SERVICE_UUID, CHAR_UUID);
                        }
                    }
                    else {
                        System.out.println("BLUETOOTH NOT CONNECTED");
                    }

                    // We'd display live data within the UI's table using this function
                    //displayData(new IMUData(1.215f, 3.983f, 0.015f, 3.947f, 5.543f, 0.132f, -1.0f, -3.2f, -9.3f, 1));

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

        exerciseViewModel.getAllExercises().observe(this, exercises -> {
            System.out.println("An exercise has been added or deleted! Refresh the ui with the list of exercises here!");

            LiveData<List<Exercise>> savedExercises = exerciseViewModel.getAllExercises();
            System.out.println("Exercise List (Console Version): ");
            for (int i = 0; i < savedExercises.getValue().size(); i++) {
                System.out.print("Exercise Type:" + savedExercises.getValue().get(i).getType());
                System.out.println(", Exercise Date:" + savedExercises.getValue().get(i).getDate());
            }
        });
    }

    public void findMatchingDevice() {
        System.out.println("+++++++MATCHING CODE BEGIN++++++");
        if (!ble.getListDevices().isEmpty()) {
            for (BluetoothLE item : ble.getListDevices()) {
                if(item != null && item.getName() != null) {
                    System.out.println(item.getName().toString());
                    System.out.println(item.getName().substring(0, 7));
                    if (item.getName().substring(0, 7).equals("IMUData")) {
                        System.out.println("DEVICE FOUND!!");
                        BluetoothDevice device = item.getDevice();
                        ble.connect(device, bleCallbacks());
                        bluetoothConnected.setText("Connected");
                        System.out.println("DEVICE CONNECTED!!");
                    }
                }
            }
        }
        System.out.println("+++++++MATCHING CODE END++++++");
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

    @Override
    protected void onDestroy() {
        ble.disconnect();
        super.onDestroy();
    }

    private BleCallback bleCallbacks(){
        return new BleCallback(){

            @Override
            public void onBleConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onBleConnectionStateChange(gatt, status, newState);

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connected to GATT server.", Toast.LENGTH_SHORT).show());
                }

                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Disconnected from GATT server.", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onBleServiceDiscovered(BluetoothGatt gatt, int status) {
                super.onBleServiceDiscovered(gatt, status);
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.e("Ble ServiceDiscovered","onServicesDiscovered received: " + status);
                }
            }

            @Override
            public void onBleCharacteristicChange(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onBleCharacteristicChange(gatt, characteristic);
                Log.i("BluetoothLEHelper","onCharacteristicChanged Value: " + Arrays.toString(characteristic.getValue()));
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onBleRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onBleRead(gatt, characteristic, status);

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    byte[] raw_data = characteristic.getValue();
                    //Log.i("TAG", Arrays.toString(raw_data));
                    String decoded = new String(raw_data);
                    //System.out.println("Decoded: " + decoded);

                    String[] values = decoded.split(",");
                    boolean valid = true;
                    for (int i = 0; i < values.length; i++) {
                        if(values[i].chars().filter(ch -> ch == '.').count() != 1) {
                            valid = false;
                        }
                    }
                    //System.out.println("Split: " + values.toString());

                    if (values != null && values.length == 10 && valid) {
                        IMUData parsed_data = new IMUData(values);
                        newExercise.addDataSample(parsed_data);
                        displayData(parsed_data);
                    }

                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "onCharacteristicRead : "+Arrays.toString(characteristic.getValue()),             Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onBleWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onBleWrite(gatt, characteristic, status);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "onCharacteristicWrite Status : " + status, Toast.LENGTH_SHORT).show());
            }
        };
    }

    public static void askForPermissions(Activity activity) {
        List<String> permissionsToAsk = new ArrayList<>();
        int requestResult = 0;

        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // Ask for permission
            permissionsToAsk.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // Ask for permission
            permissionsToAsk.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // Ask for permission
            permissionsToAsk.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            // Ask for permission
            permissionsToAsk.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.BLUETOOTH) !=
                PackageManager.PERMISSION_GRANTED) {
            // Ask for permission
            permissionsToAsk.add(Manifest.permission.BLUETOOTH);
        }
        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.BLUETOOTH_ADMIN) !=
                PackageManager.PERMISSION_GRANTED) {
            // Ask for permission
            permissionsToAsk.add(Manifest.permission.BLUETOOTH_ADMIN);
        }
        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.BLUETOOTH_PRIVILEGED) !=
                PackageManager.PERMISSION_GRANTED) {
            // Ask for permission
            permissionsToAsk.add(Manifest.permission.BLUETOOTH_PRIVILEGED);
        }

        if (permissionsToAsk.size() > 0) {
            ActivityCompat.requestPermissions(activity, permissionsToAsk.toArray(new String[permissionsToAsk.size()]), requestResult);
        }

        System.out.println("Permissions function has been called");
    }
}