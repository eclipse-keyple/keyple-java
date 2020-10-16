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
import org.eclipse.keyple.core.reader.Plugin
import org.eclipse.keyple.core.reader.PluginFactory
import org.eclipse.keyple.core.reader.exception.KeyplePluginInstantiationException

/**
 * Build asynchronously the Android OMAPI plugin.
 * Platform incompabilities are not managed
 */
class AndroidOmapiPluginFactory(private val context: Context) : PluginFactory {

    private var sdkVersion: Int = Build.VERSION.SDK_INT

    companion object {
        const val SIMALLIANCE_OMAPI_PACKAGE_NAME = "org.simalliance.openmobileapi.service"
    }

    /**
     *  sdkVersion can be forced for test purpose
     */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    constructor(context: Context, sdkVersion: Int) : this(context) {
        this.sdkVersion = sdkVersion
    }

    override fun getPluginName(): String {
        return PLUGIN_NAME
    }

    @Throws(KeyplePluginInstantiationException::class)
    override fun getPlugin(): Plugin {
        return getPluginRegardingOsVersion()
    }

    private fun getPluginRegardingOsVersion(): Plugin {
        return if (sdkVersion >= Build.VERSION_CODES.P)
            org.eclipse.keyple.plugin.android.omapi.se.AndroidOmapiPlugin.init(context)
        else
            getPluginRegardingPackages()
    }

    @Throws(KeyplePluginInstantiationException::class)
    private fun getPluginRegardingPackages(): Plugin {
        return try {
            context.packageManager
                    .getPackageInfo(SIMALLIANCE_OMAPI_PACKAGE_NAME, 0)
            org.eclipse.keyple.plugin.android.omapi.simalliance.AndroidOmapiPlugin.init(context)
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
