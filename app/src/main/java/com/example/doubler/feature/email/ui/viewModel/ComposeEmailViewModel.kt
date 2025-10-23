package com.example.doubler.feature.email.ui.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doubler.core.network.error.ApiErrorHandler
import com.example.doubler.core.network.error.ApiException
import com.example.doubler.feature.email.domain.model.Attachment
import com.example.doubler.feature.email.domain.repository.EmailRepository
import com.example.doubler.feature.email.ui.state.ComposeEmailUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ComposeEmailViewModel(
    private val emailRepository: EmailRepository
) : ViewModel() {
    
    private val _composeUiState = MutableStateFlow(ComposeEmailUiState())
    val composeUiState: StateFlow<ComposeEmailUiState> = _composeUiState.asStateFlow()
    
    fun sendEmail(
        to: List<String>,
        cc: List<String>? = null,
        bcc: List<String>? = null,
        subject: String,
        body: String,
        bodyPlain: String? = null,
        isDraft: Boolean = false,
        inReplyTo: String? = null,
        attachments: List<Attachment>? = null
    ) {
        _composeUiState.value = _composeUiState.value.copy(
            isLoading = true,
            error = null,
            isSuccess = false,
            successMessage = null
        )
        
        viewModelScope.launch {
            try {
                Log.d("ComposeEmailViewModel", "Attempting to send email to: $to")
                val email = withContext(Dispatchers.IO) {
                    emailRepository.sendEmail(
                        to = to,
                        cc = cc,
                        bcc = bcc,
                        subject = subject,
                        body = body,
                        bodyPlain = bodyPlain,
                        isDraft = isDraft,
                        inReplyTo = inReplyTo,
                        attachments = attachments
                    )
                }
                
                val successMessage = if (isDraft) "Email saved as draft" else "Email sent successfully"
                Log.d("ComposeEmailViewModel", successMessage)
                
                _composeUiState.value = _composeUiState.value.copy(
                    isLoading = false,
                    error = null,
                    isSuccess = true,
                    successMessage = successMessage
                )
            } catch (e: ApiException) {
                val userFriendlyMessage = ApiErrorHandler.getUserFriendlyMessage(e)
                Log.e("ComposeEmailViewModel", "API error while sending email", e)
                
                _composeUiState.value = _composeUiState.value.copy(
                    isLoading = false,
                    error = userFriendlyMessage,
                    isSuccess = false,
                    successMessage = null
                )
            } catch (e: Exception) {
                Log.e("ComposeEmailViewModel", "Unexpected error while sending email", e)
                
                _composeUiState.value = _composeUiState.value.copy(
                    isLoading = false,
                    error = "An unexpected error occurred. Please try again.",
                    isSuccess = false,
                    successMessage = null
                )
            }
        }
    }
    
    fun clearState() {
        _composeUiState.value = ComposeEmailUiState()
    }
}