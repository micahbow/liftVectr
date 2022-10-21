package com.example.liftvectr.util;

import android.app.Activity;
import android.Manifest;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.ederdoski.simpleble.interfaces.BleCallback;
import com.ederdoski.simpleble.models.BluetoothLE;
import com.ederdoski.simpleble.utils.BluetoothLEHelper;
import com.example.liftvectr.activities.AddExerciseActivity;
import com.example.liftvectr.data.IMUData;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Pattern;

public class BluetoothController {

    private final String SERVICE_UUID = "0000181C-0000-1000-8000-00805f9b34fb";
    private final String CHAR_UUID = "00002ADA-0000-1000-8000-00805f9b34fb";
    // private MacAddress MAC_ADDRESS = new MacAddress("1C:35:7E:C5:F9:3B"); (currently not in use)
    private boolean openRead = true;
    private BluetoothLEHelper ble;
    private boolean paired = false;
    private int count;
    private String delayCountdown;
    private String[] rawDataBuffer;
    private boolean notifSet = false;
    private Activity parentActivity;
    ArrayList<BluetoothLE> listDevices;

    public BluetoothController(Activity activity) {
        // Initialize BLE helper
        this.ble = new BluetoothLEHelper(activity);
        this.ble.setScanPeriod(1000);
        this.rawDataBuffer = new String[7];
        this.count = 0;
        this.delayCountdown = "5";
        parentActivity = activity;
    }

    public void setNotificationsOn() {
        this.writeBLE();
    }

    public void setPairedStatus(boolean value) {
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        if(value) {
            for(int i = 0; i < 3; i++) {
                toneG.startTone(ToneGenerator.TONE_SUP_CONFIRM, 250);
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            for(int i = 0; i < 3; i++) {
                toneG.startTone(ToneGenerator.TONE_SUP_ERROR, 250);
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        this.paired = value;
        ((AddExerciseActivity)(this.parentActivity)).setBluetoothConnected(value);
    }

    public boolean getPairedStatus() {
        return this.paired;
    }

    public void disconnect() {
        this.setPairedStatus(false);
        this.ble.disconnect();
    }

    public boolean getOpenRead() {
        return this.openRead;
    }

    public void writeBLE() {
        // Confirmation handshake to sync with hardware
        if (ble.isConnected()) {
            ble.write(SERVICE_UUID,CHAR_UUID,this.delayCountdown);
            Log.i("writeBLE","");
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
            ble.read(SERVICE_UUID,CHAR_UUID);
            //ble.write(SERVICE_UUID,CHAR_UUID,BluetoothController.READ_STRING + ++counts);
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

    public void setDelayCountdown(String delay){
        this.delayCountdown = delay;
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

                byte[] raw_data = characteristic.getValue();
                Log.i("onBleCharacteristicChange RAW DATA:", Arrays.toString(raw_data));
                String decoded = new String(raw_data);
                Log.i("onBleCharacteristicChange DEC DATA:", decoded);

                // This decoded string should consist of 7 float values.
                String[] values = decoded.split(",");
                // Checking for transmission issues in which a value may have multiple or no decimals
                boolean valid = true;
                Pattern validFloat = Pattern.compile("-?[0-9]+.[0-9]+");
                for (int i = 0; i < values.length; i++) {
                    if(!validFloat.matcher(values[i]).matches()) {
                        valid = false;
                    }
                }
                switch(count) {
                    case(0):
                        //Chunk 1 of data: a_x,a_y

                        // Checking that transmission does have all expected values
                        // If so, add data to exercise object and display
                        if (values != null && values.length == 2 && valid) {
                            rawDataBuffer[0] = values[0];
                            rawDataBuffer[1] = values[1];
                        }
                        else {
                            count = 0;
                            Log.e("onBleCharacteristicChange", "Invalid C0 transmission! Buffer dumped");
                        }
                        count = 1;
                        break;
                    case(1):
                        //Chunk 2 of data: a_y,g_x

                        // Checking that transmission does have all expected values
                        // If so, add data to exercise object and display
                        if (values != null && values.length == 2 && valid) {
                            rawDataBuffer[2] = values[0];
                            rawDataBuffer[3] = values[1];
                        }
                        else {
                            count = 0;
                            Log.e("onBleCharacteristicChange", "Invalid C1 transmission! Buffer dumped");
                        }
                        count = 2;
                        break;
                    case(2):
                        //Chunk 3 of data: g_y,g_z

                        // Checking that transmission does have all expected values
                        // If so, add data to exercise object and display
                        if (values != null && values.length == 2 && valid) {
                            rawDataBuffer[4] = values[0];
                            rawDataBuffer[5] = values[1];
                        }
                        else {
                            count = 0;
                            Log.e("onBleCharacteristicChange", "Invalid C2 transmission! Buffer dumped");
                        }
                        count = 3;
                        break;
                    case(3):
                        //Chunk 4: t

                        // Checking that transmission does have all expected values
                        // If so, add data to exercise object and display
                        if (values != null && values.length == 1 && valid) {
                            rawDataBuffer[6] = values[0];
                            IMUData parsed_data = new IMUData(rawDataBuffer);
                            ((AddExerciseActivity)(BLEController.parentActivity)).addDataToExercise(parsed_data);
                            ((AddExerciseActivity)(BLEController.parentActivity)).displayData(parsed_data);
                            Log.i("onBleCharacteristicChange","Transmission Success");
                        }
                        else {
                            count = 0;
                            Log.e("onBleCharacteristicChange", "Invalid C3 transmission! Buffer dumped");
                        }
                        count = 0;
                        break;
                }

                openRead = true;
                //Log.i("BluetoothLEHelper", "Characteristic Changed");
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onBleRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                System.out.println("READ CALLBACK!");

                if(!notifSet) {
                    notifSet = true;
                    gatt.setCharacteristicNotification(characteristic,true);

                    UUID charUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(charUUID);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                }
                /*
                super.onBleRead(gatt, characteristic, status);

                    UUID charUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(charUUID);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                }
                /*
                super.onBleRead(gatt, characteristic, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i("onBleRead STATUS", "SUCCESS");
                    byte[] raw_data = characteristic.getValue();
                    Log.i("onBleRead RAW DATA:", Arrays.toString(raw_data));
                    String decoded = new String(raw_data);
                    Log.i("onBleRead DEC DATA:", decoded);

                    // This decoded string should consist of 7 float values.
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
                    if (values != null && values.length == 7 && valid) {
                        IMUData parsed_data = new IMUData(values);
                        ((AddExerciseActivity)(BLEController.parentActivity)).addDataToExercise(parsed_data);
                        ((AddExerciseActivity)(BLEController.parentActivity)).displayData(parsed_data);
                    }
                    else {
                        Log.e("onBleRead", "Invalid transmission!");
                    }
                    openRead = true;

                }
                else {
                    String failVal = new String(characteristic.getValue());
                    disconnect();
                    Log.e("onBleRead STATUS FAIL:",failVal);
                }*/
            }

            @Override
            public void onBleWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onBleWrite(gatt, characteristic, status);
                Log.i("onBleWrite", "Write callback called.");
                int delayInt;
                try {
                    delayInt = Integer.parseInt(delayCountdown) * 1000;
                }
                catch (NumberFormatException e) {
                    delayInt = 5 * 1000;
                }
                if(!notifSet) {
                    notifSet = true;

                    // Countdown to data collection
                    try {
                        Thread.sleep(delayInt);
                        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                        for(int i = 0; i < 3; i++) {
                            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 250);
                            Thread.sleep(250);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    gatt.setCharacteristicNotification(characteristic,true);

                    UUID charUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(charUUID);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                }
                /*
                if(status == BluetoothGatt.GATT_SUCCESS) {
                    try {
                        Thread.sleep(5);
                    } catch(InterruptedException E) {E.printStackTrace();}
                    //openRead = true;
                    ble.read(SERVICE_UUID,CHAR_UUID);
                }
                else {
                    Log.e("onBleWrite", "Write callback failed status bad.");
                } */
            }
        };
    }
}