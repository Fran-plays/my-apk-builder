package com.petmorph.ai.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "petmorph_settings")

class SettingsManager(private val context: Context) {
    private val onboarded = booleanPreferencesKey("onboarded")
    private val darkMode = stringPreferencesKey("dark_mode")
    private val overlayOpacity = floatPreferencesKey("overlay_opacity")
    private val overlaySize = floatPreferencesKey("overlay_size")
    private val soundsOn = booleanPreferencesKey("sounds_on")
    private val pro = booleanPreferencesKey("pro")

    val isOnboarded: Flow<Boolean> = context.dataStore.data.map { it[onboarded] ?: false }
    val themeMode: Flow<String> = context.dataStore.data.map { it[darkMode] ?: "system" }
    val overlayOpacityFlow: Flow<Float> = context.dataStore.data.map { it[overlayOpacity] ?: 1.0f }
    val overlaySizeFlow: Flow<Float> = context.dataStore.data.map { it[overlaySize] ?: 1.0f }
    val soundsOnFlow: Flow<Boolean> = context.dataStore.data.map { it[soundsOn] ?: true }
    val isProFlow: Flow<Boolean> = context.dataStore.data.map { it[pro] ?: false }

    suspend fun setOnboarded(v: Boolean) = context.dataStore.edit { it[onboarded] = v }
    suspend fun setThemeMode(v: String) = context.dataStore.edit { it[darkMode] = v }
    suspend fun setOverlayOpacity(v: Float) = context.dataStore.edit { it[overlayOpacity] = v }
    suspend fun setOverlaySize(v: Float) = context.dataStore.edit { it[overlaySize] = v }
    suspend fun setSoundsOn(v: Boolean) = context.dataStore.edit { it[soundsOn] = v }
    suspend fun setPro(v: Boolean) = context.dataStore.edit { it[pro] = v }
}
