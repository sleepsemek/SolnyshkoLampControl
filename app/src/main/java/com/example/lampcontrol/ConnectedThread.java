package com.example.lampcontrol;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ConnectedThread extends Thread {
    private static final int RECEIVE_MESSAGE = 1;
    private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final String address;

    private BluetoothSocket bluetoothSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private Handler handler;
    private onConnectionStateChangeListener stateListener;
    private onCommandReceivedListener commandListener;
    private final Context context;
    private final BluetoothAdapter bluetoothAdapter;

    public ConnectedThread(Context context, String address) {
        this.stateListener = null;
        this.commandListener = null;
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.handler = new Handler();
        this.address = address;
        receiveState();
    }

    public void run() {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADVERTISE}, 101);
                }
                return;
            }
            this.bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (!bluetoothSocket.isConnected()) {
            try {
                bluetoothSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            tmpIn = bluetoothSocket.getInputStream();
            tmpOut = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            this.cancel();
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_DENIED) {
            bluetoothAdapter.cancelDiscovery();
        } else {
            this.cancel();
            return;
        }
        byte[] buffer = new byte[64];
        int bytes;

        while (true) {
            if (stateListener != null) {
                stateListener.onStateChange(bluetoothSocket.isConnected());
            }
            try {
                bytes = mmInStream.read(buffer);
                handler.obtainMessage(RECEIVE_MESSAGE, bytes, -1, buffer).sendToTarget();
            } catch (IOException e) {
                this.cancel();
                break;
            }
        }

    }

    public void sendData(String message) {
        byte[] msgBuffer = message.getBytes();
        try {
            mmOutStream.write(msgBuffer);
        } catch (IOException e) {
            e.printStackTrace();
            this.cancel();
        }
    }

    @SuppressLint("HandlerLeak")
    private void receiveState() {
        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 1) {
                    byte[] readBuf = (byte[]) msg.obj;
                    String incoming = new String(readBuf, 0, msg.arg1);
                    char[] commandArray = incoming.replaceAll("[^\\d+$]", "").toCharArray();
                    for (char command:
                            commandArray) {
                        try {
                            if (commandListener != null) {
                                commandListener.onCommandReceived(Character.getNumericValue(command));
                            }
                            System.out.println(command);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
    }

    public void cancel() {
        try {
            bluetoothSocket.close();
            if (stateListener != null) {
                stateListener.onStateChange(bluetoothSocket.isConnected());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        void onCommandReceived(int command);
    }


}