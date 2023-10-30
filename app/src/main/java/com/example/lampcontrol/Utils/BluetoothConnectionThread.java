package com.example.lampcontrol.Utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import androidx.annotation.NonNull;

import java.util.UUID;

public class BluetoothConnectionThread extends Thread {

    private onConnectionStateChangeListener stateListener;
    private onCommandReceivedListener commandListener;
    private final Context context;

    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic characteristic;

    private String address;

    public BluetoothConnectionThread(Context context, String address) {
        this.stateListener = null;
        this.commandListener = null;
        this.context = context;
        this.address = address;

        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    public void run() {
        connectToDevice();
    }

    private void connectToDevice() {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        bluetoothGatt = device.connectGatt(context, true, gattCallback, BluetoothDevice.TRANSPORT_LE);
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        bluetoothGatt.discoverServices();
                        if (stateListener != null) {
                            stateListener.onStateChange(true);
                        }
                        break;
                    case BluetoothProfile.STATE_CONNECTING:
                    case BluetoothProfile.STATE_DISCONNECTING:
                        if (stateListener != null) {
                            stateListener.onStateChange(false);
                        }
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        if (stateListener != null) {
                            stateListener.onStateChange(false);
                        }
                        bluetoothGatt.close();
                        break;
                }
            } else {
                if (stateListener != null) {
                    stateListener.onStateChange(false);
                }
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                characteristic = gatt.getService(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b"))
                        .getCharacteristic(UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8"));

                gatt.setCharacteristicNotification(characteristic, true);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] value = characteristic.getValue();
            String receivedData = new String(value);
            System.out.println("Уведомление от устройства: " + receivedData);
            if (commandListener != null) {
                commandListener.onCommandReceived(receivedData);
            }
        }
    };

    public void sendCommand(@NonNull String command) {
        if (characteristic != null) {
            characteristic.setValue(command);
            bluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    public void getLampStatus() {

    }

    public void cancel() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
        this.interrupt();
    }

    public void setOnConnectionStateChangeListener(onConnectionStateChangeListener listener) {
        this.stateListener = listener;
    }

    public void setOnCommandReceivedListener(onCommandReceivedListener listener) {
        this.commandListener = listener;
    }

    public interface onConnectionStateChangeListener {
        void onStateChange(boolean state);
    }

    public interface onCommandReceivedListener {
        void onCommandReceived(String command);
    }


}