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
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AndroidNfcPluginFactoryTest {

    lateinit var factory: AndroidNfcPluginFactory

    lateinit var activity: Activity

    private val readerObservationExceptionHandler = ReaderObservationExceptionHandler { pluginName, readerName, e -> }

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        factory = AndroidNfcPluginFactory(activity, readerObservationExceptionHandler)
    }

    @Test
    fun getPluginName() {
        Assert.assertEquals(AndroidNfcReaderPostNImpl(activity, readerObservationExceptionHandler).pluginName, factory.pluginName)
    }
}
