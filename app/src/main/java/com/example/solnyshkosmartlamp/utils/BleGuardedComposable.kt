package com.example.solnyshkosmartlamp.utils

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.solnyshkosmartlamp.ble.permissions.BlePermissions.checkBlePermissions

fun checkAndHandleBle(
    context: Context,
    bluetoothAdapter: BluetoothAdapter?,
    route: String,
    navController: NavHostController,
    enableBtLauncher: ActivityResultLauncher<Intent>,
    isWaitingForEnable: Boolean,
    setWaitingForEnable: (Boolean) -> Unit,
    setPendingRoute: (String) -> Unit
): Boolean {
    if (!checkBlePermissions(context)) {
        navController.navigate("ble_permission") {
            popUpTo(route) { inclusive = true }
        }
        return false
    }

    if (bluetoothAdapter?.state != BluetoothAdapter.STATE_ON) {
        if (!isWaitingForEnable) {
            setPendingRoute(route)
            setWaitingForEnable(true)
            enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
        return false
    }

    return true
}

fun NavGraphBuilder.bleGuardedComposable(
    route: String,
    bluetoothAdapter: BluetoothAdapter?,
    enableBtLauncher: ActivityResultLauncher<Intent>,
    navController: NavHostController,
    isWaitingForEnable: Boolean,
    setWaitingForEnable: (Boolean) -> Unit,
    setPendingRoute: (String) -> Unit,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(route) { backStackEntry ->
        val context = LocalContext.current
        val allowed = remember(context, bluetoothAdapter?.state) {
            checkBlePermissions(context) && bluetoothAdapter?.state == BluetoothAdapter.STATE_ON
        }

        LaunchedEffect(Unit) {
            if (!allowed) {
                checkAndHandleBle(
                    context = context,
                    bluetoothAdapter = bluetoothAdapter,
                    route = route,
                    navController = navController,
                    enableBtLauncher = enableBtLauncher,
                    isWaitingForEnable = isWaitingForEnable,
                    setWaitingForEnable = setWaitingForEnable,
                    setPendingRoute = setPendingRoute
                )
            }
        }

        if (allowed) {
            content(backStackEntry)
        }
    }
}
