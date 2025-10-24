package com.example.doubler.core.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

object WorkManagerDebugger {
    
    private const val TAG = "WorkManagerDebugger"
    
    fun checkWorkManagerStatus(context: Context) {
        val workManager = WorkManager.getInstance(context)
        
        Log.d(TAG, "=== WORKMANAGER STATUS CHECK ===")
        
        // Check all work
        workManager.getWorkInfosByTagLiveData("sync").observeForever { workInfos ->
            Log.d(TAG, "Total sync works: ${workInfos.size}")
            workInfos.forEach { workInfo ->
                logWorkInfo(workInfo)
            }
        }
        
        // Check specific sync work
        workManager.getWorkInfosForUniqueWorkLiveData("sync_data_work").observeForever { workInfos ->
            Log.d(TAG, "Periodic sync works: ${workInfos.size}")
            workInfos.forEach { workInfo ->
                logWorkInfo(workInfo, "PERIODIC")
            }
        }
        
        // Check general WorkManager state
        try {
            Log.d(TAG, "WorkManager initialized: true")
        } catch (e: Exception) {
            Log.e(TAG, "WorkManager not initialized", e)
        }
    }
    
    fun printAllWorkStatus(context: Context) {
        Log.d(TAG, "=== ALL WORK STATUS ===")
        
        val workManager = WorkManager.getInstance(context)
        
        // Get all work info
        workManager.getWorkInfosByTag("periodic_sync").get().forEach { workInfo ->
            Log.d(TAG, "Periodic Work - ID: ${workInfo.id}")
            Log.d(TAG, "Periodic Work - State: ${workInfo.state}")
            Log.d(TAG, "Periodic Work - Tags: ${workInfo.tags}")
        }
        
        workManager.getWorkInfosByTag("one_time_sync").get().forEach { workInfo ->
            Log.d(TAG, "One-time Work - ID: ${workInfo.id}")
            Log.d(TAG, "One-time Work - State: ${workInfo.state}")
            Log.d(TAG, "One-time Work - Tags: ${workInfo.tags}")
        }
        
        workManager.getWorkInfosByTag("repeating_one_time").get().forEach { workInfo ->
            Log.d(TAG, "Repeating One-time Work - ID: ${workInfo.id}")
            Log.d(TAG, "Repeating One-time Work - State: ${workInfo.state}")
            Log.d(TAG, "Repeating One-time Work - Tags: ${workInfo.tags}")
        }
        
        // Get unique work info
        try {
            val uniqueWorkInfo = workManager.getWorkInfosForUniqueWork("sync_data_work").get()
            uniqueWorkInfo.forEach { workInfo ->
                Log.d(TAG, "Unique Work - ID: ${workInfo.id}")
                Log.d(TAG, "Unique Work - State: ${workInfo.state}")
                Log.d(TAG, "Unique Work - Next Schedule: ${workInfo.nextScheduleTimeMillis}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unique work info", e)
        }
        
        Log.d(TAG, "=== END WORK STATUS ===")
    }
    
    fun triggerImmediateSyncForTesting(context: Context) {
        Log.d(TAG, "=== TRIGGERING IMMEDIATE SYNC FOR TESTING ===")
        
        val workRequest = OneTimeWorkRequestBuilder<SyncDataWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag("manual_test_sync")
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
        
        Log.d(TAG, "Immediate test sync queued with ID: ${workRequest.id}")
        
        // Monitor this specific work
        WorkManager.getInstance(context)
            .getWorkInfoByIdLiveData(workRequest.id)
            .observeForever { workInfo ->
                workInfo?.let {
                    Log.d(TAG, "Manual test sync status: ${it.state}")
                    if (it.state.isFinished) {
                        Log.d(TAG, "Manual test sync finished with result: ${it.outputData}")
                    }
                }
            }
    }
    
    fun startFrequentTestingSync(context: Context, intervalSeconds: Long = 30) {
        Log.d(TAG, "=== STARTING FREQUENT TESTING SYNC ===")
        Log.d(TAG, "Using repeating one-time sync with $intervalSeconds second interval")
        
        // Cancel any existing work first
        cancelAllWork(context)
        
        // Start the repeating one-time sync chain
        BackgroundSyncScheduler.scheduleRepeatingOneTimeSync(context, intervalSeconds)
        
        Log.d(TAG, "Frequent testing sync started")
    }
    
    fun startProductionSync(context: Context, intervalMinutes: Long = 15) {
        Log.d(TAG, "=== STARTING PRODUCTION SYNC ===")
        Log.d(TAG, "Using proper periodic work with $intervalMinutes minute interval")
        
        // Cancel any existing work first
        cancelAllWork(context)
        
        // Start proper periodic sync
        BackgroundSyncScheduler.schedulePeriodicSync(context, intervalMinutes)
        
        Log.d(TAG, "Production sync started")
    }
    
    private fun logWorkInfo(workInfo: WorkInfo, prefix: String = "") {
        Log.d(TAG, "[$prefix] Work ID: ${workInfo.id}")
        Log.d(TAG, "[$prefix] State: ${workInfo.state}")
        Log.d(TAG, "[$prefix] Tags: ${workInfo.tags}")
        Log.d(TAG, "[$prefix] Run attempt: ${workInfo.runAttemptCount}")
        
        if (workInfo.state == WorkInfo.State.BLOCKED) {
            Log.w(TAG, "[$prefix] Work is BLOCKED - check constraints")
        }
        
        if (workInfo.state == WorkInfo.State.FAILED) {
            Log.e(TAG, "[$prefix] Work FAILED - Output: ${workInfo.outputData}")
        }
        
        if (workInfo.state == WorkInfo.State.CANCELLED) {
            Log.w(TAG, "[$prefix] Work was CANCELLED")
        }
    }

    fun cancelAllWork(context: Context) {
        Log.d(TAG, "=== CANCELLING ALL WORK ===")
        
        val workManager = WorkManager.getInstance(context)
        
        // Cancel unique periodic work
        workManager.cancelUniqueWork("sync_data_work")
        
        // Cancel all work by tags
        workManager.cancelAllWorkByTag("periodic_sync")
        workManager.cancelAllWorkByTag("one_time_sync")
        workManager.cancelAllWorkByTag("repeating_one_time")
        workManager.cancelAllWorkByTag("manual_test_sync")
        
        workManager.pruneWork() // Clean up finished work
        
        Log.d(TAG, "All work cancelled and pruned")
    }
}