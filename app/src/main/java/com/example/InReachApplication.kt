package com.example

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.EscrowWorker
import java.util.concurrent.TimeUnit

class InReachApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Schedule periodic Hourly Escrow check task
        try {
            val escrowWorkRequest = PeriodicWorkRequestBuilder<EscrowWorker>(1, TimeUnit.HOURS)
                .build()
                
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "EscrowWorkerUniqueCheck",
                ExistingPeriodicWorkPolicy.KEEP,
                escrowWorkRequest
            )
        } catch (e: Exception) {
            android.util.Log.e("InReachApplication", "WorkManager initializer failed or was bypassed: ${e.message}", e)
        }
    }
}
