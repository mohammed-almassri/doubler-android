package com.example.doubler.feature.persona.data.remote.dto

data class PersonaResponseDto(
    val id: String,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val image_url: String? = null,
    val bio: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val deleted_at: String? = null
)