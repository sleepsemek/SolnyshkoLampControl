package com.example.lampcontrol.Utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.lampcontrol.Models.LampTimerTime;

public class ValuesSavingManager {

    private SharedPreferences sharedPreferences;
    private String address;

    public ValuesSavingManager(Context context, String address) {
        this.sharedPreferences = context.getSharedPreferences("timer", MODE_PRIVATE);
        this.address = address;
    }

    public void setLastTime(LampTimerTime time) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(address + "minutes", time.getMinutes());
        editor.putLong(address + "seconds", time.getSeconds());
        editor.apply();
    }

    public LampTimerTime getLastTime() {
        return new LampTimerTime(sharedPreferences.getLong(address + "minutes", 1), sharedPreferences.getLong(address + "seconds", 0));
    }

    public void setIterations(int iterations) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(address + "iterations", iterations);
        editor.apply();
    }

    public int getIterations() {
        return  sharedPreferences.getInt(address + "iterations", 1);
    }

}
