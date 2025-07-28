package com.sleepsemek.solnyshkosmartlamp.ui.lamp_control

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepsemek.solnyshkosmartlamp.ble.BleDeviceManager
import com.sleepsemek.solnyshkosmartlamp.data.model.LampCommand
import com.sleepsemek.solnyshkosmartlamp.data.model.LampState
import com.sleepsemek.solnyshkosmartlamp.utils.TimerPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@SuppressLint("MissingPermission")
@HiltViewModel
class LampControlViewModel @Inject constructor(
    private val bleManager: BleDeviceManager,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val address: String = requireNotNull(savedStateHandle["address"])

    val uiState: StateFlow<DeviceUiState> = bleManager.deviceUiStateFlow
    val firmwareVersionFlow: StateFlow<String?> = bleManager.firmwareVersionFlow

    private val _infoRequested = MutableSharedFlow<Unit>()
    val infoRequested = _infoRequested.asSharedFlow()

    var minutes by mutableIntStateOf(0)
        private set

    var seconds by mutableIntStateOf(30)
        private set

    var cycles by mutableIntStateOf(2)
        private set

    var isLoaded by mutableStateOf(false)
        private set

    fun updateTime(min: Int, sec: Int) {
        minutes = min
        seconds = sec
    }

    fun updateCycles(c: Int) {
        cycles = c
    }

    fun save(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            TimerPreferences.save(context, minutes, seconds, cycles)
            onComplete()
        }
    }

    fun getTotalMillis(): Long = (minutes * 60 + seconds) * 1000L

    init {
        println("LampControlViewModel created")
        viewModelScope.launch {
            val (savedMinutes, savedSeconds, savedCycles) = TimerPreferences.load(context)
            minutes = savedMinutes
            seconds = savedSeconds
            cycles = savedCycles
            isLoaded = true
            bleManager.connect(address)
        }
    }

    override fun onCleared() {
        super.onCleared()
        println("LampControlViewModel destroyed")
        runBlocking {
            bleManager.disconnect()
        }
    }

    fun sendCommand(command: LampCommand) {
        bleManager.sendCommand(command)
    }

    suspend fun requestInfo() {
        _infoRequested.emit(Unit)
    }
}


sealed class DeviceUiState {
    object Loading : DeviceUiState()
    data class Connected(val state: LampState) : DeviceUiState()
    data class Error(val message: String) : DeviceUiState()
}

