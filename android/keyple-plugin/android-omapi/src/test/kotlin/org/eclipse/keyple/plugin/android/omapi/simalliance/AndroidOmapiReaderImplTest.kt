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

import io.mockk.every
import io.mockk.mockk
import kotlin.NoSuchElementException
import kotlin.collections.LinkedHashSet
import org.eclipse.keyple.core.seproxy.SeSelector
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException
import org.eclipse.keyple.core.seproxy.message.ApduRequest
import org.eclipse.keyple.core.seproxy.message.SeRequest
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiReaderTest
import org.junit.Assert
import org.junit.Test
import org.simalliance.openmobileapi.Channel
import org.simalliance.openmobileapi.Reader
import org.simalliance.openmobileapi.SEService
import org.simalliance.openmobileapi.Session

internal class AndroidOmapiReaderImplTest : AndroidOmapiReaderTest<Reader, AndroidOmapiReaderImpl>() {

    override lateinit var nativeReader: Reader
    override lateinit var reader: AndroidOmapiReaderImpl

    override fun buildOmapiReaderImpl(nativeReader: Reader): AndroidOmapiReaderImpl {
        return AndroidOmapiReaderImpl(nativeReader, PLUGIN_NAME, nativeReader.name)
    }

    override fun getNativeReaderName(): String {
        return nativeReader.name
    }

    @Test(expected = KeypleIOReaderException::class)
    fun notUsingDefaultFOAndFCIOnPreSimAlliance30() {

        nativeReader = mockReaderForP2Test("2.04")
        reader = buildOmapiReaderImpl(nativeReader)

        val poApduRequestList = listOf(ApduRequest(ByteArrayUtil.fromHex("0000"), true))

        val seRequest = SeRequest(SeSelector(SeCommonProtocols.PROTOCOL_ISO7816_3, null,
                SeSelector.AidSelector(SeSelector.AidSelector.IsoAid(PO_AID), null,
                        SeSelector.AidSelector.FileOccurrence.NEXT,
                        SeSelector.AidSelector.FileControlInformation.FCI), null),
                poApduRequestList)

        val seRequestSet = LinkedHashSet<SeRequest>()
        seRequestSet.add(seRequest)

        reader.transmit(seRequest)
    }

    @Test
    fun usingP2InPostSimAlliance30() {
        nativeReader = mockReaderForP2Test("3")
        reader = buildOmapiReaderImpl(nativeReader)

        val poApduRequestList = listOf(ApduRequest(ByteArrayUtil.fromHex("0000"), true))

        val seRequest = SeRequest(SeSelector(SeCommonProtocols.PROTOCOL_ISO7816_3, null,
                SeSelector.AidSelector(SeSelector.AidSelector.IsoAid(PO_AID), null,
                        SeSelector.AidSelector.FileOccurrence.NEXT,
                        SeSelector.AidSelector.FileControlInformation.FCI), null),
                poApduRequestList)

        val seRequestSet = LinkedHashSet<SeRequest>()
        seRequestSet.add(seRequest)

        reader.transmit(seRequest)
        val seResponseList = reader.transmitSet(seRequestSet)

        // assert
        Assert.assertNotNull(seResponseList[0])
    }

    override fun mockReader(): Reader {
        val omapiReader = mockk<Reader>()
        val session = mockk<Session>()
        val seService = mockk<SEService>()
        val channel = mockk<Channel>()
        val version = "3.2"

        every { omapiReader.name } returns "SIM1"
        every { omapiReader.isSecureElementPresent } returns true
        every { omapiReader.seService } returns seService
        every { omapiReader.openSession() } returns session
        every { session.openLogicalChannel(ByteArrayUtil.fromHex(PO_AID)) } returns channel
        every { seService.version } returns version
        every { session.atr } returns null
        every { session.isClosed } returns false
        every { channel.selectResponse } returns ByteArrayUtil.fromHex(PO_AID_RESPONSE)
        every { channel.session } returns session
        every { channel.session.close() } returns Unit
        every { channel.transmit(any()) } returns ByteArrayUtil.fromHex("00000000000000000000000000000000000000000000000000000000000000009000")

        return omapiReader
    }

    override fun mockReaderWithExceptionOnOpenLogicalChannel(throwable: Throwable): Reader {
        val omapiReader = mockk<Reader>()
        val session = mockk<Session>()
        val seService = mockk<SEService>()
        val version = "3.2"

        every { omapiReader.name } returns "SIM1"
        every { session.isClosed } returns false
        every { session.atr } returns null
        every { session.openLogicalChannel(ByteArrayUtil.fromHex(PO_AID)) } throws throwable
        every { omapiReader.openSession() } returns session
        every { omapiReader.seService } returns seService
        every { seService.version } returns version
        return omapiReader
    }

    override fun mockReaderWithNullOnOpenLogicalChannel(): Reader {
        val omapiReader = mockk<Reader>()
        val session = mockk<Session>()
        val seService = mockk<SEService>()
        val version = "3.2"

        every { omapiReader.name } returns "SIM1"
        every { session.isClosed } returns false
        every { session.atr } returns null
        every { session.openLogicalChannel(ByteArrayUtil.fromHex(PO_AID)) } returns null
        every { omapiReader.openSession() } returns session
        every { omapiReader.seService } returns seService
        every { seService.version } returns version
        return omapiReader
    }

    override fun mockReaderWithExceptionOnOpenBasicChannel(throwable: Throwable): Reader {
        val omapiReader = mockk<Reader>()
        val session = mockk<Session>()
        val seService = mockk<SEService>()
        val version = "3.2"

        every { omapiReader.name } returns "SIM1"
        every { session.isClosed } returns false
        every { session.atr } returns null
        every { session.openBasicChannel(null) } throws throwable
        every { omapiReader.openSession() } returns session
        every { omapiReader.seService } returns seService
        every { seService.version } returns version
        return omapiReader
    }

    override fun mockReaderWithNullOnOpenBasicChannel(): Reader {
        val omapiReader = mockk<Reader>()
        val session = mockk<Session>()
        val seService = mockk<SEService>()
        val version = "3.2"

        every { omapiReader.name } returns "SIM1"
        every { session.isClosed } returns false
        every { session.atr } returns null
        every { session.openBasicChannel(null) } returns null
        every { omapiReader.openSession() } returns session
        every { omapiReader.seService } returns seService
        every { seService.version } returns version
        return omapiReader
    }

    override fun mockReaderWithNoAid(): Reader {
        val omapiReader = mockk<Reader>()
        val session = mockk<Session>()
        val seService = mockk<SEService>()
        val version = "2.04"

        every { omapiReader.name } returns "SIM1"
        every { omapiReader.isSecureElementPresent } returns true
        every { session.isClosed } returns false
        every { session.atr } returns null
        every { session.openLogicalChannel(ByteArrayUtil.fromHex(PO_AID)) } throws NoSuchElementException("")
        every { omapiReader.seService } returns seService
        every { omapiReader.openSession() } returns session
        every { seService.version } returns version
        return omapiReader
    }

    fun mockReaderForP2Test(omapiVersion: String): Reader {
        val nativeReader = mockk<Reader>()
        val session = mockk<Session>()
        val seService = mockk<SEService>()
        val channel = mockk<Channel>()

        every { nativeReader.name } returns "SIM1"
        every { nativeReader.isSecureElementPresent } returns true
        every { session.isClosed } returns false
        every { session.atr } returns null
        every { session.openLogicalChannel(ByteArrayUtil.fromHex(PO_AID)) } returns channel
        every { session.openLogicalChannel(ByteArrayUtil.fromHex(PO_AID), any()) } returns channel
        every { nativeReader.seService } returns seService
        every { nativeReader.openSession() } returns session
        every { seService.version } returns omapiVersion
        every { channel.selectResponse } returns ByteArrayUtil.fromHex(PO_AID_RESPONSE)
        every { channel.session } returns session
        every { channel.transmit(any()) } returns ByteArrayUtil.fromHex("00000000000000000000000000000000000000000000000000000000000000009000")

        return nativeReader
    }

    override fun mockReaderWithExceptionOnOpenSession(throwable: Throwable): Reader {
        val nativeReader = mockk<Reader>()
        val seService = mockk<SEService>()
        val version = "3.0"
        every { nativeReader.name } returns "SIM1"
        every { nativeReader.isSecureElementPresent } returns true
        every { nativeReader.openSession() } throws throwable
        every { nativeReader.seService } returns seService
        every { seService.version } returns version
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
        every { session.openLogicalChannel(any()) } returns channel
        every { session.isClosed } returns false
        every { seService.version } returns version
        every { session.atr } returns null
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
        every { session.openLogicalChannel(any()) } returns channel
        every { session.isClosed } returns false
        every { seService.version } returns version
        every { session.atr } returns null
        every { channel.selectResponse } returns ByteArrayUtil.fromHex(PO_AID_RESPONSE)
        every { channel.session } returns session
        every { channel.session.close() } returns Unit
        every { channel.transmit(any()) } throws throwable

        return nativeReader
    }
}
