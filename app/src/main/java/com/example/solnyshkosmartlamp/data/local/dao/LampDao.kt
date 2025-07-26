package com.example.solnyshkosmartlamp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.solnyshkosmartlamp.data.local.entity.LampEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LampDao {
    @Query("SELECT * FROM lamp_list ORDER BY lastSeen DESC")
    fun getAll(): Flow<List<LampEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(device: LampEntity)

    @Delete
    suspend fun delete(device: LampEntity)

    @Query("UPDATE lamp_list SET name = :newName where address = :address")
    suspend fun updateName(address: String, newName: String)

    @Query("SELECT COUNT(*) FROM lamp_list")
    suspend fun getCount() : Int

    @Query("SELECT * FROM lamp_list")
    suspend fun getAllOnce(): List<LampEntity>
}