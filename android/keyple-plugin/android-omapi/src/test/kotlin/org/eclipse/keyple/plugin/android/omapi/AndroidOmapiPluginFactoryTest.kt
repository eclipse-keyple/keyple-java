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
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginInstantiationException
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AndroidOmapiPluginFactoryTest {

    private lateinit var androidOmapiPluginFactory: AndroidOmapiPluginFactory

    @MockK
    lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { context.applicationContext } returns context
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun getPluginName() {
        androidOmapiPluginFactory = AndroidOmapiPluginFactory(context)
        Assert.assertEquals(PLUGIN_NAME, androidOmapiPluginFactory.pluginName)
    }
    @Test
    fun getPluginInstanceForOSSup28() {
        androidOmapiPluginFactory = AndroidOmapiPluginFactory(context, 28)
        var readerPlugin = androidOmapiPluginFactory.getPlugin()
        Assert.assertTrue(readerPlugin is org.eclipse.keyple.plugin.android.omapi.se.AndroidOmapiPlugin)

        androidOmapiPluginFactory = AndroidOmapiPluginFactory(context, 29)
        readerPlugin = androidOmapiPluginFactory.getPlugin()
        Assert.assertTrue(readerPlugin is org.eclipse.keyple.plugin.android.omapi.se.AndroidOmapiPlugin)
    }

    @Test
    fun getPluginInstanceForOSLess28WithSimAlliance() {
        every { context.bindService(any(), any(), any()) } returns false
        val packageInfo = mockk<PackageInfo>()
        every {
            context.packageManager.getPackageInfo(AndroidOmapiPluginFactory.SIMALLIANCE_OMAPI_PACKAGE_NAME, any())
        } returns packageInfo
        androidOmapiPluginFactory = AndroidOmapiPluginFactory(context, 27)
        val readerPlugin = androidOmapiPluginFactory.pluginInstance()
        Assert.assertTrue(readerPlugin is org.eclipse.keyple.plugin.android.omapi.simalliance.AndroidOmapiPlugin)
    }

    @Test(expected = KeyplePluginInstantiationException::class)
    fun exceptionForOSLess28WithoutSimalliance() {
        every { context.bindService(any(), any(), any()) } returns false
        every {
            context.packageManager.getPackageInfo(AndroidOmapiPluginFactory.SIMALLIANCE_OMAPI_PACKAGE_NAME, any())
        } throws PackageManager.NameNotFoundException()
        androidOmapiPluginFactory = AndroidOmapiPluginFactory(context, 27)
        val readerPlugin = androidOmapiPluginFactory.pluginInstance()
        Assert.assertNull(readerPlugin)
    }
}
