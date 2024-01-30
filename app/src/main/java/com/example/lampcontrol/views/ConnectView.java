package com.example.lampcontrol.views;

import android.bluetooth.BluetoothDevice;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.AddToEndSingleTagStrategy;
import moxy.viewstate.strategy.AddToEndStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndStrategy.class)
public interface ConnectView extends MvpView {

    void setScannedDevicesListAdapter();
    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = "scanningAnimation")
    void startScanningAnimation();
    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = "scanningAnimation")
    void stopScanningAnimation();
    void updateScanningDeviceList(BluetoothDevice device);
    void removeAddedDeviceFromScanningList(BluetoothDevice device);
    void checkIfLocationEnabled();
    @StateStrategyType(OneExecutionStateStrategy.class)
    void makeMessage(String message);
}
