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

import java.util.HashMap
import java.util.SortedSet
import java.util.TreeSet
import org.eclipse.keyple.core.seproxy.SeReader
import org.eclipse.keyple.core.seproxy.plugin.AbstractPlugin
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Enables Keyple to communicate with the the Android device by providing access to the
 * implementation of SeReader.
 *
 */
internal object AndroidNfcPluginImpl : AbstractPlugin(AndroidNfcPlugin.PLUGIN_NAME), AndroidNfcPlugin {

    private val parameters = HashMap<String, String>() // not in use in

    override fun getParameters(): Map<String, String> {

        Timber.w("Android NFC Plugin does not support parameters, see AndroidNfcReaderImpl instead")
        return parameters
    }

    override fun setParameter(key: String, value: String) {
        Timber.w("Android NFC Plugin does not support parameters, see AndroidNfcReaderImpl instead")
        parameters[key] = value
    }

    /**
     * For an Android NFC device, the Android NFC Plugin manages only one @[AndroidNfcReaderImpl].
     *
     * @return SortedSet<ProxyReader> : contains only one element, the
     * singleton @[AndroidNfcReaderImpl]
    </ProxyReader> */
    override fun initNativeReaders(): ConcurrentMap<String, SeReader>? {
        Timber.d("InitNativeReader() add the unique instance of AndroidNfcReaderImpl")
        val readers = ConcurrentHashMap<String, SeReader>()
        readers[AndroidNfcReaderImpl.name] = AndroidNfcReaderImpl
        // Nfc android adapter availability is checked in AndroidNfcFragment
        return readers
    }
}
