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
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.VisibleForTesting
import org.eclipse.keyple.core.service.Plugin
import org.eclipse.keyple.core.service.PluginFactory
import org.eclipse.keyple.core.service.exception.KeyplePluginInstantiationException

/**
 * Provides a factory to get the {@link AndroidOmapiPlugin}.
 *
 * @property context Android Context, cannot be null
 * @property callback Will be invoked when initiation will be done
 *
 * @since 0.9
 */
class AndroidOmapiPluginFactory(private val context: Context, callback: (AndroidOmapiPluginFactory) -> Unit) : PluginFactory {

    private var sdkVersion: Int = Build.VERSION.SDK_INT
    private var readerPlugin: AbstractAndroidOmapiPlugin<*, *>

    companion object {
        const val SIMALLIANCE_OMAPI_PACKAGE_NAME = "org.simalliance.openmobileapi.service"
    }

    init {
        readerPlugin = getPluginRegardingOsVersion()
        readerPlugin.init(context) { callback(this) }
    }

    /**
     *  sdkVersion can be forced for test purpose
     */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    constructor(context: Context, sdkVersion: Int, callback: (AndroidOmapiPluginFactory) -> Unit) : this(context, callback) {
        this.sdkVersion = sdkVersion
    }

    /**
     * {@inheritDoc}
     *
     * @since 0.9
     */
    override fun getPluginName(): String {
        return PLUGIN_NAME
    }

    /**
     * {@inheritDoc}
     *
     * @throws KeyplePluginInstantiationException if no OMAPI package is available on the device
     *
     * @since 0.9
     */
    @Throws(KeyplePluginInstantiationException::class)
    override fun getPlugin(): Plugin {
        return getPluginRegardingOsVersion()
    }

    private fun getPluginRegardingOsVersion(): AbstractAndroidOmapiPlugin<*, *> {
        return if (sdkVersion >= Build.VERSION_CODES.P)
            org.eclipse.keyple.plugin.android.omapi.se.AndroidOmapiPlugin
        else
            getPluginRegardingPackages()
    }

    @Throws(KeyplePluginInstantiationException::class)
    private fun getPluginRegardingPackages(): AbstractAndroidOmapiPlugin<*, *> {
        return try {
            context.packageManager
                    .getPackageInfo(SIMALLIANCE_OMAPI_PACKAGE_NAME, 0)
            org.eclipse.keyple.plugin.android.omapi.simalliance.AndroidOmapiPlugin
        } catch (e2: PackageManager.NameNotFoundException) {
            throw KeyplePluginInstantiationException("No OMAPI lib available within the OS")
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @Throws(KeyplePluginInstantiationException::class)
    fun pluginInstance(): Plugin {
        return this.plugin
    }
}
