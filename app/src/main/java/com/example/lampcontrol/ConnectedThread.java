package com.example.lampcontrol;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ConnectedThread extends Thread {
    private static final int RECEIVE_MESSAGE = 1;
    private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private boolean paused;

    private BluetoothSocket bluetoothSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;

    private Handler handler;
    private final Context context;

    private onConnectionStateChangeListener stateListener;
    private onCommandReceivedListener commandListener;

    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothDevice device;

    public ConnectedThread(Context context, String address) {
        this.stateListener = null;
        this.commandListener = null;
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.device = bluetoothAdapter.getRemoteDevice(address);

        this.handler = receiveState();

        paused = false;

    }

    public void run() {
        loopConnect();
    }

    public void loopConnect() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_DENIED) {
            bluetoothAdapter.cancelDiscovery();
        } else {
            this.cancel();
            return;
        }

        try {
            this.bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
            this.cancel();
        }

        while (!bluetoothSocket.isConnected() && !paused) {
            try {
                bluetoothSocket.connect();
                if (bluetoothSocket.isConnected()) {
                    if (stateListener != null) {
                        stateListener.onStateChange(bluetoothSocket.isConnected());
                    }
                    setStream();
                    System.out.println("connected");
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                System.out.println("not available");
            }
        }
    }

    private void setStream() {
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = bluetoothSocket.getInputStream();
            tmpOut = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            this.cancel();
            loopConnect();
        }

        this.mmInStream = tmpIn;
        this.mmOutStream = tmpOut;

        this.sendData("relay:status#");

        loopRead();
    }

    private void loopRead() {
        byte[] buffer = new byte[64];
        int bytes;

        while (bluetoothSocket.isConnected() && !paused) {
            try {
                bytes = mmInStream.read(buffer);
                handler.obtainMessage(RECEIVE_MESSAGE, bytes, -1, buffer).sendToTarget();
            } catch (IOException e) {
                this.cancel();
                loopConnect();
                break;
            }
        }
    }

    public void sendData(@NonNull String message) {
        byte[] msgBuffer = message.getBytes();
        try {
            mmOutStream.write(msgBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler receiveState() {
        return handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == RECEIVE_MESSAGE) {
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
            System.out.println("unable to close socket");
        }
    }

    public void setOnConnectionStateChangeListener(onConnectionStateChangeListener listener) {
        this.stateListener = listener;
    }

    public void setOnCommandReceivedListener(onCommandReceivedListener listener) {
        this.commandListener = listener;
    }

    public void onPause() {
        System.out.println("paused");
        this.cancel();
        paused = true;
    }

    public interface onConnectionStateChangeListener {
        void onStateChange(boolean state);
    }

    public interface onCommandReceivedListener {
        void onCommandReceived(int command);
    }

}