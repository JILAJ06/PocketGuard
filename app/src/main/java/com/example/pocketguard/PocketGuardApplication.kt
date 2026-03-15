package com.example.pocketguard

import android.app.Application
import com.example.pocketguard.presentation.di.ServiceLocator
import android.util.Log

class PocketGuardApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.initializeServices(this)

        // Global handler to log uncaught exceptions so we can capture stacktraces in logcat
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("GlobalExceptionHandler", "Uncaught exception in thread ${thread.name}: ${throwable.message}", throwable)
                throwable.printStackTrace()
            } catch (e: Exception) {
                // Best-effort logging
            }
        }
    }
}

