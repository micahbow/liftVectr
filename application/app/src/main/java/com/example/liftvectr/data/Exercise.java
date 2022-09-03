package com.example.liftvectr.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.liftvectr.data.IMUData;

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
    private Date date;
    private ArrayList<IMUData> data;

    public Exercise(String type, Date date) {
        this.id = UUID.randomUUID();
        this.type = type;
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
}
