package com.example.doubler.feature.persona.data.local.datasource

import com.example.doubler.feature.persona.data.local.dao.PersonaDao
import com.example.doubler.feature.persona.data.local.mapper.PersonaEntityMapper
import com.example.doubler.feature.persona.domain.model.Persona

class PersonaLocalDataSource(
    private val personaDao: PersonaDao,
) {
    
    suspend fun findPersona(): Persona? {
        val emailEntity = personaDao.findPersona()
        return emailEntity?.let { PersonaEntityMapper.toDomain(it) }
    }

    suspend fun insertPersona(persona: Persona) {
        val personaEntity = PersonaEntityMapper.toEntity(persona)
        personaDao.insertPersona(personaEntity)
    }
    
    suspend fun clearAllPersonas() {
        personaDao.clearAllPersonas()
    }
}