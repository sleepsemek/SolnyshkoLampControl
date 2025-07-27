package com.example.solnyshkosmartlamp.di

import android.content.Context
import androidx.room.Room
import com.example.solnyshkosmartlamp.data.local.AppDatabase
import com.example.solnyshkosmartlamp.data.local.dao.LampDao
import com.example.solnyshkosmartlamp.data.local.repository.LampRepository
import com.example.solnyshkosmartlamp.ui.lamp_control.BleDeviceManager
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, AppDatabase::class.java, "ble_db").build()

    @Provides
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideBleDeviceManager(
        @ApplicationContext context: Context,
        gson: Gson,
        @ApplicationScope scope: CoroutineScope
    ): BleDeviceManager {
        return BleDeviceManager(context, gson, scope)
    }

    @Provides
    fun provideBleScanner(@ApplicationContext context: Context) : BleScanner {
        return BleScanner(context)
    }

    @Provides
    fun provideLampDao(db: AppDatabase): LampDao = db.deviceDao()

    @Provides
    fun provideLampRepository(dao: LampDao) = LampRepository(dao)
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope