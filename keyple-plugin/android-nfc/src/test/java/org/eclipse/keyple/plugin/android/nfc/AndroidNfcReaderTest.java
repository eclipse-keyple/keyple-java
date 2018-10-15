/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.plugin.android.nfc;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.SeRequest;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import junit.framework.Assert;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TagProxy.class, NfcAdapter.class})
public class AndroidNfcReaderTest {

    AndroidNfcReader reader;
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

        // mock TagProxy Factory
        PowerMockito.mockStatic(TagProxy.class);
        when(TagProxy.getTagProxy(tag)).thenReturn(tagProxy);

        // mock NfcAdapter Factory
        PowerMockito.mockStatic(NfcAdapter.class);
        when(NfcAdapter.getDefaultAdapter(activity)).thenReturn(nfcAdapter);

        // instantiate a new Reader for each test
        reader = new AndroidNfcReader();
    }


    /*
     * TEST HIGH LEVEL METHOD TRANSMIT
     */

    @Test
    public void transmitSuccessfull() throws KeypleBaseException, IOException {

        // config
        reader.addSeProtocolSetting(
                new SeProtocolSetting(AndroidNfcProtocolSettings.SETTING_PROTOCOL_ISO14443_4));

        // input
        SeRequestSet requests = getRequestIsoDepSetSample();

        // init Mock
        when(tagProxy.transceive(ByteArrayUtils.fromHex("00A404000AA000000291A00000019100")))
                .thenReturn(ByteArrayUtils.fromHex(
                        "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000"));
        when(tagProxy.transceive(ByteArrayUtils.fromHex("00B201A420"))).thenReturn(ByteArrayUtils
                .fromHex("00000000000000000000000000000000000000000000000000000000000000009000"));
        when(tagProxy.getTech())
                .thenReturn(AndroidNfcProtocolSettings.SETTING_PROTOCOL_ISO14443_4.getValue());
        when(tagProxy.isConnected()).thenReturn(true);

        // test
        insertSe();
        SeResponseSet seResponse = reader.transmit(requests);

        // assert
        Assert.assertTrue(seResponse.getSingleResponse().getFci().isSuccessful());

    }

    @Test
    public void transmitWrongProtocols() throws KeypleBaseException, IOException {

        // config reader with Isodep protocols
        reader.addSeProtocolSetting(
                new SeProtocolSetting(AndroidNfcProtocolSettings.SETTING_PROTOCOL_ISO14443_4));

        // input
        SeRequestSet requests = getRequestIsoDepSetSample();

        // init Mock with Mifare Classic Smart card
        when(tagProxy.transceive(ByteArrayUtils.fromHex("00A404000AA000000291A00000019100")))
                .thenReturn(ByteArrayUtils.fromHex(
                        "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000"));
        when(tagProxy.transceive(ByteArrayUtils.fromHex("00B201A420"))).thenReturn(ByteArrayUtils
                .fromHex("00000000000000000000000000000000000000000000000000000000000000009000"));
        when(tagProxy.getTech())
                .thenReturn(AndroidNfcProtocolSettings.SETTING_PROTOCOL_MIFARE_CLASSIC.getValue());
        when(tagProxy.isConnected()).thenReturn(true);

        // test
        insertSe();
        SeResponseSet seResponse = reader.transmit(requests);

        // assert the only seRequest is null
        Assert.assertTrue(seResponse.getSingleResponse() == null);

    }

    @Test
    public void transmitWrongProtocol2() throws KeypleBaseException, IOException {

        // config reader with Mifare protocols
        reader.addSeProtocolSetting(
                new SeProtocolSetting(AndroidNfcProtocolSettings.SETTING_PROTOCOL_MIFARE_CLASSIC));

        // input
        SeRequestSet requests = getRequestIsoDepSetSample();

        // init Mock with Isodep Classic Smart card
        when(tagProxy.transceive(ByteArrayUtils.fromHex("00A404000AA000000291A00000019100")))
                .thenReturn(ByteArrayUtils.fromHex(
                        "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000"));
        when(tagProxy.transceive(ByteArrayUtils.fromHex("00B201A420"))).thenReturn(ByteArrayUtils
                .fromHex("00000000000000000000000000000000000000000000000000000000000000009000"));
        when(tagProxy.getTech())
                .thenReturn(AndroidNfcProtocolSettings.SETTING_PROTOCOL_ISO14443_4.getValue());
        when(tagProxy.isConnected()).thenReturn(true);

        // test
        insertSe();
        SeResponseSet seResponse = reader.transmit(requests);

        // assert the only seRequest is null
        Assert.assertTrue(seResponse.getSingleResponse() == null);

    }

    @Test(expected = KeypleReaderException.class)
    public void transmitCardNotConnected() throws KeypleBaseException, IOException {

        // config reader with Isodep protocols
        reader.addSeProtocolSetting(
                new SeProtocolSetting(AndroidNfcProtocolSettings.SETTING_PROTOCOL_ISO14443_4));

        // input
        SeRequestSet requests = getRequestIsoDepSetSample();

        // init Mock with Isodep Classic Smart card
        when(tagProxy.transceive(ByteArrayUtils.fromHex("00A404000AA000000291A00000019100")))
                .thenReturn(ByteArrayUtils.fromHex(
                        "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000"));
        when(tagProxy.transceive(ByteArrayUtils.fromHex("00B201A420"))).thenReturn(ByteArrayUtils
                .fromHex("00000000000000000000000000000000000000000000000000000000000000009000"));
        when(tagProxy.getTech())
                .thenReturn(AndroidNfcProtocolSettings.SETTING_PROTOCOL_ISO14443_4.getValue());


        // card is not connected
        when(tagProxy.isConnected()).thenReturn(false);

        // test
        insertSe();
        SeResponseSet seResponse = reader.transmit(requests);

        // wait for exception
    }

    @Test
    public void transmitUnkownCard() throws KeypleBaseException, IOException {

        // config reader with Isodep protocols
        reader.addSeProtocolSetting(
                new SeProtocolSetting(AndroidNfcProtocolSettings.SETTING_PROTOCOL_ISO14443_4));

        // input
        SeRequestSet requests = getRequestIsoDepSetSample();

        // init Mock with Isodep Classic Smart card
        when(tagProxy.transceive(ByteArrayUtils.fromHex("00A404000AA000000291A00000019100")))
                .thenReturn(ByteArrayUtils.fromHex(
                        "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000"));
        when(tagProxy.transceive(ByteArrayUtils.fromHex("00B201A420"))).thenReturn(ByteArrayUtils
                .fromHex("00000000000000000000000000000000000000000000000000000000000000009000"));
        when(tagProxy.isConnected()).thenReturn(false);

        // unknown card
        when(tagProxy.getTech()).thenReturn("Unknown card");

        // test
        insertSe();
        SeResponseSet seResponse = reader.transmit(requests);

        // assert the only seRequest is null
        Assert.assertTrue(seResponse.getSingleResponse() == null);
    }


    @Test
    public void transmitUnknownApplication() throws KeypleBaseException, IOException {

        // config reader with Isodep protocols
        reader.addSeProtocolSetting(
                new SeProtocolSetting(AndroidNfcProtocolSettings.SETTING_PROTOCOL_ISO14443_4));

        // input
        SeRequestSet requests = getRequestIsoDepSetSample();

        // init Mock with Isodep Classic Smart card
        when(tagProxy.transceive(ByteArrayUtils.fromHex("00B201A420"))).thenReturn(ByteArrayUtils
                .fromHex("00000000000000000000000000000000000000000000000000000000000000009000"));
        when(tagProxy.isConnected()).thenReturn(false);
        when(tagProxy.getTech()).thenReturn("Unknown card");

        // unknown Application
        when(tagProxy.transceive(ByteArrayUtils.fromHex("00A404000AA000000291A00000019100")))
                .thenReturn(ByteArrayUtils.fromHex("0000"));

        // test
        insertSe();
        SeResponseSet seResponse = reader.transmit(requests);

        // assert the only seRequest is null
        Assert.assertTrue(seResponse.getSingleResponse() == null);
    }

    /*
     * Test PUBLIC methods
     */

    @Test(expected = IllegalArgumentException.class)
    public void setUnknownParameter() throws IllegalArgumentException {
        reader.setParameter("dsfsdf", "sdfdsf");
    }


    @Test
    public void setCorrectParameter() throws IllegalArgumentException {
        reader.setParameter(AndroidNfcReader.FLAG_READER_NO_PLATFORM_SOUNDS, "1");
        Assert.assertEquals(NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS, reader.getFlags());
        reader.setParameter(AndroidNfcReader.FLAG_READER_NO_PLATFORM_SOUNDS, "0");
        Assert.assertEquals(0, reader.getFlags());
        reader.setParameter(AndroidNfcReader.FLAG_READER_SKIP_NDEF_CHECK, "1");
        Assert.assertEquals(NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, reader.getFlags());
        reader.setParameter(AndroidNfcReader.FLAG_READER_SKIP_NDEF_CHECK, "0");
        Assert.assertEquals(0, reader.getFlags());
        reader.setParameter(AndroidNfcReader.FLAG_READER_PRESENCE_CHECK_DELAY, "10");
        /*
         * Fail because android.os.Bundle is not present in the JVM, roboelectric is needed to play
         * this test Assert.assertEquals(10,
         * reader.getOptions().get(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY));
         * Assert.assertEquals(3, reader.getParameters().size());
         */

    }

    @Test(expected = IllegalArgumentException.class)
    public void setUnCorrectParameter() throws IllegalArgumentException {
        reader.setParameter(AndroidNfcReader.FLAG_READER_NO_PLATFORM_SOUNDS, "A");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setUnCorrectParameter2() throws IllegalArgumentException {
        reader.setParameter(AndroidNfcReader.FLAG_READER_SKIP_NDEF_CHECK, "2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setUnCorrectParameter3() throws IllegalArgumentException {
        reader.setParameter(AndroidNfcReader.FLAG_READER_PRESENCE_CHECK_DELAY, "-1");
    }

    @Test
    public void setIsoDepProtocol() {
        reader.addSeProtocolSetting(
                new SeProtocolSetting(AndroidNfcProtocolSettings.SETTING_PROTOCOL_ISO14443_4));
        assertEquals(NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_NFC_A,
                reader.getFlags());
    }

    @Test
    public void setMifareProtocol() {
        reader.addSeProtocolSetting(
                new SeProtocolSetting(AndroidNfcProtocolSettings.SETTING_PROTOCOL_MIFARE_CLASSIC));
        assertEquals(NfcAdapter.FLAG_READER_NFC_A, reader.getFlags());
    }

    @Test
    public void setMifareULProtocol() {
        reader.addSeProtocolSetting(
                new SeProtocolSetting(AndroidNfcProtocolSettings.SETTING_PROTOCOL_MIFARE_UL));
        assertEquals(NfcAdapter.FLAG_READER_NFC_A, reader.getFlags());
    }


    @Test
    public void insertEvent() {

        reader.addObserver(new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                assertEquals(ReaderEvent.EventType.SE_INSERTED, event.getEventType());
            }
        });

        insertSe();
    }

    @Test
    public void isConnected() {
        insertSe();
        when(tagProxy.isConnected()).thenReturn(true);
        assertEquals(true, reader.isSePresent());
    }



    /*
     * Test internal methods
     */


    @Test
    public void getATR() {
        insertSe();
        byte[] atr = new byte[] {(byte) 0x90, 0x00};
        when(tagProxy.getATR()).thenReturn(atr);
        assertEquals(atr, reader.getATR());
    }

    @Test
    public void isPhysicalChannelOpen() {
        insertSe();
        when(tagProxy.isConnected()).thenReturn(true);
        assertEquals(true, reader.isPhysicalChannelOpen());
    }

    @Test
    public void openPhysicalChannelSuccess() throws KeypleReaderException {
        insertSe();
        when(tagProxy.isConnected()).thenReturn(false);
        reader.openPhysicalChannel();
    }

    @Test(expected = KeypleReaderException.class)
    public void openPhysicalChannelError() throws KeypleBaseException, IOException {
        // init
        insertSe();
        when(tagProxy.isConnected()).thenReturn(false);
        doThrow(new IOException()).when(tagProxy).connect();

        // test
        reader.openPhysicalChannel();
    }

    @Test
    public void closePhysicalChannelSuccess() throws KeypleReaderException {
        // init
        insertSe();
        when(tagProxy.isConnected()).thenReturn(true);

        // test
        reader.closePhysicalChannel();

        // no exception

    }

    @Test(expected = KeypleReaderException.class)
    public void closePhysicalChannelError() throws KeypleBaseException, IOException {
        // init
        insertSe();
        when(tagProxy.isConnected()).thenReturn(true);
        doThrow(new IOException()).when(tagProxy).close();

        // test
        reader.closePhysicalChannel();

        // throw exception

    }

    @Test
    public void transmitAPDUSuccess() throws KeypleBaseException, IOException {
        // init
        insertSe();
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
        insertSe();
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
        insertSe();
        reader.addSeProtocolSetting(
                new SeProtocolSetting(AndroidNfcProtocolSettings.SETTING_PROTOCOL_ISO14443_4));
        when(tagProxy.getTech())
                .thenReturn(AndroidNfcProtocolSettings.SETTING_PROTOCOL_ISO14443_4.getValue());

        // test
        Assert.assertTrue(reader.protocolFlagMatches(ContactlessProtocols.PROTOCOL_ISO14443_4));
    }

    @Test
    public void processIntent() {
        reader.processIntent(intent);
        Assert.assertTrue(true);// no test
    }

    @Test
    public void printTagId() {
        reader.printTagId();
        Assert.assertTrue(true);// no test
    }

    @Test
    public void enableReaderMode() {
        // init instumented test

        // test
        reader.enableNFCReaderMode(activity);

        // nothing to assert
        Assert.assertTrue(true);
    }

    @Test
    public void disableReaderMode() {
        // init
        reader.enableNFCReaderMode(activity);

        // test
        reader.disableNFCReaderMode(activity);

        // nothing to assert
        Assert.assertTrue(true);
    }


    /*
     * Helper method
     */

    private void insertSe() {
        reader.onTagDiscovered(tag);
    }

    private SeRequestSet getRequestIsoDepSetSample() {
        String poAid = "A000000291A000000191";

        ReadRecordsCmdBuild poReadRecordCmd_T2Env = new ReadRecordsCmdBuild(PoRevision.REV3_1,
                (byte) 0x14, (byte) 0x01, true, (byte) 0x20, "Hoplink EF T2Environment");

        List<ApduRequest> poApduRequestList;

        poApduRequestList = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest());

        SeRequest seRequest =
                new SeRequest(new SeRequest.AidSelector(ByteArrayUtils.fromHex(poAid)),
                        poApduRequestList, false, ContactlessProtocols.PROTOCOL_ISO14443_4);

        return new SeRequestSet(seRequest);

    }
}
