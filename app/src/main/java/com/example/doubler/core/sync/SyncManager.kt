package com.example.doubler.core.sync

import android.content.Context
import android.util.Log
import com.example.doubler.core.service.SyncService
import com.example.doubler.core.worker.BackgroundSyncScheduler
import com.example.doubler.core.worker.WorkManagerDebugger


object SyncManager {
    
    fun enableBackgroundSync(
        context: Context,
        intervalMinutes: Long = 15  // Changed from seconds to minutes
    ) {
        Log.d("SyncManager", "Enabling background sync with $intervalMinutes minute intervals")
        BackgroundSyncScheduler.schedulePeriodicSync(context, intervalMinutes)
        Log.d("SyncManager", "WorkManager sync enabled")
    }
    
    fun enableTestingSync(
        context: Context,
        intervalSeconds: Long = 30  // For frequent testing
    ) {
        Log.d("SyncManager", "Enabling testing sync with $intervalSeconds second intervals")
        BackgroundSyncScheduler.schedulePeriodicSync(context, intervalSeconds)
        Log.d("SyncManager", "Testing sync enabled")
    }
    
    fun triggerImmediateSync(context: Context) {
        Log.d("SyncManager", "Triggering immediate sync")
        WorkManagerDebugger.triggerImmediateSyncForTesting(context)
    }
    
    fun disableBackgroundSync(context: Context) {
        WorkManagerDebugger.cancelAllWork(context)
        SyncService.stopService(context)
        Log.d("SyncManager", "All background sync disabled")
    }
}