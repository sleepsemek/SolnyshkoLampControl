package com.example.solnyshkosmartlamp.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

object TimerPreferences {
    private val Context.dataStore by preferencesDataStore(name = "timer_settings")

    private val MINUTES_KEY = intPreferencesKey("minutes")
    private val SECONDS_KEY = intPreferencesKey("seconds")
    private val CYCLES_KEY = intPreferencesKey("cycles")

    suspend fun save(context: Context, minutes: Int, seconds: Int, cycles: Int) {
        context.dataStore.edit { prefs ->
            prefs[MINUTES_KEY] = minutes
            prefs[SECONDS_KEY] = seconds
            prefs[CYCLES_KEY] = cycles
        }
    }

    suspend fun load(context: Context): Triple<Int, Int, Int> {
        val prefs = context.dataStore.data.first()
        return Triple(
            prefs[MINUTES_KEY] ?: 1,
            prefs[SECONDS_KEY] ?: 30,
            prefs[CYCLES_KEY] ?: 2
        )
    }
}
