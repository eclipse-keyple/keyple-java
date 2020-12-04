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
import org.eclipse.keyple.core.service.Reader
import org.eclipse.keyple.plugin.android.omapi.AbstractAndroidOmapiPlugin
import org.eclipse.keyple.plugin.android.omapi.PLUGIN_NAME
import timber.log.Timber

/**
 * Implementation of AbstractAndroidOmapiPlugin using the Android 9+ OMAPI implementation of Reader and SeService objects.
 *
 * @since 0.9
 */
@RequiresApi(android.os.Build.VERSION_CODES.P) // OS version providing android.se.omapi package
internal object AndroidOmapiPlugin : AbstractAndroidOmapiPlugin<android.se.omapi.Reader, SEService>() {

    override fun connectToSe(context: Context, callback: () -> Unit) {
        val seServiceFactory = SeServiceFactoryImpl(context.applicationContext)
        seService = seServiceFactory.connectToSe(SEService.OnConnectedListener {
            Timber.i("Connected, ready to register plugin")
            Timber.i("OMAPI SEService version: %s", seService?.version)
            callback()
        })
    }

    override fun getNativeReaders(): Array<android.se.omapi.Reader>? {
        return seService?.readers
    }

    override fun mapToReader(nativeReader: android.se.omapi.Reader): Reader {
        Timber.d("Reader available name : %s", nativeReader.name)
        Timber.d("Reader available isCardPresent : %S", nativeReader.isSecureElementPresent)
        return AndroidOmapiReader(nativeReader, PLUGIN_NAME, nativeReader.name)
    }
}
