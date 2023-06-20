package com.example.lampcontrol;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnectionThread extends Thread {
    private static final int RECEIVE_MESSAGE = 1;
    private final UUID uuid;

    private BluetoothSocket bluetoothSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private Handler handler;
    private onConnectionStateChangeListener stateListener;
    private onCommandReceivedListener commandListener;
    private final Context context;
    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothDevice bluetoothDevice;

    private boolean running;

    public BluetoothConnectionThread(Context context, String address) {
        this.stateListener = null;
        this.commandListener = null;
        this.context = context;

        uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        this.handler = new Handler();
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        this.running = true;
        receiveCommand();
    }

    public void run() {
        while (running) {
            loopConnect();
        }
    }

    private void loopConnect() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            this.bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (!bluetoothSocket.isConnected() && running) {
            try {
                bluetoothSocket.connect();
                if (bluetoothSocket.isConnected()) {
                    createStream();
                    break;
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void createStream() {
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            tmpIn = bluetoothSocket.getInputStream();
            tmpOut = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            this.cancel();
        }

        this.mmInStream = tmpIn;
        this.mmOutStream = tmpOut;

        if (bluetoothSocket.isConnected()) {
            readData();
        } else {
            loopConnect();
        }
    }

    public void sendData(@NonNull String message) {
        byte[] msgBuffer = message.getBytes();
        try {
            mmOutStream.write(msgBuffer);
            System.out.println("Out: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getStatus() {
        sendData("relay:status#");
        sendData("timer:gettime#");
        sendData("timer:status#");

    }

    private void readData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_DENIED) {
                bluetoothAdapter.cancelDiscovery();
            } else {
                this.cancel();
                return;
            }
        } else {
            bluetoothAdapter.cancelDiscovery();
        }

        if (stateListener != null) {
            stateListener.onStateChange(bluetoothSocket.isConnected());
        }

        byte[] buffer = new byte[128];
        int bytes = 0;

        while (bluetoothSocket.isConnected() && running) {
            try {
                buffer[bytes] = (byte) mmInStream.read();
                String readMessage;
                if (buffer[bytes] == '#'){
                    readMessage = new String(buffer,0,bytes);
                    handler.obtainMessage(RECEIVE_MESSAGE, readMessage).sendToTarget();
                    bytes = 0;
                } else {
                    bytes++;
                }
            } catch (IOException e) {
                e.printStackTrace();
                this.cancel();
                loopConnect();
                break;
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private void receiveCommand() {
        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == RECEIVE_MESSAGE) {
                    String incoming = (String) msg.obj;
                    System.out.println(incoming);
                    if (incoming.contains("time:") && commandListener != null) {
                        commandListener.onTimeReceived(Long.parseLong(incoming.replace("time:", "")));
                    } else if (incoming.length() == 1 && commandListener != null){
                        commandListener.onCommandReceived(Integer.parseInt(incoming));
                    }

                }
            }
        };
    }

    private void cancel() {
        try {
            bluetoothSocket.close();
            if (stateListener != null) {
                stateListener.onStateChange(bluetoothSocket.isConnected());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancelRunning() {
        this.running = false;
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
        void onTimeReceived(long millis);
    }


}