package com.example.lampcontrol.views;

import android.bluetooth.BluetoothDevice;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndStrategy.class)
public interface ConnectView extends MvpView {

    void setScannedDevicesListAdapter();
    void updateScanningState(boolean isScanning);
    void updateScanningDeviceList(BluetoothDevice device);
    void openFragment(Fragment fragment);
}
