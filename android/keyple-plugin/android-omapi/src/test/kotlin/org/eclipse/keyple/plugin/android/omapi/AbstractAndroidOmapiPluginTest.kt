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
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import java.util.concurrent.CountDownLatch
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

internal abstract class AbstractAndroidOmapiPluginTest<T, V> {

    companion object {
        private val READERS_TO_MOCK = mapOf(Pair("SIM1", true), Pair("SIM2", false))
    }

    lateinit var context: Context
    internal abstract var androidOmapiPlugin: AbstractAndroidOmapiPlugin<T, V>

    internal abstract fun buildAndroidOmapiPlugin(context: Context): AbstractAndroidOmapiPlugin<T, V>
    abstract fun mockReader(name: String, isPresent: Boolean): T
    internal abstract fun mockGetNativeReaders(androidOmapiPlugin: AbstractAndroidOmapiPlugin<T, V>, readersToMock: Map<String, Boolean>)

    @Before
    fun setUp() {
        val cdl = CountDownLatch(1)
        context = mockContext()
        androidOmapiPlugin = buildAndroidOmapiPlugin(context)
        mockkObject(androidOmapiPlugin)

        // We mock hardware init
        every { androidOmapiPlugin.connectToSe(any(), any()) } answers { secondArg<() -> Unit>().invoke() }

        androidOmapiPlugin.init(context) {
            cdl.countDown()
        }
        cdl.await()
        mockGetNativeReaders(androidOmapiPlugin, READERS_TO_MOCK)
        androidOmapiPlugin.register()
    }

    @After
    fun tearDown() {
        androidOmapiPlugin.unregister()
        unmockkAll()
    }

    @Test
    fun connectToSe() {
        androidOmapiPlugin.connectToSe(context) {
        }
    }

    @Test
    fun getNativeReaders() {
        Assert.assertNotNull(androidOmapiPlugin.readers)
    }

    @Test
    fun mapToReader() {
        Companion.READERS_TO_MOCK.forEach {
            val omapiReader = mockReader(it.key, it.value)
            val reader = androidOmapiPlugin.mapToReader(omapiReader)
            Assert.assertNotNull(reader)
            Assert.assertEquals(it.key, reader.name)
            Assert.assertEquals(it.value, reader.isCardPresent)
        }
    }

    private fun mockContext(): Context {
        val context = mockk<Context>()
        every { context.applicationContext } returns context
        every { context.bindService(any(), any(), any()) } returns true
        return context
    }
}
