package com.example.doubler.feature.email.ui.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doubler.core.network.error.ApiErrorHandler
import com.example.doubler.core.network.error.ApiException
import com.example.doubler.feature.email.domain.repository.EmailRepository
import com.example.doubler.feature.email.ui.state.EmailDetailUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EmailDetailViewModel(
    private val emailRepository: EmailRepository
) : ViewModel() {
    
    private val _emailDetailUiState = MutableStateFlow(EmailDetailUiState())
    val emailDetailUiState: StateFlow<EmailDetailUiState> = _emailDetailUiState.asStateFlow()
    
    fun loadEmail(emailId: String) {
        _emailDetailUiState.value = _emailDetailUiState.value.copy(
            isLoading = true,
            error = null
        )
        
        viewModelScope.launch {
            try {
                Log.d("EmailDetailViewModel", "Loading email with ID: $emailId")
                val email = withContext(Dispatchers.IO) {
                    emailRepository.getEmail(emailId)
                }
                Log.d("EmailDetailViewModel", "Successfully loaded email: ${email.subject}")
                _emailDetailUiState.value = _emailDetailUiState.value.copy(
                    email = email,
                    isLoading = false,
                    error = null
                )
            } catch (e: ApiException) {
                val userFriendlyMessage = ApiErrorHandler.getUserFriendlyMessage(e)
                Log.e("EmailDetailViewModel", "API error while loading email $emailId", e)
                _emailDetailUiState.value = _emailDetailUiState.value.copy(
                    isLoading = false,
                    error = userFriendlyMessage
                )
            } catch (e: Exception) {
                Log.e("EmailDetailViewModel", "Unexpected error while loading email $emailId", e)
                _emailDetailUiState.value = _emailDetailUiState.value.copy(
                    isLoading = false,
                    error = "Unable to load email. Please try again."
                )
            }
        }
    }
    
    fun toggleStar(emailId: String) {
        viewModelScope.launch {
            try {
                Log.d("EmailDetailViewModel", "Toggling star for email $emailId")
                val updatedEmail = withContext(Dispatchers.IO) {
                    emailRepository.toggleStar(emailId)
                }
                Log.d("EmailDetailViewModel", "Successfully toggled star for email $emailId")
                _emailDetailUiState.value = _emailDetailUiState.value.copy(
                    email = updatedEmail
                )
            } catch (e: ApiException) {
                val userFriendlyMessage = ApiErrorHandler.getUserFriendlyMessage(e)
                Log.e("EmailDetailViewModel", "API error while toggling star for email $emailId", e)
                _emailDetailUiState.value = _emailDetailUiState.value.copy(
                    error = userFriendlyMessage
                )
            } catch (e: Exception) {
                Log.e("EmailDetailViewModel", "Unexpected error while toggling star for email $emailId", e)
                _emailDetailUiState.value = _emailDetailUiState.value.copy(
                    error = "Unable to update email. Please try again."
                )
            }
        }
    }
    
    fun deleteEmail(emailId: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("EmailDetailViewModel", "Deleting email $emailId")
                withContext(Dispatchers.IO) {
                    emailRepository.deleteEmail(emailId)
                }
                Log.d("EmailDetailViewModel", "Successfully deleted email $emailId")
                onDeleted()
            } catch (e: ApiException) {
                val userFriendlyMessage = ApiErrorHandler.getUserFriendlyMessage(e)
                Log.e("EmailDetailViewModel", "API error while deleting email $emailId", e)
                _emailDetailUiState.value = _emailDetailUiState.value.copy(
                    error = userFriendlyMessage
                )
            } catch (e: Exception) {
                Log.e("EmailDetailViewModel", "Unexpected error while deleting email $emailId", e)
                _emailDetailUiState.value = _emailDetailUiState.value.copy(
                    error = "Unable to delete email. Please try again."
                )
            }
        }
    }
}