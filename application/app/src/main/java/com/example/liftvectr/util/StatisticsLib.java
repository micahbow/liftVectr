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
    public static float peakForce(ArrayList<Float> forceValues, String exerciseType) {
        float peakForce = 0f;

        float firstPeak = Collections.max(forceValues);
        int firstPeakIndex = Collections.indexOfSubList(forceValues, Collections.singletonList(firstPeak));
        forceValues.set(firstPeakIndex, 0f);
        float secondPeak = Collections.max(forceValues);
        int secondPeakIndex = Collections.indexOfSubList(forceValues, Collections.singletonList(secondPeak));
        forceValues.set(firstPeakIndex, firstPeak);

        switch (exerciseType) {
            case "Bench Press":
            case "Squat":
                peakForce = (secondPeakIndex > firstPeakIndex) ? secondPeak : firstPeak;
                break;
            case "Deadlift":
                peakForce = (secondPeakIndex > firstPeakIndex) ? firstPeak : secondPeak;
                break;
        }

        Log.i("StatisticsLib", String.format("1st Peak force: %.4f. 2nd Peak force: %.4f, Peak: %.4f", firstPeak, secondPeak, peakForce));
        return peakForce;
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

    public static ArrayList<ArrayList<Float>> isolatePeak(ArrayList<ArrayList<Float>> input) {
        ArrayList<ArrayList<Float>> output = input;

        for(int i = 0; i < output.size(); i++) {
            int lastValid = output.get(i).size()-1;
            for (int j = output.get(i).size() - 1; j > 0; j--) {
                //Find crossover of 0
                float product = output.get(i).get(j) * output.get(i).get(j-1);
                if (product <= 0.0f) {
                    lastValid = j;
                    j = 0;
                }
            }
            for (int k = lastValid+1; k < output.get(i).size(); k++) {
                output.get(i).set(k,0.0f);
            }
        }

        return output;
    }

    public static float pravError(ArrayList<ArrayList<Float>> input) {
        ArrayList<ArrayList<Float>> output = input;
        float positionDeviationSum = 0;
        float bulkPosSum = 0.001f;
        for (int i = 0; i < input.get(0).size(); i++) {
            positionDeviationSum += Math.abs(input.get(2).get(i) - input.get(0).get(i));
            bulkPosSum += Math.abs(input.get(2).get(i));
        }
        return positionDeviationSum / bulkPosSum;
    }

    public static float micError(ArrayList<ArrayList<Float>> input) {
        ArrayList<ArrayList<Float>> output = input;
        float sumAbsoluteError = 0;
        // Input is a VHB, so we only care about get(0) and get(2)
        for(int i = 0; i < input.get(0).size(); i++) {
            sumAbsoluteError += Math.abs((input.get(0).get(i)-input.get(2).get(i))/(input.get(2).get(i)!=0.0f?input.get(2).get(i):1));
        }
        return sumAbsoluteError/input.get(0).size();
    }

}
