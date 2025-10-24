package com.example.doubler.feature.persona.data.repository

import com.example.doubler.feature.persona.data.local.PersonaPreferencesDataSource
import com.example.doubler.feature.persona.domain.model.Persona
import com.example.doubler.feature.persona.domain.repository.CurrentPersonaRepository
import com.example.doubler.feature.persona.domain.repository.PersonaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CurrentPersonaRepositoryImpl(
    private val personaPreferencesDataSource: PersonaPreferencesDataSource,
    private val personaRepository: PersonaRepository
) : CurrentPersonaRepository {
    
    override fun getCurrentPersona(): Flow<Persona?> {
        return personaPreferencesDataSource.getCurrentPersonaId().map { personaId ->
            personaId?.let { id ->
                try {
                    personaRepository.getPersonaById(id)
                } catch (e: Exception) {
                    // If persona doesn't exist anymore, clear the preference
                    clearCurrentPersona()
                    null
                }
            }
        }
    }
    
    override suspend fun setCurrentPersona(persona: Persona) {
        personaPreferencesDataSource.setCurrentPersonaId(persona.id)
    }
    
    override suspend fun setCurrentPersonaById(personaId: String) {
        personaPreferencesDataSource.setCurrentPersonaId(personaId)
    }
    
    override suspend fun clearCurrentPersona() {
        personaPreferencesDataSource.clearCurrentPersona()
    }
    
    override fun getCurrentPersonaId(): Flow<String?> {
        return personaPreferencesDataSource.getCurrentPersonaId()
    }
}