package com.example.lampcontrol.repository;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;

import com.example.lampcontrol.models.AdvertisementData;

import java.util.ArrayList;
import java.util.List;

public class BluetoothLeDeviceScanner {

    private boolean isScanning = false;
    private final Handler handler = new Handler();

    private static final int scanningTime = 8000;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothScanner;

    private OnDeviceScannedListener onDeviceScannedListener;

    public BluetoothLeDeviceScanner() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    public void startScanning() {
        if (!isScanning) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isScanning = false;
                    if (onDeviceScannedListener != null) {
                        onDeviceScannedListener.onScanningStateChanged(isScanning);
                    }
                    bluetoothScanner.stopScan(scanCallback);
                }
            }, scanningTime);

            isScanning = true;
            if (onDeviceScannedListener != null) {
                onDeviceScannedListener.onScanningStateChanged(isScanning);
            }
//            bluetoothScanner.startScan(setupFilters(), setupSettings(), scanCallback);
            bluetoothScanner.startScan(scanCallback);
        } else {
            isScanning = false;
            if (onDeviceScannedListener != null) {
                onDeviceScannedListener.onScanningStateChanged(isScanning);
            }
            bluetoothScanner.stopScan(scanCallback);
        }

    }

    public void cancel() {
        isScanning = false;
        if (onDeviceScannedListener != null) {
            onDeviceScannedListener.onScanningStateChanged(isScanning);
        }
        bluetoothScanner.stopScan(scanCallback);
    }

    private List<ScanFilter> setupFilters() {
        List<ScanFilter> filters = new ArrayList<>();
        AdvertisementData advertisementData = new AdvertisementData("NS", "Solnyshko OYFB-04M");
        ScanFilter filter = new ScanFilter.Builder()
                .setManufacturerData(advertisementData.getAdvertisementId(), advertisementData.getByteArrayAdvertisementData())
                .build();
        filters.add(filter);
        return filters;
    }

    private ScanSettings setupSettings() {
        return new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setReportDelay(0L)
                .build();

    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (onDeviceScannedListener != null) {
                onDeviceScannedListener.onDeviceScanned(result.getDevice());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            System.out.println(errorCode);
        }
    };

    public void setOnDeviceScannedListener(OnDeviceScannedListener onDeviceScannedListener) {
        this.onDeviceScannedListener = onDeviceScannedListener;
    }

    public interface OnDeviceScannedListener {
        void onDeviceScanned(BluetoothDevice device);
        void onScanningStateChanged(boolean isScanning);
    }
}
