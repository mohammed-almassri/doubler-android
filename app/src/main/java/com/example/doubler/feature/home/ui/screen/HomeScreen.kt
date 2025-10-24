package com.example.doubler.feature.home.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doubler.core.auth.data.repository.LogoutRepositoryImpl
import com.example.doubler.core.user.data.repository.UserRepositoryImpl
import com.example.doubler.feature.auth.data.local.PreferencesDataSource
import com.example.doubler.feature.email.data.local.database.EmailDatabase
import com.example.doubler.feature.email.data.local.datasource.EmailLocalDataSource
import com.example.doubler.feature.home.ui.state.HomeUiState
import com.example.doubler.feature.home.ui.viewModel.HomeViewModel
import com.example.doubler.feature.persona.data.local.PersonaPreferencesDataSource
import com.example.doubler.feature.persona.data.local.database.PersonaDatabase
import com.example.doubler.feature.persona.data.local.datasource.PersonaLocalDataSource
import com.example.doubler.feature.persona.data.repository.CurrentPersonaRepositoryImpl
import com.example.doubler.feature.persona.data.repository.PersonaRepositoryImpl
import com.example.notthefinal.core.network.ApiProvider
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToEmailHome: () -> Unit = {}
) {
    val context = LocalContext.current
    val personaDb = PersonaDatabase.getDatabase(context)
    val personaLocalDataSource = PersonaLocalDataSource(
        personaDao = personaDb.personaDao(),
    )
    
    val emailDb = EmailDatabase.getDatabase(context)
    val emailLocalDataSource = EmailLocalDataSource(
        emailDao = emailDb.emailDao(),
        recipientDao = emailDb.emailRecipientDao(),
        senderDao = emailDb.emailSenderDao()
    )
    
    val preferencesDataSource = PreferencesDataSource(context)
    val personaPreferencesDataSource = PersonaPreferencesDataSource(context)
    val userRepository = UserRepositoryImpl(preferencesDataSource)

    val viewModel: HomeViewModel = viewModel {
        HomeViewModel(
            userRepository = userRepository,
            currentPersonaRepository = CurrentPersonaRepositoryImpl(
                personaRepository = PersonaRepositoryImpl(
                    personaLocalDataSource = personaLocalDataSource,
                    personaApiService =  ApiProvider.getInstance(context).personaApiService
                ),
                personaPreferencesDataSource = personaPreferencesDataSource
            ),
            personaRepository = PersonaRepositoryImpl(
                personaLocalDataSource = personaLocalDataSource,
                personaApiService =  ApiProvider.getInstance(context).personaApiService
            ),
            logoutRepository = LogoutRepositoryImpl(
                userRepository = userRepository,
                emailLocalDataSource = emailLocalDataSource,
                personaLocalDataSource = personaLocalDataSource,
                personaPreferencesDataSource = personaPreferencesDataSource
            )
        )
    }

    val uiState by viewModel.homeUiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    // Effect to handle user logout navigation
    LaunchedEffect(uiState.user) {
        if (uiState.user == null && !uiState.isLoading) {
            onNavigateToLogin()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Doubler",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(
                    onClick = { viewModel.logout() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        if (uiState.isLoading) {
            // Loading State
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading your dashboard...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (uiState.error != null) {
            // Error State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.error!!,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateToLogin
                ) {
                    Text("Go to Login")
                }
            }
        } else {
            // Main Content
            uiState.user?.let { user ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // User Avatar
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.imageUrl.isNullOrBlank()) {
                            // Default avatar
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = CircleShape
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.name.firstOrNull()?.uppercase() ?: "U",
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            // TODO: Load image from URL when implementing image loading
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ),
                                shape = CircleShape
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Welcome Message
                    Text(
                        text = "Welcome back,",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Current Date and Time
                    val currentDateTime = remember {
                        SimpleDateFormat("EEEE, MMMM dd, yyyy • HH:mm", Locale.getDefault())
                            .format(Date())
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentDateTime,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Personas Section
                    if (uiState.personas.isNotEmpty()) {
                        Text(
                            text = "Your Personas",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.personas.forEach { persona ->
                                PersonaCard(
                                    persona = persona,
                                    isSelected = persona.id == uiState.currentPersona?.id,
                                    onClick = { viewModel.setCurrentPersona(persona.id) }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                    
                    // Quick Actions
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                    
                    // Action Cards Grid
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ActionCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Email,
                                title = "Emails",
                                subtitle = "View your emails",
                                onClick = onNavigateToEmailHome
                            )
                            ActionCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Email,
                                title = "Contacts",
                                subtitle = "Manage contacts",
                                onClick = { /* TODO: Navigate to contacts */ }
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ActionCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Settings,
                                title = "Settings",
                                subtitle = "App preferences",
                                onClick = { /* TODO: Navigate to settings */ }
                            )
                            ActionCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Person,
                                title = "Profile",
                                subtitle = "Edit profile",
                                onClick = { /* TODO: Navigate to profile */ }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PersonaCard(
    modifier: Modifier = Modifier,
    persona: com.example.doubler.feature.persona.domain.model.Persona,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(
            2.dp, 
            MaterialTheme.colorScheme.primary
        ) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (persona.imageUrl.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.secondary
                        ),
                        shape = CircleShape
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = persona.name.firstOrNull()?.uppercase() ?: "P",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected) 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // TODO: Load image from URL when implementing image loading
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = CircleShape
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Persona Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = persona.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                if (!persona.email.isNullOrBlank()) {
                    Text(
                        text = persona.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!persona.bio.isNullOrBlank()) {
                    Text(
                        text = persona.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            
            // Selected indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}