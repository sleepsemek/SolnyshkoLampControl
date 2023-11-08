package com.example.lampcontrol.presenters;

import android.bluetooth.BluetoothDevice;

import com.example.lampcontrol.Fragments.PageFragmentControl;
import com.example.lampcontrol.LampApplication;
import com.example.lampcontrol.repository.BluetoothLeDeviceScanner;
import com.example.lampcontrol.repository.LampsDataBaseManager;
import com.example.lampcontrol.models.POJO.Lamp;
import com.example.lampcontrol.views.ConnectView;

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
                for (Lamp lamp:
                        lampsDataBaseManager.getList()) {
                    if (lamp.getAddress().contains(device.getAddress())) {
                        return;
                    }
                }
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

    public void handleAddButtonClick(BluetoothDevice device) {
        lampsDataBaseManager.addLamp(new Lamp(device.getName() == null ? "Без названия" : device.getName(), device.getAddress()));
        getViewState().removeAddedDeviceFromScanningList(device);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("destroyed");
        bluetoothLeDeviceScanner.setOnDeviceScannedListener(null);
        bluetoothLeDeviceScanner.cancel();
    }
}
