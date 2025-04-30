package com.example.solnyshkosmartlamp

import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.solnyshkosmartlamp.ui.AppNavHost
import com.example.solnyshkosmartlamp.ui.theme.SolnyshkoSmartLampTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var pendingRoute: String? = null
    private var isWaitingForEnable = false

    private val bluetoothAdapter by lazy {
        (getSystemService(BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    }

    private lateinit var enableBtLauncher: ActivityResultLauncher<Intent>
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableBtLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            isWaitingForEnable = false
            pendingRoute?.let {
                navController.navigate(it)
                pendingRoute = null
            }
        }

        setContent {
            SolnyshkoSmartLampTheme {
                this@MainActivity.navController = rememberNavController()
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavHost(
                        navController = navController,
                        bluetoothAdapter = bluetoothAdapter,
                        enableBtLauncher = enableBtLauncher,
                        isWaitingForEnable = isWaitingForEnable,
                        setWaitingForEnable = { isWaitingForEnable = it },
                        setPendingRoute = { pendingRoute = it }
                    )
                }
            }
        }
    }
}
