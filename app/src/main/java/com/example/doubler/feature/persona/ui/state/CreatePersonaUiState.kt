package com.example.doubler.feature.persona.ui.state

import com.example.doubler.feature.persona.domain.model.Persona

data class CreatePersonaUiState(
    val isLoading: Boolean = false,
    val isGeneratingImage: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val generatedImageUrl: String? = null,
    val createdPersona: Persona? = null
)