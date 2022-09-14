package com.example.liftvectr.util;

import android.app.Activity;
import android.Manifest;
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
import com.example.liftvectr.MainActivity;
import com.example.liftvectr.data.IMUData;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class BluetoothController {

    private final String SERVICE_UUID = "0000181C-0000-1000-8000-00805f9b34fb";
    private final String CHAR_UUID = "00002ADA-0000-1000-8000-00805f9b34fb";
    private static final String READ_STRING = "READ";
    // private MacAddress MAC_ADDRESS = new MacAddress("1C:35:7E:C5:F9:3B"); (currently not in use)
    private boolean openRead = true;
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
        ((MainActivity)(this.parentActivity)).setBluetoothConnected(value);
    }

    public boolean getPairedStatus() {
        return this.paired;
    }

    public void disconnect() {
        this.ble.disconnect();
    }

    public boolean getOpenRead() {
        return this.openRead;
    }

    public void writeBLE() {
        // Confirmation handshake to sync with hardware
        if (ble.isConnected()) {
            ble.write(SERVICE_UUID,CHAR_UUID,BluetoothController.READ_STRING);
        }
        else {
            Log.e("writeBLE", "Bluetooth not connected.");
        }
    }

    public void readBLE() {
        if (ble.isConnected()) {
            if(!this.openRead) {
                Log.e("readBLE","Out of sync with runnable");
                return;
            }
            openRead = false;
            System.out.println("READING!!!");
            /* try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Log.e("readBLE", "Read delay error.");
                e.printStackTrace();
            } */
            // Log.i("readBLE", "Calling BLE read.");
            ble.read(SERVICE_UUID, CHAR_UUID);
        }
        else {
            Log.e("readBLE", "Bluetooth not connected.");
        }
    }

    public void scanDevices() throws Exception {
        // Wait for ready to scan
        ((MainActivity)(parentActivity)).setToastText("Scanning for devices.");
        ((MainActivity)(parentActivity)).setListDevices(null);
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
        ((MainActivity)(parentActivity)).setListDevices(null);
        Handler mHandler = new Handler();
        ble.scanLeDevice(true);
        mHandler.postDelayed(() -> {
            //--The scan is over, you should recover the found devices.
            ArrayList<BluetoothLE> listDevices = ble.getListDevices();
            Log.v("Devices found: ", String.valueOf(listDevices));
            ((MainActivity)(parentActivity)).setToastText("Scan complete.");
            ((MainActivity)(parentActivity)).setListDevices(listDevices);
        }, ble.getScanPeriod());

    }

    public void childParentToastText(String text) {
        ((MainActivity)(this.parentActivity)).setToastText(text);
    }

    public void findAndPairMatchingDevice(String name) {
        boolean match = false;
        this.setPairedStatus(false);
        ((MainActivity)(this.parentActivity)).setToastText("Not Connected");
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
                    ((MainActivity)(BLEController.parentActivity)).setToastText("Connected to GATT server.");
                }

                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    BLEController.setPairedStatus(false);

                    // Notify main activity of change
                    ((MainActivity)(BLEController.parentActivity)).setToastText("Disconnected from GATT server.");
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
                Log.i("BluetoothLEHelper", "Characteristic Changed");
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onBleRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                System.out.println("READ CALLBACK!");
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
                    Pattern validFloat = Pattern.compile("-?[0-9]+.[0-9]+");

                    for (int i = 0; i < values.length; i++) {
                        if(!validFloat.matcher(values[i]).matches()) {
                            valid = false;
                        }
                    }

                    // Checking that transmission does have all expected values
                    // If so, add data to exercise object and display
                    if (values != null && values.length == 10 && valid) {
                        IMUData parsed_data = new IMUData(values);
                        ((MainActivity)(BLEController.parentActivity)).addDataToExercise(parsed_data);
                        ((MainActivity)(BLEController.parentActivity)).displayData(parsed_data);
                    }
                    else {
                        Log.e("onBleRead", "Invalid transmission!");
                    }
                    writeBLE();
                }
                else {
                    System.out.println("bruh");
//                    String failVal = new String(characteristic.getValue());
//                    if(failVal != BluetoothController.READ_STRING) {
//                        disconnect();
//                        Log.e("onBleRead STATUS", "FAILURE");
//                    }
                }
            }

            @Override
            public void onBleWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onBleWrite(gatt, characteristic, status);
                Log.i("onBleWrite", "Write callback called.");
                if(status == BluetoothGatt.GATT_SUCCESS) {
                    try {
                        Thread.sleep(1);
                    } catch(InterruptedException E) {E.printStackTrace();}
                    openRead = true;
                }
                else {
                    Log.e("onBleWrite", "Write callback failed status bad.");
                }
            }
        };
    }
}