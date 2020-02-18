package org.eclipse.keyple.example

import android.app.Application
import timber.log.Timber

class ExampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}