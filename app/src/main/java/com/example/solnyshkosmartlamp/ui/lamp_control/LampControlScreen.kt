package com.example.solnyshkosmartlamp.ui.lamp_control

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.solnyshkosmartlamp.data.model.LampState
import com.example.solnyshkosmartlamp.data.model.LampState.RelayState
import com.example.solnyshkosmartlamp.data.model.LampCommand
import kotlinx.coroutines.delay
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampControlScreen() {
    val viewModel: LampControlViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is DeviceUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is DeviceUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Ошибка: ${state.message}", color = Color.Red)
            }
        }

        is DeviceUiState.Connected -> {
            DeviceControlContent(
                state = state.state,
                onCommandSend = viewModel::sendCommand
            )
        }
    }
}

@Composable
private fun DeviceControlContent(
    state: LampState,
    onCommandSend: (LampCommand) -> Unit
) {
    val relayOn = when (state.lampState) {
        RelayState.OFF, RelayState.NONE -> false
        else -> true
    }

    val showTimerDialog = remember { mutableStateOf(false) }

    if (showTimerDialog.value) {
        TimerSetupDialog(
            onDismiss = { showTimerDialog.value = false },
            onConfirm = { time, cycles ->
                onCommandSend(LampCommand("set", time, cycles))
                showTimerDialog.value = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.TopCenter)) {
            when (state.lampState) {
                RelayState.PREHEATING -> {
                    PreheatStatusIndicator(timeLeft = state.preheat?.timeLeft ?: 0)
                }
                RelayState.ACTIVE, RelayState.PAUSED -> {
                    state.timer?.let {
                        TimerStatusIndicator(timer = it, active = state.lampState == RelayState.ACTIVE)
                    }
                }
                else -> {}
            }
        }

        Button(
            onClick = {
                val command = LampCommand(if (relayOn) 0 else 1)
                onCommandSend(command)
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Text(if (relayOn) "Выключить" else "Включить")
        }

        Button(
            onClick = {
                when (state.lampState) {
                    RelayState.OFF, RelayState.ON -> showTimerDialog.value = true
                    RelayState.ACTIVE -> onCommandSend(LampCommand("pause"))
                    RelayState.PAUSED -> onCommandSend(LampCommand("resume"))
                    else -> {}
                }
            },
            enabled = state.lampState != RelayState.PREHEATING && state.lampState != RelayState.NONE,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(8.dp)
        ) {
            Text(
                when (state.lampState) {
                    RelayState.OFF, RelayState.ON -> "Таймер"
                    RelayState.PREHEATING -> "Преднагрев"
                    RelayState.ACTIVE -> "Пауза"
                    RelayState.PAUSED -> "Продолжить"
                    else -> ""
                }
            )
        }
    }
}



@Composable
fun TimerSetupDialog(
    onDismiss: () -> Unit,
    onConfirm: (time: Long, cycles: Int) -> Unit
) {
    var timeText by remember { mutableStateOf("") }
    var cyclesText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройка таймера") },
        text = {
            Column {
                OutlinedTextField(
                    value = timeText,
                    onValueChange = { timeText = it },
                    label = { Text("Время одного цикла (сек)") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = cyclesText,
                    onValueChange = { cyclesText = it },
                    label = { Text("Количество циклов") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val time = timeText.toLongOrNull()?.times(1000) ?: 0L
                val cycles = cyclesText.toIntOrNull() ?: 0
                onConfirm(time, cycles)
            }) {
                Text("Установить")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun TimerStatusIndicator(timer: LampState.Timer, active: Boolean) {
    var localTimeLeft by remember(timer) { mutableStateOf(timer.timeLeft) }

    // Тикаем только если активен
    LaunchedEffect(active, timer.timeLeft) {
        if (active) {
            localTimeLeft = timer.timeLeft
            while (localTimeLeft > 0) {
                delay(1000L)
                localTimeLeft -= 1000
            }
        } else {
            localTimeLeft = timer.timeLeft // сброс, если был перескок
        }
    }

    val cycleTime = timer.cycleTime.takeIf { it > 0 } ?: 1
    val totalCycles = timer.generalCycles.takeIf { it > 0 } ?: 1

    val currentCycle = (totalCycles - (localTimeLeft / cycleTime))
        .coerceIn(1, totalCycles)

    val timeLeftInCurrentCycle = localTimeLeft % cycleTime

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Осталось времени: ${(localTimeLeft.toDouble() / 1000).roundToInt()} сек", style = MaterialTheme.typography.bodyMedium)
            Text("Цикл: $currentCycle / $totalCycles", style = MaterialTheme.typography.bodyMedium)
            Text("Текущий цикл завершится через: ${(timeLeftInCurrentCycle.toDouble() / 1000).roundToInt()} сек", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun PreheatStatusIndicator(timeLeft: Int) {
    var localTimeLeft by remember(timeLeft) { mutableStateOf(timeLeft) }

    LaunchedEffect(timeLeft) {
        localTimeLeft = timeLeft
        while (localTimeLeft > 0) {
            delay(1000)
            localTimeLeft -= 1000
        }
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Преднагрев: ${localTimeLeft / 1000} сек", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

