package com.example.liftvectr.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

@Entity(tableName = "exercise_table")
public class Exercise implements Serializable {

    @PrimaryKey
    @NonNull
    private UUID id;
    private String type;
    private float weight;
    private Date date;
    private ArrayList<IMUData> data;

    // Calculated data
    private float avgForce;
    private float peakForce;
    private ArrayList<Float> forceVsTimeXValues;
    private ArrayList<Float> forceVsTimeYValues;

    // Python-processed data
    //    bigList = [xAngles, yAngles, zAngles, CHECK
    //    vertVel, horzVel, bulkVel, CHECK
    //    vertPos, horzPos, bulkPos, CHECK
    //    posDeviation, CHECK
    //    df['t'].tolist(), CHECK
    //    [averagePError], [integratedPE], CHECK
    //    [notStill]]
    private ArrayList<Float> timeArray; // In seconds starting from 0

    private ArrayList<Float> posDeviation; // In seconds starting from 0

    public ArrayList<Float> getPosDeviation() {
        return posDeviation;
    }

    public void setPosDeviation(ArrayList<Float> posDeviation) {
        this.posDeviation = posDeviation;
    }

    public float getAveragePError() {
        return averagePError;
    }

    public void setAveragePError(float averagePError) {
        this.averagePError = averagePError;
    }

    public float getIntegratedPE() {
        return integratedPE;
    }

    public void setIntegratedPE(float integratedPE) {
        this.integratedPE = integratedPE;
    }

    private float averagePError, integratedPE;

    public ArrayList<Float> getTimeArray() {
        return timeArray;
    }

    public void setTimeArray(ArrayList<Float> timeArray) {
        this.timeArray = timeArray;
    }

    public ArrayList<ArrayList<Float>> getXyzAngles() {
        return xyzAngles;
    }

    public void setXyzAngles(ArrayList<ArrayList<Float>> xyzAngles) {
        this.xyzAngles = xyzAngles;
    }

    public ArrayList<ArrayList<Float>> getVhbVelocity() {
        return vhbVelocity;
    }

    public void setVhbVelocity(ArrayList<ArrayList<Float>> vhbVelocity) {
        this.vhbVelocity = vhbVelocity;
    }

    public ArrayList<ArrayList<Float>> getVhbPosition() {
        return vhbPosition;
    }

    public void setVhbPosition(ArrayList<ArrayList<Float>> vhbPosition) {
        this.vhbPosition = vhbPosition;
    }

    private ArrayList<ArrayList<Float>> xyzAngles;
    private ArrayList<ArrayList<Float>> vhbVelocity;
    private ArrayList<ArrayList<Float>> vhbPosition;

    public boolean isAccurateFlag() {
        return accurateFlag;
    }

    public void setAccurateFlag(boolean accurateFlag) {
        this.accurateFlag = accurateFlag;
    }

    private boolean accurateFlag;

    public Exercise(String type, float weight, Date date) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.weight = weight;
        this.date = date;
        this.data = new ArrayList<>();
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return this.date;
    }

    public void setData(ArrayList<IMUData> data) {
        this.data = data;
    }

    public ArrayList<IMUData> getData() {
        return this.data;
    }

    public void addDataSample(IMUData sample) {
        this.data.add(sample);
    }

    public float getAvgForce() {
        return avgForce;
    }

    public void setAvgForce(float avgForce) {
        this.avgForce = avgForce;
    }

    public float getPeakForce() {
        return peakForce;
    }

    public void setPeakForce(float peakForce) {
        this.peakForce = peakForce;
    }

    public ArrayList<Float> getForceVsTimeXValues() {
        return forceVsTimeXValues;
    }

    public void setForceVsTimeXValues(ArrayList<Float> forceVsTimeXValues) {
        this.forceVsTimeXValues = forceVsTimeXValues;
    }

    public ArrayList<Float> getForceVsTimeYValues() {
        return forceVsTimeYValues;
    }

    public void setForceVsTimeYValues(ArrayList<Float> forceVsTimeYValues) {
        this.forceVsTimeYValues = forceVsTimeYValues;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
}
