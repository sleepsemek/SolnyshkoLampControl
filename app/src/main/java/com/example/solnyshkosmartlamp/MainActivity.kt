package com.example.solnyshkosmartlamp

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import com.example.solnyshkosmartlamp.ble.permissions.BlePermissions
import com.example.solnyshkosmartlamp.ui.AppNavHost
import com.example.solnyshkosmartlamp.ui.theme.SolnyshkoSmartLampTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SolnyshkoSmartLampTheme {
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    startDestination = if (checkBlePermissions(this@MainActivity)) "lamp_list" else "ble_permission"
                }

                if (startDestination != null) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        AppNavHost(startDestination = startDestination!!)

                    }
                } else {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

            }
        }
    }

    private fun checkBlePermissions(context: Context): Boolean {
        val permissions = BlePermissions.getRequiredPermissions()
        var missing = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        return missing.isEmpty()
    }
}
