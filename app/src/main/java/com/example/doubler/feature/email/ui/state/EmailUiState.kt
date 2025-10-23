package com.example.doubler.feature.email.ui.state

import com.example.doubler.feature.email.domain.model.Email

data class EmailListUiState(
    val emails: List<Email> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
)

data class EmailDetailUiState(
    val email: Email? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ComposeEmailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val successMessage: String? = null
)