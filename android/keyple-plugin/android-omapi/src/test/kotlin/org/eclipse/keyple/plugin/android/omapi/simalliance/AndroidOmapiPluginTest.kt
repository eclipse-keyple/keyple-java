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
package org.eclipse.keyple.plugin.android.omapi.simalliance

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.eclipse.keyple.plugin.android.omapi.AbstractAndroidOmapiPlugin
import org.eclipse.keyple.plugin.android.omapi.AbstractAndroidOmapiPluginTest
import org.simalliance.openmobileapi.Reader
import org.simalliance.openmobileapi.SEService

internal class AndroidOmapiPluginTest : AbstractAndroidOmapiPluginTest<Reader, SEService>() {

    override lateinit var androidOmapiPlugin: AbstractAndroidOmapiPlugin<Reader, SEService>

    override fun buildAndroidOmapiPlugin(context: Context): AndroidOmapiPlugin {
        return AndroidOmapiPlugin.init(context) as AndroidOmapiPlugin
    }

    override fun mockReader(name: String, isPresent: Boolean): Reader {
        val reader = mockk<Reader>()
        val seService = mockk<SEService>()
        val version = "3.2"
        every { reader.isSecureElementPresent } returns isPresent
        every { reader.name } returns name
        every { reader.seService } returns seService
        every { seService.version } returns version
        return reader
    }

    override fun mockGetNativeReaders(androidOmapiPlugin: AbstractAndroidOmapiPlugin<Reader, SEService>, readersToMock: Map<String, Boolean>) {
        mockkObject(androidOmapiPlugin)
        val readers = readersToMock.map { mockReader(it.key, it.value) }.toTypedArray()
        every { androidOmapiPlugin.getNativeReaders() } returns readers
    }

    override fun triggerOnConnected() {
        val seService = mockk<SEService>()
        (androidOmapiPlugin as AndroidOmapiPlugin).serviceConnected(seService)
    }
}
