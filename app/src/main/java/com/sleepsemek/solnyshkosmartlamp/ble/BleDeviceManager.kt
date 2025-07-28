package com.sleepsemek.solnyshkosmartlamp.ble

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import com.sleepsemek.solnyshkosmartlamp.data.model.LampCommand
import com.sleepsemek.solnyshkosmartlamp.data.model.LampState
import com.sleepsemek.solnyshkosmartlamp.di.ApplicationScope
import com.sleepsemek.solnyshkosmartlamp.ui.lamp_control.DeviceUiState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectOptions
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@SuppressLint("MissingPermission")
@Singleton
class BleDeviceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    @ApplicationScope private val scope: CoroutineScope
) {

    private val options = BleGattConnectOptions(autoConnect = true, closeOnDisconnect = false)

    private var connectionCount = 0
    private val connectionMutex = Mutex()
    private var connectJob: Job? = null

    private var currentConnectionState: GattConnectionState = GattConnectionState.STATE_DISCONNECTED
    private var isInitialized = false

    private var currentGatt: ClientBleGatt? = null
    private var currentAddress: String? = null

    private var lampChar: ClientBleGattCharacteristic? = null
    private var notifyChar: ClientBleGattCharacteristic? = null
    private var versionChar: ClientBleGattCharacteristic? = null

    private var notificationJob: Job? = null
    private var timerJob: Job? = null
    private var commandSender: CommandSender<LampCommand>? = null

    private val _deviceUiStateFlow = MutableStateFlow<DeviceUiState>(DeviceUiState.Loading)
    val deviceUiStateFlow: StateFlow<DeviceUiState> = _deviceUiStateFlow

    private val _firmwareVersionFlow = MutableStateFlow<String?>(null)
    val firmwareVersionFlow: StateFlow<String?> = _firmwareVersionFlow

    private var connectTimeoutJob: Job? = null

    suspend fun connect(address: String) {
        connectionMutex.withLock {
            connectionCount++
            println("CONNECTION COUNT: $connectionCount")
            when {
                connectionCount == 1 -> {
                    _deviceUiStateFlow.value = DeviceUiState.Loading
                    connectJob = scope.launch { internalConnect(address) }
                    connectTimeoutJob = scope.launch {
                        delay(4000)
                        if (currentConnectionState != GattConnectionState.STATE_CONNECTED) {
                            println("Connection timeout — forcing reconnect")
                            reconnect(address)
                        }
                    }
                }

                currentAddress != address -> {
                    reconnect(address)
                }

                else -> {
                    when (currentConnectionState) {
                        GattConnectionState.STATE_CONNECTED -> {
                            if (isInitialized) {
                                readAndApplyState()
                                readFirmwareVersion()
                            } else {
                                _deviceUiStateFlow.value = DeviceUiState.Loading
                            }
                        }

                        GattConnectionState.STATE_DISCONNECTED -> {
                            println("Explicit reconnect")
                            reconnect(address)
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    suspend fun disconnect() {
        connectionMutex.withLock {
            connectionCount--
            connectTimeoutJob?.cancel()
            if (connectionCount <= 0) {
                connectionCount = 0
                internalDisconnect()
            }
        }
    }

    private fun reconnect(address: String) {
        internalDisconnect()
        _deviceUiStateFlow.value = DeviceUiState.Loading
        connectJob = scope.launch {
            internalConnect(address)
        }
    }

    private suspend fun internalConnect(address: String) {
        try {
            val gatt = ClientBleGatt.connect(context, address, scope, options)
            currentGatt = gatt
            currentAddress = address

            gatt.connectionState
                .onEach { handleConnectionState(it) }
                .launchIn(scope)
        } catch (e: Exception) {
            if (e !is CancellationException)
            _deviceUiStateFlow.value = DeviceUiState.Error("Connection failed: ${e.message}")
        }
    }

    private fun internalDisconnect() {
        connectJob?.cancel()
        notificationJob?.cancel()
        timerJob?.cancel()
        commandSender?.clear()

        currentGatt?.disconnect()
        currentGatt?.close()

        currentGatt = null
        currentAddress = null
        lampChar = null
        notifyChar = null
        versionChar = null
        isInitialized = false
        currentConnectionState = GattConnectionState.STATE_DISCONNECTED
    }

    private suspend fun handleConnectionState(state: GattConnectionState) {
        currentConnectionState = state

        when (state) {
            GattConnectionState.STATE_CONNECTED -> initializeDevice()
            else -> _deviceUiStateFlow.value = DeviceUiState.Loading
        }
    }

    private suspend fun initializeDevice() {
        try {
            val gatt = currentGatt ?: return
            val service = gatt.discoverServices().findService(SERVICE_UUID)
                ?: throw NoSuchElementException("Service not found")

            lampChar = service.findCharacteristic(COMMAND_CHARACTERISTIC_UUID)
            notifyChar = service.findCharacteristic(NOTIFY_CHARACTERISTIC_UUID)
            versionChar = service.findCharacteristic(FIRMWARE_VERSION_UUID)

            setupCommandSender()
            observeNotifications()
            readAndApplyState()
            readFirmwareVersion()

            isInitialized = true
        } catch (e: Exception) {
            _deviceUiStateFlow.value = DeviceUiState.Error("Init error: ${e.message}")
            isInitialized = false
        }
    }

    private fun setupCommandSender() {
        commandSender = CommandSender<LampCommand>(
            scope = scope,
            serializer = { command ->
                DataByteArray(
                    gson.toJson(command).toByteArray(Charsets.UTF_8)
                )
            },
            writer = { data ->
                lampChar?.write(data) ?: throw IllegalStateException("Characteristic not ready")
            }
        ).apply {
            init()
            errors.onEach {
                println("Command error: ${it.message}")
            }.launchIn(scope)
        }
    }

    private suspend fun observeNotifications() {
        notificationJob = notifyChar?.getNotifications()
            ?.onEach { readAndApplyState() }
            ?.catch { _deviceUiStateFlow.value = DeviceUiState.Error("Notify error: ${it.message}") }
            ?.launchIn(scope)
    }

    private suspend fun readAndApplyState() {
        try {
            val data = lampChar?.read() ?: throw IllegalStateException("Read failed")
            println("<-----${data.value.toString(Charsets.UTF_8)}")
            val state = gson.fromJson(data.value.toString(Charsets.UTF_8), LampState::class.java)
            _deviceUiStateFlow.value = DeviceUiState.Connected(state)
            restartTimers(state)
        } catch (e: Exception) {
            _deviceUiStateFlow.value = DeviceUiState.Error("State error: ${e.message}")
        }
    }

    private suspend fun readFirmwareVersion() {
        try {
            val json = versionChar?.read()?.value?.toString(Charsets.UTF_8)
            val parsed = gson.fromJson(json, FirmwareVersion::class.java)
            _firmwareVersionFlow.value = "MAC адрес: $currentAddress\nВерсия прошивки: ${parsed.version}"
        } catch (e: Exception) {
            _deviceUiStateFlow.value = DeviceUiState.Error("FW error: ${e.message}")
        }
    }

    private fun restartTimers(state: LampState) {
        timerJob?.cancel()
        timerJob = scope.launch {
            when (state.lampState) {
                LampState.RelayState.PREHEATING -> {
                    var left = state.preheat?.timeLeft ?: return@launch
                    while (left > 0) {
                        delay(1000)
                        left -= 1000
                        _deviceUiStateFlow.value = DeviceUiState.Connected(
                            state.copy(preheat = state.preheat.copy(timeLeft = left.coerceAtLeast(0)))
                        )
                    }
                }
                LampState.RelayState.ACTIVE -> {
                    var total = state.timer?.timeLeft ?: return@launch
                    val cycle = state.timer.cycleTime
                    var cycleLeft = (total % cycle).takeIf { it > 0 } ?: cycle
                    while (cycleLeft > 0) {
                        delay(1000)
                        total -= 1000
                        cycleLeft -= 1000
                        _deviceUiStateFlow.value = DeviceUiState.Connected(
                            state.copy(timer = state.timer.copy(timeLeft = total.coerceAtLeast(0)))
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun sendCommand(command: LampCommand) {
        try {
            println("----->$command")
            commandSender?.submit(command)
        } catch (e: Exception) {
            _deviceUiStateFlow.value = DeviceUiState.Error("Cmd send error: ${e.message}")
        }
    }

    private val SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
    private val COMMAND_CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
    private val NOTIFY_CHARACTERISTIC_UUID = UUID.fromString("1fd32b0a-aa51-4e49-92b2-9a8be97473c9")
    private val FIRMWARE_VERSION_UUID = UUID.fromString("b3103938-3c4c-4330-8f56-e58c77f4b0bd")

    data class FirmwareVersion(val version: String)
}