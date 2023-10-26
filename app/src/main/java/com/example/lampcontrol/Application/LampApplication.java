package com.example.lampcontrol.Application;

import com.example.lampcontrol.Utils.LampsDataBase;

public class LampApplication extends android.app.Application {

    private LampsDataBase lampsDataBase;

    @Override
    public void onCreate() {
        super.onCreate();

        lampsDataBase = new LampsDataBase(getApplicationContext());
    }

    public LampsDataBase getLampsDataBase() {
        return lampsDataBase;
    }

}
