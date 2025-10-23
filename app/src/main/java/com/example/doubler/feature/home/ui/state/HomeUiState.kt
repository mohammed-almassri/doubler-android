package com.example.doubler.feature.home.ui.state

import com.example.doubler.feature.auth.domain.model.User

data class HomeUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)