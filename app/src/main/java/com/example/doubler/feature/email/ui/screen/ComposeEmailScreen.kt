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
import com.example.doubler.feature.persona.data.local.PersonaPreferencesDataSource
import com.example.doubler.feature.persona.data.local.database.PersonaDatabase
import com.example.doubler.feature.persona.data.local.datasource.PersonaLocalDataSource
import com.example.doubler.feature.persona.data.repository.CurrentPersonaRepositoryImpl
import com.example.doubler.feature.persona.data.repository.PersonaRepositoryImpl
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
        val personaDb = PersonaDatabase.getDatabase(context)
        val personaLocalDataSource = PersonaLocalDataSource(
            personaDao = personaDb.personaDao(),
        )


        ComposeEmailViewModel(
            emailRepository = EmailRepositoryImpl(
                emailApiService = ApiProvider.getInstance(context).emailApiService,
                localDataSource = localDataSource,
                networkObserver = networkObserver
            ),
            currentPersonaRepository = CurrentPersonaRepositoryImpl(
                personaRepository = PersonaRepositoryImpl(
                    personaLocalDataSource = personaLocalDataSource,
                    personaApiService =  ApiProvider.getInstance(context).personaApiService
                ),
                personaPreferencesDataSource = PersonaPreferencesDataSource(context)
            ),
            personaRepository = PersonaRepositoryImpl(
                personaLocalDataSource = personaLocalDataSource,
                personaApiService =  ApiProvider.getInstance(context).personaApiService
            )
        )
    }

    val uiState by viewModel.composeUiState.collectAsStateWithLifecycle()
    val personas by viewModel.personas.collectAsStateWithLifecycle()
    val selectedPersona by viewModel.selectedPersona.collectAsStateWithLifecycle()
    val isLoadingPersonas by viewModel.isLoadingPersonas.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    var to by remember { mutableStateOf(initialTo ?: "") }
    var cc by remember { mutableStateOf("") }
    var bcc by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf(initialSubject ?: "") }
    var body by remember { mutableStateOf(initialBody ?: "") }
    var showCcBcc by remember { mutableStateOf(false) }
    var expandedPersonaDropdown by remember { mutableStateOf(false) }
    
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
                        if (to.isNotBlank() && subject.isNotBlank() && selectedPersona != null) {
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
                    enabled = to.isNotBlank() && subject.isNotBlank() && selectedPersona != null && !uiState.isLoading
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Save Draft")
                }
                
                // Send Email
                IconButton(
                    onClick = {
                        if (to.isNotBlank() && subject.isNotBlank() && selectedPersona != null) {
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
                    enabled = to.isNotBlank() && subject.isNotBlank() && selectedPersona != null && !uiState.isLoading
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

            // Persona Selection Dropdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Send as:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = expandedPersonaDropdown,
                        onExpandedChange = { expandedPersonaDropdown = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedPersona?.name ?: "Select a persona",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Persona") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Persona"
                                )
                            },
                            trailingIcon = {
                                if (isLoadingPersonas) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = expandedPersonaDropdown
                                    )
                                }
                            },
                            placeholder = { Text("Choose your persona") },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expandedPersonaDropdown,
                            onDismissRequest = { expandedPersonaDropdown = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            personas.forEach { persona ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = persona.name,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            if (persona.email != null) {
                                                Text(
                                                    text = persona.email!!,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectPersona(persona)
                                        expandedPersonaDropdown = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = "Persona Avatar"
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            
                            if (personas.isEmpty() && !isLoadingPersonas) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "No personas available",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    onClick = { expandedPersonaDropdown = false },
                                    enabled = false
                                )
                            }
                        }
                    }
                    
                    // Show selected persona details
                    if (selectedPersona != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sending as: ${selectedPersona!!.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
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