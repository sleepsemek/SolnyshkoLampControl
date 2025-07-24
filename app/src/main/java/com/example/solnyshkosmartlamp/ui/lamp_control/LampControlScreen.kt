package com.example.solnyshkosmartlamp.ui.lamp_control

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.solnyshkosmartlamp.data.model.LampCommand
import com.example.solnyshkosmartlamp.data.model.LampState
import com.example.solnyshkosmartlamp.data.model.LampState.RelayState
import kotlin.math.roundToInt

@Preview(
    showBackground = true,
    )
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampControlScreenPreview() {
    val uiState = DeviceUiState.Connected(
        state = LampState(
            lampState = RelayState.ACTIVE,
            timer = LampState.Timer(
                timeLeft = 80000,
                generalCycles = 2,
                cycleTime = 60000
            )
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(text = "Солнышко ОУФБ-04М")
            })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            DeviceControlContent(
                state = uiState.state,
                onCommandSend = {  }
            )
        }
    }

}

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
    val relayOn = state.lampState != RelayState.OFF && state.lampState != RelayState.NONE
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f),
            contentAlignment = Alignment.Center
        ) {
            LampStatusIndicator(
                lampState       = state.lampState,
                timer           = state.timer,
                preheatTimeLeft = state.preheat?.timeLeft
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    when (state.lampState) {
                        RelayState.OFF, RelayState.ON -> showTimerDialog.value = true
                        RelayState.ACTIVE -> onCommandSend(LampCommand("pause"))
                        RelayState.PAUSED -> onCommandSend(LampCommand("resume"))
                        else -> {}
                    }
                },
                enabled = state.lampState != RelayState.PREHEATING
                        && state.lampState != RelayState.NONE,
                modifier = Modifier
                    .size(100.dp),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    text = when (state.lampState) {
                        RelayState.OFF, RelayState.ON    -> "Таймер"
                        RelayState.PREHEATING            -> "Преднагрев"
                        RelayState.ACTIVE                -> "Пауза"
                        RelayState.PAUSED                -> "Пуск"
                        else                             -> ""
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            val (targetContainerColor, targetContentColor) = if (relayOn) {
                MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
            } else {
                MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
            }

            val animatedContainerColor by animateColorAsState(
                targetValue = targetContainerColor,
                animationSpec = tween(durationMillis = 500),
                label = "buttonContainerColor"
            )

            val animatedContentColor by animateColorAsState(
                targetValue = targetContentColor,
                animationSpec = tween(durationMillis = 500),
                label = "buttonContentColor"
            )

            Button(
                onClick = {
                    val command = LampCommand(if (relayOn) 0 else 1)
                    onCommandSend(command)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = animatedContainerColor,
                    contentColor = animatedContentColor
                )
            ) {
                Text(if (relayOn) "Выключить" else "Включить")
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LampStatusIndicator(
    lampState: RelayState,
    timer: LampState.Timer?,
    preheatTimeLeft: Int?
) {
    val isPreheating = lampState == RelayState.PREHEATING
    val isActive = lampState == RelayState.ACTIVE
    val isPaused = lampState == RelayState.PAUSED
    val isTimerState = isPreheating || isActive || isPaused
    val isOn = lampState == RelayState.ON

    val cycleTime = timer?.cycleTime?.takeIf { it > 0 } ?: 1
    val totalCycles = timer?.generalCycles?.takeIf { it > 0 } ?: 1
    val timeLeft = when {
        isPreheating -> (preheatTimeLeft ?: 0).takeIf { it > 0 } ?: 1
        isTimerState -> timer?.timeLeft?.takeIf { it > 0 } ?: 1
        else -> 1
    }

    val currentCycle = if (isTimerState && !isPreheating)
        (totalCycles - (timeLeft / cycleTime)).coerceIn(1, totalCycles)
    else 0

    val timeLeftInCurrentCycle = if (isPreheating) timeLeft else timeLeft % cycleTime
    val targetProgress = if (isTimerState) {
        if (isPreheating) timeLeft / 60000f else timeLeftInCurrentCycle / cycleTime.toFloat()
    } else 1f

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )

    val (targetColor, trackColor) = when {
        isPreheating -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.errorContainer
        isActive || isPaused -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.outline to MaterialTheme.colorScheme.surfaceVariant
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 500),
        label = "color"
    )
    val animatedTrackColor by animateColorAsState(
        targetValue = trackColor,
        animationSpec = tween(durationMillis = 500),
        label = "trackColor"
    )

    val statusText = when {
        isPreheating -> "Преднагрев: ${timeLeft / 1000} сек"
        isActive || isPaused -> {
            val totalSeconds = (timeLeftInCurrentCycle / 1000.0).roundToInt()
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            "Таймер: ${minutes}:${"%02d".format(seconds)}\nЦикл: $currentCycle/$totalCycles"
        }
        isOn -> "Лампа включена"
        else -> "Лампа выключена"
    }

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1f)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 24.dp,
                color = animatedColor,
                trackColor = animatedTrackColor
            )
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(1f)
                    .aspectRatio(1f),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = animatedColor
                    )
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerSetupDialog(
    onDismiss: () -> Unit,
    onConfirm: (time: Long, cycles: Int) -> Unit
) {
    var minutes by remember { mutableStateOf(1) }
    var seconds by remember { mutableStateOf(0) }
    var cycles by remember { mutableStateOf(2) }

    val sliderRange = 0f..59f
    val sliderSteps = 58

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройка таймера") },
        text = {
            Column {
                Text("Время одного цикла")

                Text("$minutes минут")
                Slider(
                    value = minutes.toFloat(),
                    onValueChange = { minutes = it.toInt() },
                    valueRange = sliderRange,
                    steps = sliderSteps
                )

                Text("$seconds секунд")
                Slider(
                    value = seconds.toFloat(),
                    onValueChange = { seconds = it.toInt() },
                    valueRange = sliderRange,
                    steps = sliderSteps
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Количество циклов: $cycles")
                Slider(
                    value = cycles.toFloat(),
                    onValueChange = { cycles = it.toInt() },
                    valueRange = 1f..10f,
                    steps = 8
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val totalMillis = (minutes * 60 + seconds) * 1000L
                onConfirm(totalMillis, cycles)
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




