package com.example.solnyshkosmartlamp.ui.lamp_scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solnyshkosmartlamp.data.local.entity.LampEntity
import com.example.solnyshkosmartlamp.data.local.repository.LampRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanFilter
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScannerSettings
import no.nordicsemi.android.kotlin.ble.core.scanner.FilteredManufacturerData
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import javax.inject.Inject

const val MANUFACTURER_ID_INT = 0x4E53
const val MANUFACTURER_DATA_STRING = "Solnyshko OYFB-04M"

@SuppressLint("MissingPermission")
@HiltViewModel
class LampScannerViewModel @Inject constructor(
    private val bleScanner: BleScanner,
    private val repository: LampRepository,
) : ViewModel() {

    private val _devices = MutableStateFlow<List<LampEntity>>(emptyList())
    val devices: StateFlow<List<LampEntity>> = _devices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var scanningJob: Job? = null

    init {
        startScan()
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
    }

    fun startScan() {
        viewModelScope.launch {
            val savedAddresses = repository.getAllOnce()
                .map { it.address }
                .toSet()

            _isScanning.value = true

            val filters = listOf(
                BleScanFilter(
                    manufacturerData = FilteredManufacturerData(
                        MANUFACTURER_ID_INT,
                        DataByteArray.from(MANUFACTURER_DATA_STRING)
                    )
                )
            )

            val settings = BleScannerSettings(
                includeStoredBondedDevices = false
            )

            scanningJob = bleScanner.scan(filters, settings)
                .map { scanResult ->
                    LampEntity(
                        address = scanResult.device.address,
                        name = scanResult.device.name ?: "Неизвестное устройство"
                    )
                }
                .onEach { device ->
                    if (
                        device.address !in savedAddresses &&
                        _devices.value.none { it.address == device.address }
                    ) {
                        _devices.update { it + device }
                    }
                }
                .catch { e ->
                    println("Scan error: ${e.message}")
                }
                .onCompletion {
                    _isScanning.value = false
                }
                .launchIn(viewModelScope)
        }
    }

    fun stopScan() {
        scanningJob?.cancel()
        _isScanning.value = false
    }

    fun saveDevice(address: String, name: String?) {
        viewModelScope.launch {
            val device = _devices.value.firstOrNull { it.address == address }

            val baseName = name ?: device?.name ?: "Без имени"
            val count = repository.getDevicesCount()
            val finalName = if (count == 0) baseName else "$baseName (${count + 1})"

            repository.saveDevice(
                LampEntity(
                    address = address,
                    name = finalName
                )
            )
        }
    }
}