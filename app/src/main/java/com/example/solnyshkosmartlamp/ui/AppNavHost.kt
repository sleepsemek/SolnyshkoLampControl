package com.example.solnyshkosmartlamp.ui

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.solnyshkosmartlamp.ui.lamp_control.LampControlScreen
import com.example.solnyshkosmartlamp.ui.lamp_list.LampListScreen
import com.example.solnyshkosmartlamp.ui.lamp_scanner.LampScannerScreen
import com.example.solnyshkosmartlamp.ui.lamp_scanner.LampScannerViewModel
import com.example.solnyshkosmartlamp.ui.permission.BlePermissionScreen
import com.example.solnyshkosmartlamp.utils.bleGuardedComposable
import com.example.solnyshkosmartlamp.utils.checkAndHandleBle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    bluetoothAdapter: BluetoothAdapter?,
    enableBtLauncher: ActivityResultLauncher<Intent>,
    isWaitingForEnable: Boolean,
    setWaitingForEnable: (Boolean) -> Unit,
    setPendingRoute: (String) -> Unit
) {
    val context = LocalContext.current
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val deviceName = backStack?.arguments?.getString("name")

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(text = when {
                    currentRoute == "lamp_list" -> "Устройства"
                    currentRoute == "scanner" -> "Сканер"
                    currentRoute?.startsWith("lamp/") == true -> deviceName.toString()
                    else -> "Солнышко Connect"
                })
            })
        },
        floatingActionButton = {
            when (currentRoute) {
                "lamp_list" -> FloatingActionButton(onClick = {
                    val route = "scanner"
                    if (checkAndHandleBle(context, bluetoothAdapter, route, navController, enableBtLauncher, isWaitingForEnable, setWaitingForEnable, setPendingRoute)) {
                        navController.navigate(route)
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Сканировать")
                }

                "scanner" -> FloatingActionButton(onClick = {
                    navController.popBackStack("lamp_list", false)
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "lamp_list",
            modifier = Modifier.padding(padding)
        ) {
            composable("lamp_list") {
                LampListScreen(
                    onDeviceSelected = { address, name ->
                        val route = "lamp/$address?name=$name"
                        if (checkAndHandleBle(context, bluetoothAdapter, route, navController, enableBtLauncher, isWaitingForEnable, setWaitingForEnable, setPendingRoute)) {
                            navController.navigate(route)
                        }
                    }
                )
            }

            bleGuardedComposable(
                route = "scanner",
                bluetoothAdapter = bluetoothAdapter,
                enableBtLauncher = enableBtLauncher,
                navController = navController,
                isWaitingForEnable = isWaitingForEnable,
                setWaitingForEnable = setWaitingForEnable,
                setPendingRoute = setPendingRoute
            ) {
                val scannerVM: LampScannerViewModel = hiltViewModel()
                LampScannerScreen(
                    onDeviceSelected = { address, name ->
                        scannerVM.saveDevice(address, name)
                        navController.navigate("lamp_list") {
                            popUpTo("scanner") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            bleGuardedComposable(
                route = "lamp/{address}?name={name}",
                bluetoothAdapter = bluetoothAdapter,
                enableBtLauncher = enableBtLauncher,
                navController = navController,
                isWaitingForEnable = isWaitingForEnable,
                setWaitingForEnable = setWaitingForEnable,
                setPendingRoute = setPendingRoute
            ) {
                LampControlScreen()
            }

            composable("ble_permission") {
                BlePermissionScreen(
                    onPermissionsGranted = {
                        navController.navigate("lamp_list") {
                            popUpTo("ble_permission") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

