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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LampControlViewModel @Inject constructor(
    private val bleManager: BleDeviceManager,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val address: String = requireNotNull(savedStateHandle["address"])

    private val _uiState = MutableStateFlow<DeviceUiState>(DeviceUiState.Loading)
    val uiState: StateFlow<DeviceUiState> = _uiState

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

    var displayTimeLeft by mutableIntStateOf(0)
        private set

    private var timerJob: Job? = null

    init {
        println("LampControlViewModel created")
        viewModelScope.launch {
            val (savedMinutes, savedSeconds, savedCycles) = TimerPreferences.load(context)
            minutes = savedMinutes
            seconds = savedSeconds
            cycles = savedCycles
            isLoaded = true

            bleManager.connect(address)

            bleManager.deviceUiStateFlow.collect { state ->
                _uiState.value = state

                if (state is DeviceUiState.Connected) {
                    observeTimerFromState(state.state)
                } else {
                    timerJob?.cancel()
                    timerJob = null
                }
            }

        }
    }

    private fun observeTimerFromState(state: LampState) {
        timerJob?.cancel()

        val timeLeft = when (state.lampState) {
            LampState.RelayState.PREHEATING -> state.preheat?.timeLeft
            LampState.RelayState.ACTIVE -> state.timer?.timeLeft
            else -> null
        }

        timeLeft?.takeIf { it > 0 }?.let {
            startCountdown(it, state)
        }
    }

    private fun startCountdown(initial: Int, baseState: LampState) {
        timerJob?.cancel()
        if (initial <= 0) return

        timerJob = viewModelScope.launch {
            var remaining = initial
            while (remaining > 0) {
                delay(1000)
                remaining -= 1000

                val updatedState = when (baseState.lampState) {
                    LampState.RelayState.PREHEATING -> baseState.copy(
                        preheat = baseState.preheat?.copy(timeLeft = remaining)
                    )
                    LampState.RelayState.ACTIVE -> baseState.copy(
                        timer = baseState.timer?.copy(timeLeft = remaining)
                    )
                    else -> baseState
                }

                _uiState.value = DeviceUiState.Connected(updatedState)
            }
        }
    }

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

    fun sendCommand(command: LampCommand) {
        bleManager.sendCommand(command)
    }

    suspend fun requestInfo() {
        _infoRequested.emit(Unit)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        runBlocking { bleManager.disconnect() }
    }
}
