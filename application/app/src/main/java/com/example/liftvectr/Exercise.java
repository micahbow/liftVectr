package com.example.liftvectr;

import java.util.ArrayList;
import java.util.Date;

public class Exercise {

    private String type;
    private Date date;
    private ArrayList<IMUData> data;

    public Exercise(String _type, Date _date) {
        this.type = _type;
        this.date = _date;
    }

    public void setType(String _type) {
        this.type = _type;
    }

    public String getType() {
        return this.type;
    }

    public void setDate(Date _date) {
        this.date = _date;
    }

    public Date getDate() {
        return this.date;
    }

    public void addDataSample(IMUData sample) {
        data.add(sample);
    }

    public ArrayList<IMUData> getExerciseData() {
        return this.data;
    }

}
