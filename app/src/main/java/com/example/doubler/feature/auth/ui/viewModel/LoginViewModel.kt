package com.example.doubler.feature.auth.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doubler.feature.auth.domain.repository.AuthRepository
import com.example.doubler.feature.auth.ui.state.LoginUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LoginViewModel(val authRepository: AuthRepository,val onLoginSuccess: () -> Unit = {}): ViewModel() {
    private val _loginUiState = MutableStateFlow(LoginUiState(isLoading = false))
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    fun login(
        email: String,
        password: String,
    ){
        _loginUiState.value = _loginUiState.value.copy(
            isLoading = true,
            error = null
        )
        
        viewModelScope.launch {
            try {
                val res = withContext(Dispatchers.IO){
                    authRepository.login(email, password, "Android Device")
                }
                _loginUiState.value = _loginUiState.value.copy(
                    isLoading = false,
                    error = null,
                )
                onLoginSuccess()
            } catch (e: Exception) {
                _loginUiState.value = _loginUiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Login failed. Please try again.",
                )
            }
        }
    }
}