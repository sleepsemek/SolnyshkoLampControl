package com.example.solnyshkosmartlamp.ui.lamp_control

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solnyshkosmartlamp.data.model.LampState
import com.example.solnyshkosmartlamp.data.model.LampCommand
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectOptions
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import java.util.UUID
import javax.inject.Inject

@SuppressLint("MissingPermission")
@HiltViewModel
class LampControlViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private val SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
        private val COMMAND_CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
        private val NOTIFY_CHARACTERISTIC_UUID = UUID.fromString("1fd32b0a-aa51-4e49-92b2-9a8be97473c9")
    }

    private val _uiState = MutableStateFlow<DeviceUiState>(DeviceUiState.Loading)
    val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

    private val options = BleGattConnectOptions(autoConnect = true, closeOnDisconnect = false)

    private var gattClient: ClientBleGatt? = null
    private var lampChar: ClientBleGattCharacteristic? = null
    private var notifyChar: ClientBleGattCharacteristic? = null

    private var connectionJob: Job? = null
    private var notificationJob: Job? = null

    private lateinit var commandSender: CommandSender<LampCommand>

    init {
        val address = savedStateHandle.get<String>("address")!!
        connect(address)
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }

    fun connect(address: String) {
        connectionJob = viewModelScope.launch {
            try {
                _uiState.value = DeviceUiState.Loading
                val connection = ClientBleGatt.connect(context, address, viewModelScope, options)
                gattClient = connection

                connection.connectionState
                    .onEach { handleConnectionState(it) }
                    .launchIn(viewModelScope)

            } catch (e: Exception) {
                _uiState.value = DeviceUiState.Error("Connection error: ${e.localizedMessage}")
            }
        }
    }

    fun disconnect() {
        connectionJob?.cancel()
        notificationJob?.cancel()
        commandSender.clear()
        gattClient?.disconnect()
        gattClient?.close()
        gattClient = null
    }

    private suspend fun handleConnectionState(state: GattConnectionState) {
        when (state) {
            GattConnectionState.STATE_CONNECTED -> initializeDevice()
            GattConnectionState.STATE_DISCONNECTED,
            GattConnectionState.STATE_DISCONNECTING,
            GattConnectionState.STATE_CONNECTING -> {
                _uiState.value = DeviceUiState.Loading
            }
        }
    }

    private suspend fun initializeDevice() {
        try {
            val services = gattClient?.discoverServices()
                ?: throw IllegalStateException("GATT client not initialized")

            val service = services.findService(SERVICE_UUID)
                ?: throw NoSuchElementException("Service $SERVICE_UUID not found")

            lampChar = service.findCharacteristic(COMMAND_CHARACTERISTIC_UUID)
                ?: throw NoSuchElementException("Command characteristic not found")

            notifyChar = service.findCharacteristic(NOTIFY_CHARACTERISTIC_UUID)
                ?: throw NoSuchElementException("Notify characteristic not found")

            setupCommandHandlers()
            observeNotifications()
            readState()

        } catch (e: Exception) {
            _uiState.value = DeviceUiState.Error("Initialization error: $ {e.message}")
        }
    }

    private fun setupCommandHandlers() {
        commandSender = CommandSender<LampCommand> (
            scope = viewModelScope,
            serializer = { command ->
                val json = gson.toJson(command)
                DataByteArray(json.toByteArray(Charsets.UTF_8))
            },
            writer = { data ->
                lampChar?.write(data) ?: throw IllegalStateException("Characteristic not ready")
            }
        ).apply {
            init()
            errors.onEach { error ->
                _uiState.value = DeviceUiState.Error("Command error: ${error.localizedMessage}")
            }.launchIn(viewModelScope)
        }

    }

    private suspend fun readState() {
        try {
            val data = lampChar?.read()
                ?: throw IllegalStateException("Characteristic not initialized")
            updateState(data)
        } catch (e: Exception) {
            _uiState.value = DeviceUiState.Error("Read error: ${e.message}")
        }
    }

    private suspend fun observeNotifications() {
        notificationJob = notifyChar?.getNotifications()
            ?.onEach { readState() }
            ?.catch { e ->
                _uiState.value = DeviceUiState.Error("Notification error: ${e.message}")
            }
            ?.launchIn(viewModelScope)
    }

    private fun updateState(data: DataByteArray) {
        try {
            val state = parseState(data)
            _uiState.value = DeviceUiState.Connected(state)
        } catch (e: Exception) {
            _uiState.value = DeviceUiState.Error("State parsing error: ${e.message}")
        }
    }

    private fun parseState(data: DataByteArray): LampState {
        val json = data.value.toString(Charsets.UTF_8)
        println(json)
        return gson.fromJson(json, LampState::class.java)
    }

    fun sendCommand(command: LampCommand) {
        try {
            commandSender.submit(command)
        } catch (e: Exception) {
            _uiState.value = DeviceUiState.Error("Send command error: ${e.message}")
        }
    }

}

sealed class DeviceUiState {
    object Loading : DeviceUiState()
    data class Connected(val state: LampState) : DeviceUiState()
    data class Error(val message: String) : DeviceUiState()
}

class CommandSender<T : Any>(
    private val scope: CoroutineScope,
    private val serializer: (T) -> DataByteArray,
    private val writer: suspend (DataByteArray) -> Unit,
    private val maxRetries: Int = 3,
    private val delayBetween: Long = 150L
) {
    private val commandChannel = Channel<T>(Channel.UNLIMITED)
    private val _errors = MutableSharedFlow<Throwable>()
    val errors: SharedFlow<Throwable> = _errors.asSharedFlow()

    fun init() {
        scope.launch {
            for (command in commandChannel) {
                var attempt = 0
                while (attempt < maxRetries) {
                    try {
                        val data = serializer(command)
                        writer(data)
                        delay(delayBetween)
                        break
                    } catch (e: Throwable) {
                        if (attempt == maxRetries - 1) {
                            _errors.emit(e)
                        } else {
                            delay(100L)
                        }
                    }
                    attempt++
                }
            }
        }
    }

    fun submit(command: T) {
        commandChannel.trySend(command)
    }

    fun clear() {
        commandChannel.close()
    }
}
