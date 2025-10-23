package com.example.doubler.feature.persona.domain.model

data class Persona(
    val id: String,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val imageUrl: String? = null,
    val bio: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val deletedAt: String? = null
)