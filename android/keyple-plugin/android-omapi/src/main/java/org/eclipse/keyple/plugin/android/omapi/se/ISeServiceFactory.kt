package org.eclipse.keyple.plugin.android.omapi.se

import android.se.omapi.SEService

interface ISeServiceFactory {
    fun connectToSe(onConnectedListener: SEService.OnConnectedListener): SEService
}