package org.eclipse.keyple.plugin.android.omapi

import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import java.io.IOException
import kotlin.NoSuchElementException
import org.eclipse.keyple.core.seproxy.ChannelControl
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing
import org.eclipse.keyple.core.seproxy.SeSelector
import org.eclipse.keyple.core.seproxy.exception.KeypleChannelControlException
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException
import org.eclipse.keyple.core.seproxy.message.ApduRequest
import org.eclipse.keyple.core.seproxy.message.SeRequest
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

internal abstract class AndroidOmapiReaderTest<T, V : AndroidOmapiReader> {

    companion object {
        internal const val PLUGIN_NAME = "AndroidOmapiPluginImpl"
        internal const val PO_AID = "A000000291A000000191"
        internal const val PO_AID_RESPONSE = "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000"
    }

    abstract var nativeReader: T
    abstract var reader: V

    abstract fun mockReader(): T
    abstract fun mockReaderWithExceptionOnOpenLogicalChannel(throwable: Throwable): T
    abstract fun mockReaderWithNullOnOpenLogicalChannel(): T
    abstract fun mockReaderWithExceptionOnOpenBasicChannel(throwable: Throwable): T
    abstract fun mockReaderWithNullOnOpenBasicChannel(): T
    abstract fun mockReaderWithNoAid(): T
    abstract fun mockReaderWithExceptionOnOpenSession(throwable: Throwable): T
    abstract fun buildOmapiReaderImpl(nativeReader: T): V
    abstract fun getNativeReaderName(): String
    abstract fun mockReaderWithExceptionOnCloseChannel(throwable: Throwable): T
    abstract fun mockReaderWithExceptionWhileTransmittingApdu(throwable: Throwable): T

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        // default reader connected with secure element with poAid
        nativeReader = mockReader()

        // instantiate reader with nativeReader
        reader = buildOmapiReaderImpl(nativeReader)
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
        Assert.assertNotNull(reader)
    }

    @Test
    fun getName() {
        Assert.assertEquals(getNativeReaderName(), reader.name)
    }

    @Test
    fun getTransmissionMode() {
        Assert.assertEquals(TransmissionMode.CONTACTS, reader.transmissionMode)
    }

    @Test
    fun isSEPresent() {
        Assert.assertEquals(true, reader.isSePresent)
    }

    @Test
    fun getParameters() {
        Assert.assertNotNull(reader.parameters)
    }

    @Test
    fun setParameters() {
        val parameters = HashMap<String, String>()
        parameters["key1"] = "value1"
        reader.parameters = parameters
        Assert.assertTrue(reader.parameters.size == 1)
        Assert.assertTrue(reader.parameters["key1"] == "value1")
    }

    @Test
    fun setParameter() {
        reader.setParameter("key2", "value2")
        Assert.assertTrue(reader.parameters.size == 1)
        Assert.assertTrue(reader.parameters["key2"] == "value2")
    }

    @Test(expected = KeypleIOReaderException::class)
    fun transmitIOException() {
        nativeReader = mockReaderWithExceptionOnOpenLogicalChannel(IOException())
        reader = buildOmapiReaderImpl(nativeReader)
        // test
        reader.transmitSet(getSampleSeRequest())
    }

    @Test(expected = KeypleChannelControlException::class)
    fun transmitSecurityException() {
        nativeReader = mockReaderWithExceptionOnOpenLogicalChannel(SecurityException())
        reader = buildOmapiReaderImpl(nativeReader)
        // test
        reader.transmitSet(getSampleSeRequest())
    }

    @Test
    fun transmitNoSuchElementException() {
        nativeReader = mockReaderWithExceptionOnOpenLogicalChannel(NoSuchElementException())
        reader = buildOmapiReaderImpl(nativeReader)
        // test
        val seResponseList = reader.transmitSet(getSampleSeRequest())
        Assert.assertNull(seResponseList[0]) // If container is not found a null responsed is returned
    }

    @Test(expected = KeypleIOReaderException::class)
    fun transmitOpenChannelFailed() {
        nativeReader = mockReaderWithNullOnOpenLogicalChannel()
        reader = buildOmapiReaderImpl(nativeReader)
        // test
        reader.transmitSet(getSampleSeRequest())
    }

    @Test(expected = KeypleIOReaderException::class)
    fun transmitNoAidOpenChannelFailed() {
        nativeReader = mockReaderWithNullOnOpenBasicChannel()
        reader = buildOmapiReaderImpl(nativeReader)
        // test
        reader.transmitSet(getNoAidSampleSeRequest())
    }

    @Test(expected = KeypleIOReaderException::class)
    fun transmitNoAidIOException() {
        nativeReader = mockReaderWithExceptionOnOpenBasicChannel(IOException())
        reader = buildOmapiReaderImpl(nativeReader)
        // test
        reader.transmitSet(getNoAidSampleSeRequest())
    }

    @Test(expected = KeypleChannelControlException::class)
    fun transmitNoAidSecurityException() {
        nativeReader = mockReaderWithExceptionOnOpenBasicChannel(SecurityException())
        reader = buildOmapiReaderImpl(nativeReader)
        // test
        reader.transmitSet(getNoAidSampleSeRequest())
    }

    @Test
    fun transmitNoAid() {

        // init
        nativeReader = mockReaderWithNoAid()
        reader = buildOmapiReaderImpl(nativeReader)

        // test
        val seResponseList = reader.transmitSet(getSampleSeRequest())

        // assert
        Assert.assertNull(seResponseList[0])
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
        Assert.assertNull(seResponseList[0])
    }

    @Test(expected = KeypleReaderException::class)
    fun transmitNotConnected() {

        // init
        val nativeReader = mockReaderWithExceptionOnOpenSession(IOException())
        reader = buildOmapiReaderImpl(nativeReader)

        // test
        reader.transmitSet(getSampleSeRequest())
    }

    @Test
    fun closeChannelAfterTransmit() {

        // init
        val nativeReader = mockReader()
        reader = buildOmapiReaderImpl(nativeReader)

        // test
        val seResponseList = reader.transmitSet(getSampleSeRequest(), MultiSeRequestProcessing.PROCESS_ALL, ChannelControl.CLOSE_AFTER)
        Assert.assertNotNull(seResponseList)
    }

    @Test(expected = KeypleIOReaderException::class)
    @Ignore("Implementation of closePhysicalChannel() may encounter a IOException, it should be catched as a KeypleIOReaderException, it is not")
    fun exceptionOnChannelAfterTransmit() {
        // init
        val nativeReader = mockReaderWithExceptionOnCloseChannel(IOException())
        reader = buildOmapiReaderImpl(nativeReader)

        // test
        reader.transmitSet(getSampleSeRequest(), MultiSeRequestProcessing.PROCESS_ALL, ChannelControl.CLOSE_AFTER)
    }

    @Test(expected = KeypleIOReaderException::class)
    fun exceptionWhileTransmittingApdu() {
        // init
        val nativeReader = mockReaderWithExceptionWhileTransmittingApdu(IOException())
        reader = buildOmapiReaderImpl(nativeReader)

        // test
        reader.transmitSet(getSampleSeRequest())
    }

    private fun getSampleSeRequest(): Set<SeRequest> {

        val poApduRequestList = listOf(ApduRequest(ByteArrayUtil.fromHex("0000"), true))

        val seRequest = SeRequest(SeSelector(SeCommonProtocols.PROTOCOL_ISO7816_3, null,
                SeSelector.AidSelector(SeSelector.AidSelector.IsoAid(PO_AID), null), null), poApduRequestList)

        val seRequestSet = LinkedHashSet<SeRequest>()
        seRequestSet.add(seRequest)
        return seRequestSet
    }

    private fun getNoAidSampleSeRequest(): Set<SeRequest> {

        val poApduRequestList = listOf(ApduRequest(ByteArrayUtil.fromHex("0000"), true))

        val seRequest = SeRequest(SeSelector(SeCommonProtocols.PROTOCOL_ISO7816_3, null,
                SeSelector.AidSelector(null, null), null), poApduRequestList)

        val seRequestSet = LinkedHashSet<SeRequest>()
        seRequestSet.add(seRequest)
        return seRequestSet
    }
}
