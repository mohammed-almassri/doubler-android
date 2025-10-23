package com.example.doubler.core.sync

import android.content.Context
import android.util.Log
import com.example.doubler.core.alarm.AlarmScheduler
import com.example.doubler.core.service.SyncService
import com.example.doubler.core.worker.BackgroundSyncScheduler

enum class SyncStrategy {
    WORK_MANAGER,    // Best for most cases - survives app restart
    ALARM_MANAGER,   // For exact timing requirements
    FOREGROUND_SERVICE, // For continuous background work
    COMBINED         // Uses multiple strategies
}

object SyncManager {
    
    fun enableBackgroundSync(
        context: Context, 
        strategy: SyncStrategy = SyncStrategy.WORK_MANAGER,
        intervalHours: Long = 1
    ) {
        when (strategy) {
            SyncStrategy.WORK_MANAGER -> {
                BackgroundSyncScheduler.schedulePeriodicSync(context, intervalHours)
                Log.d("SyncManager", "WorkManager sync enabled")
            }
            
            SyncStrategy.ALARM_MANAGER -> {
                val intervalMillis = intervalHours * 60 * 60 * 1000L
                AlarmScheduler.scheduleRepeatingAlarm(context, intervalMillis)
                Log.d("SyncManager", "AlarmManager sync enabled")
            }
            
            SyncStrategy.FOREGROUND_SERVICE -> {
                SyncService.startService(context)
                Log.d("SyncManager", "Foreground service sync enabled")
            }
            
            SyncStrategy.COMBINED -> {
                // Use WorkManager as primary + AlarmManager as backup
                BackgroundSyncScheduler.schedulePeriodicSync(context, intervalHours)
                val intervalMillis = intervalHours * 60 * 60 * 1000L
                AlarmScheduler.scheduleRepeatingAlarm(context, intervalMillis)
                Log.d("SyncManager", "Combined sync strategy enabled")
            }
        }
    }
    
    fun disableBackgroundSync(context: Context) {
        BackgroundSyncScheduler.cancelAllSync(context)
        AlarmScheduler.cancelAlarm(context)
        SyncService.stopService(context)
        Log.d("SyncManager", "All background sync disabled")
    }
    
    fun triggerImmediateSync(context: Context, delayMinutes: Long = 0) {
        BackgroundSyncScheduler.scheduleOneTimeSync(context, delayMinutes)
        Log.d("SyncManager", "Immediate sync triggered")
    }
    
    // Example usage for persona feature
    fun schedulePersonaImageGeneration(context: Context, delayMinutes: Long = 15) {
        // Schedule a one-time sync after user stops using the app
        BackgroundSyncScheduler.scheduleOneTimeSync(context, delayMinutes)
        Log.d("SyncManager", "Persona image generation scheduled in $delayMinutes minutes")
    }
}