package com.sleepsemek.solnyshkosmartlamp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sleepsemek.solnyshkosmartlamp.data.local.dao.LampDao
import com.sleepsemek.solnyshkosmartlamp.data.local.entity.LampEntity

@Database(entities = [LampEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): LampDao
}