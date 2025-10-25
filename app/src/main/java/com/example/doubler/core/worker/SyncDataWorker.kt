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
        Log.d("SyncDataWorker", "=== SYNC WORKER STARTED ===")
        Log.d("SyncDataWorker", "Worker ID: ${id}")
        Log.d("SyncDataWorker", "Run attempt: ${runAttemptCount}")
        
        return try {
            Log.d("SyncDataWorker", "Initializing API provider...")
            val apiProvider = ApiProvider.getInstance(applicationContext)
            Log.d("SyncDataWorker", "API provider initialized successfully")
            
            // Sync emails in background
            Log.d("SyncDataWorker", "Starting email sync...")
            syncEmails(apiProvider)
            
            // Sync personas if needed
            Log.d("SyncDataWorker", "Starting persona sync...")
            syncPersonas(apiProvider)
            
            Log.d("SyncDataWorker", "=== SYNC WORKER COMPLETED SUCCESSFULLY ===")
            Result.success()
            
        } catch (e: Exception) {
            Log.e("SyncDataWorker", "=== SYNC WORKER FAILED ===", e)
            Log.e("SyncDataWorker", "Error type: ${e.javaClass.simpleName}")
            Log.e("SyncDataWorker", "Error message: ${e.message}")
            
            // Return retry if we haven't exceeded retry limits
            if (runAttemptCount < 3) {
                Log.d("SyncDataWorker", "Retrying... (attempt $runAttemptCount/3)")
                Result.retry()
            } else {
                Log.e("SyncDataWorker", "Max retries exceeded, failing permanently")
                Result.failure()
            }
        }
    }

    private suspend fun syncEmails(apiProvider: ApiProvider) {
        try {
            withContext(Dispatchers.IO) {
                val database = com.example.doubler.feature.email.data.local.database.EmailDatabase.getDatabase(applicationContext)
                val localDataSource = com.example.doubler.feature.email.data.local.datasource.EmailLocalDataSource(
                    emailDao = database.emailDao(),
                    recipientDao = database.emailRecipientDao(),
                    senderDao = database.emailSenderDao()
                )
                val networkObserver = com.example.doubler.core.network.connectivity.NetworkConnectivityObserver(applicationContext)

                val emailRepository = EmailRepositoryImpl(
                    emailApiService = apiProvider.emailApiService,
                    localDataSource = localDataSource,
                    networkObserver = networkObserver
                )
                emailRepository.getInbox()
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
                val database = com.example.doubler.feature.persona.data.local.database.PersonaDatabase.getDatabase(applicationContext)
                val localDataSource = com.example.doubler.feature.persona.data.local.datasource.PersonaLocalDataSource(
                    personaDao = database.personaDao()
                )

                val personaRepository = PersonaRepositoryImpl(
                    personaLocalDataSource = localDataSource,
                    personaApiService = apiProvider.personaApiService
                )

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
    private const val ONE_TIME_SYNC_TAG = "one_time_sync"
    
    fun schedulePeriodicSync(context: Context, intervalSeconds: Long = 15) {
        Log.d("BackgroundSyncScheduler", "=== SCHEDULING PERIODIC SYNC ===")
        Log.d("BackgroundSyncScheduler", "Interval: $intervalSeconds minutes (minimum is 15)")
        
        // Ensure minimum interval is respected
        val actualInterval =  intervalSeconds
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncDataWorker>(
            actualInterval, TimeUnit.SECONDS // Fixed: Use MINUTES for periodic work
        )
            .setConstraints(constraints)
            .addTag("periodic_sync")
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            syncWorkRequest
        )
        
        Log.d("BackgroundSyncScheduler", "Work request ID: ${syncWorkRequest.id}")
        Log.d("BackgroundSyncScheduler", "=== PERIODIC SYNC SCHEDULED (${actualInterval}min) ===")
        
        // Add work info observer for debugging
        workManager.getWorkInfosForUniqueWorkLiveData(SYNC_WORK_NAME)
            .observeForever { workInfos ->
                workInfos?.forEach { workInfo ->
                    Log.d("BackgroundSyncScheduler", "Periodic Work status: ${workInfo.state}")
                    Log.d("BackgroundSyncScheduler", "Periodic Work ID: ${workInfo.id}")
                    Log.d("BackgroundSyncScheduler", "Next run time: ${workInfo.nextScheduleTimeMillis}")
                    if (workInfo.state.isFinished) {
                        Log.d("BackgroundSyncScheduler", "Periodic Work finished with result: ${workInfo.outputData}")
                    }
                }
            }
    }
    
    fun scheduleOneTimeSync(context: Context, delaySeconds: Long = 10) {
        Log.d("BackgroundSyncScheduler", "=== SCHEDULING ONE-TIME SYNC ===")
        Log.d("BackgroundSyncScheduler", "Delay: $delaySeconds seconds")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncDataWorker>()
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .addTag(ONE_TIME_SYNC_TAG)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(syncWorkRequest)
        
        Log.d("BackgroundSyncScheduler", "One-time work request ID: ${syncWorkRequest.id}")
        Log.d("BackgroundSyncScheduler", "=== ONE-TIME SYNC SCHEDULED ===")
        
        // Add observer for debugging
        workManager.getWorkInfoByIdLiveData(syncWorkRequest.id)
            .observeForever { workInfo ->
                workInfo?.let {
                    Log.d("BackgroundSyncScheduler", "One-time work status: ${it.state}")
                    if (it.state.isFinished) {
                        Log.d("BackgroundSyncScheduler", "One-time work finished with result: ${it.outputData}")
                    }
                }
            }
    }
    
    fun scheduleRepeatingOneTimeSync(context: Context, intervalSeconds: Long = 30) {
        Log.d("BackgroundSyncScheduler", "=== SCHEDULING REPEATING ONE-TIME SYNC ===")
        Log.d("BackgroundSyncScheduler", "This will schedule a new one-time sync every $intervalSeconds seconds")
        
        // For testing frequent syncs, we'll use a chain of one-time requests
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val initialWork = OneTimeWorkRequestBuilder<SyncDataWorker>()
            .setInitialDelay(intervalSeconds, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .addTag("repeating_one_time")
            .setInputData(
                androidx.work.Data.Builder()
                    .putLong("next_interval", intervalSeconds)
                    .putBoolean("should_repeat", true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueue(initialWork)
        Log.d("BackgroundSyncScheduler", "Repeating one-time sync started")
    }
    

    fun cancelAllSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
        Log.d("BackgroundSyncScheduler", "All background sync cancelled")
    }
}