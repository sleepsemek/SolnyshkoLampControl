package com.example.solnyshkosmartlamp.ui.permission

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.BLUETOOTH_SCAN
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.solnyshkosmartlamp.R
import com.example.solnyshkosmartlamp.ble.permissions.BlePermissions
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@SuppressLint("InlinedApi")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BlePermissionScreen(onPermissionsGranted: () -> Unit) {

    val permissions = BlePermissions.getRequiredPermissions().toList()
    val permissionsState = rememberMultiplePermissionsState(permissions = permissions)

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            onPermissionsGranted()
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }


    val permissionsInfo = mapOf(
        BLUETOOTH_SCAN to Pair(
            "Поиск устройств",
            "Необходим для сканирования ближайших Bluetooth устройств"
        ),
        ACCESS_FINE_LOCATION to Pair(
            "Точное местоположение",
            "Используется для работы с Bluetooth на современных устройствах"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_bluetooth_24),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Необходимые разрешения",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                permissionsState.permissions.forEach { permissionState ->
                    val permissionInfo = permissionsInfo[permissionState.permission]
                    if (permissionInfo != null) {
                        PermissionItem(
                            title = permissionInfo.first,
                            description = permissionInfo.second,
                            permissionState = permissionState,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "После предоставления всех разрешений приложение продолжит работу автоматически",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionItem(
    title: String,
    description: String,
    permissionState: PermissionState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val status = permissionState.status

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = when (status) {
                    is PermissionStatus.Granted -> "✓ Разрешено"
                    is PermissionStatus.Denied -> if (!status.shouldShowRationale) "Требуется разрешение"
                    else "✕ Заблокировано"
                },
                color = when (status) {
                    is PermissionStatus.Granted -> MaterialTheme.colorScheme.primary
                    is PermissionStatus.Denied -> if (!status.shouldShowRationale) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.error
                },
                style = MaterialTheme.typography.labelMedium
            )
        }

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        when (status) {
            is PermissionStatus.Denied -> {
                Spacer(modifier = Modifier.height(8.dp))

                if (!status.shouldShowRationale) {
                    Column {
                        Button(
                            onClick = { permissionState.launchPermissionRequest() },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Разрешить $title")
                        }
                    }
                } else {
                    Column {
                        Text(
                            text = "Разрешение заблокировано. Перейдите в настройки и выдайте его вручную, или попробуйте перезагрузить приложение",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                context.startActivity(
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Открыть настройки")
                        }
                    }
                }
            }
            else -> {}
        }
    }
}
