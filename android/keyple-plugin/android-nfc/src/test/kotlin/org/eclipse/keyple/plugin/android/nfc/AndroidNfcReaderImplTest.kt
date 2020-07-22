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
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import java.io.IOException
import org.eclipse.keyple.core.seproxy.exception.KeypleException
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(RobolectricTestRunner::class)
class AndroidNfcReaderImplTest {

    private lateinit var reader: AndroidNfcReaderImpl

    @MockK
    internal lateinit var tag: Tag

    @MockK
    internal var tagProxy: TagProxy? = null

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        val app = RuntimeEnvironment.application

        reader = AndroidNfcReaderImpl

        // We need to mock tag.* because it's called in printTagId() called when channel is closed
        every { tagProxy?.tag } returns tag
        every { tag.techList } returns arrayOf("android.nfc.tech.IsoDep")
        every { tag.id } returns "00".toByteArray()
        mockkStatic(TagProxy::class)
        mockkObject(TagProxy.Companion)
        every { TagProxy.getTagProxy(tag) } returns tagProxy!!

        mockkStatic(NfcAdapter::class)
        val nfcAdapter = NfcAdapter.getDefaultAdapter(app)
        every { NfcAdapter.getDefaultAdapter(any()) } returns nfcAdapter
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun aInitReaderTest() { // Must be ran in 1st position as AndroidNfcReaderImpl is a singleton
        // Assert.assertEquals(AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION, reader.currentMonitoringState)
        Assert.assertEquals(TransmissionMode.CONTACTLESS, reader.transmissionMode)
        Assert.assertEquals(AndroidNfcPlugin.PLUGIN_NAME, reader.pluginName)
        Assert.assertEquals(AndroidNfcReader.READER_NAME, reader.name)
        Assert.assertTrue(reader.parameters.isEmpty())
    }

    // ---- TAG EVENTS  TESTS ----------- //

    @Test
    fun checkSePresenceTest() {
        every { tagProxy?.isConnected } returns true
        presentMockTag()
        Assert.assertTrue(reader.checkSePresence())
    }

    @Test
    fun processIntent() {
        reader.processIntent(Intent())
        Assert.assertTrue(true) // no test?
    }

    @Test
    fun getATR() {
        presentMockTag()
        val atr = byteArrayOf(0x90.toByte(), 0x00)
        every { tagProxy?.atr } returns atr
        Assert.assertEquals(atr, reader.atr)
    }

    // ---- PHYSICAL CHANNEL TESTS ----------- //

    @Test
    fun isPhysicalChannelOpen() {
        presentMockTag()
        every { tagProxy?.isConnected } returns true
        Assert.assertEquals(true, reader.isPhysicalChannelOpen)
    }

    @Test
    fun openPhysicalChannelSuccess() {
        presentMockTag()
        every { tagProxy?.isConnected } returns true
        reader.openPhysicalChannel()
    }

    @Test(expected = KeypleReaderException::class)
    fun openPhysicalChannelError() {
        // init
        presentMockTag()
        every { tagProxy?.isConnected } returns false
        every { tagProxy?.connect() } throws IOException()

        // test
        reader.openPhysicalChannel()
    }

    @Test
    fun closePhysicalChannelSuccess() {
        // init
        presentMockTag()
        every { tagProxy?.isConnected } returns true

        // test
        reader.closePhysicalChannel()
        // no exception
        Assert.assertTrue(true)
    }

    @Test(expected = KeypleReaderException::class)
    fun closePhysicalChannelError() {
        // init
        presentMockTag()
        every { tagProxy?.isConnected } returns true
        every { tagProxy?.close() } throws IOException()

        // test
        reader.closePhysicalChannel()
        // throw exception
    }

    // ---- TRANSMIT TEST ----------- //

    @Test
    fun transmitAPDUSuccess() {
        // init
        presentMockTag()
        val `in` = byteArrayOf(0x90.toByte(), 0x00)
        val out = byteArrayOf(0x90.toByte(), 0x00)
        every { tagProxy?.transceive(`in`) } returns out

        // test
        val outBB = reader.transmitApdu(`in`)

        // assert
        Assert.assertArrayEquals(out, outBB)
    }

    @Test(expected = KeypleReaderException::class)
    fun transmitAPDUError() {
        // init
        presentMockTag()
        val `in` = byteArrayOf(0x90.toByte(), 0x00)
        every { tagProxy?.transceive(`in`) } throws IOException()

        // test
        reader.transmitApdu(`in`)

        // throw exception
    }

    @Test
    @Throws(KeypleException::class, IOException::class)
    fun protocolFlagMatchesTrue() {
        // init
        presentMockTag()
        reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_ISO14443_4))
        every { tagProxy?.tech } returns AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_ISO14443_4)

        // test
        Assert.assertTrue(reader.protocolFlagMatches(SeCommonProtocols.PROTOCOL_ISO14443_4))
    }

    // ----- TEST PARAMETERS ------ //

    @Test
    @Throws(IllegalArgumentException::class)
    fun bSetCorrectParameter() { // Must be ran in 2nd position as AndroidNfcReaderImpl is a singleton
        reader.setParameter(AndroidNfcReader.FLAG_READER_NO_PLATFORM_SOUNDS, "1")
        Assert.assertEquals(NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS, reader.flags)
        reader.setParameter(AndroidNfcReader.FLAG_READER_NO_PLATFORM_SOUNDS, "0")
        Assert.assertEquals(0, reader.flags)
        reader.setParameter(AndroidNfcReader.FLAG_READER_SKIP_NDEF_CHECK, "1")
        Assert.assertEquals(NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, reader.flags)
        reader.setParameter(AndroidNfcReader.FLAG_READER_SKIP_NDEF_CHECK, "0")
        Assert.assertEquals(0, reader.flags)
        reader.setParameter(AndroidNfcReader.FLAG_READER_PRESENCE_CHECK_DELAY, "10")

        /*
         * Fail because android.os.Bundle is not present in the JVM, roboelectric is needed to play
         * this test Assert.assertEquals(10,
         * reader.getOptions().get(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY));
         * Assert.assertEquals(3, reader.getParameters().size());
         */
        Assert.assertEquals(3, reader.parameters.count())
        Assert.assertEquals(10, reader.options.get(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY))
    }

    @Test(expected = IllegalArgumentException::class)
    fun setUnCorrectParameter() {
        reader.setParameter(AndroidNfcReader.FLAG_READER_NO_PLATFORM_SOUNDS, "A")
    }

    @Test(expected = IllegalArgumentException::class)
    fun setUnCorrectParameter2() {
        reader.setParameter(AndroidNfcReader.FLAG_READER_SKIP_NDEF_CHECK, "2")
    }

    @Test(expected = IllegalArgumentException::class)
    fun setUnCorrectParameter3() {
        reader.setParameter(AndroidNfcReader.FLAG_READER_PRESENCE_CHECK_DELAY, "-1")
    }

    @Test
    fun onTagReceivedException() {
        every { TagProxy.getTagProxy(tag) } throws KeypleReaderIOException("")
        reader.onTagDiscovered(tag) // Should not throw an exception
    }

    @Test
    fun waitForCardAbsentNative() {
        Assert.assertFalse(reader.waitForCardAbsentNative())
    }

    @Test
    fun stopWaitForCardRemoval() {
        reader.stopWaitForCardRemoval()
        Assert.assertTrue(true) // Previous call didn't throw any exception
    }
    // -------- helpers ---------- //

    private fun presentMockTag() {
        reader.onTagDiscovered(tag)
    }

    class MyActivity : Activity()
}
