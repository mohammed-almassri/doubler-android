package com.example.doubler.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.doubler.R
import com.example.doubler.core.worker.SyncDataWorker
import kotlinx.coroutines.*

class SyncService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var syncJob: Job? = null
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "sync_service_channel"
        private const val SYNC_INTERVAL_MS = 30 * 60 * 1000L // 30 minutes
        
        fun startService(context: Context) {
            val intent = Intent(context, SyncService::class.java)
            context.startForegroundService(intent)
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, SyncService::class.java)
            context.stopService(intent)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d("SyncService", "Service created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startPeriodicSync()
        Log.d("SyncService", "Service started")
        return START_STICKY // Restart if killed
    }
    
    override fun onDestroy() {
        super.onDestroy()
        syncJob?.cancel()
        serviceScope.cancel()
        Log.d("SyncService", "Service destroyed")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun startPeriodicSync() {
        syncJob?.cancel()
        syncJob = serviceScope.launch {
            while (isActive) {
                try {
                    // Trigger WorkManager sync
                    val syncWorkRequest = OneTimeWorkRequestBuilder<SyncDataWorker>().build()
                    WorkManager.getInstance(this@SyncService).enqueue(syncWorkRequest)
                    
                    Log.d("SyncService", "Triggered sync via WorkManager")
                    
                    delay(SYNC_INTERVAL_MS)
                } catch (e: Exception) {
                    Log.e("SyncService", "Error in periodic sync", e)
                    delay(SYNC_INTERVAL_MS) // Continue even if error
                }
            }
        }
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Background Sync",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Syncing data in the background"
            setShowBadge(false)
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Syncing Data")
            .setContentText("Keeping your data up to date")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // You'll need to add this icon
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }
}