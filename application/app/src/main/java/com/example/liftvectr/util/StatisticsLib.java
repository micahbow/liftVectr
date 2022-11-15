package com.example.liftvectr.util;

import android.util.Log;

import com.example.liftvectr.data.Exercise;
import com.example.liftvectr.data.IMUData;

import java.util.ArrayList;
import java.util.Collections;

public class StatisticsLib {
    // takes the raw IMU data from an exercise and calculates stats

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

    public static ArrayList<Float> getTimeValues(ArrayList<IMUData> data) {
        ArrayList<Float> timeValues = new ArrayList<Float>();

        for (int i =0; i<data.size(); i++) {
            timeValues.add(data.get(i).micros);
        }
        return timeValues;
    }

    public static float averageForce(ArrayList<IMUData> data, ArrayList<Float> forceValues) {
        // caller must calc force values w/ getForceValues prior to calling this function
        float averageForce = 0;

        for (int i=0; i<data.size(); i++) {
            averageForce+= forceValues.get(i);
        }
            averageForce /= data.size();
        return averageForce;
    }
    public static float peakForce(ArrayList<Float> forceValues) {
        float max = Collections.max(forceValues);
        Log.i("StatisticsLib", String.format("Peak force: %.4f", max));
        return max;
    }

    public static ArrayList<ArrayList<Float>> zeroOutliers(ArrayList<ArrayList<Float>> input, float absoluteMax) {
        ArrayList<ArrayList<Float>> output = input;
        for(int i = 0; i < output.size(); i++) {
            for(int j = 0; j < output.get(i).size(); j++) {
                output.get(i).set(j,Math.abs(output.get(i).get(j)) > absoluteMax?0:output.get(i).get(j));
            }
        }
        return output;
    }

}
