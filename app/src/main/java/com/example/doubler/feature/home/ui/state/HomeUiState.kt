package com.example.doubler.feature.home.ui.state

import com.example.doubler.feature.auth.domain.model.User
import com.example.doubler.feature.persona.domain.model.Persona

data class HomeUiState(
    val user: User? = null,
    val currentPersona: Persona? = null,
    val personas: List<Persona> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)