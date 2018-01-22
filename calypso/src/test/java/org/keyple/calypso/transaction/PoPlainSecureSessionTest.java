package org.keyple.calypso.transaction;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keyple.commands.calypso.InconsistentCommandException;
import org.keyple.commands.calypso.dto.AID;
import org.keyple.commands.calypso.po.PoRevision;
import org.keyple.commands.calypso.po.SendableInSession;
import org.keyple.commands.calypso.po.builder.OpenSessionCmdBuild;
import org.keyple.commands.calypso.po.builder.PoGetChallengeCmdBuild;
import org.keyple.commands.calypso.po.builder.ReadRecordsCmdBuild;
import org.keyple.commands.calypso.po.parser.GetDataFciRespPars;
import org.keyple.commands.calypso.utils.TestsUtilsResponseTabByteGenerator;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.ProxyReader;
import org.keyple.seproxy.SeRequest;
import org.keyple.seproxy.SeResponse;
import org.keyple.seproxy.exceptions.ChannelStateReaderException;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.seproxy.exceptions.InvalidApduReaderException;
import org.keyple.seproxy.exceptions.TimeoutReaderException;
import org.keyple.seproxy.exceptions.UnexpectedReaderException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class PoPlainSecureSessionTest {

    static final Logger logger = LoggerFactory.getLogger(PoPlainSecureSessionTest.class);

    @Mock
    ProxyReader poReader;

    @Mock
    ProxyReader csmSessionReader;

    byte defaultKeyIndex = (byte) 0x03;

    PoPlainSecureSession poPlainSecrureSession;


    /** The revision. */
    private PoRevision revision = PoRevision.REV3_1;

    @Mock
    private GetDataFciRespPars poFciRespPars;

    @Mock
    AID selectedAID;

    byte[] samchallenge;

    private SeResponse responseTerminalSessionSignature;
    private SeResponse responseTerminalSessionSignatureError;
    private SeResponse responseFci;
    private SeResponse responseFciError;
    private SeResponse responseOpenSession;
    private SeResponse responseOpenSessionError;

    @Before
    public void setUp(){
        samchallenge = new byte[] { 0x01, 0x02, 0x03, 0x04 };

        ApduResponse apduResponse = TestsUtilsResponseTabByteGenerator.generateApduResponseOpenSessionCmd();
        List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();
        apduResponseList.add(apduResponse);
        responseOpenSession = new SeResponse(true, apduResponse, apduResponseList);

        ApduResponse apduResponseErr = TestsUtilsResponseTabByteGenerator.generateApduResponseOpenSessionCmdError();
        List<ApduResponse> apduResponseListErr = new ArrayList<ApduResponse>();
        apduResponseListErr.add(apduResponseErr);
        responseOpenSessionError = new SeResponse(true, apduResponseErr, apduResponseListErr);



        ApduResponse apduResponseTerminalSessionSignature = TestsUtilsResponseTabByteGenerator.generateApduResponseTerminalSessionSignatureCmd();
        List<ApduResponse> apduResponseTerminalSessionSignatureList = new ArrayList<ApduResponse>();
        apduResponseTerminalSessionSignatureList.add(apduResponseTerminalSessionSignature);
        responseTerminalSessionSignature= new SeResponse(true, apduResponseTerminalSessionSignature, apduResponseTerminalSessionSignatureList);

        ApduResponse apduResponseTerminalSessionSignatureErr = TestsUtilsResponseTabByteGenerator.generateApduResponseTerminalSessionSignatureCmdError();
        List<ApduResponse> apduResponseTerminalSessionSignatureListErr = new ArrayList<ApduResponse>();
        apduResponseTerminalSessionSignatureListErr.add(apduResponseTerminalSessionSignatureErr);
        responseTerminalSessionSignatureError= new SeResponse(true, apduResponseTerminalSessionSignatureErr, apduResponseTerminalSessionSignatureListErr);




        ApduResponse apduResponseFci= TestsUtilsResponseTabByteGenerator.generateApduResponseFciCmd();
        List<ApduResponse> apduResponseFciList = new ArrayList<ApduResponse>();
        apduResponseFciList.add(apduResponseFci);
        responseFci= new SeResponse(true, apduResponseFci, apduResponseFciList);

        ApduResponse apduResponseFciErr= TestsUtilsResponseTabByteGenerator.generateApduResponseFciCmdError();
        List<ApduResponse> apduResponseFciListErr = new ArrayList<ApduResponse>();
        apduResponseFciListErr.add(apduResponseFciErr);
        responseFciError= new SeResponse(true, apduResponseFciErr, apduResponseFciListErr);
    }

    private void setBeforeTest(byte key) throws ChannelStateReaderException, InvalidApduReaderException, IOReaderException, TimeoutReaderException, UnexpectedReaderException {

        poPlainSecrureSession = new PoPlainSecureSession(poReader, csmSessionReader, key);
        Mockito.when(poReader.transmit(any(SeRequest.class))).thenReturn(responseOpenSession);
        Mockito.when(csmSessionReader.transmit(any(SeRequest.class))).thenReturn(responseOpenSession);
    }

    @Test
    public void processOpeningTestKif0xFFKey0x03noCmdInside()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {

        byte key = (byte) 0x03;
        this.setBeforeTest(key);
        byte sfi = (byte) 0x08;
        byte recordNumber = (byte) 0x01;

        SeResponse seResponse2 = this.processOpeningTestKif0xFFKey(key, sfi, recordNumber, responseOpenSession.getApduResponses(), null);

        assertEquals(2, seResponse2.getApduResponses().size());
        Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        assertArrayEquals(responseOpenSession.getApduResponses().get(0).getbytes(), seResponse2.getApduResponses().get(0).getbytes());
        assertArrayEquals(responseOpenSession.getApduResponses().get(0).getStatusCode(), seResponse2.getApduResponses().get(0).getStatusCode());
        assertArrayEquals(responseOpenSession.getApduResponses().get(0).getbytes(), seResponse2.getApduResponses().get(1).getbytes());
        assertArrayEquals(responseOpenSession.getApduResponses().get(0).getStatusCode(), seResponse2.getApduResponses().get(1).getStatusCode());
    }

    @Test
    public void processOpeningTestKif0xFFKey0x01noCmdInside()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {

        byte key = (byte) 0x01;
        this.setBeforeTest(key);
        byte sfi = (byte) 0x08;
        byte recordNumber = (byte) 0x01;

        SeResponse seResponse2 = this.processOpeningTestKif0xFFKey(key, sfi, recordNumber, responseOpenSession.getApduResponses(), null);

        assertEquals(2, seResponse2.getApduResponses().size());
        Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        assertArrayEquals(responseOpenSession.getApduResponses().get(0).getbytes(), seResponse2.getApduResponses().get(0).getbytes());
        assertArrayEquals(responseOpenSession.getApduResponses().get(0).getStatusCode(), seResponse2.getApduResponses().get(0).getStatusCode());
        assertArrayEquals(responseOpenSession.getApduResponses().get(0).getbytes(), seResponse2.getApduResponses().get(1).getbytes());
        assertArrayEquals(responseOpenSession.getApduResponses().get(0).getStatusCode(), seResponse2.getApduResponses().get(1).getStatusCode());
    }

    @Test
    public void processOpeningTestKif0xFFKey0x02noCmdInside()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {

        byte key = (byte) 0x02;
        this.setBeforeTest(key);
        byte sfi = (byte) 0x08;
        byte recordNumber = (byte) 0x01;

        SeResponse seResponse2 = this.processOpeningTestKif0xFFKey(key, sfi, recordNumber, responseOpenSession.getApduResponses(), null);

        assertEquals(2, seResponse2.getApduResponses().size());
        Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        assertArrayEquals(responseOpenSession.getApduResponses().get(0).getbytes(), seResponse2.getApduResponses().get(0).getbytes());
        assertArrayEquals(responseOpenSession.getApduResponses().get(0).getStatusCode(), seResponse2.getApduResponses().get(0).getStatusCode());
        assertArrayEquals(responseOpenSession.getApduResponses().get(0).getbytes(), seResponse2.getApduResponses().get(1).getbytes());
        assertArrayEquals(responseOpenSession.getApduResponses().get(0).getStatusCode(), seResponse2.getApduResponses().get(1).getStatusCode());
    }


    @Test
    public void processOpeningTestKif0xFFKey0x03WithCmdInside()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {

        byte key = (byte) 0x03;
        this.setBeforeTest(key);
        byte sfi = (byte) 0x08;
        byte recordNumber = (byte) 0x01;

        SendableInSession[] poCommandsInsideSession = new SendableInSession[1];

        poCommandsInsideSession[0] = new ReadRecordsCmdBuild(PoRevision.REV2_4, recordNumber, false, (byte) 0x08,
                (byte) 0x00);

        SeResponse seResponse2 = this.processOpeningTestKif0xFFKey(key, sfi, recordNumber, responseOpenSession.getApduResponses(), poCommandsInsideSession);

        assertEquals(3, seResponse2.getApduResponses().size());
        Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        assertArrayEquals(responseOpenSession.getApduResponses().get(0).getbytes(), seResponse2.getApduResponses().get(0).getbytes());
        assertArrayEquals(responseOpenSession.getApduResponses().get(0).getStatusCode(), seResponse2.getApduResponses().get(0).getStatusCode());
        assertArrayEquals(responseOpenSession.getApduResponses().get(0).getbytes(), seResponse2.getApduResponses().get(1).getbytes());
        assertArrayEquals(responseOpenSession.getApduResponses().get(0).getStatusCode(), seResponse2.getApduResponses().get(1).getStatusCode());
        assertArrayEquals(responseOpenSession.getApduResponses().get(0).getbytes(), seResponse2.getApduResponses().get(2).getbytes());
        assertArrayEquals(responseOpenSession.getApduResponses().get(0).getStatusCode(), seResponse2.getApduResponses().get(2).getStatusCode());
    }

    @Test (expected = UnexpectedReaderException.class)
    public void processOpeningTestKif0xFFKey0x03WithCmdInsideUnexpectedReaderException()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {

        byte key = (byte) 0x03;
        this.setBeforeTest(key);
        Mockito.when(poReader.transmit(any(SeRequest.class))).thenReturn(responseOpenSessionError);
        byte sfi = (byte) 0x08;
        byte recordNumber = (byte) 0x01;

        SendableInSession[] poCommandsInsideSession = new SendableInSession[1];

        poCommandsInsideSession[0] = new ReadRecordsCmdBuild(PoRevision.REV2_4, recordNumber, false, (byte) 0x08,
                (byte) 0x00);

        SeResponse seResponse2 = this.processOpeningTestKif0xFFKey(key, sfi, recordNumber, responseOpenSession.getApduResponses(), poCommandsInsideSession);

    }


    @Test (expected = InconsistentCommandException.class)
    public void processProceedingTestInconsitenteCommandException()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {

        this.setBeforeTest(this.defaultKeyIndex);
        Mockito.when(csmSessionReader.transmit(any(SeRequest.class))).thenReturn(responseOpenSessionError);
        byte recordNumber = (byte) 0x01;

        SendableInSession[] poCommandsInsideSession = new SendableInSession[1];

        poCommandsInsideSession[0] = new ReadRecordsCmdBuild(PoRevision.REV2_4, recordNumber, false, (byte) 0x08,
                (byte) 0x00);

        SeResponse seResponse2 = this.poPlainSecrureSession.processProceeding(poCommandsInsideSession);
    }

    @Test
    public void processProceedingTest()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {

        this.setBeforeTest(this.defaultKeyIndex);
        byte recordNumber = (byte) 0x01;

        SendableInSession[] poCommandsInsideSession = new SendableInSession[1];

        poCommandsInsideSession[0] = new ReadRecordsCmdBuild(PoRevision.REV2_4, recordNumber, false, (byte) 0x08,
                (byte) 0x00);

        SeResponse seResponse2 = this.poPlainSecrureSession.processProceeding(poCommandsInsideSession);

        assertEquals(1, seResponse2.getApduResponses().size());
        Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        assertArrayEquals(responseOpenSession.getApduResponses().get(0).getbytes(), seResponse2.getApduResponses().get(0).getbytes());
        assertArrayEquals(responseOpenSession.getApduResponses().get(0).getStatusCode(), seResponse2.getApduResponses().get(0).getStatusCode());

    }


    @Test (expected = InvalidApduReaderException.class)
    public void processClosingTestNoCmdInsideInvalidApduReaderException() throws Exception {

        this.setBeforeTest(this.defaultKeyIndex);
        SendableInSession[] poCommandsInsideSession = null;

        Mockito.when(poReader.transmit(any(SeRequest.class))).thenReturn(responseTerminalSessionSignatureError);
        Mockito.when(csmSessionReader.transmit(any(SeRequest.class))).thenReturn(responseTerminalSessionSignature);

        PoGetChallengeCmdBuild ratificationCommand = new PoGetChallengeCmdBuild(this.poPlainSecrureSession.getRevision());

        SeResponse seResponse2 = poPlainSecrureSession.processClosing(poCommandsInsideSession, null,
                ratificationCommand);
    }

    @Test
    public void processClosingTestNoCmdInside() throws Exception {

        this.setBeforeTest(this.defaultKeyIndex);
        SendableInSession[] poCommandsInsideSession = null;

        Mockito.when(poReader.transmit(any(SeRequest.class))).thenReturn(responseTerminalSessionSignature);
        Mockito.when(csmSessionReader.transmit(any(SeRequest.class))).thenReturn(responseTerminalSessionSignature);

        PoGetChallengeCmdBuild ratificationCommand = new PoGetChallengeCmdBuild(this.poPlainSecrureSession.getRevision());

        SeResponse seResponse2 = poPlainSecrureSession.processClosing(poCommandsInsideSession, null,
                ratificationCommand);
        assertEquals(1, seResponse2.getApduResponses().size());
        Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        assertArrayEquals(responseTerminalSessionSignature.getApduResponses().get(0).getbytes(), seResponse2.getApduResponses().get(0).getbytes());
        assertArrayEquals(responseTerminalSessionSignature.getApduResponses().get(0).getStatusCode(), seResponse2.getApduResponses().get(0).getStatusCode());
    }

    @Test
    public void processClosingTestWithCmdInside() throws Exception {

        this.setBeforeTest(this.defaultKeyIndex);
        byte recordNumber = (byte) 0x01;
        SendableInSession[] poCommandsInsideSession = new SendableInSession[1];

        Mockito.when(poReader.transmit(any(SeRequest.class))).thenReturn(responseTerminalSessionSignature);
        Mockito.when(csmSessionReader.transmit(any(SeRequest.class))).thenReturn(responseTerminalSessionSignature);

        poCommandsInsideSession[0] = new ReadRecordsCmdBuild(PoRevision.REV2_4, recordNumber, false, (byte) 0x08,
                (byte) 0x00);
        PoGetChallengeCmdBuild ratificationCommand = new PoGetChallengeCmdBuild(this.poPlainSecrureSession.getRevision());

        SeResponse seResponse2 = poPlainSecrureSession.processClosing(poCommandsInsideSession, null,
                ratificationCommand);
        assertEquals(2, seResponse2.getApduResponses().size());
        Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        assertArrayEquals(responseTerminalSessionSignature.getApduResponses().get(0).getbytes(), seResponse2.getApduResponses().get(0).getbytes());
        assertArrayEquals(responseTerminalSessionSignature.getApduResponses().get(0).getStatusCode(), seResponse2.getApduResponses().get(0).getStatusCode());
        assertArrayEquals(responseTerminalSessionSignature.getApduResponses().get(0).getbytes(), seResponse2.getApduResponses().get(1).getbytes());
        assertArrayEquals(responseTerminalSessionSignature.getApduResponses().get(0).getStatusCode(), seResponse2.getApduResponses().get(1).getStatusCode());
    }


    @Test
    public void processIdentificationTest()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {

        this.setBeforeTest(this.defaultKeyIndex);

        Mockito.when(poReader.transmit(any(SeRequest.class))).thenReturn(responseFci);
        Mockito.when(csmSessionReader.transmit(any(SeRequest.class))).thenReturn(responseFci);
        byte recordNumber = (byte) 0x01;

        SendableInSession[] poCommandsInsideSession = new SendableInSession[1];

        poCommandsInsideSession[0] = new ReadRecordsCmdBuild(PoRevision.REV2_4, recordNumber, false, (byte) 0x08,
                (byte) 0x00);

        byte[] aid = new byte[] { 0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49, 0x43, 0x41 };
        SeResponse seResponse2 = this.poPlainSecrureSession.processIdentification(aid, poCommandsInsideSession);

        assertEquals(3, seResponse2.getApduResponses().size());
        Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNotNull(seResponse2.getFci());
        assertArrayEquals(responseFci.getApduResponses().get(0).getbytes(), seResponse2.getApduResponses().get(0).getbytes());
        assertArrayEquals(responseFci.getApduResponses().get(0).getStatusCode(), seResponse2.getApduResponses().get(0).getStatusCode());

    }

    @Test
    public void computePoRevisionTest(){

        byte applicationTypeByte = (byte) 0x1F;
        PoRevision retourExpected = PoRevision.REV2_4;
        PoRevision retour = this.poPlainSecrureSession.computePoRevision(applicationTypeByte);
        assertEquals(retourExpected, retour);

        applicationTypeByte = (byte) 0x21;
        retourExpected = PoRevision.REV3_1;
        retour = this.poPlainSecrureSession.computePoRevision(applicationTypeByte);
        assertEquals(retourExpected, retour);

        applicationTypeByte = (byte) 0X28;
        retourExpected = PoRevision.REV3_2;
        retour = this.poPlainSecrureSession.computePoRevision(applicationTypeByte);
        assertEquals(retourExpected, retour);

    }

    private SeResponse processOpeningTestKif0xFFKey(byte key, byte sfi, byte recordNumber, List<ApduResponse> apduExpected, SendableInSession[] poCommandsInsideSession)
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, TimeoutReaderException, InconsistentCommandException {
        OpenSessionCmdBuild openCommand = new OpenSessionCmdBuild(PoRevision.REV2_4, key, samchallenge, sfi, recordNumber);
        return poPlainSecrureSession.processOpening(openCommand, poCommandsInsideSession);

    }
}
