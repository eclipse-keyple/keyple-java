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
import java.util.concurrent.ConcurrentSkipListMap
import org.eclipse.keyple.core.reader.Plugin
import org.eclipse.keyple.core.reader.Reader
import org.eclipse.keyple.core.reader.plugin.AbstractPlugin
import timber.log.Timber

/**
 * The AndroidOmapiPlugin interface provides the public elements used to manage the Android OMAPI plugin.
 */
const val PLUGIN_NAME = "AndroidOmapiPlugin"

internal abstract class AbstractAndroidOmapiPlugin<T, V> : AbstractPlugin(PLUGIN_NAME), Plugin {

    abstract fun connectToSe(context: Context)
    abstract fun getNativeReaders(): Array<T>?
    abstract fun mapToReader(nativeReader: T): Reader

    protected var seService: V? = null
    private val params = mutableMapOf<String, String>()

    /**
     * Initialize plugin by connecting to {@link SEService}
     */
    fun init(context: Context): AbstractAndroidOmapiPlugin<T, V> {
        return if (seService != null) {
            this
        } else {
            Timber.d("Connect to a card")
            connectToSe(context.applicationContext)
            this
        }
    }

    override fun initNativeReaders(): ConcurrentSkipListMap<String, Reader> {

        Timber.d("initNativeReaders")
        val readers = ConcurrentSkipListMap<String, Reader>() // empty list is returned us service not connected
        getNativeReaders()?.forEach { nativeReader ->
            val reader = mapToReader(nativeReader)
            readers[reader.name] = reader
        }

        if (readers.isEmpty()) {
            Timber.w("OMAPI SeService is not connected yet")
        }

        return readers
    }
}
