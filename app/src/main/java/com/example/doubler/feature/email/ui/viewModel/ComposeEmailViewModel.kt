package com.example.doubler.feature.email.ui.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doubler.core.network.error.ApiErrorHandler
import com.example.doubler.core.network.error.ApiException
import com.example.doubler.feature.email.domain.model.Attachment
import com.example.doubler.feature.email.domain.repository.EmailRepository
import com.example.doubler.feature.email.ui.state.ComposeEmailUiState
import com.example.doubler.feature.persona.domain.model.Persona
import com.example.doubler.feature.persona.domain.repository.CurrentPersonaRepository
import com.example.doubler.feature.persona.domain.repository.PersonaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ComposeEmailViewModel(
    private val emailRepository: EmailRepository,
    private val currentPersonaRepository: CurrentPersonaRepository,
    private val personaRepository: PersonaRepository
) : ViewModel() {
    
    private val _composeUiState = MutableStateFlow(ComposeEmailUiState())
    val composeUiState: StateFlow<ComposeEmailUiState> = _composeUiState.asStateFlow()
    
    private val _personas = MutableStateFlow<List<Persona>>(emptyList())
    val personas: StateFlow<List<Persona>> = _personas.asStateFlow()
    
    private val _selectedPersona = MutableStateFlow<Persona?>(null)
    val selectedPersona: StateFlow<Persona?> = _selectedPersona.asStateFlow()
    
    private val _isLoadingPersonas = MutableStateFlow(false)
    val isLoadingPersonas: StateFlow<Boolean> = _isLoadingPersonas.asStateFlow()
    
    init {
        loadPersonas()
        loadCurrentPersona()
    }
    
    private fun loadPersonas() {
        viewModelScope.launch {
            _isLoadingPersonas.value = true
            try {
                val personaList = withContext(Dispatchers.IO) {
                    personaRepository.getPersonas()
                }
                _personas.value = personaList
            } catch (e: Exception) {
                Log.e("ComposeEmailViewModel", "Error loading personas", e)
                _composeUiState.value = _composeUiState.value.copy(
                    error = "Failed to load personas: ${e.message}"
                )
            } finally {
                _isLoadingPersonas.value = false
            }
        }
    }
    
    private fun loadCurrentPersona() {
        viewModelScope.launch {
            currentPersonaRepository.getCurrentPersona().collect { persona ->
                _selectedPersona.value = persona
            }
        }
    }
    
    fun selectPersona(persona: Persona) {
        viewModelScope.launch {
            try {
                currentPersonaRepository.setCurrentPersona(persona)
                _selectedPersona.value = persona
            } catch (e: Exception) {
                Log.e("ComposeEmailViewModel", "Error selecting persona", e)
                _composeUiState.value = _composeUiState.value.copy(
                    error = "Failed to select persona: ${e.message}"
                )
            }
        }
    }
    
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
                // Get the current persona ID
                val currentPersonaId = currentPersonaRepository.getCurrentPersonaId().first()
                if (currentPersonaId == null) {
                    _composeUiState.value = _composeUiState.value.copy(
                        isLoading = false,
                        error = "Please select a persona before sending emails",
                        isSuccess = false,
                        successMessage = null
                    )
                    return@launch
                }
                
                Log.d("ComposeEmailViewModel", "Attempting to send email to: $to using persona: $currentPersonaId")
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
                        personaId = currentPersonaId,
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