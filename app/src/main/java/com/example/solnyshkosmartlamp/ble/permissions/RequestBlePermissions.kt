package com.example.solnyshkosmartlamp.ble.permissions

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat

/**
 * Композиционный утилитный метод для запроса BLE-разрешений.
 * @param onResult вызывается с true, если все разрешения выданы, иначе false.
 */
@Composable
fun RequestBlePermissions(onResult: (Boolean) -> Unit) {
    val context = LocalContext.current
    val permissions = remember { BlePermissions.getRequiredPermissions() }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        onResult(result.values.all { it })
    }

    LaunchedEffect(Unit) {
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        for (missed in missing) {
            println(missed)
        }
        if (missing.isEmpty()) {
            onResult(true)
        } else {
            launcher.launch(missing.toTypedArray())
        }
    }
}
