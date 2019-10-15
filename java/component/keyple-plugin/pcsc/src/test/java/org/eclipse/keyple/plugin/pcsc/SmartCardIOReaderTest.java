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
package org.eclipse.keyple.plugin.pcsc;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import javax.smartcardio.*;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.exception.*;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SmartCardIOReaderTest {

    private PcscReaderImpl reader;

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

        responseApduByte =
                ByteArrayUtil.fromHex("851700010000001212000001030101007E7E7E000000000000");
        res = new ResponseAPDU(responseApduByte);

        readerName = "reader";
        reader = new PcscReaderImpl("pcscPlugin", terminal);
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
        ApduRequest apduRequestMF = new ApduRequest(ByteArrayUtil.fromHex("94A40000023F02"), false);

        // code de la requete
        byte[] aidToSelect = new byte[0];

        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();
        apduRequests.add(apduRequestMF);

        Set<SeRequest> seApplicationRequest = new LinkedHashSet<SeRequest>();
        seApplicationRequest.add(new SeRequest(apduRequests));

        List<SeResponse> reponseActuelle = reader.transmitSet(seApplicationRequest);

        assertNull(reponseActuelle.get(0).getSelectionStatus().getFci());
        assertEquals(reponseActuelle.get(0).getApduResponses().size(), 0);
        assertFalse(reponseActuelle.get(0).wasChannelPreviouslyOpen());
    }

    // TODO redesign @Test
    public void testTransmitToCardWithoutAidToSelect()
            throws CardException, KeypleReaderException, KeypleReaderException {

        atr = new ATR(ByteArrayUtil.fromHex("85170001"));
        when(terminal.isCardPresent()).thenReturn(true);
        when(channel.transmit(any(CommandAPDU.class))).thenReturn(res);
        when(card.getATR()).thenReturn(atr);
        // this.reader = new PcscReader(terminal, readerName);
        byte[] returnOK = {(byte) 0x90, (byte) 0x00};
        ApduResponse responseMockMF = new ApduResponse(
                ByteArrayUtil.fromHex("0x851700010000001212000001030101007E7E7E0000000000009000"),
                null);
        ApduRequest apduRequestMF = new ApduRequest(ByteArrayUtil.fromHex("94A40000023F02"), false);

        // code de la requete
        byte[] aidToSelect = null;

        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();
        apduRequests.add(apduRequestMF);

        Set<SeRequest> seApplicationRequest = new LinkedHashSet<SeRequest>();
        seApplicationRequest.add(new SeRequest(apduRequests));

        PcscReaderImpl spiedReader = spy(this.reader);
        List<SeResponse> reponseActuelle = spiedReader.transmitSet(seApplicationRequest,
                MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.KEEP_OPEN);

        assertEquals(reponseActuelle.get(0).getApduResponses().size(),
                seApplicationRequest.iterator().next().getApduRequests().size());
        // assertNotNull(Whitebox.getInternalState(spiedReader, "card"));
        // assertNotNull(Whitebox.getInternalState(spiedReader, "channel"));
        assertNotNull(reponseActuelle.get(0).getSelectionStatus().getFci());
    }

    // TODO redesign @Test
    public void testTransmitToCardWithAidToSelect()
            throws CardException, KeypleReaderException, KeypleReaderException {


        when(terminal.isCardPresent()).thenReturn(true);
        when(channel.transmit(any(CommandAPDU.class))).thenReturn(res);
        atr = new ATR(ByteArrayUtil.fromHex("85170001"));
        when(card.getATR()).thenReturn(atr);
        byte[] returnOK = {(byte) 0x90, (byte) 0x00};
        ApduResponse responseMockMF = new ApduResponse(
                ByteArrayUtil.fromHex("0x851700010000001212000001030101007E7E7E0000000000009000"),
                null);
        ApduRequest apduRequestMF = new ApduRequest(ByteArrayUtil.fromHex("94A40000023F02"), false);

        // code de la requete
        byte[] aidToSelect = ByteArrayUtil.fromHex("94CA004F00");

        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();
        apduRequests.add(apduRequestMF);

        Set<SeRequest> seApplicationRequest = new LinkedHashSet<SeRequest>();
        seApplicationRequest.add(new SeRequest(apduRequests));

        PcscReaderImpl spiedReader = spy(this.reader);

        List<SeResponse> reponseActuelle = spiedReader.transmitSet(seApplicationRequest);
        assertNotNull(reponseActuelle.get(0).getSelectionStatus().getFci());
        assertEquals(reponseActuelle.get(0).getApduResponses().size(),
                seApplicationRequest.iterator().next().getApduRequests().size());
    }

    // TODO redesign @Test
    public void testTransmitToCardAndDisconnect()
            throws CardException, KeypleReaderException, KeypleReaderException {


        when(terminal.isCardPresent()).thenReturn(true);
        when(channel.transmit(any(CommandAPDU.class))).thenReturn(res);
        byte[] returnOK = {(byte) 0x90, (byte) 0x00};
        ApduResponse responseMockMF = new ApduResponse(
                ByteArrayUtil.fromHex("0x851700010000001212000001030101007E7E7E0000000000009000"),
                null);
        ApduRequest apduRequestMF = new ApduRequest(ByteArrayUtil.fromHex("94A40000023F02"), false);
        // code de la requete
        byte[] aidToSelect = ByteArrayUtil.fromHex("94CA004F00");

        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();
        apduRequests.add(apduRequestMF);

        Set<SeRequest> seApplicationRequest = new LinkedHashSet<SeRequest>();
        seApplicationRequest.add(new SeRequest(apduRequests));

        PcscReaderImpl spiedReader = spy(this.reader);

        List<SeResponse> reponseActuelle = spiedReader.transmitSet(seApplicationRequest,
                MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.KEEP_OPEN);
        assertNotNull(reponseActuelle.get(0).getSelectionStatus().getFci());
        assertEquals(reponseActuelle.get(0).getApduResponses().size(),
                seApplicationRequest.iterator().next().getApduRequests().size());
        // assertNull(Whitebox.getInternalState(spiedReader, "card"));
        // assertNull(Whitebox.getInternalState(spiedReader, "channel"));
    }
}
