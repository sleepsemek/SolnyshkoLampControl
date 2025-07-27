package com.example.solnyshkosmartlamp.ui.permission

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class BluetoothStatusViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _bluetoothState = MutableStateFlow<BluetoothState>(BluetoothState.Unknown)

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        bluetoothManager?.adapter
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR
                    )
                    _bluetoothState.value = when (state) {
                        BluetoothAdapter.STATE_ON -> BluetoothState.Enabled
                        BluetoothAdapter.STATE_OFF -> BluetoothState.Disabled
                        else -> BluetoothState.Unknown
                    }
                }
            }
        }
    }

    init {
        checkBluetoothState()
        registerReceiver()
    }

    fun checkBluetoothState() {
        val currentState = bluetoothAdapter?.state ?: BluetoothAdapter.STATE_OFF
        _bluetoothState.value = when (currentState) {
            BluetoothAdapter.STATE_ON -> BluetoothState.Enabled
            else -> BluetoothState.Disabled
        }
    }

    private fun registerReceiver() {
        context.registerReceiver(
            receiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
    }

    override fun onCleared() {
        super.onCleared()
        context.unregisterReceiver(receiver)
    }

    @SuppressLint("MissingPermission")
    fun requestEnableBluetooth() {
        bluetoothAdapter?.enable()
    }
}

sealed class BluetoothState {
    object Enabled : BluetoothState()
    object Disabled : BluetoothState()
    object Unknown : BluetoothState()
}