/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.keyple.plugin.android.omapi

import android.content.Context
import android.content.pm.PackageManager
import org.eclipse.keyple.core.seproxy.AbstractPluginFactory
import org.eclipse.keyple.core.seproxy.ReaderPlugin
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginInstantiationException

/**
 * Build asynchronously the Android OMAPI plugin.
 * Platform incompabilities are not managed
 */
class AndroidOmapiPluginFactory(private val context: Context) : AbstractPluginFactory() {

    companion object{
        private const val SIMALLIANCE_OMAPI_PACKAGE_NAME = "org.simalliance.openmobileapi.service"
    }

    override fun getPluginName(): String {
        return AndroidOmapiPlugin.PLUGIN_NAME
    }

    //TODO throw error is Android is not compatible with OMAPI
    @Throws(KeyplePluginInstantiationException::class)
    override fun getPluginInstance(): ReaderPlugin{
        return getReaderPluginRegardingOsVersion()
    }

    private fun getReaderPluginRegardingOsVersion(): ReaderPlugin{
        return if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P)
            org.eclipse.keyple.plugin.android.omapi.se.AndroidOmapiPluginImpl.init(context)
        else
            getReaderPluginRegardingPackages()
    }

    @Throws(KeyplePluginInstantiationException::class)
    private fun getReaderPluginRegardingPackages(): ReaderPlugin{
        return try {
            context.packageManager
                    .getPackageInfo(SIMALLIANCE_OMAPI_PACKAGE_NAME, 0)
            org.eclipse.keyple.plugin.android.omapi.simalliance.AndroidOmapiPluginImpl.init(context)
        } catch (e2: PackageManager.NameNotFoundException) {
            throw KeyplePluginInstantiationException("No OMAPI lib available within the OS")
        }

    }
}
