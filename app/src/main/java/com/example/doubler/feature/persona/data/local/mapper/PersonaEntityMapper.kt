package com.example.doubler.feature.persona.data.local.mapper

import com.example.doubler.feature.persona.data.local.entity.PersonaEntity
import com.example.doubler.feature.persona.domain.model.Persona

object PersonaEntityMapper {
    fun toEntity(persona: Persona): PersonaEntity{
        return PersonaEntity(
            id = persona.id,
            name = persona.name,
            email = persona.email,
            bio = persona.bio,
            phone = persona.phone,
        )
    }

    fun toDomain(entity: PersonaEntity): Persona{
        return Persona(
            id = entity.id,
            name = entity.name,
            email = entity.email,
            bio = entity.bio,
            phone = entity.phone,
        )
    }
}