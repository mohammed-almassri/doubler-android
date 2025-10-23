package com.example.doubler.feature.persona.ui.state

import com.example.doubler.feature.persona.domain.model.Persona

data class PersonaListUiState(
    val personas: List<Persona> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)