package com.cnn.mushroom.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.cnn.mushroom.data.NameDisplayFormat
import com.cnn.mushroom.data.TimeDisplayFormat
import com.cnn.mushroom.data.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

@Singleton
class UserSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // 1. Definicje Kluczy DataStore
    private object PreferencesKeys {
        val DISPLAY_TIMESTAMP = booleanPreferencesKey("display_timestamp")
        val NAME_FORMAT = stringPreferencesKey("name_format")
        val TIME_FORMAT = stringPreferencesKey("time_format")
    }

    val userSettingsFlow: Flow<UserSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val displayTimestamp = preferences[PreferencesKeys.DISPLAY_TIMESTAMP] ?: UserSettings.DEFAULT.displayTimestamp

            val nameFormatString = preferences[PreferencesKeys.NAME_FORMAT] ?: UserSettings.DEFAULT.nameDisplayFormat.name
            val nameDisplayFormat = NameDisplayFormat.valueOf(nameFormatString)

            val timeFormatString = preferences[PreferencesKeys.TIME_FORMAT] ?: UserSettings.DEFAULT.timeDisplayFormat.name
            val timeDisplayFormat = TimeDisplayFormat.valueOf(timeFormatString)

            UserSettings(
                displayTimestamp = displayTimestamp,
                nameDisplayFormat = nameDisplayFormat,
                timeDisplayFormat = timeDisplayFormat
            )
        }

    suspend fun saveSettings(settings: UserSettings) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DISPLAY_TIMESTAMP] = settings.displayTimestamp
            preferences[PreferencesKeys.NAME_FORMAT] = settings.nameDisplayFormat.name
            preferences[PreferencesKeys.TIME_FORMAT] = settings.timeDisplayFormat.name
        }
    }
}