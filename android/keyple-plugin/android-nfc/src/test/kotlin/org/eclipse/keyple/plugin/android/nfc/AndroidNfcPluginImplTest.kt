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

import java.io.IOException
import org.eclipse.keyple.core.service.exception.KeypleReaderException
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AndroidNfcPluginImplTest {

    private lateinit var plugin: AndroidNfcPluginImpl

    // init before each test
    @Before
    @Throws(IOException::class)
    fun setUp() {
        // get unique instance
        plugin = AndroidNfcPluginImpl
    }

    /*
     * TEST PUBLIC METHODS
     */

    @Test
    @Throws(IOException::class)
    fun getInstance() {
        Assert.assertNotNull(plugin)
    }

    @Test
    fun pluginName() {
        Assert.assertEquals("AndroidNfcPlugin", AndroidNfcPlugin.PLUGIN_NAME)
    }

    @Test
    @Throws(KeypleReaderException::class)
    fun getReaders() {
        Assert.assertEquals(1, plugin.readers.size)
        Assert.assertTrue(plugin.readers.values.first() is AndroidNfcReaderImpl)
    }

    @Test
    @Throws(Exception::class)
    fun getName() {
        Assert.assertEquals(AndroidNfcPluginImpl.name, plugin.name)
    }

    /*
     * TEST INTERNAL METHODS
     */

    @Test
    @Throws(Exception::class)
    fun getNativeReader() {
        Assert.assertTrue(plugin.getReader(AndroidNfcReaderImpl.name) is AndroidNfcReaderImpl)
    }

    @Test
    @Throws(Exception::class)
    fun getNativeReaders() {
        Assert.assertEquals(1, plugin.readers.size)
        Assert.assertTrue(plugin.readers.values.first() is AndroidNfcReaderImpl)
    }
}
