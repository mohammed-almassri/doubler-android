package com.example.doubler.feature.persona.data.remote.dto

data class PersonaListResponseDto(
    val data: List<PersonaResponseDto>,
    val links: LinksDto? = null,
    val meta: MetaDto? = null
)

data class PersonaDetailResponseDto(
    val data: PersonaResponseDto
)

data class LinksDto(
    val first: String? = null,
    val last: String? = null,
    val prev: String? = null,
    val next: String? = null
)

data class MetaDto(
    val current_page: Int,
    val from: Int? = null,
    val last_page: Int,
    val per_page: Int,
    val to: Int? = null,
    val total: Int
)