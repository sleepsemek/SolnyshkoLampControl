package com.example.lampcontrol.models;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.lampcontrol.models.POJO.LampTimerTime;

public class PreferencesManager {

    private SharedPreferences sharedPreferences;

    public PreferencesManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences("timer", MODE_PRIVATE);
    }

    public void setLastTime(int minutes, int seconds, int iterations, String address) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(address + "minutes", minutes);
        editor.putInt(address + "seconds", seconds);
        editor.putInt(address + "iterations", iterations);
        editor.apply();
    }

    public int getIterations(String address) {
        return  sharedPreferences.getInt(address + "iterations", 1);
    }

    public int getMinutes(String address) {
        return sharedPreferences.getInt(address + "minutes", 1);
    }

    public int getSeconds(String address) {
        return sharedPreferences.getInt(address + "seconds", 0);
    }
}
