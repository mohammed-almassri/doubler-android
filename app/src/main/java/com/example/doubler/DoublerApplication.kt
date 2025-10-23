package com.example.doubler

import android.app.Application
import android.util.Log
import com.example.doubler.core.sync.SyncManager
import com.example.doubler.core.sync.SyncStrategy

class DoublerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        Log.d("DoublerApplication", "Application started")
        
        // Initialize background sync when app starts
        initializeBackgroundSync()
    }
    
    private fun initializeBackgroundSync() {
        try {
            // Enable periodic background sync every 2 hours
            SyncManager.enableBackgroundSync(
                context = this,
                strategy = SyncStrategy.COMBINED, // Use multiple strategies for reliability
                intervalHours = 2
            )
            
            Log.d("DoublerApplication", "Background sync initialized successfully")
        } catch (e: Exception) {
            Log.e("DoublerApplication", "Failed to initialize background sync", e)
        }
    }
}