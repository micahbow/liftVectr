package com.example.liftvectr.util;

import android.app.Activity;
import android.widget.Button;

import com.example.liftvectr.MainActivity;

public class ReadRunnable implements Runnable {
    BluetoothController controller;
    Button button;
    Activity parentActivity;
    public ReadRunnable(BluetoothController controller, Button button) {
        this.controller = controller;
        this.button = button;
    }

    @Override
    public void run() {
        while (controller.getPairedStatus() && button.getText() != "Start Exercise") {
            controller.readBLE(1, 10);
        }
        controller.childParentToastText("Read stopped.");
    }
}