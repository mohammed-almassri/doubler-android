package com.example.doubler.feature.email.ui.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doubler.core.network.error.ApiErrorHandler
import com.example.doubler.core.network.error.ApiException
import com.example.doubler.feature.email.domain.repository.EmailRepository
import com.example.doubler.feature.email.ui.state.EmailListUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EmailListViewModel(
    private val emailRepository: EmailRepository
) : ViewModel() {
    
    private val _inboxUiState = MutableStateFlow(EmailListUiState())
    val inboxUiState: StateFlow<EmailListUiState> = _inboxUiState.asStateFlow()
    
    private val _outboxUiState = MutableStateFlow(EmailListUiState())
    val outboxUiState: StateFlow<EmailListUiState> = _outboxUiState.asStateFlow()
    
    private val _draftsUiState = MutableStateFlow(EmailListUiState())
    val draftsUiState: StateFlow<EmailListUiState> = _draftsUiState.asStateFlow()
    
    private val _starredUiState = MutableStateFlow(EmailListUiState())
    val starredUiState: StateFlow<EmailListUiState> = _starredUiState.asStateFlow()
    
    fun loadInbox(
        search: String? = null,
        from: String? = null,
        status: String? = null,
        isStarred: Boolean? = null,
        refresh: Boolean = false
    ) {
        if (refresh) {
            _inboxUiState.value = _inboxUiState.value.copy(isRefreshing = true)
        } else {
            _inboxUiState.value = _inboxUiState.value.copy(isLoading = true)
        }
        
        viewModelScope.launch {
            try {
                Log.d("EmailListViewModel", "Loading inbox emails")
                val emails = withContext(Dispatchers.IO) {
                    emailRepository.getInbox(search, from, status, isStarred)
                }
                Log.d("EmailListViewModel", "Successfully loaded ${emails.size} inbox emails")
                _inboxUiState.value = _inboxUiState.value.copy(
                    emails = emails,
                    isLoading = false,
                    isRefreshing = false,
                    error = null
                )
            } catch (e: ApiException) {
                val userFriendlyMessage = ApiErrorHandler.getUserFriendlyMessage(e)
                Log.e("EmailListViewModel", "API error while loading inbox", e)
                _inboxUiState.value = _inboxUiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = userFriendlyMessage
                )
            } catch (e: Exception) {
                Log.e("EmailListViewModel", "Unexpected error while loading inbox", e)
                _inboxUiState.value = _inboxUiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = "Unable to load inbox. Please try again."
                )
            }
        }
    }
    
    fun loadOutbox(
        search: String? = null,
        status: String? = null,
        isStarred: Boolean? = null,
        refresh: Boolean = false
    ) {
        if (refresh) {
            _outboxUiState.value = _outboxUiState.value.copy(isRefreshing = true)
        } else {
            _outboxUiState.value = _outboxUiState.value.copy(isLoading = true)
        }
        
        viewModelScope.launch {
            try {
                Log.d("EmailListViewModel", "Loading outbox emails")
                val emails = withContext(Dispatchers.IO) {
                    emailRepository.getOutbox(search, status, isStarred)
                }
                Log.d("EmailListViewModel", "Successfully loaded ${emails.size} outbox emails")
                _outboxUiState.value = _outboxUiState.value.copy(
                    emails = emails,
                    isLoading = false,
                    isRefreshing = false,
                    error = null
                )
            } catch (e: ApiException) {
                val userFriendlyMessage = ApiErrorHandler.getUserFriendlyMessage(e)
                Log.e("EmailListViewModel", "API error while loading outbox", e)
                _outboxUiState.value = _outboxUiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = userFriendlyMessage
                )
            } catch (e: Exception) {
                Log.e("EmailListViewModel", "Unexpected error while loading outbox", e)
                _outboxUiState.value = _outboxUiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = "Unable to load outbox. Please try again."
                )
            }
        }
    }
    
    fun loadDrafts(
        search: String? = null,
        refresh: Boolean = false
    ) {
        if (refresh) {
            _draftsUiState.value = _draftsUiState.value.copy(isRefreshing = true)
        } else {
            _draftsUiState.value = _draftsUiState.value.copy(isLoading = true)
        }
        
        viewModelScope.launch {
            try {
                Log.d("EmailListViewModel", "Loading draft emails")
                val emails = withContext(Dispatchers.IO) {
                    emailRepository.getDrafts(search)
                }
                Log.d("EmailListViewModel", "Successfully loaded ${emails.size} draft emails")
                _draftsUiState.value = _draftsUiState.value.copy(
                    emails = emails,
                    isLoading = false,
                    isRefreshing = false,
                    error = null
                )
            } catch (e: ApiException) {
                val userFriendlyMessage = ApiErrorHandler.getUserFriendlyMessage(e)
                Log.e("EmailListViewModel", "API error while loading drafts", e)
                _draftsUiState.value = _draftsUiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = userFriendlyMessage
                )
            } catch (e: Exception) {
                Log.e("EmailListViewModel", "Unexpected error while loading drafts", e)
                _draftsUiState.value = _draftsUiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = "Unable to load drafts. Please try again."
                )
            }
        }
    }
    
    fun loadStarred(
        search: String? = null,
        refresh: Boolean = false
    ) {
        if (refresh) {
            _starredUiState.value = _starredUiState.value.copy(isRefreshing = true)
        } else {
            _starredUiState.value = _starredUiState.value.copy(isLoading = true)
        }
        
        viewModelScope.launch {
            try {
                Log.d("EmailListViewModel", "Loading starred emails")
                val emails = withContext(Dispatchers.IO) {
                    emailRepository.getStarred(search)
                }
                Log.d("EmailListViewModel", "Successfully loaded ${emails.size} starred emails")
                _starredUiState.value = _starredUiState.value.copy(
                    emails = emails,
                    isLoading = false,
                    isRefreshing = false,
                    error = null
                )
            } catch (e: ApiException) {
                val userFriendlyMessage = ApiErrorHandler.getUserFriendlyMessage(e)
                Log.e("EmailListViewModel", "API error while loading starred emails", e)
                _starredUiState.value = _starredUiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = userFriendlyMessage
                )
            } catch (e: Exception) {
                Log.e("EmailListViewModel", "Unexpected error while loading starred emails", e)
                _starredUiState.value = _starredUiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = "Unable to load starred emails. Please try again."
                )
            }
        }
    }
    
    fun toggleStar(emailId: String, currentState: MutableStateFlow<EmailListUiState>) {
        viewModelScope.launch {
            try {
                Log.d("EmailListViewModel", "Toggling star for email $emailId")
                withContext(Dispatchers.IO) {
                    emailRepository.toggleStar(emailId)
                }
                Log.d("EmailListViewModel", "Successfully toggled star for email $emailId")
                // Refresh the current list to update star status
                when (currentState) {
                    _inboxUiState -> loadInbox(refresh = true)
                    _outboxUiState -> loadOutbox(refresh = true)
                    _draftsUiState -> loadDrafts(refresh = true)
                    _starredUiState -> loadStarred(refresh = true)
                }
            } catch (e: ApiException) {
                val userFriendlyMessage = ApiErrorHandler.getUserFriendlyMessage(e)
                Log.e("EmailListViewModel", "API error while toggling star for email $emailId", e)
                currentState.value = currentState.value.copy(
                    error = userFriendlyMessage
                )
            } catch (e: Exception) {
                Log.e("EmailListViewModel", "Unexpected error while toggling star for email $emailId", e)
                currentState.value = currentState.value.copy(
                    error = "Unable to update email. Please try again."
                )
            }
        }
    }
    
    fun deleteEmail(emailId: String, currentState: MutableStateFlow<EmailListUiState>) {
        viewModelScope.launch {
            try {
                Log.d("EmailListViewModel", "Deleting email $emailId")
                withContext(Dispatchers.IO) {
                    emailRepository.deleteEmail(emailId)
                }
                Log.d("EmailListViewModel", "Successfully deleted email $emailId")
                // Refresh the current list to remove deleted email
                when (currentState) {
                    _inboxUiState -> loadInbox(refresh = true)
                    _outboxUiState -> loadOutbox(refresh = true)
                    _draftsUiState -> loadDrafts(refresh = true)
                    _starredUiState -> loadStarred(refresh = true)
                }
            } catch (e: ApiException) {
                val userFriendlyMessage = ApiErrorHandler.getUserFriendlyMessage(e)
                Log.e("EmailListViewModel", "API error while deleting email $emailId", e)
                currentState.value = currentState.value.copy(
                    error = userFriendlyMessage
                )
            } catch (e: Exception) {
                Log.e("EmailListViewModel", "Unexpected error while deleting email $emailId", e)
                currentState.value = currentState.value.copy(
                    error = "Unable to delete email. Please try again."
                )
            }
        }
    }
}