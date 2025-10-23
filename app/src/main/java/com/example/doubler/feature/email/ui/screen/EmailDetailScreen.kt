package com.example.doubler.feature.email.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doubler.feature.email.data.repository.EmailRepositoryImpl
import com.example.doubler.feature.email.ui.viewModel.EmailDetailViewModel
import com.example.notthefinal.core.network.ApiProvider
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailDetailScreen(
    emailId: String,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onNavigateToCompose: (replyTo: String?, subject: String?) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val viewModel: EmailDetailViewModel = viewModel {
        val database = com.example.doubler.feature.email.data.local.database.EmailDatabase.getDatabase(context)
        val localDataSource = com.example.doubler.feature.email.data.local.datasource.EmailLocalDataSource(
            emailDao = database.emailDao(),
            recipientDao = database.emailRecipientDao(),
            senderDao = database.emailSenderDao()
        )
        val networkObserver = com.example.doubler.core.network.connectivity.NetworkConnectivityObserver(context)
        
        EmailDetailViewModel(
            emailRepository = EmailRepositoryImpl(
                emailApiService = ApiProvider.getInstance(context).emailApiService,
                localDataSource = localDataSource,
                networkObserver = networkObserver
            )
        )
    }

    val uiState by viewModel.emailDetailUiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Load email when screen loads
    LaunchedEffect(emailId) {
        viewModel.loadEmail(emailId)
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("Email") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                uiState.email?.let { email ->
                    IconButton(
                        onClick = {
                            viewModel.toggleStar(emailId)
                        }
                    ) {
                        Icon(
                            imageVector = if (email.isStarred) Icons.Default.Star else Icons.Default.Email,
                            contentDescription = if (email.isStarred) "Unstar" else "Star",
                            tint = if (email.isStarred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            onNavigateToCompose(
                                email.messageId,
                                "Re: ${email.subject}"
                            )
                        }
                    ) {
                        Icon(Icons.Default.Email, contentDescription = "Reply")
                    }
                }
            }
        )

        // Content
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                ErrorContent(
                    error = uiState.error!!,
                    onRetry = { viewModel.loadEmail(emailId) }
                )
            }
            uiState.email != null -> {
                EmailContent(
                    email = uiState.email!!,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EmailContent(
    email: com.example.doubler.feature.email.domain.model.Email,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Email Header
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = email.subject?:"",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "From: ${email.fromName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = email.fromEmail?:"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if(email.toEmails!=null)
                        Text(
                            text = "To: ${email.toEmails.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        email.ccEmails?.takeIf { it.isNotEmpty() }?.let { ccEmails ->
                            Text(
                                text = "CC: ${ccEmails.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                            .format(email.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Email Body
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = email.bodyPlain?:"",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        
        // Attachments
        email.attachments?.takeIf { it.isNotEmpty() }?.let { attachments ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Attachments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    attachments.forEach { attachment ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Attachment",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = attachment.name?:"",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
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