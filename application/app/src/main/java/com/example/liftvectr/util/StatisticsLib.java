package com.example.liftvectr.util;

import com.example.liftvectr.data.Exercise;
import com.example.liftvectr.data.IMUData;

import java.util.ArrayList;

public class StatisticsLib {
    // takes the raw IMU data from the exercise and calculates stats


    public static ArrayList<Float> getForceValues(Exercise exercise) {
        ArrayList<IMUData> data = exercise.getData();
        float weight_kg = exercise.getWeight() / 2.205f;
        float mass = weight_kg / 9.81f;
        ArrayList<Float> forceData = new ArrayList<Float>();
        float magAccel;

        for (int i=0; i<data.size(); i++) {
            magAccel = (float) Math.sqrt(Math.pow(data.get(i).x_lin_acc,2) +
                    Math.pow(data.get(i).y_lin_acc,2) +
                    Math.pow(data.get(i).z_lin_acc,2) );
            forceData.add(magAccel * mass);
        }

        return forceData;
    }

    public static ArrayList<Float> getTimeValues(Exercise exercise) {
        ArrayList<IMUData> data = exercise.getData();
        ArrayList<Float> timeValues = new ArrayList<Float>();

        for (int i =0; i<data.size(); i++) {
            timeValues.add(data.get(i).micros);
        }
        return timeValues;
    }

    public static float averageForce(Exercise exercise) {
        // pass in array of float forces
        ArrayList<IMUData> data = exercise.getData();
        ArrayList<Float> forceValues = exercise.getForceVsTimeYValues();
        float averageForce = 0;
        float deltaTime = 0;

        for (int i=0; i<data.size(); i++) {
            averageForce+= forceValues.get(i);
        }
            averageForce /= data.size();
        return averageForce;
    }
}
