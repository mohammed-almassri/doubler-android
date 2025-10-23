package com.example.doubler.feature.auth.ui.state

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
)