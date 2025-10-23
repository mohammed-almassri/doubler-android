package com.example.doubler.feature.auth.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doubler.feature.auth.domain.repository.AuthRepository
import com.example.doubler.feature.auth.ui.state.RegisterUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterViewModel(val authRepository: AuthRepository,val onRegisterSuccess: () -> Unit = {}): ViewModel() {
    private val _registerUiState = MutableStateFlow(RegisterUiState(isLoading = false))
    val registerUiState: StateFlow<RegisterUiState> = _registerUiState.asStateFlow()

     fun register(
        name: String,
        email: String,
        password: String,
    ){
        _registerUiState.value = _registerUiState.value.copy(
            isLoading = true,
            error = null
        )
        
        viewModelScope.launch {
            try {
                val res = withContext(Dispatchers.IO){
                    authRepository.register(name,email,password,"Android Device")
                }
                _registerUiState.value = _registerUiState.value.copy(
                    isLoading = false,
                    error = null,
                )
                onRegisterSuccess()
            } catch (e: Exception) {
                _registerUiState.value = _registerUiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Registration failed. Please try again.",
                )
            }
        }
    }

}