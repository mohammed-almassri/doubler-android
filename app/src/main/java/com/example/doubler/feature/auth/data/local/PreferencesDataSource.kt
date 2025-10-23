package com.example.doubler.feature.auth.data.local

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.doubler.feature.auth.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class PreferencesDataSource(private val context: Context) {
    suspend fun saveUser(user: User) {
        context.dataStore.edit { preferences: MutablePreferences ->
            preferences[DataStoreKeys.ID] = user.id
            preferences[DataStoreKeys.USER_NAME] = user.name
            preferences[DataStoreKeys.EMAIL] = user.email
            preferences[DataStoreKeys.TOKEN] = user.token
            preferences[DataStoreKeys.IMAGE_URL] = user.imageUrl ?: ""
        }
    }
    fun getUser(): Flow<User?> {
        return context
            .dataStore
            .data
            .map { preferences: Preferences ->
                val id = preferences[DataStoreKeys.ID]
                val username = preferences[DataStoreKeys.USER_NAME]
                val email = preferences[DataStoreKeys.EMAIL]
                val imageUrl = preferences[DataStoreKeys.IMAGE_URL]
                val token = preferences[DataStoreKeys.TOKEN]
                if (username != null
                    && id != null && token != null && email != null) {
                    User(id,username,email,imageUrl, token=token)
                } else {
                    null
                }
            }.distinctUntilChanged()
    }
    
    suspend fun clearUser() {
        context.dataStore.edit { preferences: MutablePreferences ->
            preferences.clear()
        }
    }
}