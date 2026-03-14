package com.example.pocketguard

import android.app.Application
import com.example.pocketguard.presentation.di.ServiceLocator

class PocketGuardApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.initializeServices(this)
    }
}

