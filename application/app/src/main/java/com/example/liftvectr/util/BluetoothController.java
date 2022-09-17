package com.example.liftvectr.util;

import android.app.Activity;
import android.os.Build;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.ederdoski.simpleble.interfaces.BleCallback;
import com.ederdoski.simpleble.models.BluetoothLE;
import com.ederdoski.simpleble.utils.BluetoothLEHelper;
import com.example.liftvectr.activities.AddExerciseActivity;
import com.example.liftvectr.data.IMUData;

import java.util.Arrays;
import java.util.ArrayList;

public class BluetoothController {

    private final String SERVICE_UUID = "0000181C-0000-1000-8000-00805f9b34fb";
    private final String CHAR_UUID = "00002ADA-0000-1000-8000-00805f9b34fb";
    // private MacAddress MAC_ADDRESS = new MacAddress("1C:35:7E:C5:F9:3B"); (currently not in use)

    private BluetoothLEHelper ble;
    private boolean paired = false;
    private Activity parentActivity;
    ArrayList<BluetoothLE> listDevices;

    public BluetoothController(Activity activity) {
        // Initialize BLE helper
        this.ble = new BluetoothLEHelper(activity);

        parentActivity = activity;
    }

    public void setPairedStatus(boolean value) {
        this.paired = value;
        ((AddExerciseActivity)(this.parentActivity)).setBluetoothConnected(value);
    }

    public boolean getPairedStatus() {
        return this.paired;
    }

    public void disconnect() {
        this.ble.disconnect();
    }

    public void readBLE(int messages, long delay) {
        if (ble.isConnected()) {
            System.out.println("READING!!!");
            for(int i = 0; i < messages; i++) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Log.e("readBLE", "Read delay error.");
                    e.printStackTrace();
                }
                Log.i("readBLE", "Calling BLE read.");
                ble.read(SERVICE_UUID, CHAR_UUID);
            }
        }
        else {
            Log.e("readBLE", "Bluetooth not connected.");
        }
    }

    public void scanDevices() throws Exception {
        // Wait for ready to scan
        ((AddExerciseActivity)(parentActivity)).setToastText("Scanning for devices.");
        ((AddExerciseActivity)(parentActivity)).setListDevices(null);
        Log.i("scanDevices", "Waiting for ready to scan.");
        int retries = 0;
        while(!ble.isReadyForScan()) {
            Log.i("scanDevices", "Scan not ready. Retrying.");
            if(retries >= 3) { throw new Exception("BLE Not ready to scan after 3 retries! Check permissions."); }
            retries++;
            Thread.sleep(100);
        }
        Log.i("scanDevices","Enabling scan!");
        // Clear old devices list before scanning
        ((AddExerciseActivity)(parentActivity)).setListDevices(null);
        Handler mHandler = new Handler();
        ble.scanLeDevice(true);
        mHandler.postDelayed(() -> {
            //--The scan is over, you should recover the found devices.
            ArrayList<BluetoothLE> listDevices = ble.getListDevices();
            Log.v("Devices found: ", String.valueOf(listDevices));
            ((AddExerciseActivity)(parentActivity)).setToastText("Scan complete.");
            ((AddExerciseActivity)(parentActivity)).setListDevices(listDevices);
        }, ble.getScanPeriod());

    }

    public void childParentToastText(String text) {
        ((AddExerciseActivity)(this.parentActivity)).setToastText(text);
    }

    public void findAndPairMatchingDevice(String name) {
        boolean match = false;
        this.setPairedStatus(false);
        ((AddExerciseActivity)(this.parentActivity)).setToastText("Not Connected");
        Log.i("findAndPairMatchDev","BEGIN FINDING MATCH");
        if (!ble.getListDevices().isEmpty()) {
            for (BluetoothLE item : ble.getListDevices()) {
                if(item != null && item.getName() != null) {
                    Log.i("findAndPairMatchDev","DEVICES LISTED:");
                    Log.i("findAndPairMatchDev",item.getName().toString());
                    if (item.getName().length() >= name.length() && item.getName().substring(0, name.length()).equals(name)) {
                        Log.i("findAndPairMatchDev","IMUData DEVICE MATCH FOUND");
                        BluetoothDevice device = item.getDevice();
                        ble.connect(device, bleCallbacks(this));
                        Log.i("findAndPairMatchDevice","DEVICE CONNECTED");
                        match = true;
                    }
                }
            }
            if(!match) {
                Log.e("BLECONTROLLER","NO DEVICE MATCH FOUND");
            }
        }
        else {
            Log.e("BLECONTROLLER","NO DEVICES FOUND");
        }
    }

    private BleCallback bleCallbacks(BluetoothController BLEController){
        return new BleCallback(){

            @Override
            public void onBleConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onBleConnectionStateChange(gatt, status, newState);

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    BLEController.setPairedStatus(true);

                    // Notify main activity of change
                    ((AddExerciseActivity)(BLEController.parentActivity)).setToastText("Connected to GATT server.");
                }

                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    BLEController.setPairedStatus(false);

                    // Notify main activity of change
                    ((AddExerciseActivity)(BLEController.parentActivity)).setToastText("Disconnected from GATT server.");
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
                    Log.i("onBleRead STATUS", "SUCCESS");
                    byte[] raw_data = characteristic.getValue();
                    Log.i("onBleRead RAW DATA:", Arrays.toString(raw_data));
                    String decoded = new String(raw_data);
                    Log.i("onBleRead DEC DATA:", decoded);

                    // This decoded string should consist of 10 float values.
                    String[] values = decoded.split(",");
                    // Checking for transmission issues in which a value may have multiple or no decimals
                    boolean valid = true;
                    for (int i = 0; i < values.length; i++) {
                        if(values[i].chars().filter(ch -> ch == '.').count() != 1) {
                            valid = false;
                        }
                    }

                    // Checking that transmission does have all expected values
                    // If so, add data to exercise object and display
                    if (values != null && values.length == 10 && valid) {
                        IMUData parsed_data = new IMUData(values);
                        ((AddExerciseActivity)(BLEController.parentActivity)).addDataToExercise(parsed_data);
                        ((AddExerciseActivity)(BLEController.parentActivity)).displayData(parsed_data);
                    }
                    else {
                        Log.e("onBleRead", "Invalid transmission!");
                    }

                    // Handle oncharacteristicread here
                }
                else {
                    Log.e("onBleRead STATUS", "FAILURE");
                    // Add error handling here
                }
            }

            @Override
            // Not used for app
            public void onBleWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onBleWrite(gatt, characteristic, status);
                Log.i("onBleWrite", "Write callback called (unexpected).");
            }
        };
    }
}