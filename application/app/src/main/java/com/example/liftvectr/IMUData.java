package com.example.liftvectr;

import java.io.Serializable;

public class IMUData implements Serializable {
    public float x_lin_acc, y_lin_acc, z_lin_acc;
    public float x_ang_vel, y_ang_vel, z_ang_vel;
    public int millisec;

    public IMUData(float x_a, float y_a, float z_a, float x_g, float y_g, float z_g, int ms) {
        this.x_lin_acc = x_a;
        this.y_lin_acc = y_a;
        this.z_lin_acc = z_a;
        this.x_ang_vel = x_g;
        this.y_ang_vel = y_g;
        this.z_ang_vel = z_g;
        this.millisec = ms;
    }
}
