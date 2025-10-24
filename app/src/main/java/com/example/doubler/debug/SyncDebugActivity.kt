package com.example.doubler.debug

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.doubler.core.sync.SyncManager
import com.example.doubler.core.worker.WorkManagerDebugger

@OptIn(ExperimentalMaterial3Api::class)
class SyncDebugActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                SyncDebugScreen()
            }
        }
    }
}

@Composable
fun SyncDebugScreen() {
    val context = LocalContext.current
    var statusText by remember { mutableStateOf("Ready") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        Text(
            text = "Sync Debug Console",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Status: $statusText",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Immediate Sync
        Button(
            onClick = {
                statusText = "Triggering immediate sync..."
                Log.d("SyncDebugActivity", "Manual immediate sync triggered")
                SyncManager.triggerImmediateSync(context)
                statusText = "Immediate sync triggered - check logs"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Trigger Immediate Sync")
        }
        
        // Start Testing Sync (30 second intervals)
        Button(
            onClick = {
                statusText = "Starting testing sync (30s intervals)..."
                Log.d("SyncDebugActivity", "Starting testing sync")
                SyncManager.enableTestingSync(context, intervalSeconds = 30)
                statusText = "Testing sync started - will repeat every 30s"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Testing Sync (30s)")
        }
        
        // Start Production Sync (15 minute intervals)
        Button(
            onClick = {
                statusText = "Starting production sync (15 min intervals)..."
                Log.d("SyncDebugActivity", "Starting production sync")
                SyncManager.enableBackgroundSync(context, intervalMinutes = 15)
                statusText = "Production sync started - will repeat every 15 minutes"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Production Sync (15 min)")
        }
        
        // Cancel All Work
        Button(
            onClick = {
                statusText = "Cancelling all sync work..."
                Log.d("SyncDebugActivity", "Cancelling all work")
                SyncManager.disableBackgroundSync(context)
                statusText = "All sync work cancelled"
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Cancel All Sync")
        }
        
        // Print Work Status
        Button(
            onClick = {
                statusText = "Printing work status to logs..."
                Log.d("SyncDebugActivity", "Printing work status")
                WorkManagerDebugger.printAllWorkStatus(context)
                WorkManagerDebugger.checkWorkManagerStatus(context)
                statusText = "Work status printed - check logs"
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Print Work Status to Logs")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Debug Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Testing Sync: Uses one-time work requests chained together for fast testing",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "• Production Sync: Uses proper periodic work with 15-minute minimum intervals",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "• Check Android Logcat for detailed sync logs",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "• WorkManager minimum periodic interval is 15 minutes",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}