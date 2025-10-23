package com.example.doubler.feature.persona.data.mapper

import com.example.doubler.feature.persona.data.remote.dto.PersonaResponseDto
import com.example.doubler.feature.persona.domain.model.Persona

object PersonaMapper {
    
    fun mapToPersona(dto: PersonaResponseDto): Persona {
        return Persona(
            id = dto.id,
            name = dto.name,
            email = dto.email,
            phone = dto.phone,
            imageUrl = dto.image_url,
            bio = dto.bio,
            createdAt = dto.created_at,
            updatedAt = dto.updated_at,
            deletedAt = dto.deleted_at
        )
    }
    
    fun mapToPersonaList(dtos: List<PersonaResponseDto>): List<Persona> {
        return dtos.map { mapToPersona(it) }
    }
}