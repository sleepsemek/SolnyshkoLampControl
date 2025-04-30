package com.example.solnyshkosmartlamp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.solnyshkosmartlamp.data.local.dao.LampDao
import com.example.solnyshkosmartlamp.data.local.entity.LampEntity

@Database(entities = [LampEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): LampDao
}