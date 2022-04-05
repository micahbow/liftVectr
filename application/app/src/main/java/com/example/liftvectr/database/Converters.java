package com.example.liftvectr.database;

import androidx.room.*;

import com.example.liftvectr.IMUData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;
import java.lang.*;


public class Converters {
    @TypeConverter
    public static ArrayList<String> fromString(String value) {
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayList(ArrayList<IMUData> list) {
        Gson gson = new Gson();
        // String json = gson.toJson(list);
        //return json;
        return gson.toJson(list);
    }

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
