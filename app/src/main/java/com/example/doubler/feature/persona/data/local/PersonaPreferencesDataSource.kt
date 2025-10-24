package com.example.doubler.feature.persona.data.local

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.doubler.feature.auth.data.local.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class PersonaPreferencesDataSource(private val context: Context) {
    
    private object PersonaKeys {
        val CURRENT_PERSONA_ID = stringPreferencesKey("current_persona_id")
    }
    
    suspend fun setCurrentPersonaId(personaId: String) {
        context.dataStore.edit { preferences: MutablePreferences ->
            preferences[PersonaKeys.CURRENT_PERSONA_ID] = personaId
        }
    }
    
    fun getCurrentPersonaId(): Flow<String?> {
        return context
            .dataStore
            .data
            .map { preferences: Preferences ->
                preferences[PersonaKeys.CURRENT_PERSONA_ID]
            }
            .distinctUntilChanged()
    }
    
    suspend fun clearCurrentPersona() {
        context.dataStore.edit { preferences: MutablePreferences ->
            preferences.remove(PersonaKeys.CURRENT_PERSONA_ID)
        }
    }
}