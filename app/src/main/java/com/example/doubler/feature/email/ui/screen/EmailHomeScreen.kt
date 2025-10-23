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
import com.example.doubler.core.user.data.repository.UserRepositoryImpl
import com.example.doubler.feature.auth.data.local.PreferencesDataSource
import com.example.doubler.feature.home.ui.viewModel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailHomeScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onNavigateToInbox: () -> Unit = {},
    onNavigateToOutbox: () -> Unit = {},
    onNavigateToDrafts: () -> Unit = {},
    onNavigateToStarred: () -> Unit = {},
    onNavigateToCompose: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel {
        HomeViewModel(
            userRepository = UserRepositoryImpl(
                preferencesDataSource = PreferencesDataSource(context)
            )
        )
    }

    val uiState by viewModel.homeUiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

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
                IconButton(onClick = onNavigateToCompose) {
                    Icon(Icons.Default.Add, contentDescription = "Compose")
                }
            }
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Welcome message
            uiState.user?.let { user ->
                Text(
                    text = "Hello ${user.name}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Manage your emails",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Email Categories
            EmailCategoryCard(
                icon = Icons.Default.Email,
                title = "Inbox",
                subtitle = "Received emails",
                onClick = onNavigateToInbox
            )

            EmailCategoryCard(
                icon = Icons.Default.Send,
                title = "Sent",
                subtitle = "Emails you've sent",
                onClick = onNavigateToOutbox
            )

            EmailCategoryCard(
                icon = Icons.Default.Email,
                title = "Drafts",
                subtitle = "Unsent email drafts",
                onClick = onNavigateToDrafts
            )

            EmailCategoryCard(
                icon = Icons.Default.Star,
                title = "Starred",
                subtitle = "Important emails",
                onClick = onNavigateToStarred
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Compose Button
            Button(
                onClick = onNavigateToCompose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Compose"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Compose New Email",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun EmailCategoryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Go to $title",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}