package com.solnyshco.lampcontrol.repository;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.solnyshco.lampcontrol.models.POJO.ReceivedLampState;
import com.solnyshco.lampcontrol.models.POJO.SentCommand;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;


public class BluetoothLeConnectionThread extends Thread {
    private onDataReceivedListener dataReceivedListener;

    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic commandCharacteristic;
    private BluetoothGattCharacteristic notifyCharacteristic;
    private BluetoothGattCharacteristic versionCharacteristic;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable connectionTimeoutRunnable;
    private boolean isConnecting = false;
    private boolean disconnectInit = false;

    private final Gson gson = new Gson();
    private final Context context;
    private String address;

    private static final UUID SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    private static final UUID COMMAND_CHARACTERISTICS_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    private static final UUID NOTIFY_CHARACTERISTICS_UUID = UUID.fromString("1fd32b0a-aa51-4e49-92b2-9a8be97473c9");
    private static final UUID VERSION_CHARACTERISTICS_UUID = UUID.fromString("b3103938-3c4c-4330-8f56-e58c77f4b0bd");
    private static final UUID NOTIFY_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private final Queue<BluetoothOperation> operationQueue = new ConcurrentLinkedQueue<>();
    private boolean operationInProgress = false;

    private void handleGattError(String source, int status) {
        System.err.println("Gatt error in " + source + ", status: " + status);
        if (status != BluetoothGatt.GATT_SUCCESS) {
            resetConnectionAndReconnect();
        }
    }

    private void resetConnectionAndReconnect() {
        System.out.println("Resetting connection due to GATT error...");
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
        isConnecting = false;
        connectToDevice();
    }

    public BluetoothLeConnectionThread(Context context) {
        this.dataReceivedListener = null;
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void startConnection(String address) {
        this.address = address;
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            System.err.println("Bluetooth adapter null or disabled");
            return;
        }
        this.start();
    }

    @Override
    public void run() {
        connectToDevice();
    }

    private void connectToDevice() {
        if (isConnecting) return;
        isConnecting = true;

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE);

        connectionTimeoutRunnable = () -> {
            if (isConnecting) {
                System.out.println("Connection timeout, retrying...");
                if (bluetoothGatt != null) {
                    bluetoothGatt.disconnect();
                    bluetoothGatt.close();
                    bluetoothGatt = null;
                }
                isConnecting = false;

                if (dataReceivedListener != null) {
                    mainHandler.post(() -> dataReceivedListener.onStateChange(false));
                }


                connectToDevice();
            }
        };

        mainHandler.postDelayed(connectionTimeoutRunnable, 6000);
    }

    private void processNextOperation() {
        if (operationInProgress) {
            return;
        }
        BluetoothOperation op = operationQueue.poll();
        if (op == null) {
            return;
        }
        operationInProgress = true;

        boolean result = false;
        switch (op.type) {
            case WRITE_CHARACTERISTIC:
                System.out.println("Writing characteristic: " + op.characteristic.getUuid());
                result = bluetoothGatt.writeCharacteristic(op.characteristic);
                break;
            case READ_CHARACTERISTIC:
                System.out.println("Reading characteristic: " + op.characteristic.getUuid());
                result = bluetoothGatt.readCharacteristic(op.characteristic);
                break;
            case WRITE_DESCRIPTOR:
                System.out.println("Writing descriptor: " + op.descriptor.getUuid());
                result = bluetoothGatt.writeDescriptor(op.descriptor);
                break;
        }

        if (!result) {
            System.err.println("Operation " + op.type + " для UUID " +
                    (op.characteristic != null ? op.characteristic.getUuid() : op.descriptor.getUuid()) +
                    " не была принята (writeDescriptor returned false / writeCharacteristic returned false)");
            operationInProgress = false;

            mainHandler.postDelayed(this::processNextOperation, 100);
        }
    }

    private void enqueueOperation(BluetoothOperation op) {
        operationQueue.add(op);
        processNextOperation();
    }

    public boolean sendCommand(@NonNull SentCommand command) {
        if (commandCharacteristic == null) {
            System.err.println("sendCommand failed: commandCharacteristic is null");
            return false;
        }

        String jsonCommand = gson.toJson(command);
        commandCharacteristic.setValue(jsonCommand);
        System.out.println("Sending command JSON: " + jsonCommand);

        enqueueOperation(new BluetoothOperation(OperationType.WRITE_CHARACTERISTIC, commandCharacteristic, null));
        return true;
    }

    public void getVersion() {
        if (versionCharacteristic == null) {
            System.err.println("getVersion failed: versionCharacteristic is null");
            return;
        }
        enqueueOperation(new BluetoothOperation(OperationType.READ_CHARACTERISTIC, versionCharacteristic, null));
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            mainHandler.removeCallbacks(connectionTimeoutRunnable);
            System.out.println("Connection timeout cancelled: state changed");

            if (status == BluetoothGatt.GATT_SUCCESS) {
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        isConnecting = false;
                        System.out.println("Connected, requesting MTU...");
                        bluetoothGatt.requestMtu(247);
                        break;

                    case BluetoothProfile.STATE_DISCONNECTED:
                        System.out.println("Disconnected");
                        if (dataReceivedListener != null) {
                            mainHandler.post(() -> dataReceivedListener.onStateChange(false));
                        }
                        if (disconnectInit) {
                            gatt.close();
                            disconnectInit = false;
                        } else {
                            handleGattError("onConnectionStateChange", status);
                        }
                        break;

                    case BluetoothProfile.STATE_CONNECTING:
                    case BluetoothProfile.STATE_DISCONNECTING:
                        if (dataReceivedListener != null) {
                            mainHandler.post(() -> dataReceivedListener.onStateChange(false));
                        }
                        break;
                }
            } else {
                System.out.println("Connection failed with status " + status + ", retrying...");
                isConnecting = false;
                if (bluetoothGatt != null) {
                    bluetoothGatt.close();
                    bluetoothGatt = null;
                }
                if (dataReceivedListener != null) {
                    mainHandler.post(() -> dataReceivedListener.onStateChange(false));
                }
                connectToDevice();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(NOTIFY_CHARACTERISTICS_UUID)) {
                System.out.println("Characteristic changed: " + characteristic.getUuid());
                enqueueOperation(new BluetoothOperation(OperationType.READ_CHARACTERISTIC, commandCharacteristic, null));
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            System.out.println("On services discovered status: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service == null) {
                    System.err.println("Service not found");
                    return;
                }

                commandCharacteristic = service.getCharacteristic(COMMAND_CHARACTERISTICS_UUID);
                notifyCharacteristic = service.getCharacteristic(NOTIFY_CHARACTERISTICS_UUID);
                versionCharacteristic = service.getCharacteristic(VERSION_CHARACTERISTICS_UUID);

                if (notifyCharacteristic == null) {
                    System.err.println("Notify characteristic not found");
                    return;
                }

                boolean notificationSet = gatt.setCharacteristicNotification(notifyCharacteristic, true);
                System.out.println("setCharacteristicNotification result: " + notificationSet);

                BluetoothGattDescriptor notifyDescriptor = notifyCharacteristic.getDescriptor(NOTIFY_DESCRIPTOR_UUID);
                if (notifyDescriptor == null) {
                    System.err.println("Notify descriptor not found");
                    return;
                }

                notifyDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                mainHandler.postDelayed(() -> {
                    System.out.println("Posting descriptor write to queue after delay");
                    enqueueOperation(new BluetoothOperation(OperationType.WRITE_DESCRIPTOR, null, notifyDescriptor));
                }, 200);

            } else {
                handleGattError("onServicesDiscovered", status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            operationInProgress = false;
            System.out.println("onDescriptorWrite called, UUID: " + descriptor.getUuid() + ", status: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                System.out.println("Descriptor write succeeded");
                if (dataReceivedListener != null) {
                    mainHandler.post(() -> dataReceivedListener.onStateChange(true));
                }
                enqueueOperation(new BluetoothOperation(OperationType.READ_CHARACTERISTIC, commandCharacteristic, null));
            } else {
                handleGattError("onDescriptorWrite", status);
            }
            processNextOperation();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            operationInProgress = false;
            System.out.println("Characteristic write completed: UUID=" + characteristic.getUuid() + ", status=" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                enqueueOperation(new BluetoothOperation(OperationType.READ_CHARACTERISTIC, commandCharacteristic, null));
            } else {
                handleGattError("onCharacteristicWrite", status);
            }
            processNextOperation();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            operationInProgress = false;
            System.out.println("Characteristic read completed: UUID=" + characteristic.getUuid() + ", status=" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (characteristic.getUuid().equals(COMMAND_CHARACTERISTICS_UUID) ||
                        characteristic.getUuid().equals(VERSION_CHARACTERISTICS_UUID)) {

                    final String value = characteristic.getStringValue(0);
                    System.out.println("Raw JSON string: " + value);

                    try {
                        ReceivedLampState lampState = gson.fromJson(value, ReceivedLampState.class);
                        System.out.println("Parsed JSON: " + gson.toJson(lampState));
                        if (dataReceivedListener != null) {
                            mainHandler.post(() -> dataReceivedListener.onCommandReceived(lampState));
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to parse JSON: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                handleGattError("onCharacteristicRead", status);
            }
            processNextOperation();
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                System.out.println("MTU changed successfully to " + mtu);
            } else {
                handleGattError("onMtuChanged", status);
            }
            gatt.discoverServices();
        }
    };

    public void cancel() {
        if (bluetoothGatt != null) {
            disconnectInit = true;
            bluetoothGatt.disconnect();
        }

        if (connectionTimeoutRunnable != null) {
            mainHandler.removeCallbacks(connectionTimeoutRunnable);
            System.out.println("Connection timeout cancelled on close");
        }

        operationQueue.clear();
        operationInProgress = false;

        bluetoothGatt = null;
        address = null;
        isConnecting = false;

        this.interrupt();
    }

    public void setOnDataReceivedListener(onDataReceivedListener listener) {
        this.dataReceivedListener = listener;
    }

    public interface onDataReceivedListener {
        void onStateChange(boolean state);
        void onCommandReceived(ReceivedLampState lampState);
    }

    private enum OperationType {
        WRITE_CHARACTERISTIC,
        READ_CHARACTERISTIC,
        WRITE_DESCRIPTOR
    }

    private static class BluetoothOperation {
        final OperationType type;
        final BluetoothGattCharacteristic characteristic;
        final BluetoothGattDescriptor descriptor;

        BluetoothOperation(OperationType type, BluetoothGattCharacteristic characteristic, BluetoothGattDescriptor descriptor) {
            this.type = type;
            this.characteristic = characteristic;
            this.descriptor = descriptor;
        }
    }
}
