package com.sleepsemek.solnyshkosmartlamp.ui.lamp_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sleepsemek.solnyshkosmartlamp.data.local.entity.LampEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampListScreen(onDeviceSelected: (String, String) -> Unit) {
    val viewModel = hiltViewModel<LampListViewModel>()
    val devices by viewModel.devices.collectAsState(initial = emptyList())

    var showRenameDialog by remember { mutableStateOf(false) }
    var dialogDevice by remember { mutableStateOf<LampEntity?>(null) }
    var textState by remember { mutableStateOf(TextFieldValue()) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deviceToDelete by remember { mutableStateOf<LampEntity?>(null) }

    if (showRenameDialog && dialogDevice != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Переименовать устройство") },
            text = {
                OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    label = { Text("Имя") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.renameDevice(dialogDevice!!.address, textState.text)
                        showRenameDialog = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showDeleteDialog && deviceToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Подтвердите удаление") },
            text = {
                Text(
                    "Вы уверены, что хотите удалить устройство из списка сохранённых? " +
                            "Его можно будет снова добавить в меню ＋"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDevice(deviceToDelete!!)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (devices.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .wrapContentSize(),
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "Список пуст. Добавьте новые устройства в меню ＋",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(devices) { dev ->
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 4.dp)
                        .clickable { onDeviceSelected(dev.address, dev.name.toString()) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = dev.name ?: "Неизвестное устройство",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Row {
                            IconButton(
                                onClick = {
                                    dialogDevice = dev
                                    textState = TextFieldValue(dev.name ?: "")
                                    showRenameDialog = true
                                },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Переименовать")
                            }
                            IconButton(
                                onClick = {
                                    deviceToDelete = dev
                                    showDeleteDialog = true
                                },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Удалить")
                            }
                        }
                    }
                }
            }
        }
    }
}


