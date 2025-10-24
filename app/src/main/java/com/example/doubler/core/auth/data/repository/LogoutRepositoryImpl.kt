package com.example.doubler.core.auth.data.repository

import android.util.Log
import com.example.doubler.core.auth.domain.repository.LogoutRepository
import com.example.doubler.core.user.domain.repository.UserRepository
import com.example.doubler.feature.email.data.local.datasource.EmailLocalDataSource
import com.example.doubler.feature.persona.data.local.PersonaPreferencesDataSource
import com.example.doubler.feature.persona.data.local.datasource.PersonaLocalDataSource

class LogoutRepositoryImpl(
    private val userRepository: UserRepository,
    private val emailLocalDataSource: EmailLocalDataSource,
    private val personaLocalDataSource: PersonaLocalDataSource,
    private val personaPreferencesDataSource: PersonaPreferencesDataSource
) : LogoutRepository {
    
    override suspend fun logout() {
        try {
            Log.d("LogoutRepository", "Starting comprehensive logout process...")
            
            // Clear user preferences and auth data
            Log.d("LogoutRepository", "Clearing user authentication data...")
            userRepository.clearUser()
            
            // Clear current persona preference
            Log.d("LogoutRepository", "Clearing current persona preference...")
            personaPreferencesDataSource.clearCurrentPersona()
            
            // Clear all email data (emails, recipients, senders)
            Log.d("LogoutRepository", "Clearing all email data...")
            emailLocalDataSource.clearAllEmails()
            
            // Clear all persona data
            Log.d("LogoutRepository", "Clearing all persona data...")
            personaLocalDataSource.clearAllPersonas()
            
            Log.d("LogoutRepository", "Logout process completed successfully")
            
        } catch (e: Exception) {
            Log.e("LogoutRepository", "Error during logout process", e)
            throw e
        }
    }
}