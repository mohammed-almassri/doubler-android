package com.example.doubler

import android.app.Application
import android.util.Log
import com.example.doubler.core.sync.SyncManager

class DoublerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        Log.d("DoublerApplication", "=== APPLICATION STARTED ===")
        
        // Initialize background sync when app starts
        initializeBackgroundSync()
    }
    
    private fun initializeBackgroundSync() {
        try {
            Log.d("DoublerApplication", "Initializing background sync...")
            
            // For production: Use proper 15-minute intervals
            // SyncManager.enableBackgroundSync(this, intervalMinutes = 15)
            
            // For testing: Use frequent one-time syncs (30 second intervals)
            SyncManager.enableTestingSync(this, intervalSeconds = 5)
            
            Log.d("DoublerApplication", "Background sync initialized successfully")
        } catch (e: Exception) {
            Log.e("DoublerApplication", "Failed to initialize background sync", e)
        }
    }
}