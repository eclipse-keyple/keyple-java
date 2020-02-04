/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.android.omapi.se

import android.content.Context
import android.se.omapi.SEService
import androidx.annotation.RequiresApi
import java.util.concurrent.Executors
import org.eclipse.keyple.plugin.android.omapi.ISeServiceFactory

class SeServiceFactoryImpl(private val applicationContext: Context) : ISeServiceFactory<SEService, SEService.OnConnectedListener> {

    @RequiresApi(android.os.Build.VERSION_CODES.P)
    override fun connectToSe(onConnectedListener: SEService.OnConnectedListener): SEService {
        return SEService(applicationContext, Executors.newSingleThreadExecutor(), onConnectedListener)
    }
}
