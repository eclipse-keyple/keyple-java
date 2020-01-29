package org.eclipse.keyple.example.calypso.android.omapi

import android.app.Application
import timber.log.Timber

class ExampleApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}