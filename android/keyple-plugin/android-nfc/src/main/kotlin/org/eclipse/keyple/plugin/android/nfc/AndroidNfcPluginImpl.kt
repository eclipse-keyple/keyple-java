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
package org.eclipse.keyple.plugin.android.nfc

import android.os.Build
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import org.eclipse.keyple.core.plugin.AbstractPlugin
import org.eclipse.keyple.core.service.Reader
import timber.log.Timber

/**
 * Enables Keyple to communicate with the the Android device by providing access to the
 * implementation of Reader.
 *
 * Will provide 2 version of Android NFC reader based on Android OS version.
 *
 */
internal object AndroidNfcPluginImpl : AbstractPlugin(AndroidNfcPlugin.PLUGIN_NAME), AndroidNfcPlugin {

    /**
     * For an Android NFC device, the Android NFC Plugin manages only one @[AndroidNfcReader].
     *
     * @return SortedSet<ProxyReader> : contains only one element, the
     * singleton @[AndroidNfcReader]
    </ProxyReader> */
    override fun initNativeReaders(): ConcurrentMap<String, Reader>? {
        Timber.d("InitNativeReader() add the unique instance of AndroidNfcReaderImpl")
        val readers = ConcurrentHashMap<String, Reader>()
        readers[InterfaceAndroidNfcReader.READER_NAME] = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            AndroidNfcReaderPreNImpl
        } else {
            AndroidNfcReaderPostNImpl
        }
        // Nfc android adapter availability is checked in AndroidNfcFragment
        return readers
    }
}
