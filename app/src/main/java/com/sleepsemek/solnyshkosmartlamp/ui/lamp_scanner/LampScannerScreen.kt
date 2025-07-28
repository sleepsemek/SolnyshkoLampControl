package com.sleepsemek.solnyshkosmartlamp.ui.lamp_scanner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampScannerScreen(onDeviceSelected: (String, String?) -> Unit) {
    val viewModel = hiltViewModel<LampScannerViewModel>()
    val devices by viewModel.devices.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    if (isScanning) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(devices) { device ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onDeviceSelected(device.address, device.name) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(device.name ?: "Без названия", style = MaterialTheme.typography.titleMedium)
                    Text(device.address, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}