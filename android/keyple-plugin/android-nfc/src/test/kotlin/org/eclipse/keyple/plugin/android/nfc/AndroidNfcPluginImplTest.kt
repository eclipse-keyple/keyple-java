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

import android.app.Activity
import java.io.IOException
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler
import org.eclipse.keyple.core.service.exception.KeypleReaderException
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AndroidNfcPluginImplTest {

    private lateinit var plugin: AndroidNfcPluginImpl

    lateinit var activity: Activity

    private val readerObservationExceptionHandler = ReaderObservationExceptionHandler { pluginName, readerName, e -> }

    // init before each test
    @Before
    @Throws(IOException::class)
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        plugin = AndroidNfcPluginImpl(activity, readerObservationExceptionHandler)
        // get unique instance
        plugin.register()
    }

    @After
    fun tearDown() {
        plugin.unregister()
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
        Assert.assertTrue(plugin.readers.values.first() is AbstractAndroidNfcReader)
    }

    @Test
    @Throws(Exception::class)
    fun getName() {
        Assert.assertEquals(AndroidNfcPluginImpl(activity, readerObservationExceptionHandler).name, plugin.name)
    }

    /*
     * TEST INTERNAL METHODS
     */

    @Test
    @Throws(Exception::class)
    fun getNativeReader() {
        Assert.assertTrue(plugin.getReader(AndroidNfcReaderPostNImpl(activity, readerObservationExceptionHandler).name) is AndroidNfcReaderPostNImpl)
    }

    @Test
    @Throws(Exception::class)
    fun getNativeReaders() {
        Assert.assertEquals(1, plugin.readers.size)
        Assert.assertTrue(plugin.readers.values.first() is AbstractAndroidNfcReader)
    }
}
