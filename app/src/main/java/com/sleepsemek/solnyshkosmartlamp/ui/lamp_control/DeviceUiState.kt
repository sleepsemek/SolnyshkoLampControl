package com.sleepsemek.solnyshkosmartlamp.ui.lamp_control

import com.sleepsemek.solnyshkosmartlamp.data.model.LampState

sealed class DeviceUiState {
    object Loading : DeviceUiState()
    data class Connected(val state: LampState) : DeviceUiState()
    data class Error(val message: String) : DeviceUiState()
}