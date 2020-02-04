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
package org.eclipse.keyple.plugin.android.omapi

import android.content.Context
import org.eclipse.keyple.core.seproxy.ReaderPlugin
import org.eclipse.keyple.core.seproxy.SeReader
import org.eclipse.keyple.core.seproxy.plugin.AbstractPlugin
import timber.log.Timber
import java.util.*

/**
 * The AndroidOmapiPlugin interface provides the public elements used to manage the Android OMAPI plugin.
 */
internal abstract class AndroidOmapiPlugin<T, V> : AbstractPlugin(PLUGIN_NAME), ReaderPlugin {

    abstract fun connectToSe(context: Context)
    abstract fun getNativeReaders(): Array<T>?
    abstract fun mapToSeReader(nativeReader: T): SeReader

    protected var seService: V? = null
    private val params = mutableMapOf<String, String>()

    /**
     * Initialize plugin by connecting to {@link SEService}
     */
    fun init(context: Context): AndroidOmapiPlugin<T, V> {
        return if (seService != null) {
            this
        } else {
            Timber.d("Connect to SE")
            connectToSe(context.applicationContext)
            this
        }
    }

    override fun initNativeReaders(): SortedSet<SeReader> {

        Timber.d("initNativeReaders")
        val readers = sortedSetOf<SeReader>() // empty list is returned us service not connected
        getNativeReaders()?.let { nativeReaders ->
            readers.addAll(nativeReaders.map { nativeReader ->
                mapToSeReader(nativeReader)
            })
        }

        if (readers.isEmpty()) {
            Timber.w("OMAPI SeService is not connected yet")
            // throw new KeypleReaderException("OMAPI SeService is not connected yet, try again");
            // can throw an exception to notif
        }

        return readers
    }

    override fun getParameters(): MutableMap<String, String> {
        Timber.w("Android OMAPI Plugin does not support parameters, see OMAPINfcReader instead")
        return params
    }

    override fun setParameter(key: String, value: String) {
        Timber.w("Android OMAPI Plugin does not support parameters, see OMAPINfcReader instead")
        params[key] = value
    }
}
