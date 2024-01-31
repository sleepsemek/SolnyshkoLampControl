package com.example.lampcontrol.presenters;

import android.bluetooth.BluetoothDevice;

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
                if (isScanning) {
                    getViewState().startScanningAnimation();
                } else {
                    getViewState().stopScanningAnimation();
                }
            }
        });

        getViewState().setScannedDevicesListAdapter();

        getViewState().checkIfLocationEnabled();
        getViewState().checkIfScanIsPermitted();

    }

    public void handlePermissionResult(boolean result) {
        if (!result) {
            getViewState().makeMessage("Предоставтье доступ к устройствам поблизости");
            getViewState().redirectToAppSettings();
            return;
        }
        bluetoothLeDeviceScanner.startScanning();
    }

    public void handleGPSpermissionResult(boolean result) {
        if (!result) {
            getViewState().makeMessage("Включите определение местоположения");
            getViewState().redirectToGPSSettings();
            getViewState().backPress();
        }
    }

    public void handleScanButtonPress() {
        getViewState().checkIfScanIsPermitted();
    }

    public void handleAddButtonClick(BluetoothDevice device) {
        lampsDataBaseManager.addLamp(new Lamp(device.getName() == null ? "Без названия" : device.getName(), device.getAddress()));
        getViewState().removeAddedDeviceFromScanningList(device);
        getViewState().makeMessage("Устройство добавлено");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bluetoothLeDeviceScanner.setOnDeviceScannedListener(null);
        bluetoothLeDeviceScanner.cancel();
    }
}
