package org.eclipse.keyple.plugin.android.omapi.se

import android.se.omapi.Channel
import android.se.omapi.Reader
import android.se.omapi.SEService
import android.se.omapi.Session
import io.mockk.every
import io.mockk.mockk
import kotlin.NoSuchElementException
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiReaderTest

internal class AndroidOmapiReaderImplTest : AndroidOmapiReaderTest<Reader, AndroidOmapiReaderImpl>() {

    override lateinit var nativeReader: Reader
    override lateinit var reader: AndroidOmapiReaderImpl

    override fun buildOmapiReaderImpl(nativeReader: Reader): AndroidOmapiReaderImpl {
        return AndroidOmapiReaderImpl(nativeReader, PLUGIN_NAME, nativeReader.name)
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
        every { session.atr } returns null
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
        every { session.atr } returns null
        every { session.openLogicalChannel(ByteArrayUtil.fromHex(PO_AID), 0) } throws NoSuchElementException("")
        every { nativeReader.seService } returns seService
        every { nativeReader.openSession() } returns session
        every { seService.version } returns version
        return nativeReader
    }

    override fun mockReaderWithNullOnOpenLogicalChannel(): Reader {

        val nativeReader = mockk<Reader>()
        val session = mockk<Session>()

        every { nativeReader.name } returns "SIM1"
        every { session.isClosed } returns false
        every { session.atr } returns null
        every { session.openLogicalChannel(ByteArrayUtil.fromHex(PO_AID), 0) } returns null
        every { nativeReader.openSession() } returns session
        return nativeReader
    }

    override fun mockReaderWithExceptionOnOpenLogicalChannel(exception: Throwable): Reader {

        val nativeReader = mockk<Reader>()
        val session = mockk<Session>()

        every { nativeReader.name } returns "SIM1"
        every { session.isClosed } returns false
        every { session.atr } returns null
        every { session.openLogicalChannel(ByteArrayUtil.fromHex(PO_AID), 0) } throws exception
        every { nativeReader.openSession() } returns session
        return nativeReader
    }

    override fun mockReaderWithNullOnOpenBasicChannel(): Reader {

        val nativeReader = mockk<Reader>()
        val session = mockk<Session>()

        every { nativeReader.name } returns "SIM1"
        every { session.isClosed } returns false
        every { session.atr } returns null
        every { session.openBasicChannel(null) } returns null
        every { nativeReader.openSession() } returns session
        return nativeReader
    }

    override fun mockReaderWithExceptionOnOpenBasicChannel(exception: Throwable): Reader {

        val nativeReader = mockk<Reader>()
        val session = mockk<Session>()

        every { nativeReader.name } returns "SIM1"
        every { session.isClosed } returns false
        every { session.atr } returns null
        every { session.openBasicChannel(null) } throws exception
        every { nativeReader.openSession() } returns session
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
        every { session.openLogicalChannel(any(), any()) } returns channel
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
