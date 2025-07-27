package com.example.solnyshkosmartlamp.ui.lamp_control

import android.annotation.SuppressLint
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solnyshkosmartlamp.data.model.LampCommand
import com.example.solnyshkosmartlamp.data.model.LampState
import dagger.hilt.android.lifecycle.HiltViewModel
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val address: String = requireNotNull(savedStateHandle["address"])

    val uiState: StateFlow<DeviceUiState> = bleManager.deviceUiStateFlow
    val firmwareVersionFlow: StateFlow<String?> = bleManager.firmwareVersionFlow

    private val _infoRequested = MutableSharedFlow<Unit>()
    val infoRequested = _infoRequested.asSharedFlow()

    init {
        println("LampControlViewModel created")
        viewModelScope.launch {
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

