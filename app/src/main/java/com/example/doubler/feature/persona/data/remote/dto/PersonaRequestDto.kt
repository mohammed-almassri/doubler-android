package com.example.doubler.feature.persona.data.remote.dto

data class CreatePersonaRequestDto(
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val image_url: String? = null,
    val bio: String? = null
)

data class UpdatePersonaRequestDto(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val image_url: String? = null,
    val bio: String? = null
)

data class GenerateImageRequestDto(
    val prompt: String
)

data class GenerateImageResponseDto(
    val message: String,
    val data: ImageDataDto
)

data class ImageDataDto(
    val url: String
)