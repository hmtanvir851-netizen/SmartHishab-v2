package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

enum class ThemeMode(val value: String) {
    SYSTEM("SYSTEM"),
    LIGHT("LIGHT"),
    DARK("DARK")
}

class ThemeDataStore(private val context: Context) {

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val THEME_PRESET_KEY = stringPreferencesKey("theme_preset")
    }

    val themeModeFlow: Flow<String> = context.themeDataStore.data
        .map { preferences ->
            preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.value
        }

    val themePresetFlow: Flow<String> = context.themeDataStore.data
        .map { preferences ->
            preferences[THEME_PRESET_KEY] ?: "EMERALD_GREEN"
        }

    suspend fun setThemeMode(themeMode: String) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode
        }
    }

    suspend fun setThemePreset(themePreset: String) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_PRESET_KEY] = themePreset
        }
    }
}
