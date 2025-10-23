package com.example.doubler.feature.email.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doubler.feature.email.data.repository.EmailRepositoryImpl
import com.example.doubler.feature.email.ui.viewModel.ComposeEmailViewModel
import com.example.notthefinal.core.network.ApiProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeEmailScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    initialTo: String? = null,
    initialSubject: String? = null,
    initialBody: String? = null,
    inReplyTo: String? = null
) {
    val context = LocalContext.current
    val viewModel: ComposeEmailViewModel = viewModel {
        val database = com.example.doubler.feature.email.data.local.database.EmailDatabase.getDatabase(context)
        val localDataSource = com.example.doubler.feature.email.data.local.datasource.EmailLocalDataSource(
            emailDao = database.emailDao(),
            recipientDao = database.emailRecipientDao(),
            senderDao = database.emailSenderDao()
        )
        val networkObserver = com.example.doubler.core.network.connectivity.NetworkConnectivityObserver(context)
        
        ComposeEmailViewModel(
            emailRepository = EmailRepositoryImpl(
                emailApiService = ApiProvider.getInstance(context).emailApiService,
                localDataSource = localDataSource,
                networkObserver = networkObserver
            )
        )
    }

    val uiState by viewModel.composeUiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    var to by remember { mutableStateOf(initialTo ?: "") }
    var cc by remember { mutableStateOf("") }
    var bcc by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf(initialSubject ?: "") }
    var body by remember { mutableStateOf(initialBody ?: "") }
    var showCcBcc by remember { mutableStateOf(false) }
    
    // Handle success state
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("Compose Email") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                // Save as Draft
                IconButton(
                    onClick = {
                        if (to.isNotBlank() && subject.isNotBlank()) {
                            viewModel.sendEmail(
                                to = listOf(to.trim()),
                                cc = if (cc.isNotBlank()) cc.split(",").map { it.trim() } else null,
                                bcc = if (bcc.isNotBlank()) bcc.split(",").map { it.trim() } else null,
                                subject = subject,
                                body = body,
                                isDraft = true,
                                inReplyTo = inReplyTo
                            )
                        }
                    },
                    enabled = to.isNotBlank() && subject.isNotBlank() && !uiState.isLoading
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Save Draft")
                }
                
                // Send Email
                IconButton(
                    onClick = {
                        if (to.isNotBlank() && subject.isNotBlank()) {
                            viewModel.sendEmail(
                                to = listOf(to.trim()),
                                cc = if (cc.isNotBlank()) cc.split(",").map { it.trim() } else null,
                                bcc = if (bcc.isNotBlank()) bcc.split(",").map { it.trim() } else null,
                                subject = subject,
                                body = body,
                                isDraft = false,
                                inReplyTo = inReplyTo
                            )
                        }
                    },
                    enabled = to.isNotBlank() && subject.isNotBlank() && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error Message
            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // To Field
            OutlinedTextField(
                value = to,
                onValueChange = { to = it },
                label = { Text("To") },
                placeholder = { Text("recipient@example.com") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                trailingIcon = {
                    IconButton(onClick = { showCcBcc = !showCcBcc }) {
                        Icon(
                            imageVector = if (showCcBcc) Icons.Default.Email else Icons.Default.Person,
                            contentDescription = if (showCcBcc) "Hide CC/BCC" else "Show CC/BCC"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // CC and BCC Fields (when expanded)
            if (showCcBcc) {
                OutlinedTextField(
                    value = cc,
                    onValueChange = { cc = it },
                    label = { Text("CC") },
                    placeholder = { Text("cc@example.com") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bcc,
                    onValueChange = { bcc = it },
                    label = { Text("BCC") },
                    placeholder = { Text("bcc@example.com") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Subject Field
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject") },
                placeholder = { Text("Email subject") },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Body Field
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Message") },
                placeholder = { Text("Write your message here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                maxLines = Int.MAX_VALUE
            )

            // Attachment Section (Placeholder for future implementation)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Attachments",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Attachments (Coming Soon)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}