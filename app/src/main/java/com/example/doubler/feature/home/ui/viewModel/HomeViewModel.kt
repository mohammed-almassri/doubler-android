package com.example.doubler.feature.home.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doubler.core.auth.domain.repository.LogoutRepository
import com.example.doubler.core.user.domain.repository.UserRepository
import com.example.doubler.feature.home.ui.state.HomeUiState
import com.example.doubler.feature.persona.domain.repository.CurrentPersonaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository,
    private val currentPersonaRepository: CurrentPersonaRepository,
    private val logoutRepository: LogoutRepository
) : ViewModel() {
    
    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()
    
    init {
        loadUserAndPersona()
    }
    
    private fun loadUserAndPersona() {
        viewModelScope.launch {
            combine(
                userRepository.getCurrentUser(),
                currentPersonaRepository.getCurrentPersona()
            ) { user, persona ->
                HomeUiState(
                    user = user,
                    currentPersona = persona,
                    isLoading = false,
                    error = if (user == null) "No user found" else null
                )
            }.collect { newState ->
                _homeUiState.value = newState
            }
        }
    }
    
    fun setCurrentPersona(personaId: String) {
        viewModelScope.launch {
            try {
                currentPersonaRepository.setCurrentPersonaById(personaId)
            } catch (e: Exception) {
                _homeUiState.value = _homeUiState.value.copy(
                    error = "Failed to set current persona: ${e.message}"
                )
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            try {
                // Use the comprehensive logout repository to clear all user data
                logoutRepository.logout()
                
                _homeUiState.value = _homeUiState.value.copy(
                    user = null,
                    currentPersona = null,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _homeUiState.value = _homeUiState.value.copy(
                    error = "Failed to logout: ${e.message}"
                )
            }
        }
    }
}