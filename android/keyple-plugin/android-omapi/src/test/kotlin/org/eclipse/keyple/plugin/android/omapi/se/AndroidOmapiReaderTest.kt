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

import android.se.omapi.Channel
import android.se.omapi.Reader
import android.se.omapi.SEService
import android.se.omapi.Session
import io.mockk.every
import io.mockk.mockk
import org.eclipse.keyple.core.seproxy.plugin.reader.util.ContactsCardCommonProtocols
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.plugin.android.omapi.AbstractAndroidOmapiReaderTest
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiSupportedProtocols

internal class AndroidOmapiReaderTest : AbstractAndroidOmapiReaderTest<Reader, AndroidOmapiReader>() {

    override lateinit var nativeReader: Reader
    override lateinit var reader: AndroidOmapiReader

    override fun buildOmapiReaderImpl(nativeReader: Reader): AndroidOmapiReader {
        var androidOmapiReader = AndroidOmapiReader(nativeReader, PLUGIN_NAME, nativeReader.name)
        androidOmapiReader.activateProtocol(AndroidOmapiSupportedProtocols.ISO_7816_3.name, ContactsCardCommonProtocols.ISO_7816_3.name)
        return androidOmapiReader
    }

    override fun getNativeReaderName(): String {
        return reader.name
    }

    override fun mockReader(): Reader {
        val nativeReader = mockk<Reader>()
        val session = mockk<Session>()
        val seService = mockk<SEService>()
        val channel = mockk<Channel>()
        val version = "3.2"

        every { nativeReader.name } returns "SIM1"
        every { nativeReader.isSecureElementPresent } returns true
        every { nativeReader.seService } returns seService
        every { nativeReader.openSession() } returns session
        every { session.openLogicalChannel(ByteArrayUtil.fromHex(PO_AID), 0x00.toByte()) } returns channel
        every { session.isClosed } returns false
        every { seService.version } returns version
        every { seService.isConnected } returns true
        every { session.atr } returns ByteArrayUtil.fromHex("")
        every { session.closeChannels() } returns Unit
        every { channel.selectResponse } returns ByteArrayUtil.fromHex(PO_AID_RESPONSE)
        every { channel.session } returns session
        every { channel.session.close() } returns Unit
        every { channel.transmit(any()) } returns ByteArrayUtil.fromHex("00000000000000000000000000000000000000000000000000000000000000009000")

        return nativeReader
    }

    override fun mockReaderWithNoAid(): Reader {

        val nativeReader = mockk<Reader>()
        val session = mockk<Session>()
        val seService = mockk<SEService>()
        val version = "2.04"

        every { nativeReader.name } returns "SIM1"
        every { nativeReader.isSecureElementPresent } returns true
        every { session.isClosed } returns false
        every { session.atr } returns ByteArrayUtil.fromHex("")
        every { session.closeChannels() } returns Unit
        every { session.openLogicalChannel(ByteArrayUtil.fromHex(PO_AID), 0) } throws NoSuchElementException("")
        every { nativeReader.seService } returns seService
        every { nativeReader.openSession() } returns session
        every { seService.version } returns version
        return nativeReader
    }

    override fun mockReaderWithNullOnOpenLogicalChannel(): Reader {

        val nativeReader = mockk<Reader>()
        val session = mockk<Session>()
        val seService = mockk<SEService>()

        every { nativeReader.name } returns "SIM1"
        every { session.isClosed } returns false
        every { session.atr } returns ByteArrayUtil.fromHex("")
        every { session.closeChannels() } returns Unit
        every { session.openLogicalChannel(ByteArrayUtil.fromHex(PO_AID), 0) } returns null
        every { nativeReader.openSession() } returns session
        every { seService.isConnected } returns true
        return nativeReader
    }

    override fun mockReaderWithExceptionOnOpenLogicalChannel(throwable: Throwable): Reader {

        val nativeReader = mockk<Reader>()
        val session = mockk<Session>()
        val seService = mockk<SEService>()

        every { nativeReader.name } returns "SIM1"
        every { session.isClosed } returns false
        every { session.atr } returns ByteArrayUtil.fromHex("")
        every { session.closeChannels() } returns Unit
        every { session.openLogicalChannel(ByteArrayUtil.fromHex(PO_AID), 0) } throws throwable
        every { nativeReader.openSession() } returns session
        every { seService.isConnected } returns true
        return nativeReader
    }

    override fun mockReaderWithNullOnOpenBasicChannel(): Reader {

        val nativeReader = mockk<Reader>()
        val session = mockk<Session>()
        val seService = mockk<SEService>()

        every { nativeReader.name } returns "SIM1"
        every { session.isClosed } returns false
        every { session.atr } returns ByteArrayUtil.fromHex("")
        every { session.closeChannels() } returns Unit
        every { session.openBasicChannel(null) } returns null
        every { nativeReader.openSession() } returns session
        every { seService.isConnected } returns true
        return nativeReader
    }

    override fun mockReaderWithExceptionOnOpenBasicChannel(throwable: Throwable): Reader {

        val nativeReader = mockk<Reader>()
        val session = mockk<Session>()
        val seService = mockk<SEService>()

        every { nativeReader.name } returns "SIM1"
        every { session.isClosed } returns false
        every { session.atr } returns ByteArrayUtil.fromHex("")
        every { session.closeChannels() } returns Unit
        every { session.openBasicChannel(null) } throws throwable
        every { nativeReader.openSession() } returns session
        every { seService.isConnected } returns true
        return nativeReader
    }

    override fun mockReaderWithExceptionOnOpenSession(throwable: Throwable): Reader {
        val nativeReader = mockk<Reader>()
        every { nativeReader.name } returns "SIM1"
        every { nativeReader.isSecureElementPresent } returns true
        every { nativeReader.openSession() } throws throwable
        return nativeReader
    }

    override fun mockReaderWithExceptionOnCloseChannel(throwable: Throwable): Reader {

        val nativeReader = mockk<Reader>()
        val session = mockk<Session>()
        val seService = mockk<SEService>()
        val channel = mockk<Channel>()
        val version = "3.2"

        every { nativeReader.name } returns "SIM1"
        every { nativeReader.isSecureElementPresent } returns true
        every { nativeReader.seService } returns seService
        every { nativeReader.openSession() } returns session
        every { session.openLogicalChannel(ByteArrayUtil.fromHex(PO_AID), 0x00.toByte()) } returns channel
        every { session.isClosed } returns false
        every { seService.version } returns version
        every { seService.isConnected } returns true
        every { session.atr } returns ByteArrayUtil.fromHex("")
        every { session.closeChannels() } returns Unit
        every { channel.selectResponse } returns ByteArrayUtil.fromHex(PO_AID_RESPONSE)
        every { channel.session } returns session
        every { channel.session.close() } throws throwable
        every { channel.transmit(any()) } returns ByteArrayUtil.fromHex("00000000000000000000000000000000000000000000000000000000000000009000")

        return nativeReader
    }

    override fun mockReaderWithExceptionWhileTransmittingApdu(throwable: Throwable): Reader {
        val nativeReader = mockk<Reader>()
        val session = mockk<Session>()
        val seService = mockk<SEService>()
        val channel = mockk<Channel>()
        val version = "3.2"

        every { nativeReader.name } returns "SIM1"
        every { nativeReader.isSecureElementPresent } returns true
        every { nativeReader.seService } returns seService
        every { nativeReader.openSession() } returns session
        every { session.openLogicalChannel(any(), any()) } returns channel
        every { session.isClosed } returns false
        every { seService.version } returns version
        every { seService.isConnected } returns true
        every { session.atr } returns ByteArrayUtil.fromHex("")
        every { session.closeChannels() } returns Unit
        every { channel.selectResponse } returns ByteArrayUtil.fromHex(PO_AID_RESPONSE)
        every { channel.session } returns session
        every { channel.session.close() } returns Unit
        every { channel.transmit(any()) } throws throwable
        return nativeReader
    }
}
