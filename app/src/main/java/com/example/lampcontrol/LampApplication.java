package com.example.lampcontrol;

import com.example.lampcontrol.repository.LampsDataBaseManager;

public class LampApplication extends android.app.Application {

    private static LampApplication instance;
    private LampsDataBaseManager lampsDataBaseManager;

    @Override
    public void onCreate() {
        super.onCreate();
        lampsDataBaseManager = new LampsDataBaseManager(this.getApplicationContext());
        instance = this;
    }

    public static LampApplication getInstance() {
        return instance;
    }
    public LampsDataBaseManager getDatabaseManager() {
        return this.lampsDataBaseManager;
    }

}
