package com.example.liftvectr;

public class IMUData {
    public double x_lin_acc, y_lin_acc, z_lin_acc;
    public double x_ang_vel, y_ang_vel, z_ang_vel;

    public IMUData(double x_a, double y_a, double z_a, double x_g, double y_g, double z_g) {
        this.x_lin_acc = x_a;
        this.y_lin_acc = y_a;
        this.z_lin_acc = z_a;
        this.x_ang_vel = x_g;
        this.y_ang_vel = y_g;
        this.z_ang_vel = z_g;
    }
}
