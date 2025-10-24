package com.example.doubler.feature.persona.domain.repository

import com.example.doubler.feature.persona.domain.model.Persona
import kotlinx.coroutines.flow.Flow

interface CurrentPersonaRepository {
    fun getCurrentPersona(): Flow<Persona?>
    suspend fun setCurrentPersona(persona: Persona)
    suspend fun setCurrentPersonaById(personaId: String)
    suspend fun clearCurrentPersona()
    fun getCurrentPersonaId(): Flow<String?>
}