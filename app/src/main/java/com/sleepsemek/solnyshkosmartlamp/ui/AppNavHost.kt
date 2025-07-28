package com.sleepsemek.solnyshkosmartlamp.ui

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sleepsemek.solnyshkosmartlamp.ui.lamp_control.LampControlScreen
import com.sleepsemek.solnyshkosmartlamp.ui.lamp_control.LampControlViewModel
import com.sleepsemek.solnyshkosmartlamp.ui.lamp_list.LampListScreen
import com.sleepsemek.solnyshkosmartlamp.ui.lamp_scanner.LampScannerScreen
import com.sleepsemek.solnyshkosmartlamp.ui.lamp_scanner.LampScannerViewModel
import com.sleepsemek.solnyshkosmartlamp.ui.permission.BlePermissionScreen
import com.sleepsemek.solnyshkosmartlamp.utils.bleGuardedComposable
import com.sleepsemek.solnyshkosmartlamp.utils.checkAndHandleBle
import kotlinx.coroutines.launch

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
            TopAppBar(
                title = {
                    Text(text = when {
                        currentRoute == "lamp_list" -> "Устройства"
                        currentRoute == "scanner" -> "Сканер"
                        currentRoute?.startsWith("lamp/") == true -> deviceName.toString()
                        else -> "Солнышко Connect"
                    })
                },
                actions = {
                    if (currentRoute?.startsWith("lamp/") == true) {
                        val navBackStackEntry = backStack
                        if (navBackStackEntry != null) {
                            val lampViewModel: LampControlViewModel = hiltViewModel(navBackStackEntry)
                            val scope = rememberCoroutineScope()

                            IconButton(onClick = {
                                scope.launch {
                                    lampViewModel.requestInfo()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Информация",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            )
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
            ) { backStackEntry ->
                val viewModel: LampControlViewModel = hiltViewModel(backStackEntry)
                LampControlScreen(viewModel)
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

