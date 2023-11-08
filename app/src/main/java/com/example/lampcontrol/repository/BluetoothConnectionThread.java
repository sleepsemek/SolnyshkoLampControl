package com.example.lampcontrol.repository;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import androidx.annotation.NonNull;

import com.example.lampcontrol.models.POJO.ReceivedLampState;
import com.example.lampcontrol.models.POJO.SentCommand;
import com.google.gson.Gson;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BluetoothConnectionThread extends Thread {

    private onConnectionStateChangeListener stateListener;
    private onCommandReceivedListener commandListener;

    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic commandCharacteristic;
    private BluetoothGattCharacteristic notifyCharacteristic;
    private BluetoothGattDescriptor descriptor;
    private boolean firstConnection = true;

    private final Queue<BluetoothGattCharacteristic> commandQueue = new ConcurrentLinkedQueue<>();
    private HandlerThread commandHandlerThread;
    private Handler commandHandler;

    private final Gson gson = new Gson();
    private final Context context;
    private final String address;

    private static final UUID SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    private static final UUID COMMAND_CHARACTERISTICS_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    private static final UUID NOTIFY_CHARACTERISTICS_UUID = UUID.fromString("1fd32b0a-aa51-4e49-92b2-9a8be97473c9");
    protected static final UUID NOTIFY_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public BluetoothConnectionThread(Context context, String address) {
        this.stateListener = null;
        this.commandListener = null;
        this.context = context;
        this.address = address;

        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private void releaseResources() {
        if (commandHandlerThread != null) {
            commandHandlerThread.quitSafely();

            try {
                commandHandlerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            commandHandlerThread = null;
            commandHandler = null;
        }
    }

    public void run() {
        connectToDevice();
        commandHandlerThread = new HandlerThread("CommandHandlerThread");
        commandHandlerThread.start();
        commandHandler = new Handler(commandHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj instanceof BluetoothGattCharacteristic) {
                    BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) msg.obj;
                    handleCommand(characteristic);
                }
                commandQueue.poll();
            }
        };

    }

    private void handleCommand(BluetoothGattCharacteristic characteristic) {
        if (firstConnection && stateListener != null) {
            firstConnection = false;
            stateListener.onStateChange(true);
        }
        if (characteristic.getUuid() == null) {
            return;
        }

        final String value = new String(characteristic.getStringValue(0));

        if (characteristic.getUuid().equals(COMMAND_CHARACTERISTICS_UUID)) {
            ReceivedLampState lampState = gson.fromJson(value, ReceivedLampState.class);
            System.out.println(gson.toJson(lampState));
            if (commandListener != null) {
                commandListener.onCommandReceived(lampState);
            }
        } else if (characteristic.getUuid().equals(NOTIFY_CHARACTERISTICS_UUID)) {
            bluetoothGatt.readCharacteristic(commandCharacteristic);
            System.out.println(value);
        }

    }

    private void connectToDevice() {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        bluetoothGatt = device.connectGatt(context, true, gattCallback, BluetoothDevice.TRANSPORT_LE);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        bluetoothGatt.discoverServices();
                        if (stateListener != null && !firstConnection) {
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
                commandCharacteristic = gatt.getService(SERVICE_UUID)
                        .getCharacteristic(COMMAND_CHARACTERISTICS_UUID);
                notifyCharacteristic = gatt.getService(SERVICE_UUID)
                                .getCharacteristic(NOTIFY_CHARACTERISTICS_UUID);

                gatt.setCharacteristicNotification(notifyCharacteristic, true);
                descriptor = notifyCharacteristic.getDescriptor(NOTIFY_DESCRIPTOR_UUID);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                bluetoothGatt.writeDescriptor(descriptor);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            commandQueue.add(characteristic);
            commandHandler.sendMessage(commandHandler.obtainMessage(0, characteristic));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                commandQueue.add(characteristic);
                commandHandler.sendMessage(commandHandler.obtainMessage(0, characteristic));
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            bluetoothGatt.readCharacteristic(commandCharacteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            bluetoothGatt.readCharacteristic(commandCharacteristic);
        }
    };

    public boolean sendCommand(@NonNull SentCommand command) {
        if (commandCharacteristic == null) {
            return false;
        }

        String jsonCommand = gson.toJson(command);
        commandCharacteristic.setValue(jsonCommand);
        System.out.println(jsonCommand);
        return bluetoothGatt.writeCharacteristic(commandCharacteristic);
    }

    public void cancel() {
        releaseResources();
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
        void onCommandReceived(ReceivedLampState lampState);
    }


}