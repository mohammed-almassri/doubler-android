package com.example.doubler.core.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.doubler.feature.email.data.repository.EmailRepositoryImpl
import com.example.doubler.feature.persona.data.repository.PersonaRepositoryImpl
import com.example.notthefinal.core.network.ApiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class SyncDataWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("SyncDataWorker", "Starting background sync")
            
            val apiProvider = ApiProvider.getInstance(applicationContext)
            
            // Sync emails in background
            syncEmails(apiProvider)
            
            // Sync personas if needed
            syncPersonas(apiProvider)
            
            Log.d("SyncDataWorker", "Background sync completed successfully")
            Result.success()
            
        } catch (e: Exception) {
            Log.e("SyncDataWorker", "Background sync failed", e)
            Result.retry()
        }
    }

    private suspend fun syncEmails(apiProvider: ApiProvider) {
        try {
            withContext(Dispatchers.IO) {
                val emailRepository = EmailRepositoryImpl(apiProvider.emailApiService)
                
                // Sync inbox emails
                emailRepository.getInbox()
                
                // Sync other email folders if needed
                emailRepository.getOutbox()
                emailRepository.getDrafts()
                emailRepository.getStarred()
                
                Log.d("SyncDataWorker", "Email sync completed")
            }
        } catch (e: Exception) {
            Log.e("SyncDataWorker", "Email sync failed", e)
        }
    }

    private suspend fun syncPersonas(apiProvider: ApiProvider) {
        try {
            withContext(Dispatchers.IO) {
                val personaRepository = PersonaRepositoryImpl(apiProvider.personaApiService)
                
                // Sync personas
                personaRepository.getPersonas()
                
                Log.d("SyncDataWorker", "Persona sync completed")
            }
        } catch (e: Exception) {
            Log.e("SyncDataWorker", "Persona sync failed", e)
        }
    }
}

// Helper class to schedule work
object BackgroundSyncScheduler {
    
    private const val SYNC_WORK_NAME = "sync_data_work"
    
    fun schedulePeriodicSync(context: Context, intervalHours: Long = 1) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncDataWorker>(
            intervalHours, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
        
        Log.d("BackgroundSyncScheduler", "Periodic sync scheduled for every $intervalHours hours")
    }
    
    fun scheduleOneTimeSync(context: Context, delayMinutes: Long = 15) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncDataWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(syncWorkRequest)
        
        Log.d("BackgroundSyncScheduler", "One-time sync scheduled in $delayMinutes minutes")
    }
    
    fun cancelAllSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
        Log.d("BackgroundSyncScheduler", "All background sync cancelled")
    }
}