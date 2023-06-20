package com.example.lampcontrol;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.lampcontrol.POJO.TimerTime;

public class ValuesSavingManager {

    private SharedPreferences sharedPreferences;
    private String address;

    public ValuesSavingManager(Context context, String address) {
        this.sharedPreferences = context.getSharedPreferences("timer", MODE_PRIVATE);
        this.address = address;
    }

    public void setLastTime(long minutes, long seconds) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(address + "minutes", minutes);
        editor.putLong(address + "seconds", seconds);
        editor.apply();
    }

    public TimerTime getLastTime() {
        return new TimerTime(sharedPreferences.getLong(address + "minutes", 1), sharedPreferences.getLong(address + "seconds", 0));
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
