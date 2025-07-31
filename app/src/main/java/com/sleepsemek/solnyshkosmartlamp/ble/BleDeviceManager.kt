package com.sleepsemek.solnyshkosmartlamp.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.sleepsemek.solnyshkosmartlamp.data.model.LampCommand
import com.sleepsemek.solnyshkosmartlamp.data.model.LampState
import com.sleepsemek.solnyshkosmartlamp.di.ApplicationScope
import com.sleepsemek.solnyshkosmartlamp.ui.lamp_control.DeviceUiState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("MissingPermission")
@Singleton
class BleDeviceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    @ApplicationScope private val scope: CoroutineScope
) {
    private val connectionMutex = Mutex()
    private var connectionCount = 0
    private var currentAddress: String? = null
    private var isInitialized = false

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter().takeIf { it?.isEnabled == true }
    }

    private var bluetoothGatt: BluetoothGatt? = null
    private var commandCharacteristic: BluetoothGattCharacteristic? = null
    private var notifyCharacteristic: BluetoothGattCharacteristic? = null
    private var versionCharacteristic: BluetoothGattCharacteristic? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private var connectionTimeoutRunnable: Runnable? = null
    private var isConnecting = false

    private val operationQueue = ConcurrentLinkedQueue<BluetoothOperation>()
    private var operationInProgress = false

    private val _deviceUiStateFlow = MutableStateFlow<DeviceUiState>(DeviceUiState.Loading)
    val deviceUiStateFlow: StateFlow<DeviceUiState> = _deviceUiStateFlow

    private val _firmwareVersionFlow = MutableStateFlow<String?>(null)
    val firmwareVersionFlow: StateFlow<String?> = _firmwareVersionFlow

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            connectionTimeoutRunnable?.let(mainHandler::removeCallbacks)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    isConnecting = false
                    gatt.requestMtu(247)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    handleDisconnection(status)
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.discoverServices()
            } else {
                Timber.e("MTU change failed: $status")
                resetConnection()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Timber.e("Service discovery failed: $status")
                resetConnection()
                return
            }

            val service = gatt.getService(SERVICE_UUID) ?: run {
                Timber.e("Service not found")
                resetConnection()
                return
            }

            commandCharacteristic = service.getCharacteristic(COMMAND_CHARACTERISTIC_UUID)
            notifyCharacteristic = service.getCharacteristic(NOTIFY_CHARACTERISTIC_UUID)
            versionCharacteristic = service.getCharacteristic(FIRMWARE_VERSION_UUID)

            notifyCharacteristic ?: run {
                Timber.e("Notify characteristic not found")
                resetConnection()
                return
            }

            enableNotifications()
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid == NOTIFY_CHARACTERISTIC_UUID) {
                readCharacteristic(commandCharacteristic)
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            operationInProgress = false
            processNextOperation()

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Timber.e("Characteristic read failed: $status")
                return
            }

            when (characteristic.uuid) {
                COMMAND_CHARACTERISTIC_UUID -> handleStateUpdate(characteristic)
                FIRMWARE_VERSION_UUID -> handleVersionUpdate(characteristic)
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            operationInProgress = false
            processNextOperation()

            if (status == BluetoothGatt.GATT_SUCCESS) {
                readCharacteristic(commandCharacteristic)
            } else {
                Timber.e("Characteristic write failed: $status")
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            operationInProgress = false
            processNextOperation()

            if (status == BluetoothGatt.GATT_SUCCESS) {
                isInitialized = true
                readCharacteristic(commandCharacteristic)
                readCharacteristic(versionCharacteristic)
            } else {
                Timber.e("Descriptor write failed: $status")
            }
        }
    }

    suspend fun connect(address: String) = connectionMutex.withLock {
        connectionCount++
        Timber.d("Connection count: $connectionCount")

        when {
            connectionCount == 1 -> startConnection(address)
            currentAddress != address -> reconnect(address)
            else -> handleReconnection()
        }
    }

    suspend fun disconnect() = connectionMutex.withLock {
        connectionCount--
        if (connectionCount <= 0) {
            connectionCount = 0
            internalDisconnect()
        }
    }

    fun sendCommand(command: LampCommand) {
        val json = gson.toJson(command)
        commandCharacteristic?.value = json.toByteArray(Charsets.UTF_8)
        enqueueOperation(OperationType.WRITE_CHARACTERISTIC, commandCharacteristic)
    }

    private fun startConnection(address: String) {
        _deviceUiStateFlow.value = DeviceUiState.Loading
        currentAddress = address
        isConnecting = true

        val device = bluetoothAdapter?.getRemoteDevice(address) ?: run {
            Timber.e("Bluetooth adapter not available")
            return
        }

        bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        setupConnectionTimeout()
    }

    private fun reconnect(address: String) {
        internalDisconnect()
        startConnection(address)
    }

    private fun handleReconnection() {
        val device = bluetoothAdapter?.getRemoteDevice(currentAddress ?: return) ?: return
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val state = bluetoothManager?.getConnectionState(device, BluetoothProfile.GATT)

        when (state) {
            BluetoothProfile.STATE_CONNECTED -> {
                if (isInitialized) {
                    readCharacteristic(commandCharacteristic)
                    readCharacteristic(versionCharacteristic)
                } else {
                    _deviceUiStateFlow.value = DeviceUiState.Loading
                }
            }
            BluetoothProfile.STATE_DISCONNECTED -> reconnect(currentAddress ?: return)
        }
    }

    private fun internalDisconnect() {
        connectionTimeoutRunnable?.let(mainHandler::removeCallbacks)
        operationQueue.clear()

        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null

        commandCharacteristic = null
        notifyCharacteristic = null
        versionCharacteristic = null

        isInitialized = false
        isConnecting = false
    }

    private fun setupConnectionTimeout() {
        connectionTimeoutRunnable = Runnable {
            if (isConnecting) {
                Timber.d("Connection timeout - reconnecting")
                resetConnection()
            }
        }
        mainHandler.postDelayed(connectionTimeoutRunnable!!, 6000)
    }

    private fun resetConnection() {
        internalDisconnect()
        currentAddress?.let { startConnection(it) }
    }

    private fun handleDisconnection(status: Int) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Timber.d("Unexpected disconnection: $status")
            resetConnection()
        } else {
            _deviceUiStateFlow.value = DeviceUiState.Loading
        }
    }

    private fun enableNotifications() {
        bluetoothGatt?.setCharacteristicNotification(notifyCharacteristic, true)

        val descriptor = notifyCharacteristic?.getDescriptor(NOTIFY_DESCRIPTOR_UUID)?.apply {
            value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        }
        descriptor?.let { enqueueOperation(OperationType.WRITE_DESCRIPTOR, descriptor = it) }
    }

    private fun readCharacteristic(characteristic: BluetoothGattCharacteristic?) {
        characteristic?.let { enqueueOperation(OperationType.READ_CHARACTERISTIC, it) }
    }

    private fun enqueueOperation(
        type: OperationType,
        characteristic: BluetoothGattCharacteristic? = null,
        descriptor: BluetoothGattDescriptor? = null
    ) {
        operationQueue.add(BluetoothOperation(type, characteristic, descriptor))
        processNextOperation()
    }

    private fun processNextOperation() {
        if (operationInProgress || operationQueue.isEmpty()) return

        val operation = operationQueue.poll() ?: return
        operationInProgress = true

        when (operation.type) {
            OperationType.WRITE_CHARACTERISTIC -> {
                operation.characteristic?.let {
                    bluetoothGatt?.writeCharacteristic(it)
                }
            }
            OperationType.READ_CHARACTERISTIC -> {
                operation.characteristic?.let {
                    bluetoothGatt?.readCharacteristic(it)
                }
            }
            OperationType.WRITE_DESCRIPTOR -> {
                operation.descriptor?.let {
                    bluetoothGatt?.writeDescriptor(it)
                }
            }
        }
    }

    private fun handleStateUpdate(characteristic: BluetoothGattCharacteristic) {
        val json = characteristic.getStringValue(0)
        try {
            val state = gson.fromJson(json, LampState::class.java)
            _deviceUiStateFlow.value = DeviceUiState.Connected(state)
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse lamp state")
        }
    }

    private fun handleVersionUpdate(characteristic: BluetoothGattCharacteristic) {
        val json = characteristic.getStringValue(0)
        try {
            val version = gson.fromJson(json, FirmwareVersion::class.java)
            _firmwareVersionFlow.value = "MAC адрес: $currentAddress\nВерсия прошивки: ${version.version}"
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse firmware version")
        }
    }

    companion object {
        private val SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
        private val COMMAND_CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
        private val NOTIFY_CHARACTERISTIC_UUID = UUID.fromString("1fd32b0a-aa51-4e49-92b2-9a8be97473c9")
        private val FIRMWARE_VERSION_UUID = UUID.fromString("b3103938-3c4c-4330-8f56-e58c77f4b0bd")
        private val NOTIFY_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private data class FirmwareVersion(val version: String)

    private enum class OperationType {
        WRITE_CHARACTERISTIC,
        READ_CHARACTERISTIC,
        WRITE_DESCRIPTOR
    }

    private data class BluetoothOperation(
        val type: OperationType,
        val characteristic: BluetoothGattCharacteristic? = null,
        val descriptor: BluetoothGattDescriptor? = null
    )
}
