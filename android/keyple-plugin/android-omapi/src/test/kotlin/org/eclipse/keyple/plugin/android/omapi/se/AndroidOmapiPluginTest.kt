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
package org.eclipse.keyple.plugin.android.omapi.se

import android.content.Context
import android.se.omapi.Reader
import android.se.omapi.SEService
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.eclipse.keyple.plugin.android.omapi.AbstractAndroidOmapiPlugin
import org.eclipse.keyple.plugin.android.omapi.AbstractAndroidOmapiPluginTest

internal class AndroidOmapiPluginTest : AbstractAndroidOmapiPluginTest<Reader, SEService>() {

    override lateinit var androidOmapiPlugin: AbstractAndroidOmapiPlugin<Reader, SEService>

    override fun buildAndroidOmapiPlugin(context: Context): AbstractAndroidOmapiPlugin<Reader, SEService> {
        return AndroidOmapiPlugin.init(context) as AndroidOmapiPlugin
    }

    override fun mockReader(name: String, isPresent: Boolean): Reader {
        val reader = mockk<Reader>()
        every { reader.isSecureElementPresent } returns isPresent
        every { reader.name } returns name
        return reader
    }

    override fun mockGetNativeReaders(androidOmapiPlugin: AbstractAndroidOmapiPlugin<Reader, SEService>, readersToMock: Map<String, Boolean>) {
        mockkObject(androidOmapiPlugin)
        val readers = readersToMock.map { mockReader(it.key, it.value) }.toTypedArray()
        every { androidOmapiPlugin.getNativeReaders() } returns readers
    }

    override fun triggerOnConnected() {
        (androidOmapiPlugin as AndroidOmapiPlugin).onConnected()
    }
}
