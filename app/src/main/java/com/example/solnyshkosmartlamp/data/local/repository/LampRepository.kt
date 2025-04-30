package com.example.solnyshkosmartlamp.data.local.repository

import com.example.solnyshkosmartlamp.data.local.dao.LampDao
import com.example.solnyshkosmartlamp.data.local.entity.LampEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LampRepository @Inject constructor(
    private val dao: LampDao
) {
    fun getDevices(): Flow<List<LampEntity>> = dao.getAll()
    suspend fun saveDevice(entity: LampEntity) = dao.insert(entity)
    suspend fun deleteDevice(entity: LampEntity) = dao.delete(entity)
    suspend fun renameDevice(address: String, newName: String) = dao.updateName(address, newName)
    suspend fun getDevicesCount() : Int = dao.getCount()
}