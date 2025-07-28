package com.sleepsemek.solnyshkosmartlamp.ui.lamp_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepsemek.solnyshkosmartlamp.data.local.repository.LampRepository
import com.sleepsemek.solnyshkosmartlamp.data.local.entity.LampEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LampListViewModel @Inject constructor(
    private val repository: LampRepository
) : ViewModel() {
    val devices: Flow<List<LampEntity>> = repository.getDevices()

    fun deleteDevice(device: LampEntity) = viewModelScope.launch {
        repository.deleteDevice(device)
    }

    fun renameDevice(address: String, newName: String) = viewModelScope.launch {
        repository.renameDevice(address, newName)
    }
}