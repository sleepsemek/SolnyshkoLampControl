package com.example.solnyshkosmartlamp.ui

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.solnyshkosmartlamp.ui.lamp_control.LampControlScreen
import com.example.solnyshkosmartlamp.ui.lamp_list.LampListScreen
import com.example.solnyshkosmartlamp.ui.lamp_scanner.LampScannerScreen
import com.example.solnyshkosmartlamp.ui.lamp_scanner.LampScannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
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
                    else -> "Устройство"
                })
            })
        },
        floatingActionButton = {
            when (currentRoute) {
                "lamp_list" -> FloatingActionButton(onClick = { navController.navigate("scanner") }) {
                    Icon(Icons.Default.Add, contentDescription = "Сканировать")
                }
                "scanner" -> FloatingActionButton(onClick = { navController.popBackStack("lamp_list", false) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
                else -> {}
            }
        }
    ) { padding ->
        NavHost(
            navController,
            startDestination = "lamp_list",
            Modifier.padding(padding)
        ) {
            composable("lamp_list") {
                LampListScreen(
                    onDeviceSelected = { address, name ->
                        navController.navigate("lamp/$address?name=$name")
                })
            }
            composable("scanner") {
                val scannerVM: LampScannerViewModel = hiltViewModel()

                LampScannerScreen(
                    onDeviceSelected = { address, name ->
                        scannerVM.saveDevice(address, name)
                        navController.navigate("lamp_list") {
                            popUpTo("scanner") {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }

                    }
                )
            }
            composable("lamp/{address}?name={name}") { backStackEntry ->
                LampControlScreen()
            }
        }
    }
}

