package com.example.doubler.feature.home.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doubler.core.user.domain.repository.UserRepository
import com.example.doubler.feature.home.ui.state.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()
    
    init {
        loadUser()
    }
    
    private fun loadUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser().collect { user ->
                _homeUiState.value = _homeUiState.value.copy(
                    user = user,
                    isLoading = false,
                    error = if (user == null) "No user found" else null
                )
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            try {
                userRepository.clearUser()
                _homeUiState.value = _homeUiState.value.copy(
                    user = null,
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