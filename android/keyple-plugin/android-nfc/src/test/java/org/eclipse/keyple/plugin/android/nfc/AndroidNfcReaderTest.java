/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.android.nfc;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import junit.framework.Assert;
import org.eclipse.keyple.core.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableState;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TagProxy.class, NfcAdapter.class})
public class AndroidNfcReaderTest {

    AndroidNfcReaderImpl reader;
    NfcAdapter nfcAdapter;
    Tag tag;
    TagProxy tagProxy;
    Intent intent;
    Activity activity;

    // init before each test
    @Before
    public void SetUp() throws KeypleReaderException {

        // Mock others objects
        tagProxy = Mockito.mock(TagProxy.class);
        intent = Mockito.mock(Intent.class);
        activity = Mockito.mock(Activity.class);
        nfcAdapter = Mockito.mock(NfcAdapter.class);
        tag = Mockito.mock(Tag.class);

        // mock TagProxy Factory
        PowerMockito.mockStatic(TagProxy.class);
        when(TagProxy.getTagProxy(tag)).thenReturn(tagProxy);

        // mock NfcAdapter Factory
        PowerMockito.mockStatic(NfcAdapter.class);
        when(NfcAdapter.getDefaultAdapter(activity)).thenReturn(nfcAdapter);

        // instantiate a new Reader for each test
        reader = new AndroidNfcReaderImpl();
    }

    // ---- INIT READER TESTS ----------- //


    @Test
    public void initReaderTest(){
        Assert.assertEquals(AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION,
                reader.getCurrentMonitoringState());
        Assert.assertEquals(TransmissionMode.CONTACTLESS, reader.getTransmissionMode());
        Assert.assertEquals(AndroidNfcPlugin.PLUGIN_NAME, reader.getPluginName());
        Assert.assertEquals(AndroidNfcReader.READER_NAME, reader.getName());
        Assert.assertTrue(reader.getParameters().isEmpty());
    }



    // ---- TAG EVENTS  TESTS ----------- //


    @Test
    public void checkSePresenceTest(){
        doReturn(true).when(tagProxy).isConnected();
        
        presentMockTag();
        Assert.assertTrue(reader.checkSePresence());
    }

    @Test
    public void processIntent() {
        reader.processIntent(intent);
        Assert.assertTrue(true);// no test
    }

    @Test
    public void getATR() {
        presentMockTag();
        byte[] atr = new byte[] {(byte) 0x90, 0x00};
        when(tagProxy.getATR()).thenReturn(atr);
        assertEquals(atr, reader.getATR());
    }

    // ---- PHYSICAL CHANNEL TESTS ----------- //


    @Test
    public void isPhysicalChannelOpen() {
        presentMockTag();
        when(tagProxy.isConnected()).thenReturn(true);
        assertEquals(true, reader.isPhysicalChannelOpen());
    }

    @Test
    public void openPhysicalChannelSuccess() throws KeypleReaderException {
        presentMockTag();
        when(tagProxy.isConnected()).thenReturn(false);
        reader.openPhysicalChannel();
    }

    @Test(expected = KeypleReaderException.class)
    public void openPhysicalChannelError() throws KeypleBaseException, IOException {
        // init
        presentMockTag();
        when(tagProxy.isConnected()).thenReturn(false);
        doThrow(new IOException()).when(tagProxy).connect();

        // test
        reader.openPhysicalChannel();
    }

    @Test
    public void closePhysicalChannelSuccess() throws KeypleReaderException {
        // init
        presentMockTag();
        when(tagProxy.isConnected()).thenReturn(true);

        // test
        reader.closePhysicalChannel();
        // no exception

    }

    @Test(expected = KeypleReaderException.class)
    public void closePhysicalChannelError() throws KeypleBaseException, IOException {
        // init
        presentMockTag();
        when(tagProxy.isConnected()).thenReturn(true);
        doThrow(new IOException()).when(tagProxy).close();

        // test
        reader.closePhysicalChannel();
        // throw exception

    }

    // ---- TRANSMIT TEST ----------- //

    @Test
    public void transmitAPDUSuccess() throws KeypleBaseException, IOException {
        // init
        presentMockTag();
        byte[] in = new byte[] {(byte) 0x90, 0x00};
        byte[] out = new byte[] {(byte) 0x90, 0x00};
        when(tagProxy.transceive(in)).thenReturn(out);
        // test
        byte[] outBB = reader.transmitApdu(in);

        // assert
        assertArrayEquals(out, outBB);

    }

    @Test(expected = KeypleReaderException.class)
    public void transmitAPDUError() throws KeypleBaseException, IOException {
        // init
        presentMockTag();
        byte[] in = new byte[] {(byte) 0x90, 0x00};
        byte[] out = new byte[] {(byte) 0x90, 0x00};
        when(tagProxy.transceive(in)).thenThrow(new IOException(""));

        // test
        byte[] outBB = reader.transmitApdu(in);

        // throw exception
    }

    @Test
    public void protocolFlagMatchesTrue() throws KeypleBaseException, IOException {
        // init
        presentMockTag();
        reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                AndroidNfcProtocolSettings.NFC_PROTOCOL_SETTING.get(SeCommonProtocols.PROTOCOL_ISO14443_4));
        when(tagProxy.getTech())
                .thenReturn(AndroidNfcProtocolSettings.NFC_PROTOCOL_SETTING.get(SeCommonProtocols.PROTOCOL_ISO14443_4));

        // test
        Assert.assertTrue(reader.protocolFlagMatches(SeCommonProtocols.PROTOCOL_ISO14443_4));
    }



    // ----- TEST PARAMETERS ------ //

    @Test
    public void setCorrectParameter() throws IllegalArgumentException {
        reader.setParameter(AndroidNfcReaderImpl.FLAG_READER_NO_PLATFORM_SOUNDS, "1");
        Assert.assertEquals(NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS, reader.getFlags());
        reader.setParameter(AndroidNfcReaderImpl.FLAG_READER_NO_PLATFORM_SOUNDS, "0");
        Assert.assertEquals(0, reader.getFlags());
        reader.setParameter(AndroidNfcReaderImpl.FLAG_READER_SKIP_NDEF_CHECK, "1");
        Assert.assertEquals(NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, reader.getFlags());
        reader.setParameter(AndroidNfcReaderImpl.FLAG_READER_SKIP_NDEF_CHECK, "0");
        Assert.assertEquals(0, reader.getFlags());
        reader.setParameter(AndroidNfcReaderImpl.FLAG_READER_PRESENCE_CHECK_DELAY, "10");
        /*
         * Fail because android.os.Bundle is not present in the JVM, roboelectric is needed to play
         * this test Assert.assertEquals(10,
         * reader.getOptions().get(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY));
         * Assert.assertEquals(3, reader.getParameters().size());
         */
    }

    @Test(expected = IllegalArgumentException.class)
    public void setUnCorrectParameter() throws IllegalArgumentException {
        reader.setParameter(AndroidNfcReaderImpl.FLAG_READER_NO_PLATFORM_SOUNDS, "A");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setUnCorrectParameter2() throws IllegalArgumentException {
        reader.setParameter(AndroidNfcReaderImpl.FLAG_READER_SKIP_NDEF_CHECK, "2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setUnCorrectParameter3() throws IllegalArgumentException {
        reader.setParameter(AndroidNfcReaderImpl.FLAG_READER_PRESENCE_CHECK_DELAY, "-1");
    }



    // -------- helpers ---------- //

    private void presentMockTag() {
        reader.onTagDiscovered(tag);
    }

}
