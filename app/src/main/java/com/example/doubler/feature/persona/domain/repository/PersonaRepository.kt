package com.example.doubler.feature.persona.domain.repository

import com.example.doubler.feature.persona.domain.model.Persona

interface PersonaRepository {
    suspend fun getPersonas(
        search: String? = null,
        perPage: Int = 15,
        page: Int = 1,
        withTrashed: Boolean = false
    ): List<Persona>
    
    suspend fun getPersonaById(id: String, withTrashed: Boolean = false): Persona?
    
    suspend fun createPersona(
        name: String,
        email: String? = null,
        phone: String? = null,
        imageUrl: String? = null,
        bio: String? = null
    ): Persona
    
    suspend fun updatePersona(
        id: String,
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        imageUrl: String? = null,
        bio: String? = null
    ): Persona
    
    suspend fun deletePersona(id: String): Boolean
    
    suspend fun restorePersona(id: String): Persona
    
    suspend fun forceDeletePersona(id: String): Boolean
    
    suspend fun getTrashedPersonas(
        search: String? = null,
        perPage: Int = 15,
        page: Int = 1
    ): List<Persona>
    
    suspend fun generateImage(prompt: String): String // Returns image URL
}