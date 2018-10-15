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

package org.eclipse.keyple.plugin.pcsc;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.smartcardio.*;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.exception.*;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SmartCardIOReaderTest {

    private PcscReader reader;

    private String readerName;

    @Mock
    CardTerminal terminal;

    @Mock
    Card card;

    @Mock
    CardChannel channel;

    ATR atr;

    ResponseAPDU res;

    private byte[] responseApduByte;

    @Before
    public void setUp() throws CardException, IllegalArgumentException, KeypleBaseException {
        when(terminal.connect(any(String.class))).thenReturn(card);
        when(card.getBasicChannel()).thenReturn(channel);

        responseApduByte = new byte[] {(byte) 0x85, 0x17, 0x00, 0x01, 0x00, 0x00, 0x00, 0x12, 0x12,
                0x00, 0x00, 0x01, 0x03, 0x01, 0x01, 0x00, 0x7E, 0x7E, 0x7E, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00};
        res = new ResponseAPDU(responseApduByte);

        readerName = "reader";
        reader = new PcscReader("pcscPlugin", terminal);
        reader.setParameter(PcscReader.SETTING_KEY_LOGGING, "true");
    }

    @Test
    public void testSmartCardIOReader() {
        assertNotNull(reader);
    }

    // TODO redesign @Test
    public void testGettersSetters() throws IllegalArgumentException, KeypleBaseException {
        // this.reader = new PcscReader(terminal, readerName);
        reader.setParameter("TOTO", "TOTO");
        assertEquals(reader.getParameters().size(), 1);

        Map<String, String> test = new HashMap<String, String>();
        test.put("TITI", "TITI");
        test.put("TATA", "TATA");
        reader.setParameters(test);
        assertEquals(reader.getParameters().size(), 3);

        assertTrue(readerName.equals(reader.getName()));
    }

    @Test
    public void testIsSEPresent() throws CardException, NoStackTraceThrowable {

        // this.reader = new PcscReader(terminal, readerName);
        when(terminal.isCardPresent()).thenReturn(true);
        assertTrue(reader.isSePresent());
        when(terminal.isCardPresent()).thenReturn(false);
        assertFalse(reader.isSePresent());

    }

    @Test(expected = KeypleReaderException.class)
    public void testIsSEPresentWithException() throws CardException, NoStackTraceThrowable {

        when(terminal.waitForCardAbsent(0)).thenReturn(false);
        doThrow(new CardException("erreur", new Exception())).when(terminal).isCardPresent();
        reader.isSePresent();


    }

    // TODO redesign @Test
    public void testTransmitCardNotPresent()
            throws CardException, KeypleReaderException, KeypleReaderException {

        when(terminal.isCardPresent()).thenReturn(false);
        ApduRequest apduRequestMF =
                new ApduRequest(ByteArrayUtils.fromHex("94A40000023F02"), false);

        // code de la requete
        byte[] aidToSelect = new byte[0];

        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();
        apduRequests.add(apduRequestMF);
        SeRequestSet seApplicationRequest = new SeRequestSet(
                new SeRequest(new SeRequest.AidSelector(aidToSelect), apduRequests, true));

        SeResponseSet reponseActuelle = reader.transmit(seApplicationRequest);

        assertNull(reponseActuelle.getSingleResponse().getFci());
        assertEquals(reponseActuelle.getSingleResponse().getApduResponses().size(), 0);
        assertFalse(reponseActuelle.getSingleResponse().wasChannelPreviouslyOpen());
    }

    // TODO redesign @Test
    public void testTransmitToCardWithoutAidToSelect()
            throws CardException, KeypleReaderException, KeypleReaderException {

        atr = new ATR(new byte[] {(byte) 0x85, 0x17, 0x00, 0x01});
        when(terminal.isCardPresent()).thenReturn(true);
        when(channel.transmit(any(CommandAPDU.class))).thenReturn(res);
        when(card.getATR()).thenReturn(atr);
        // this.reader = new PcscReader(terminal, readerName);
        byte[] returnOK = {(byte) 0x90, (byte) 0x00};
        ApduResponse responseMockMF = new ApduResponse(new byte[] {(byte) 0x85, 0x17, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x12, 0x12, 0x00, 0x00, 0x01, 0x03, 0x01, 0x01, 0x00, 0x7E, 0x7E,
                0x7E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x90, 0x00}, null);
        ApduRequest apduRequestMF =
                new ApduRequest(ByteArrayUtils.fromHex("94A40000023F02"), false);

        // code de la requete
        byte[] aidToSelect = null;

        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();
        apduRequests.add(apduRequestMF);
        SeRequestSet seApplicationRequest = new SeRequestSet(
                new SeRequest(new SeRequest.AidSelector(aidToSelect), apduRequests, true));

        PcscReader spiedReader = spy(this.reader);
        SeResponseSet reponseActuelle = spiedReader.transmit(seApplicationRequest);

        assertEquals(reponseActuelle.getSingleResponse().getApduResponses().size(),
                seApplicationRequest.getSingleRequest().getApduRequests().size());
        // assertNotNull(Whitebox.getInternalState(spiedReader, "card"));
        // assertNotNull(Whitebox.getInternalState(spiedReader, "channel"));
        assertNotNull(reponseActuelle.getSingleResponse().getFci());
    }

    // TODO redesign @Test
    public void testTransmitToCardWithAidToSelect()
            throws CardException, KeypleReaderException, KeypleReaderException {


        when(terminal.isCardPresent()).thenReturn(true);
        when(channel.transmit(any(CommandAPDU.class))).thenReturn(res);
        atr = new ATR(new byte[] {(byte) 0x85, 0x17, 0x00, 0x01});
        when(card.getATR()).thenReturn(atr);
        byte[] returnOK = {(byte) 0x90, (byte) 0x00};
        ApduResponse responseMockMF = new ApduResponse(new byte[] {(byte) 0x85, 0x17, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x12, 0x12, 0x00, 0x00, 0x01, 0x03, 0x01, 0x01, 0x00, 0x7E, 0x7E,
                0x7E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x90, 0x00}, null);
        ApduRequest apduRequestMF =
                new ApduRequest(ByteArrayUtils.fromHex("94A40000023F02"), false);

        // code de la requete
        byte[] aidToSelect = new byte[] {(byte) 0x94, (byte) 0xCA, 0x00, 0x4F, 0x00};

        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();
        apduRequests.add(apduRequestMF);
        SeRequestSet seApplicationRequest = new SeRequestSet(
                new SeRequest(new SeRequest.AidSelector(aidToSelect), apduRequests, true));

        PcscReader spiedReader = spy(this.reader);

        SeResponseSet reponseActuelle = spiedReader.transmit(seApplicationRequest);
        assertNotNull(reponseActuelle.getSingleResponse().getFci());
        assertEquals(reponseActuelle.getSingleResponse().getApduResponses().size(),
                seApplicationRequest.getSingleRequest().getApduRequests().size());
    }

    // TODO redesign @Test
    public void testTransmitToCardAndDisconnect()
            throws CardException, KeypleReaderException, KeypleReaderException {


        when(terminal.isCardPresent()).thenReturn(true);
        when(channel.transmit(any(CommandAPDU.class))).thenReturn(res);
        byte[] returnOK = {(byte) 0x90, (byte) 0x00};
        ApduResponse responseMockMF = new ApduResponse(new byte[] {(byte) 0x85, 0x17, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x12, 0x12, 0x00, 0x00, 0x01, 0x03, 0x01, 0x01, 0x00, 0x7E, 0x7E,
                0x7E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x90, 0x00}, null);
        ApduRequest apduRequestMF =
                new ApduRequest(ByteArrayUtils.fromHex("94A40000023F02"), false);
        // code de la requete
        byte[] aidToSelect = new byte[] {(byte) 0x94, (byte) 0xCA, 0x00, 0x4F, 0x00};

        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();
        apduRequests.add(apduRequestMF);
        SeRequestSet seApplicationRequest = new SeRequestSet(
                new SeRequest(new SeRequest.AidSelector(aidToSelect), apduRequests, false));

        PcscReader spiedReader = spy(this.reader);

        SeResponseSet reponseActuelle = spiedReader.transmit(seApplicationRequest);
        assertNotNull(reponseActuelle.getSingleResponse().getFci());
        assertEquals(reponseActuelle.getSingleResponse().getApduResponses().size(),
                seApplicationRequest.getSingleRequest().getApduRequests().size());
        // assertNull(Whitebox.getInternalState(spiedReader, "card"));
        // assertNull(Whitebox.getInternalState(spiedReader, "channel"));
    }
}
