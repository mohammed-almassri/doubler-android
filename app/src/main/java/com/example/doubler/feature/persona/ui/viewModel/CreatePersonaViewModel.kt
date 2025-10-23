package com.example.doubler.feature.persona.ui.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doubler.core.network.error.ApiErrorHandler
import com.example.doubler.core.network.error.ApiException
import com.example.doubler.feature.persona.domain.repository.PersonaRepository
import com.example.doubler.feature.persona.ui.state.CreatePersonaUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreatePersonaViewModel(
    private val personaRepository: PersonaRepository,
    private val onPersonaCreated: () -> Unit
) : ViewModel() {
    
    private val _createPersonaUiState = MutableStateFlow(CreatePersonaUiState())
    val createPersonaUiState: StateFlow<CreatePersonaUiState> = _createPersonaUiState.asStateFlow()
    
    fun generateImage(prompt: String) {
        _createPersonaUiState.value = _createPersonaUiState.value.copy(
            isGeneratingImage = true,
            error = null
        )
        
        viewModelScope.launch {
            try {
                Log.d("CreatePersonaViewModel", "Generating image for prompt: $prompt")
                val imageUrl = withContext(Dispatchers.IO) {
                    personaRepository.generateImage(prompt)
                }
                
                Log.d("CreatePersonaViewModel", "Successfully generated image")
                _createPersonaUiState.value = _createPersonaUiState.value.copy(
                    isGeneratingImage = false,
                    error = null,
                    generatedImageUrl = imageUrl
                )
            } catch (e: ApiException) {
                val userFriendlyMessage = ApiErrorHandler.getUserFriendlyMessage(e)
                Log.e("CreatePersonaViewModel", "API error while generating image", e)
                
                _createPersonaUiState.value = _createPersonaUiState.value.copy(
                    isGeneratingImage = false,
                    error = userFriendlyMessage
                )
            } catch (e: Exception) {
                Log.e("CreatePersonaViewModel", "Unexpected error while generating image", e)
                
                _createPersonaUiState.value = _createPersonaUiState.value.copy(
                    isGeneratingImage = false,
                    error = "Failed to generate image. Please try again."
                )
            }
        }
    }
    
    fun createPersona(
        name: String,
        bio: String,
        email: String? = null,
        phone: String? = null
    ) {
        _createPersonaUiState.value = _createPersonaUiState.value.copy(
            isLoading = true,
            error = null,
            isSuccess = false,
            successMessage = null
        )
        
        viewModelScope.launch {
            try {
                Log.d("CreatePersonaViewModel", "Creating persona: $name")
                val imageUrl = _createPersonaUiState.value.generatedImageUrl
                
                val persona = withContext(Dispatchers.IO) {
                    personaRepository.createPersona(
                        name = name,
                        email = email,
                        phone = phone,
                        imageUrl = imageUrl,
                        bio = bio
                    )
                }
                
                Log.d("CreatePersonaViewModel", "Successfully created persona: ${persona.name}")
                
                _createPersonaUiState.value = _createPersonaUiState.value.copy(
                    isLoading = false,
                    error = null,
                    isSuccess = true,
                    successMessage = "Persona created successfully!",
                    createdPersona = persona
                )
                
                // Navigate to next screen
                onPersonaCreated()
                
            } catch (e: ApiException) {
                val userFriendlyMessage = ApiErrorHandler.getUserFriendlyMessage(e)
                Log.e("CreatePersonaViewModel", "API error while creating persona", e)
                
                _createPersonaUiState.value = _createPersonaUiState.value.copy(
                    isLoading = false,
                    error = userFriendlyMessage,
                    isSuccess = false,
                    successMessage = null
                )
            } catch (e: Exception) {
                Log.e("CreatePersonaViewModel", "Unexpected error while creating persona", e)
                
                _createPersonaUiState.value = _createPersonaUiState.value.copy(
                    isLoading = false,
                    error = "An unexpected error occurred. Please try again.",
                    isSuccess = false,
                    successMessage = null
                )
            }
        }
    }
    
    fun clearError() {
        _createPersonaUiState.value = _createPersonaUiState.value.copy(error = null)
    }
    
    fun clearState() {
        _createPersonaUiState.value = CreatePersonaUiState()
    }
}