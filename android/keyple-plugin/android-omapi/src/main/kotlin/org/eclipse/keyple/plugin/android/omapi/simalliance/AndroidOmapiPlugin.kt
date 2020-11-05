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
package org.eclipse.keyple.plugin.android.omapi.simalliance

import android.content.Context
import org.eclipse.keyple.core.service.Reader
import org.eclipse.keyple.plugin.android.omapi.AbstractAndroidOmapiPlugin
import org.eclipse.keyple.plugin.android.omapi.PLUGIN_NAME
import org.simalliance.openmobileapi.SEService
import timber.log.Timber

/**
 * Allow to provide an implementation of AbstractAndroidOmapiPlugin using the Simalliance
 * OMAPI implementation of Reader and SeService objects.
 */
internal object AndroidOmapiPlugin : AbstractAndroidOmapiPlugin<org.simalliance.openmobileapi.Reader, SEService>() {

    override fun connectToSe(context: Context, callback: () -> Unit) {
        val seServiceFactory = SeServiceFactoryImpl(context)
        seService = seServiceFactory.connectToSe(SEService.CallBack {
            Timber.i("Connected, ready to register plugin")
            Timber.i("OMAPI SEService version: %s", seService?.version)
            callback()
        })
    }

    override fun getNativeReaders(): Array<org.simalliance.openmobileapi.Reader>? {
        return seService?.readers
    }

    override fun mapToReader(nativeReader: org.simalliance.openmobileapi.Reader): Reader {
        Timber.d("Reader available name : %s", nativeReader.name)
        Timber.d("Reader available isCardPresent : %S", nativeReader.isSecureElementPresent)
        return AndroidOmapiReader(nativeReader, PLUGIN_NAME, nativeReader.name)
    }
}
