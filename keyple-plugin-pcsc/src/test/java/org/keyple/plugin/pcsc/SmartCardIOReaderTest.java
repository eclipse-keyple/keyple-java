/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.pcsc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keyple.seproxy.*;
import org.keyple.seproxy.exceptions.ChannelStateReaderException;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.seproxy.exceptions.InvalidApduReaderException;
import org.keyple.seproxy.exceptions.ReaderTimeoutException;
import org.keyple.seproxy.exceptions.UnexpectedReaderException;
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
    public void setUp() throws CardException, IOReaderException {
        when(terminal.connect(any(String.class))).thenReturn(card);
        when(card.getBasicChannel()).thenReturn(channel);

        responseApduByte = new byte[] {(byte) 0x85, 0x17, 0x00, 0x01, 0x00, 0x00, 0x00, 0x12, 0x12,
                0x00, 0x00, 0x01, 0x03, 0x01, 0x01, 0x00, 0x7E, 0x7E, 0x7E, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00};
        res = new ResponseAPDU(responseApduByte);

        readerName = "lecteur";
        reader = new PcscReader(terminal);
        reader.setParameter(PcscReader.SETTING_KEY_LOGGING, "true");
    }

    @Test
    public void testSmartCardIOReader() {
        assertNotNull(reader);
    }

    @Test
    public void testGettersSetters() throws IOReaderException {
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
    public void testIsSEPresent() throws CardException, IOReaderException {

        // this.reader = new PcscReader(terminal, readerName);
        when(terminal.isCardPresent()).thenReturn(true);
        assertTrue(reader.isSEPresent());
        when(terminal.isCardPresent()).thenReturn(false);
        assertFalse(reader.isSEPresent());

    }

    @Test(expected = IOReaderException.class)
    public void testIsSEPresentWithException() throws CardException, IOReaderException {

        when(terminal.waitForCardAbsent(0)).thenReturn(false);
        doThrow(new CardException("erreur", new Exception())).when(terminal).isCardPresent();
        reader.isSEPresent();


    }

    @Test
    public void testTransmitCardNotPresent()
            throws CardException, ChannelStateReaderException, InvalidApduReaderException,
            IOReaderException, ReaderTimeoutException, UnexpectedReaderException {

        when(terminal.isCardPresent()).thenReturn(false);
        ApduRequest apduRequestMF = new ApduRequest(
                new byte[] {(byte) 0x94, (byte) 0xA4, 0x00, 0x00, 0x02, 0x3F, 0x02}, false);

        // code de la requete
        ByteBuffer aidToSelect = ByteBuffer.allocate(0);

        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();
        apduRequests.add(apduRequestMF);
        SeRequestSet seApplicationRequest = new SeRequestSet(aidToSelect, apduRequests, true);

        SeResponseSet reponseActuelle = reader.transmit(seApplicationRequest);

        assertNull(reponseActuelle.getFci());
        assertEquals(reponseActuelle.getApduResponses().size(), 0);
        assertFalse(reponseActuelle.wasChannelPreviouslyOpen());
    }

    @Test
    public void testTransmitToCardWithoutAidToSelect()
            throws CardException, ChannelStateReaderException, InvalidApduReaderException,
            IOReaderException, ReaderTimeoutException, UnexpectedReaderException {

        atr = new ATR(new byte[] {(byte) 0x85, 0x17, 0x00, 0x01});
        when(terminal.isCardPresent()).thenReturn(true);
        when(channel.transmit(any(CommandAPDU.class))).thenReturn(res);
        when(card.getATR()).thenReturn(atr);
        // this.reader = new PcscReader(terminal, readerName);
        byte[] returnOK = {(byte) 0x90, (byte) 0x00};
        ApduResponse responseMockMF = new ApduResponse(new byte[] {(byte) 0x85, 0x17, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x12, 0x12, 0x00, 0x00, 0x01, 0x03, 0x01, 0x01, 0x00, 0x7E, 0x7E,
                0x7E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}, true, returnOK);
        ApduRequest apduRequestMF = new ApduRequest(
                new byte[] {(byte) 0x94, (byte) 0xA4, 0x00, 0x00, 0x02, 0x3F, 0x02}, false);

        // code de la requete
        ByteBuffer aidToSelect = null;

        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();
        apduRequests.add(apduRequestMF);
        SeRequestSet seApplicationRequest = new SeRequestSet(aidToSelect, apduRequests, true);

        PcscReader spiedReader = spy(this.reader);
        SeResponseSet reponseActuelle = spiedReader.transmit(seApplicationRequest);

        assertEquals(reponseActuelle.getApduResponses().size(),
                seApplicationRequest.getApduRequests().size());
        // assertNotNull(Whitebox.getInternalState(spiedReader, "card"));
        // assertNotNull(Whitebox.getInternalState(spiedReader, "channel"));
        assertNotNull(reponseActuelle.getFci());
    }

    @Test
    public void testTransmitToCardWithAidToSelect()
            throws CardException, ChannelStateReaderException, InvalidApduReaderException,
            IOReaderException, ReaderTimeoutException, UnexpectedReaderException {


        when(terminal.isCardPresent()).thenReturn(true);
        when(channel.transmit(any(CommandAPDU.class))).thenReturn(res);
        atr = new ATR(new byte[] {(byte) 0x85, 0x17, 0x00, 0x01});
        when(card.getATR()).thenReturn(atr);
        byte[] returnOK = {(byte) 0x90, (byte) 0x00};
        ApduResponse responseMockMF = new ApduResponse(new byte[] {(byte) 0x85, 0x17, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x12, 0x12, 0x00, 0x00, 0x01, 0x03, 0x01, 0x01, 0x00, 0x7E, 0x7E,
                0x7E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}, true, returnOK);
        ApduRequest apduRequestMF = new ApduRequest(
                new byte[] {(byte) 0x94, (byte) 0xA4, 0x00, 0x00, 0x02, 0x3F, 0x02}, false);

        // code de la requete
        ByteBuffer aidToSelect =
                ByteBufferUtils.wrap(new byte[] {(byte) 0x94, (byte) 0xCA, 0x00, 0x4F, 0x00});

        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();
        apduRequests.add(apduRequestMF);
        SeRequestSet seApplicationRequest = new SeRequestSet(aidToSelect, apduRequests, true);

        PcscReader spiedReader = spy(this.reader);

        SeResponseSet reponseActuelle = spiedReader.transmit(seApplicationRequest);
        assertNotNull(reponseActuelle.getFci());
        assertEquals(reponseActuelle.getApduResponses().size(),
                seApplicationRequest.getApduRequests().size());
    }

    @Test
    public void testTransmitToCardAndDisconnect()
            throws CardException, ChannelStateReaderException, InvalidApduReaderException,
            IOReaderException, ReaderTimeoutException, UnexpectedReaderException {


        when(terminal.isCardPresent()).thenReturn(true);
        when(channel.transmit(any(CommandAPDU.class))).thenReturn(res);
        byte[] returnOK = {(byte) 0x90, (byte) 0x00};
        ApduResponse responseMockMF = new ApduResponse(new byte[] {(byte) 0x85, 0x17, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x12, 0x12, 0x00, 0x00, 0x01, 0x03, 0x01, 0x01, 0x00, 0x7E, 0x7E,
                0x7E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}, true, returnOK);
        ApduRequest apduRequestMF = new ApduRequest(
                new byte[] {(byte) 0x94, (byte) 0xA4, 0x00, 0x00, 0x02, 0x3F, 0x02}, false);

        // code de la requete
        ByteBuffer aidToSelect =
                ByteBufferUtils.wrap(new byte[] {(byte) 0x94, (byte) 0xCA, 0x00, 0x4F, 0x00});

        List<ApduRequest> apduRequests = new ArrayList<ApduRequest>();
        apduRequests.add(apduRequestMF);
        SeRequestSet seApplicationRequest = new SeRequestSet(aidToSelect, apduRequests, false);

        PcscReader spiedReader = spy(this.reader);

        SeResponseSet reponseActuelle = spiedReader.transmit(seApplicationRequest);
        assertNotNull(reponseActuelle.getFci());
        assertEquals(reponseActuelle.getApduResponses().size(),
                seApplicationRequest.getApduRequests().size());
        // assertNull(Whitebox.getInternalState(spiedReader, "card"));
        // assertNull(Whitebox.getInternalState(spiedReader, "channel"));
    }
}
