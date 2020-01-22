package org.eclipse.keyple.plugin.android.omapi.se

import android.se.omapi.Channel
import android.se.omapi.Reader
import android.se.omapi.SEService
import android.se.omapi.Session
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.unmockkAll
import org.eclipse.keyple.core.seproxy.SeSelector
import org.eclipse.keyple.core.seproxy.exception.KeypleApplicationSelectionException
import org.eclipse.keyple.core.seproxy.exception.KeypleChannelControlException
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException
import org.eclipse.keyple.core.seproxy.message.ApduRequest
import org.eclipse.keyple.core.seproxy.message.SeRequest
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.*
import kotlin.NoSuchElementException

class AndroidOmapiReaderTest {
    companion object{
        private const val PLUGIN_NAME = "AndroidOmapiPluginImpl"
        private const val PO_AID = "A000000291A000000191"
        private const val PO_AID_RESPONSE = "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000"
    }

    private lateinit var omapiReader: Reader
    private lateinit var reader: AndroidOmapiReaderImpl

    @Before
    fun setUp(){
        // default reader connected with secure element with poAid
        omapiReader = mockReader()

        // instantiate reader with omapiReader
        reader = AndroidOmapiReaderImpl(omapiReader, PLUGIN_NAME, omapiReader.name)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
    /*
     * TEST READERS
     */

    @Test
    fun getInstance() {
        assertNotNull(reader)
    }

    @Test
    fun getName() {
        assertEquals(omapiReader.name, reader.name)
    }

    @Test
    fun isSEPresent() {
        assertEquals(true, reader.isSePresent)
    }

    @Test
    fun getParameters() {
        assertNotNull(reader.parameters)
    }

    @Test
    fun setParameters() {
        val parameters = HashMap<String, String>()
        parameters["key1"] = "value1"
        reader.parameters = parameters
        assertTrue(reader.parameters.size == 1)
        assertTrue(reader.parameters["key1"] == "value1")
    }

    @Test
    fun setParameter() {
        reader.setParameter("key2", "value2")
        assertTrue(reader.parameters.size == 1)
        assertTrue(reader.parameters["key2"] == "value2")
    }

    @Test(expected = KeypleIOReaderException::class)
    fun transmitIOException(){
        omapiReader = mockReaderWithExceptionOnOpenLogicalChannel(IOException())
        reader = AndroidOmapiReaderImpl(omapiReader, PLUGIN_NAME, omapiReader.name)
        // test
        reader.transmitSet(getSampleSeRequest())
    }

    @Test(expected = KeypleChannelControlException::class)
    fun transmitSecurityException(){
        omapiReader = mockReaderWithExceptionOnOpenLogicalChannel(SecurityException())
        reader = AndroidOmapiReaderImpl(omapiReader, PLUGIN_NAME, omapiReader.name)
        // test
        reader.transmitSet(getSampleSeRequest())
    }

    @Test
    fun transmitNoSuchElementException(){
        omapiReader = mockReaderWithExceptionOnOpenLogicalChannel(NoSuchElementException())
        reader = AndroidOmapiReaderImpl(omapiReader, PLUGIN_NAME, omapiReader.name)
        // test
        val seResponseList = reader.transmitSet(getSampleSeRequest())
        assertNull(seResponseList[0]) //If container is not found a null responsed is returned
    }

    @Test(expected = KeypleIOReaderException::class)
    fun transmitOpenChannelFailed(){
        omapiReader = mockReaderWithNullOnOpenLogicalChannel()
        reader = AndroidOmapiReaderImpl(omapiReader, PLUGIN_NAME, omapiReader.name)
        // test
        reader.transmitSet(getSampleSeRequest())
    }

    @Test(expected = KeypleIOReaderException::class)
    fun transmitNoAidOpenChannelFailed(){
        omapiReader = mockReaderWithNullOnOpenBasicChannel()
        reader = AndroidOmapiReaderImpl(omapiReader, PLUGIN_NAME, omapiReader.name)
        // test
        reader.transmitSet(getNoAidSampleSeRequest())
    }

    @Test(expected = KeypleIOReaderException::class)
    fun transmitNoAidIOException(){
        omapiReader = mockReaderWithExceptionOnOpenBasicChannel(IOException())
        reader = AndroidOmapiReaderImpl(omapiReader, PLUGIN_NAME, omapiReader.name)
        // test
        reader.transmitSet(getNoAidSampleSeRequest())
    }

    @Test(expected = KeypleChannelControlException::class)
    fun transmitNoAidSecurityException(){
        omapiReader = mockReaderWithExceptionOnOpenBasicChannel(SecurityException())
        reader = AndroidOmapiReaderImpl(omapiReader, PLUGIN_NAME, omapiReader.name)
        // test
        reader.transmitSet(getNoAidSampleSeRequest())
    }

    @Test
    fun transmitNoAid() {

        // init
        omapiReader = mockReaderWithNoAid()
        reader = AndroidOmapiReaderImpl(omapiReader, PLUGIN_NAME, omapiReader.name)

        // test
        val seResponseList = reader.transmitSet(getSampleSeRequest())

        // assert
        assertNull(seResponseList[0])

    }

    @Test
    fun transmitWrongProtocol() {
        // init
        val poAid = "A000000291A000000191"

        // wrong protocol
        val seRequest = SeRequest(SeSelector(SeCommonProtocols.PROTOCOL_MIFARE_UL, null,
                SeSelector.AidSelector(SeSelector.AidSelector.IsoAid(poAid), null), null), ArrayList())

        // test
        val seRequestSet = LinkedHashSet<SeRequest>()
        seRequestSet.add(seRequest)
        val seResponseList = reader.transmitSet(seRequestSet)

        // assert
        assertNull(seResponseList[0])

    }

    @Test(expected = KeypleReaderException::class)
    fun transmitNotConnected() {

        // init
        val omapiReader = mockk<Reader>()
        every { omapiReader.name } returns "SIM1"
        every { omapiReader.isSecureElementPresent} returns true
        every { omapiReader.openSession() } throws IOException()
        reader = AndroidOmapiReaderImpl(omapiReader, PLUGIN_NAME, omapiReader.name)

        // test
        reader.transmitSet(getSampleSeRequest())
    }

    private fun mockReader(): Reader{
        val omapiReader = mockk<Reader>()
        val session = mockk<Session>()
        val seService = mockk<SEService>()
        val channel = mockk<Channel>()
        val version = "3.2"


        every { omapiReader.name } returns "SIM1"
        every { omapiReader.isSecureElementPresent} returns true
        every { omapiReader.seService } returns seService
        every { omapiReader.openSession() } returns session
        every { session.openLogicalChannel(ByteArrayUtil.fromHex(PO_AID), 0x00.toByte()) } returns channel
        every { seService.version } returns version
        every { session.atr} returns null
        every { channel.selectResponse } returns ByteArrayUtil.fromHex(PO_AID_RESPONSE)
        every { channel.session } returns session
        every { channel.transmit(ByteArrayUtil.fromHex("00B201A420")) } returns ByteArrayUtil.fromHex("00000000000000000000000000000000000000000000000000000000000000009000")

        return omapiReader
    }

    private fun mockReaderWithNoAid(): Reader {

        val omapiReader = mockk<Reader>()
        val session = mockk<Session>()
        val seService = mockk<SEService>()
        val version = "2.04"

        every { omapiReader.name } returns "SIM1"
        every { omapiReader.isSecureElementPresent} returns true
        every { session.isClosed } returns false
        every { session.atr} returns null
        every { session.openLogicalChannel(ByteArrayUtil.fromHex(PO_AID), 0) } throws NoSuchElementException("")
        every { omapiReader.seService } returns seService
        every { omapiReader.openSession() } returns session
        every { seService.version } returns version
        return omapiReader

    }

    private fun mockReaderWithNullOnOpenLogicalChannel(): Reader{

        val omapiReader = mockk<Reader>()
        val session = mockk<Session>()

        every { omapiReader.name } returns "SIM1"
        every { session.isClosed } returns false
        every { session.atr} returns null
        every { session.openLogicalChannel(ByteArrayUtil.fromHex(PO_AID), 0) } returns null
        every { omapiReader.openSession() } returns session
        return omapiReader
    }

    private fun mockReaderWithExceptionOnOpenLogicalChannel(exception: Throwable): Reader{

        val omapiReader = mockk<Reader>()
        val session = mockk<Session>()

        every { omapiReader.name } returns "SIM1"
        every { session.isClosed } returns false
        every { session.atr} returns null
        every { session.openLogicalChannel(ByteArrayUtil.fromHex(PO_AID), 0) } throws exception
        every { omapiReader.openSession() } returns session
        return omapiReader
    }

    private fun mockReaderWithNullOnOpenBasicChannel(): Reader{

        val omapiReader = mockk<Reader>()
        val session = mockk<Session>()

        every { omapiReader.name } returns "SIM1"
        every { session.isClosed } returns false
        every { session.atr} returns null
        every { session.openBasicChannel(null) } returns null
        every { omapiReader.openSession() } returns session
        return omapiReader
    }

    private fun mockReaderWithExceptionOnOpenBasicChannel(exception: Throwable): Reader{

        val omapiReader = mockk<Reader>()
        val session = mockk<Session>()

        every { omapiReader.name } returns "SIM1"
        every { session.isClosed } returns false
        every { session.atr} returns null
        every { session.openBasicChannel(null) } throws exception
        every { omapiReader.openSession() } returns session
        return omapiReader
    }

    private fun getSampleSeRequest(): Set<SeRequest> {

        val poApduRequestList = Arrays.asList(ApduRequest(ByteArrayUtil.fromHex("0000"), true))

        val seRequest = SeRequest(SeSelector(SeCommonProtocols.PROTOCOL_ISO7816_3, null,
                SeSelector.AidSelector(SeSelector.AidSelector.IsoAid(PO_AID), null), null), poApduRequestList)

        val seRequestSet = LinkedHashSet<SeRequest>()
        seRequestSet.add(seRequest)
        return seRequestSet

    }

    private fun getNoAidSampleSeRequest(): Set<SeRequest> {

        val poApduRequestList = Arrays.asList(ApduRequest(ByteArrayUtil.fromHex("0000"), true))

        val seRequest = SeRequest(SeSelector(SeCommonProtocols.PROTOCOL_ISO7816_3, null,
                SeSelector.AidSelector(null, null), null), poApduRequestList)

        val seRequestSet = LinkedHashSet<SeRequest>()
        seRequestSet.add(seRequest)
        return seRequestSet

    }
}