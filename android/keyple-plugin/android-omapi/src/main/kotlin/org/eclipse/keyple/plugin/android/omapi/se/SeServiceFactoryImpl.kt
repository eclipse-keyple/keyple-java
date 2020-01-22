package org.eclipse.keyple.plugin.android.omapi.se

import android.content.Context
import android.se.omapi.SEService
import androidx.annotation.RequiresApi
import org.eclipse.keyple.plugin.android.omapi.ISeServiceFactory
import java.util.concurrent.Executors

class SeServiceFactoryImpl(private val applicationContext: Context): ISeServiceFactory<SEService, SEService.OnConnectedListener>{

    @RequiresApi(android.os.Build.VERSION_CODES.P)
    override fun connectToSe(onConnectedListener: SEService.OnConnectedListener): SEService {
        return SEService(applicationContext, Executors.newSingleThreadExecutor(), onConnectedListener)
    }
}