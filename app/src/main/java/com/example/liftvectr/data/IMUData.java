package com.example.liftvectr.data;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class IMUData implements Serializable {
    public float x_lin_acc, y_lin_acc, z_lin_acc;
    public float x_ang_vel, y_ang_vel, z_ang_vel;
    public float micros;

    public IMUData(float x_a, float y_a, float z_a,
                   float x_g, float y_g, float z_g,
                   float ms) {
        this.x_lin_acc = x_a;
        this.y_lin_acc = y_a;
        this.z_lin_acc = z_a;
        this.x_ang_vel = x_g;
        this.y_ang_vel = y_g;
        this.z_ang_vel = z_g;
        this.micros = ms;
    }

    public IMUData(String[] raw_data) {
        this.x_lin_acc = Float.parseFloat(raw_data[0]);
        this.y_lin_acc = Float.parseFloat(raw_data[1]);
        this.z_lin_acc = Float.parseFloat(raw_data[2]);
        this.x_ang_vel = Float.parseFloat(raw_data[3]);
        this.y_ang_vel = Float.parseFloat(raw_data[4]);
        this.z_ang_vel = Float.parseFloat(raw_data[5]);
        this.micros = Float.parseFloat(raw_data[6]);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        IMUData data = (IMUData) obj;
        return this.x_lin_acc == data.x_lin_acc &&
                this.y_lin_acc == data.y_lin_acc &&
                this.z_lin_acc == data.z_lin_acc &&
                this.x_ang_vel == data.x_ang_vel &&
                this.y_ang_vel == data.y_ang_vel &&
                this.z_ang_vel == data.z_ang_vel &&
                this.micros == data.micros;
    }
}
