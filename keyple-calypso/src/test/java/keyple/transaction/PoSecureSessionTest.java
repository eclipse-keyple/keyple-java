/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package keyple.transaction;

import static keyple.commands.utils.TestsUtilsResponseTabByteGenerator.*;
import static org.junit.Assert.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keyple.calypso.commands.SendableInSession;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.po.builder.AbstractOpenSessionCmdBuild;
import org.keyple.calypso.commands.po.builder.OpenSession24CmdBuild;
import org.keyple.calypso.commands.po.builder.PoGetChallengeCmdBuild;
import org.keyple.calypso.commands.po.builder.ReadRecordsCmdBuild;
import org.keyple.calypso.commands.po.parser.GetDataFciRespPars;
import org.keyple.calypso.transaction.PoSecureSession;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.*;
import org.keyple.seproxy.exceptions.*;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PoSecureSessionTest {
    @Mock
    ProxyReader poReader;

    @Mock
    ProxyReader csmSessionReader;

    byte defaultKeyIndex = (byte) 0x03;

    PoSecureSession poPlainSecrureSession;


    /**
     * The revision.
     */
    private PoRevision revision = PoRevision.REV3_1;

    @Mock
    private GetDataFciRespPars poFciRespPars;

    ByteBuffer samchallenge;

    private SeResponseSet responseTerminalSessionSignature;
    private SeResponseSet responseTerminalSessionSignatureError;
    private SeResponseSet responseFci;
    private SeResponseSet responseFciError;
    private SeResponseSet responseOpenSession;
    private SeResponseSet responseOpenSessionError;

    @Before
    public void setUp() {
        samchallenge = ByteBuffer.wrap(new byte[] {0x01, 0x02, 0x03, 0x04});

        ApduResponse apduResponse = generateApduResponseOpenSessionCmd();
        List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();
        apduResponseList.add(apduResponse);
        responseOpenSession = new SeResponseSet(true, apduResponse, apduResponseList);

        ApduResponse apduResponseErr = generateApduResponseOpenSessionCmdError();
        List<ApduResponse> apduResponseListErr = new ArrayList<ApduResponse>();
        apduResponseListErr.add(apduResponseErr);
        responseOpenSessionError = new SeResponseSet(true, apduResponseErr, apduResponseListErr);


        ApduResponse apduResponseTerminalSessionSignature =
                generateApduResponseTerminalSessionSignatureCmd();
        List<ApduResponse> apduResponseTerminalSessionSignatureList = new ArrayList<ApduResponse>();
        apduResponseTerminalSessionSignatureList.add(apduResponseTerminalSessionSignature);
        responseTerminalSessionSignature = new SeResponseSet(true,
                apduResponseTerminalSessionSignature, apduResponseTerminalSessionSignatureList);

        ApduResponse apduResponseTerminalSessionSignatureErr =
                generateApduResponseTerminalSessionSignatureCmdError();
        List<ApduResponse> apduResponseTerminalSessionSignatureListErr =
                new ArrayList<ApduResponse>();
        apduResponseTerminalSessionSignatureListErr.add(apduResponseTerminalSessionSignatureErr);
        responseTerminalSessionSignatureError =
                new SeResponseSet(true, apduResponseTerminalSessionSignatureErr,
                        apduResponseTerminalSessionSignatureListErr);


        ApduResponse apduResponseFci = generateApduResponseFciCmd();
        List<ApduResponse> apduResponseFciList = new ArrayList<ApduResponse>();
        apduResponseFciList.add(apduResponseFci);
        responseFci = new SeResponseSet(true, apduResponseFci, apduResponseFciList);

        ApduResponse apduResponseFciErr = generateApduResponseFciCmdError();
        List<ApduResponse> apduResponseFciListErr = new ArrayList<ApduResponse>();
        apduResponseFciListErr.add(apduResponseFciErr);
        responseFciError = new SeResponseSet(true, apduResponseFciErr, apduResponseFciListErr);
    }

    private void setBeforeTest(byte key) throws IOReaderException {

        poPlainSecrureSession = new PoSecureSession(poReader, csmSessionReader, key);
        Mockito.when(poReader.transmit(Matchers.any(SeRequestSet.class)))
                .thenReturn(responseOpenSession);
        Mockito.when(csmSessionReader.transmit(Matchers.any(SeRequestSet.class)))
                .thenReturn(responseOpenSession);
    }

    @Test
    public void processOpeningTestKif0xFFKey0x03noCmdInside()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, ReaderTimeoutException, InconsistentCommandException {

        byte key = (byte) 0x03;
        this.setBeforeTest(key);
        byte sfi = (byte) 0x08;
        byte recordNumber = (byte) 0x01;

        SeResponse seResponse2 = this.processOpeningTestKif0xFFKey(key, sfi, recordNumber,
                responseOpenSession.getApduResponses(), null);

        assertEquals(2, seResponse2.getApduResponses().size());
        // Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        assertEquals(responseOpenSession.getApduResponses().get(0).getBuffer(),
                seResponse2.getApduResponses().get(0).getBuffer());
        assertEquals(responseOpenSession.getApduResponses().get(0).getStatusCode(),
                seResponse2.getApduResponses().get(0).getStatusCode());
        assertEquals(responseOpenSession.getApduResponses().get(0).getBuffer(),
                seResponse2.getApduResponses().get(1).getBuffer());
        assertEquals(responseOpenSession.getApduResponses().get(0).getStatusCode(),
                seResponse2.getApduResponses().get(1).getStatusCode());
    }

    @Test
    public void processOpeningTestKif0xFFKey0x01noCmdInside()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, ReaderTimeoutException, InconsistentCommandException {

        byte key = (byte) 0x01;
        this.setBeforeTest(key);
        byte sfi = (byte) 0x08;
        byte recordNumber = (byte) 0x01;

        SeResponse seResponse2 = this.processOpeningTestKif0xFFKey(key, sfi, recordNumber,
                responseOpenSession.getApduResponses(), null);

        assertEquals(2, seResponse2.getApduResponses().size());
        // Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        assertEquals(responseOpenSession.getApduResponses().get(0).getBuffer(),
                seResponse2.getApduResponses().get(0).getBuffer());
        assertEquals(responseOpenSession.getApduResponses().get(0).getStatusCode(),
                seResponse2.getApduResponses().get(0).getStatusCode());
        assertEquals(responseOpenSession.getApduResponses().get(0).getBuffer(),
                seResponse2.getApduResponses().get(1).getBuffer());
        assertEquals(responseOpenSession.getApduResponses().get(0).getStatusCode(),
                seResponse2.getApduResponses().get(1).getStatusCode());
    }

    @Test
    public void processOpeningTestKif0xFFKey0x02noCmdInside()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, ReaderTimeoutException, InconsistentCommandException {

        byte key = (byte) 0x02;
        this.setBeforeTest(key);
        byte sfi = (byte) 0x08;
        byte recordNumber = (byte) 0x01;

        SeResponse seResponse2 = this.processOpeningTestKif0xFFKey(key, sfi, recordNumber,
                responseOpenSession.getApduResponses(), null);

        assertEquals(2, seResponse2.getApduResponses().size());
        // Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        assertEquals(responseOpenSession.getApduResponses().get(0).getBuffer(),
                seResponse2.getApduResponses().get(0).getBuffer());
        assertEquals(responseOpenSession.getApduResponses().get(0).getStatusCode(),
                seResponse2.getApduResponses().get(0).getStatusCode());
        assertEquals(responseOpenSession.getApduResponses().get(0).getBuffer(),
                seResponse2.getApduResponses().get(1).getBuffer());
        assertEquals(responseOpenSession.getApduResponses().get(0).getStatusCode(),
                seResponse2.getApduResponses().get(1).getStatusCode());
    }


    @Test
    public void processOpeningTestKif0xFFKey0x03WithCmdInside()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, ReaderTimeoutException, InconsistentCommandException {

        byte key = (byte) 0x03;
        this.setBeforeTest(key);
        byte sfi = (byte) 0x08;
        byte recordNumber = (byte) 0x01;

        SendableInSession[] poCommandsInsideSession = new SendableInSession[1];

        poCommandsInsideSession[0] = new ReadRecordsCmdBuild(PoRevision.REV2_4, recordNumber, false,
                (byte) 0x08, (byte) 0x00);

        SeResponse seResponse2 = this.processOpeningTestKif0xFFKey(key, sfi, recordNumber,
                responseOpenSession.getApduResponses(), poCommandsInsideSession);

        assertEquals(3, seResponse2.getApduResponses().size());
        // Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        for (int i = 0; i < 3; i++) {
            assertEquals(responseOpenSession.getApduResponses().get(0).getBuffer(),
                    seResponse2.getApduResponses().get(i).getBuffer());
            assertEquals(responseOpenSession.getApduResponses().get(0).getStatusCode(),
                    seResponse2.getApduResponses().get(i).getStatusCode());
        }
    }

    @Test(expected = UnexpectedReaderException.class)
    public void processOpeningTestKif0xFFKey0x03WithCmdInsideUnexpectedReaderException()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, ReaderTimeoutException, InconsistentCommandException {

        byte key = (byte) 0x03;
        this.setBeforeTest(key);
        Mockito.when(poReader.transmit(Matchers.any(SeRequestSet.class)))
                .thenReturn(responseOpenSessionError);
        byte sfi = (byte) 0x08;
        byte recordNumber = (byte) 0x01;

        SendableInSession[] poCommandsInsideSession = new SendableInSession[1];

        poCommandsInsideSession[0] = new ReadRecordsCmdBuild(PoRevision.REV2_4, recordNumber, false,
                (byte) 0x08, (byte) 0x00);

        SeResponse seResponse2 = this.processOpeningTestKif0xFFKey(key, sfi, recordNumber,
                responseOpenSession.getApduResponses(), poCommandsInsideSession);

    }


    @Test(expected = InconsistentCommandException.class)
    public void processProceedingTestInconsitenteCommandException()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, ReaderTimeoutException, InconsistentCommandException {

        this.setBeforeTest(this.defaultKeyIndex);
        Mockito.when(csmSessionReader.transmit(Matchers.any(SeRequestSet.class)))
                .thenReturn(responseOpenSessionError);
        byte recordNumber = (byte) 0x01;

        SendableInSession[] poCommandsInsideSession = new SendableInSession[1];

        poCommandsInsideSession[0] = new ReadRecordsCmdBuild(PoRevision.REV2_4, recordNumber, false,
                (byte) 0x08, (byte) 0x00);

        SeResponse seResponse2 = this.poPlainSecrureSession
                .processProceeding(Arrays.asList(poCommandsInsideSession));
    }

    @Test
    public void processProceedingTest()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, ReaderTimeoutException, InconsistentCommandException {

        this.setBeforeTest(this.defaultKeyIndex);
        byte recordNumber = (byte) 0x01;

        SendableInSession[] poCommandsInsideSession = new SendableInSession[1];

        poCommandsInsideSession[0] = new ReadRecordsCmdBuild(PoRevision.REV2_4, recordNumber, false,
                (byte) 0x08, (byte) 0x00);

        SeResponse seResponse2 = this.poPlainSecrureSession
                .processProceeding(Arrays.asList(poCommandsInsideSession));

        assertEquals(1, seResponse2.getApduResponses().size());
        // Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        assertEquals(responseOpenSession.getApduResponses().get(0).getBuffer(),
                seResponse2.getApduResponses().get(0).getBuffer());
        assertEquals(responseOpenSession.getApduResponses().get(0).getStatusCode(),
                seResponse2.getApduResponses().get(0).getStatusCode());
    }


    @Test(expected = InvalidApduReaderException.class)
    public void processClosingTestNoCmdInsideInvalidApduReaderException() throws Exception {

        this.setBeforeTest(this.defaultKeyIndex);
        SendableInSession[] poCommandsInsideSession = null;

        Mockito.when(poReader.transmit(Matchers.any(SeRequestSet.class)))
                .thenReturn(responseTerminalSessionSignatureError);
        Mockito.when(csmSessionReader.transmit(Matchers.any(SeRequestSet.class)))
                .thenReturn(responseTerminalSessionSignature);

        PoGetChallengeCmdBuild ratificationCommand =
                new PoGetChallengeCmdBuild(this.poPlainSecrureSession.getRevision());

        SeResponse seResponse2 = poPlainSecrureSession
                .processClosing(Arrays.asList(poCommandsInsideSession), null, ratificationCommand);
    }

    @Test
    public void processClosingTestNoCmdInside() throws Exception {

        this.setBeforeTest(this.defaultKeyIndex);
        // SendableInSession[] poCommandsInsideSession = null;

        Mockito.when(poReader.transmit(Matchers.any(SeRequestSet.class)))
                .thenReturn(responseTerminalSessionSignature);
        Mockito.when(csmSessionReader.transmit(Matchers.any(SeRequestSet.class)))
                .thenReturn(responseTerminalSessionSignature);

        PoGetChallengeCmdBuild ratificationCommand =
                new PoGetChallengeCmdBuild(this.poPlainSecrureSession.getRevision());

        SeResponse seResponse2 =
                poPlainSecrureSession.processClosing(null, null, ratificationCommand);
        // assertEquals(1, seResponse2.getApduResponses().size());
        // Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        assertEquals(responseTerminalSessionSignature.getApduResponses().get(0).getBuffer(),
                seResponse2.getApduResponses().get(0).getBuffer());
        assertEquals(responseTerminalSessionSignature.getApduResponses().get(0).getStatusCode(),
                seResponse2.getApduResponses().get(0).getStatusCode());
    }

    @Test
    public void processClosingTestWithCmdInside() throws Exception {

        this.setBeforeTest(this.defaultKeyIndex);
        byte recordNumber = (byte) 0x01;
        SendableInSession[] poCommandsInsideSession = new SendableInSession[1];

        Mockito.when(poReader.transmit(Matchers.any(SeRequestSet.class)))
                .thenReturn(responseTerminalSessionSignature);
        Mockito.when(csmSessionReader.transmit(Matchers.any(SeRequestSet.class)))
                .thenReturn(responseTerminalSessionSignature);

        poCommandsInsideSession[0] = new ReadRecordsCmdBuild(PoRevision.REV2_4, recordNumber, false,
                (byte) 0x08, (byte) 0x00);
        PoGetChallengeCmdBuild ratificationCommand =
                new PoGetChallengeCmdBuild(this.poPlainSecrureSession.getRevision());

        SeResponse seResponse2 = poPlainSecrureSession
                .processClosing(Arrays.asList(poCommandsInsideSession), null, ratificationCommand);
        assertEquals(2, seResponse2.getApduResponses().size());
        // Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        assertEquals(responseTerminalSessionSignature.getApduResponses().get(0).getBuffer(),
                seResponse2.getApduResponses().get(0).getBuffer());
        assertEquals(responseTerminalSessionSignature.getApduResponses().get(0).getStatusCode(),
                seResponse2.getApduResponses().get(0).getStatusCode());
        assertEquals(responseTerminalSessionSignature.getApduResponses().get(0).getBuffer(),
                seResponse2.getApduResponses().get(1).getBuffer());
        assertEquals(responseTerminalSessionSignature.getApduResponses().get(0).getStatusCode(),
                seResponse2.getApduResponses().get(1).getStatusCode());
    }


    @Test
    public void processIdentificationTest()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, ReaderTimeoutException, InconsistentCommandException {

        this.setBeforeTest(this.defaultKeyIndex);

        Mockito.when(poReader.transmit(Matchers.any(SeRequestSet.class))).thenReturn(responseFci);
        Mockito.when(csmSessionReader.transmit(Matchers.any(SeRequestSet.class)))
                .thenReturn(responseFci);
        byte recordNumber = (byte) 0x01;

        SendableInSession[] poCommandsInsideSession = new SendableInSession[1];

        poCommandsInsideSession[0] = new ReadRecordsCmdBuild(PoRevision.REV2_4, recordNumber, false,
                (byte) 0x08, (byte) 0x00);

        ByteBuffer aid =
                ByteBuffer.wrap(new byte[] {0x33, 0x4D, 0x54, 0x52, 0x2E, 0x49, 0x43, 0x41});
        SeResponse seResponse2 = this.poPlainSecrureSession.processIdentification(aid,
                Arrays.asList(poCommandsInsideSession));

        assertEquals(3, seResponse2.getApduResponses().size());
        // Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNotNull(seResponse2.getFci());
        assertEquals(responseFci.getApduResponses().get(0).getBuffer(),
                seResponse2.getApduResponses().get(0).getBuffer());
        assertEquals(responseFci.getApduResponses().get(0).getStatusCode(),
                seResponse2.getApduResponses().get(0).getStatusCode());
    }

    @Test
    public void computePoRevisionTest() {

        byte applicationTypeByte = (byte) 0x1F;
        PoRevision retourExpected = PoRevision.REV2_4;
        PoRevision retour = PoSecureSession.computePoRevision(applicationTypeByte);
        Assert.assertEquals(retourExpected, retour);

        applicationTypeByte = (byte) 0x21;
        retourExpected = PoRevision.REV3_1;
        retour = PoSecureSession.computePoRevision(applicationTypeByte);
        Assert.assertEquals(retourExpected, retour);

        applicationTypeByte = (byte) 0X28;
        retourExpected = PoRevision.REV3_2;
        retour = PoSecureSession.computePoRevision(applicationTypeByte);
        Assert.assertEquals(retourExpected, retour);

    }

    private SeResponse processOpeningTestKif0xFFKey(byte key, byte sfi, byte recordNumber,
            List<ApduResponse> apduExpected, SendableInSession[] poCommandsInsideSession)
            throws IOReaderException, InconsistentCommandException {
        AbstractOpenSessionCmdBuild openCommand =
                new OpenSession24CmdBuild(key, samchallenge, sfi, recordNumber);
        return poPlainSecrureSession.processOpening(openCommand,
                poCommandsInsideSession != null ? Arrays.asList(poCommandsInsideSession) : null);

    }
}
