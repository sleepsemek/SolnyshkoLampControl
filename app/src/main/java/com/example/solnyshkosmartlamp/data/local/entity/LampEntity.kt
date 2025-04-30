package com.example.solnyshkosmartlamp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lamp_list")
data class LampEntity(
    @PrimaryKey val address: String,
    val name: String?,
    val lastSeen: Long = System.currentTimeMillis()
)