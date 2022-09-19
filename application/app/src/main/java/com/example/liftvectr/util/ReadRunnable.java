package com.example.liftvectr.util;

import android.app.Activity;
import android.widget.Button;

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
        if (controller.getPairedStatus() && button.getText() != "Start Exercise") {
            while(!controller.getOpenRead());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            controller.setNotificationsOn();
        }
        while(button.getText() != "Start Exercise");
        controller.childParentToastText("Read stopped.");
        controller.disconnect();
    }
}