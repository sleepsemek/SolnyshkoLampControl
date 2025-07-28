package com.sleepsemek.solnyshkosmartlamp.ui.lamp_control

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.sleepsemek.solnyshkosmartlamp.data.model.LampCommand
import com.sleepsemek.solnyshkosmartlamp.data.model.LampState
import com.sleepsemek.solnyshkosmartlamp.data.model.LampState.RelayState
import com.sleepsemek.solnyshkosmartlamp.utils.TimerPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
fun LampControlScreen(viewModel: LampControlViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val firmwareVersion by viewModel.firmwareVersionFlow.collectAsState()
    var showInfoDialog by remember { mutableStateOf(false) }
    var infoDialogText by remember { mutableStateOf("") }

    LaunchedEffect(firmwareVersion) {
        if (firmwareVersion != null) {
            infoDialogText = firmwareVersion!!
        }
    }

    LaunchedEffect(Unit) {
        viewModel.infoRequested.collect {
            showInfoDialog = true
        }
    }

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

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Об устройстве") },
            text = { Text(infoDialogText) },
            confirmButton = {
                Button(onClick = { showInfoDialog = false }) {
                    Text("ОК")
                }
            }
        )
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
    val context = LocalContext.current
    var minutes by remember { mutableIntStateOf(0) }
    var seconds by remember { mutableIntStateOf(30) }
    var cycles by remember { mutableIntStateOf(2) }
    var isLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val (savedMinutes, savedSeconds, savedCycles) = TimerPreferences.load(context)
        minutes = savedMinutes
        seconds = savedSeconds
        cycles = savedCycles
        isLoaded = true
    }

    if (!isLoaded) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройка таймера") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(2f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Мин., сек.:", style = MaterialTheme.typography.titleMedium)

                        TimeWheelPicker(
                            minutes = minutes,
                            seconds = seconds,
                            size = DpSize(200.dp, 160.dp),
                            textStyle = MaterialTheme.typography.labelLarge,
                            onTimeChange = { min, sec ->
                                minutes = min
                                seconds = sec
                            }
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Цикл.:", style = MaterialTheme.typography.titleMedium)

                        WheelNumberPicker(
                            value = cycles,
                            valueRange = 0..10,
                            size = DpSize(100.dp, 160.dp),
                            textStyle = MaterialTheme.typography.labelLarge,
                            onValueChange = { cycles = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val totalMillis = (minutes * 60 + seconds) * 1000L

                    CoroutineScope(Dispatchers.IO).launch {
                        TimerPreferences.save(
                            context = context,
                            minutes = minutes,
                            seconds = seconds,
                            cycles = cycles
                        )
                    }

                    onConfirm(totalMillis, cycles)
                }
            ) {
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













