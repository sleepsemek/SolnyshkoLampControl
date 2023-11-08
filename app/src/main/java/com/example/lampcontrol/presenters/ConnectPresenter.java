package com.example.lampcontrol.presenters;

import android.bluetooth.BluetoothDevice;

import com.example.lampcontrol.Fragments.PageFragmentControl;
import com.example.lampcontrol.LampApplication;
import com.example.lampcontrol.models.BluetoothLeDeviceScanner;
import com.example.lampcontrol.models.LampsDataBaseManager;
import com.example.lampcontrol.models.POJO.Lamp;
import com.example.lampcontrol.views.ConnectView;

import moxy.InjectViewState;
import moxy.MvpPresenter;

public class ConnectPresenter extends MvpPresenter<ConnectView> {

    private BluetoothLeDeviceScanner bluetoothLeDeviceScanner;
    private LampsDataBaseManager lampsDataBaseManager;

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();

        LampApplication application = LampApplication.getInstance();
        lampsDataBaseManager = application.getDatabaseManager();

        bluetoothLeDeviceScanner = new BluetoothLeDeviceScanner();
        bluetoothLeDeviceScanner.setOnDeviceScannedListener(new BluetoothLeDeviceScanner.OnDeviceScannedListener() {
            @Override
            public void onDeviceScanned(BluetoothDevice device) {
                getViewState().updateScanningDeviceList(device);
            }

            @Override
            public void onScanningStateChanged(boolean isScanning) {
                getViewState().updateScanningState(isScanning);
            }
        });

        getViewState().setScannedDevicesListAdapter();
        bluetoothLeDeviceScanner.startScanning();
    }

    public void handleScanButton() {
        bluetoothLeDeviceScanner.startScanning();
    }

    public void handleAddButtonClick(String name, String address) {
        lampsDataBaseManager.addLamp(new Lamp(name == null ? "Без названия" : name, address));
        getViewState().openFragment(new PageFragmentControl());
    }
}
