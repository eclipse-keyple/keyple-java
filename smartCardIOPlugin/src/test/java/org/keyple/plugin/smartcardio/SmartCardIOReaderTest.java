package org.keyple.plugin.smartcardio;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;

import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.keyple.plugin.smartcardio.SmartCardIOReader;
import org.keyple.seproxy.APDURequest;
import org.keyple.seproxy.APDUResponse;
import org.keyple.seproxy.SERequest;
import org.keyple.seproxy.SEResponse;
import org.keyple.seproxy.exceptions.ReaderException;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class SmartCardIOReaderTest {
    Logger logger = LoggerFactory.getLogger(SmartCardIOReaderTest.class);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testSmartCardIOReader() {
        CardTerminal terminal = Mockito.mock(CardTerminal.class);
        String name = "lecteur";
        SmartCardIOReader reader = new SmartCardIOReader(terminal, name);
        assertNotNull(reader);
    }

    @Test
    public void testGetName() throws CardException {
        byte[] samATR = new byte[] { (byte) 0x3B, (byte) 0x3F, (byte) 0x96, (byte) 0x00, (byte) 0x80, (byte) 0x5A,
                (byte) 0x2A, (byte) 0x80, (byte) 0xE1, (byte) 0x08, (byte) 0x40, (byte) 0x23, (byte) 0xAE, (byte) 0x10,
                (byte) 0x42, (byte) 0x2E, (byte) 0x82, (byte) 0x90, (byte) 0x00 };

        Card card = Mockito.mock(Card.class);
        CardTerminal terminal = Mockito.mock(CardTerminal.class);
        String name = "lecteur";
        ATR sam = new ATR(samATR);

        Mockito.when(terminal.connect("*")).thenReturn(card);
        SmartCardIOReader reader = new SmartCardIOReader(terminal, name);
        Mockito.when(card.getATR()).thenReturn(sam);
        assertEquals("lecteur", reader.getName());

        Mockito.doThrow(new CardException("")).when(terminal).connect("*");
        assertNull(reader.getName());

    }

    @Test
    public void testIsSEPresent() throws ReaderException, CardException {
        CardTerminal terminal = Mockito.mock(CardTerminal.class);
        String name = "lecteur";
        SmartCardIOReader reader = new SmartCardIOReader(terminal, name);

        Mockito.when(terminal.isCardPresent()).thenReturn(true);
        assertTrue(reader.isSEPresent());
        Mockito.when(terminal.isCardPresent()).thenReturn(false);
        assertFalse(reader.isSEPresent());

        Mockito.doThrow(new CardException("")).when(terminal).isCardPresent();
        assertFalse(reader.isSEPresent());

    }

    @Test
    public void testTransmit() throws ReaderException, CardException {
        byte[] returnOK = { (byte) 0x90, (byte) 0x00 };
        APDUResponse responseMockMF = new APDUResponse(
                new byte[] { (byte) 0x85, 0x17, 0x00, 0x01, 0x00, 0x00, 0x00, 0x12, 0x12, 0x00, 0x00, 0x01, 0x03, 0x01,
                        0x01, 0x00, 0x7E, 0x7E, 0x7E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },
                true, returnOK);
        APDURequest apduRequestMF = new APDURequest(
                new byte[] { (byte) 0x94, (byte) 0xA4, 0x00, 0x00, 0x02, 0x3F, 0x02 }, false);

        // code de la reponse attendu
        
        APDUResponse responseMockFci = new APDUResponse(new byte[] { 0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54,
                0x52, 0x2E, 0x49, 0x43, 0x41, (byte) 0xA5, 0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08, 0x00, 0x00,
                0x00, 0x00, 0x27, 0x4A, (byte) 0x9A, (byte) 0xB9, 0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01,
                (byte) 0x90, 0x00 }, true, returnOK);
        List<APDUResponse> apduResponses = new ArrayList<>();
        apduResponses.add(responseMockMF);
        
        SEResponse reponseMock = new SEResponse(true, responseMockFci, apduResponses);

        ResponseAPDU respAPDU= new ResponseAPDU(new byte[] { (byte) 0x85, 0x17, 0x00, 0x01, 0x00, 0x00, 0x00, 0x12, 0x12,
                0x00, 0x00, 0x01, 0x03, 0x01, 0x01, 0x00, 0x7E, 0x7E, 0x7E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                (byte) 0x90, 0x00 });
        
        CardTerminal terminal = Mockito.mock(CardTerminal.class);
        String name = "lecteur";
        Card card = Mockito.mock(Card.class);
        CardChannel channel = Mockito.mock(CardChannel.class);

        Mockito.when(terminal.connect("*")).thenReturn(card);
        Mockito.when(terminal.isCardPresent()).thenReturn(true);
        Mockito.when(card.getBasicChannel()).thenReturn(channel);
        Mockito.when(channel.transmit(any(CommandAPDU.class))).thenReturn(respAPDU);
//        Mockito.when(channel.transmit(new CommandAPDU(apduRequestMF.getbytes())))
//                .thenReturn(respAPDU);

        SmartCardIOReader reader = new SmartCardIOReader(terminal, name);
        // code de la requete
        byte[] aidToSelect = new byte[] { (byte) 0x94, (byte) 0xCA, 0x00, 0x4F, 0x00 };

        List<APDURequest> apduRequests = new ArrayList<>();
        apduRequests.add(apduRequestMF);
        SERequest seApplicationRequest = new SERequest(aidToSelect, true, apduRequests);

        SEResponse reponseActuelle = reader.transmit(seApplicationRequest);

        assertArrayEquals(reponseActuelle.getApduResponses().get(0).getbytes(),
                reponseMock.getApduResponses().get(0).getbytes());

        seApplicationRequest = new SERequest(aidToSelect, false, apduRequests);
        reponseActuelle = reader.transmit(seApplicationRequest);

        assertArrayEquals(reponseActuelle.getApduResponses().get(0).getbytes(),
                reponseMock.getApduResponses().get(0).getbytes());

        Mockito.when(terminal.isCardPresent()).thenReturn(false);
        List<APDUResponse> apduResponseList = new ArrayList<>();
        SEResponse expected = new SEResponse(false, null, apduResponseList);
        reponseActuelle = reader.transmit(seApplicationRequest);
        assertEquals(expected.getApduResponses().isEmpty(), reponseActuelle.getApduResponses().isEmpty());
        assertEquals(expected.getFci(), reponseActuelle.getFci());

        Mockito.when(terminal.isCardPresent()).thenReturn(true);

        Mockito.doThrow(new CardException("")).when(card).disconnect(true);
        assertNotNull(reader.transmit(seApplicationRequest));

        Mockito.doThrow(new CardException("")).when(channel).transmit(any(CommandAPDU.class));
        reponseActuelle = reader.transmit(seApplicationRequest);
//        assertFalse(reader.transmit(seApplicationRequest).getApduResponses().get(0).isSuccessful());
        assertEquals(expected.getApduResponses().isEmpty(), reponseActuelle.getApduResponses().isEmpty());
        assertEquals(expected.getFci(), reponseActuelle.getFci());

        Mockito.doThrow(new CardException("")).when(terminal).connect("*");
//        assertNull(reader.transmit(seApplicationRequest));

    }

}
