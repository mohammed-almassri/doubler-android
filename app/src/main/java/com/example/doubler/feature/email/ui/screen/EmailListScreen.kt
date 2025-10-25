package com.example.doubler.feature.email.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doubler.feature.email.data.repository.EmailRepositoryImpl
import com.example.doubler.feature.email.domain.model.Email
import com.example.doubler.feature.email.ui.state.EmailListUiState
import com.example.doubler.feature.email.ui.viewModel.EmailListViewModel
import com.example.notthefinal.core.network.ApiProvider
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

enum class EmailListType {
    INBOX, OUTBOX, DRAFTS, STARRED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailListScreen(
    listType: EmailListType,
    modifier: Modifier = Modifier,
    onNavigateToCompose: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: EmailListViewModel = viewModel {
        val database = com.example.doubler.feature.email.data.local.database.EmailDatabase.getDatabase(context)
        val localDataSource = com.example.doubler.feature.email.data.local.datasource.EmailLocalDataSource(
            emailDao = database.emailDao(),
            recipientDao = database.emailRecipientDao(),
            senderDao = database.emailSenderDao()
        )
        val networkObserver = com.example.doubler.core.network.connectivity.NetworkConnectivityObserver(context)
        
        EmailListViewModel(
            emailRepository = EmailRepositoryImpl(
                emailApiService = ApiProvider.getInstance(context).emailApiService,
                localDataSource = localDataSource,
                networkObserver = networkObserver
            )
        )
    }

    val uiState = when (listType) {
        EmailListType.INBOX -> viewModel.inboxUiState.collectAsStateWithLifecycle()
        EmailListType.OUTBOX -> viewModel.outboxUiState.collectAsStateWithLifecycle()
        EmailListType.DRAFTS -> viewModel.draftsUiState.collectAsStateWithLifecycle()
        EmailListType.STARRED -> viewModel.starredUiState.collectAsStateWithLifecycle()
    }

    val title = when (listType) {
        EmailListType.INBOX -> "Inbox"
        EmailListType.OUTBOX -> "Sent"
        EmailListType.DRAFTS -> "Drafts"
        EmailListType.STARRED -> "Starred"
    }

    // Load emails when screen loads
    LaunchedEffect(listType) {
        when (listType) {
            EmailListType.INBOX -> viewModel.loadInbox()
            EmailListType.OUTBOX -> viewModel.loadOutbox()
            EmailListType.DRAFTS -> viewModel.loadDrafts()
            EmailListType.STARRED -> viewModel.loadStarred()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (listType != EmailListType.STARRED) {
                    IconButton(onClick = onNavigateToCompose) {
                        Icon(Icons.Default.Add, contentDescription = "Compose")
                    }
                }
                IconButton(
                    onClick = {
                        when (listType) {
                            EmailListType.INBOX -> viewModel.loadInbox(refresh = true)
                            EmailListType.OUTBOX -> viewModel.loadOutbox(refresh = true)
                            EmailListType.DRAFTS -> viewModel.loadDrafts(refresh = true)
                            EmailListType.STARRED -> viewModel.loadStarred(refresh = true)
                        }
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        )

        // Content
        when {
            uiState.value.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.value.error != null -> {
                ErrorContent(
                    error = uiState.value.error!!,
                    onRetry = {
                        when (listType) {
                            EmailListType.INBOX -> viewModel.loadInbox()
                            EmailListType.OUTBOX -> viewModel.loadOutbox()
                            EmailListType.DRAFTS -> viewModel.loadDrafts()
                            EmailListType.STARRED -> viewModel.loadStarred()
                        }
                    }
                )
            }
            uiState.value.emails.isEmpty() -> {
                EmptyContent(listType = listType)
            }
            else -> {
                EmailList(
                    emails = uiState.value.emails,
                    isRefreshing = uiState.value.isRefreshing,
                    onEmailClick = onNavigateToDetail,
                    onStarClick = { emailId ->
                        val currentState = when (listType) {
                            EmailListType.INBOX -> viewModel.inboxUiState
                            EmailListType.OUTBOX -> viewModel.outboxUiState
                            EmailListType.DRAFTS -> viewModel.draftsUiState
                            EmailListType.STARRED -> viewModel.starredUiState
                        }
                        viewModel.toggleStar(emailId, currentState as kotlinx.coroutines.flow.MutableStateFlow<EmailListUiState>)
                    },
                    onRefresh = {
                        when (listType) {
                            EmailListType.INBOX -> viewModel.loadInbox(refresh = true)
                            EmailListType.OUTBOX -> viewModel.loadOutbox(refresh = true)
                            EmailListType.DRAFTS -> viewModel.loadDrafts(refresh = true)
                            EmailListType.STARRED -> viewModel.loadStarred(refresh = true)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun EmailList(
    emails: List<Email>,
    isRefreshing: Boolean,
    onEmailClick: (String) -> Unit,
    onStarClick: (String) -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(emails) { email ->
            EmailItem(
                email = email,
                onClick = { onEmailClick(email.id) },
                onStarClick = { onStarClick(email.id) }
            )
        }
    }
}

@Composable
private fun EmailItem(
    email: Email,
    onClick: () -> Unit,
    onStarClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.Top
//                ) {
//                    Text(
//                        text = email.fromName?:"",
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = if (!email.isRead) FontWeight.Bold else FontWeight.Normal,
//                        modifier = Modifier.weight(1f),
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                    if(email.createdAt != null)
//                    Text(
//                        text = formatDate(email.createdAt),
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(4.dp))
//
                Text(
                    text = email.subject?:"",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (!email.isRead) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = if(email.body!=null ) email.body.replace(Regex("<[^>]*>"), "") else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (email.attachments?.isNotEmpty() == true) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Has attachments",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${email.attachments.size} attachment${if (email.attachments.size > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
            
            IconButton(onClick = onStarClick) {
                Icon(
                    imageVector = if (email.isStarred) Icons.Default.Star else Icons.Default.Email,
                    contentDescription = if (email.isStarred) "Unstar" else "Star",
                    tint = if (email.isStarred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyContent(listType: EmailListType) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val (icon, message) = when (listType) {
                EmailListType.INBOX -> Icons.Default.Email to "No emails in your inbox"
                EmailListType.OUTBOX -> Icons.Default.Send to "No sent emails"
                EmailListType.DRAFTS -> Icons.Default.Email to "No draft emails"
                EmailListType.STARRED -> Icons.Default.Star to "No starred emails"
            }
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Error",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

private fun formatDate(date: Date): String {
    val now = Date()
    val diffInMillis = now.time - date.time
    val diffInHours = diffInMillis / (1000 * 60 * 60)
    
    return when {
        diffInHours < 24 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        diffInHours < 24 * 7 -> SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
    }
}