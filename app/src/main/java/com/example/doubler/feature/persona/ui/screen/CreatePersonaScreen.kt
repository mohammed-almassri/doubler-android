package com.example.doubler.feature.persona.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.doubler.feature.persona.data.local.dao.PersonaDao
import com.example.doubler.feature.persona.data.local.database.PersonaDatabase
import com.example.doubler.feature.persona.data.local.datasource.PersonaLocalDataSource
import com.example.doubler.feature.persona.data.repository.PersonaRepositoryImpl
import com.example.doubler.feature.persona.ui.viewModel.CreatePersonaViewModel
import com.example.notthefinal.core.network.ApiProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePersonaScreen(
    onPersonaCreated: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val database = PersonaDatabase.getDatabase(context)
    val localDataSource = PersonaLocalDataSource(
        personaDao = database.personaDao(),
    )
    val viewModel: CreatePersonaViewModel = viewModel {
        CreatePersonaViewModel(
            personaRepository = PersonaRepositoryImpl(
                personaApiService = ApiProvider.getInstance(context).personaApiService,
                personaLocalDataSource = localDataSource
            ),
            onPersonaCreated = onPersonaCreated
        )
    }

    val uiState by viewModel.createPersonaUiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    var debounceJob by remember { mutableStateOf<Job?>(null) }
    
    LaunchedEffect(bio) {
        debounceJob?.cancel()
        if ( bio.isNotBlank()) {
            debounceJob = launch {
                delay(1000)
                viewModel.generateImage(bio)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Create Your Persona",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Tell us about yourself to create your digital persona",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Generated Image Preview
        if (uiState.generatedImageUrl != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                AsyncImage(
                    model = uiState.generatedImageUrl,
                    contentDescription = "Generated avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        } else if (uiState.isGeneratingImage) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Generating your avatar...")
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Avatar will be generated automatically",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Name Field (Required)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name *") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = name.isBlank()
        )
        
        // Bio Field (Required)
        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Bio/Description *") },
            placeholder = { Text("Tell us about yourself...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            isError = bio.isBlank()
        )

        // Email Field (Optional)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        // Phone Field (Optional)
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        // Error Message
        if (uiState.error != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Create Button
        Button(
            onClick = {
                viewModel.createPersona(
                    name = name,
                    bio = bio,
                    email = email.takeIf { it.isNotBlank() },
                    phone = phone.takeIf { it.isNotBlank() }
                )
            },
            enabled = name.isNotBlank() && bio.isNotBlank() && !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Create Persona")
            }
        }

        // Help Text
        Text(
            text = "* Required fields. Your avatar will be generated automatically based on your name and description.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}