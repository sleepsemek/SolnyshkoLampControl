package com.example.lampcontrol;

import com.example.lampcontrol.models.PreferencesManager;
import com.example.lampcontrol.repository.BluetoothLeConnectionThread;
import com.example.lampcontrol.repository.LampsDataBaseManager;

public class LampApplication extends android.app.Application {

    private static LampApplication instance;
    private PreferencesManager preferencesManager;
    private LampsDataBaseManager lampsDataBaseManager;

    @Override
    public void onCreate() {
        super.onCreate();
        lampsDataBaseManager = new LampsDataBaseManager(this.getApplicationContext());
        preferencesManager = new PreferencesManager(this.getApplicationContext());
        instance = this;
    }

    public static LampApplication getInstance() {
        return instance;
    }
    public LampsDataBaseManager getDatabaseManager() {
        return this.lampsDataBaseManager;
    }
    public PreferencesManager getPreferencesManager() {
        return this.preferencesManager;
    }
    public BluetoothLeConnectionThread getBluetoothConnectionThread() {
        return new BluetoothLeConnectionThread(getApplicationContext());
    }

}
