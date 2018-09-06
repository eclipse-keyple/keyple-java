/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.transaction;

import static org.eclipse.keyple.command.util.TestsUtilsResponseTabByteGenerator.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.keyple.calypso.command.po.PoModificationCommand;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.PoSendableInSession;
import org.eclipse.keyple.calypso.command.po.builder.DecreaseCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.PoGetChallengeCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.command.po.parser.GetDataFciRespPars;
import org.eclipse.keyple.calypso.transaction.PoSecureSession;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.exception.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
    public void setUp() throws InconsistentParameterValueException {
        samchallenge = ByteBuffer.wrap(new byte[] {0x01, 0x02, 0x03, 0x04});

        ApduResponse apduResponse = generateApduResponseOpenSessionCmd();
        List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();
        apduResponseList.add(apduResponse);
        responseOpenSession =
                new SeResponseSet(new SeResponse(true, null, apduResponse, apduResponseList));

        ApduResponse apduResponseErr = generateApduResponseOpenSessionCmdError();
        List<ApduResponse> apduResponseListErr = new ArrayList<ApduResponse>();
        apduResponseListErr.add(apduResponseErr);
        responseOpenSessionError =
                new SeResponseSet(new SeResponse(true, null, apduResponseErr, apduResponseListErr));


        ApduResponse apduResponseTerminalSessionSignature =
                generateApduResponseTerminalSessionSignatureCmd();
        List<ApduResponse> apduResponseTerminalSessionSignatureList = new ArrayList<ApduResponse>();
        apduResponseTerminalSessionSignatureList.add(apduResponseTerminalSessionSignature);
        responseTerminalSessionSignature = new SeResponseSet(new SeResponse(true, null,
                apduResponseTerminalSessionSignature, apduResponseTerminalSessionSignatureList));

        ApduResponse apduResponseTerminalSessionSignatureErr =
                generateApduResponseTerminalSessionSignatureCmdError();
        List<ApduResponse> apduResponseTerminalSessionSignatureListErr =
                new ArrayList<ApduResponse>();
        apduResponseTerminalSessionSignatureListErr.add(apduResponseTerminalSessionSignatureErr);
        responseTerminalSessionSignatureError = new SeResponseSet(
                new SeResponse(true, null, apduResponseTerminalSessionSignatureErr,
                        apduResponseTerminalSessionSignatureListErr));


        ApduResponse apduResponseFci = generateApduResponseFciCmd();
        List<ApduResponse> apduResponseFciList = new ArrayList<ApduResponse>();
        apduResponseFciList.add(apduResponseFci);
        responseFci =
                new SeResponseSet(new SeResponse(true, null, apduResponseFci, apduResponseFciList));

        ApduResponse apduResponseFciErr = generateApduResponseFciCmdError();
        List<ApduResponse> apduResponseFciListErr = new ArrayList<ApduResponse>();
        apduResponseFciListErr.add(apduResponseFciErr);
        responseFciError = new SeResponseSet(
                new SeResponse(true, null, apduResponseFciErr, apduResponseFciListErr));
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
            InvalidApduReaderException, ReaderTimeoutException, IllegalArgumentException {

        byte key = (byte) 0x03;
        this.setBeforeTest(key);
        byte sfi = (byte) 0x08;
        byte recordNumber = (byte) 0x01;

        SeResponse seResponse2 = this.processOpeningTestKif0xFFKey(key, sfi, recordNumber,
                responseOpenSession.getSingleResponse().getApduResponses(), null);

        assertEquals(2, seResponse2.getApduResponses().size());
        // Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        assertEquals(responseOpenSession.getSingleResponse().getApduResponses().get(0).getBytes(),
                seResponse2.getApduResponses().get(0).getBytes());
        assertEquals(
                responseOpenSession.getSingleResponse().getApduResponses().get(0).getStatusCode(),
                seResponse2.getApduResponses().get(0).getStatusCode());
        assertEquals(responseOpenSession.getSingleResponse().getApduResponses().get(0).getBytes(),
                seResponse2.getApduResponses().get(1).getBytes());
        assertEquals(
                responseOpenSession.getSingleResponse().getApduResponses().get(0).getStatusCode(),
                seResponse2.getApduResponses().get(1).getStatusCode());
    }

    @Test
    public void processOpeningTestKif0xFFKey0x01noCmdInside()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, ReaderTimeoutException, IllegalArgumentException {

        byte key = (byte) 0x01;
        this.setBeforeTest(key);
        byte sfi = (byte) 0x08;
        byte recordNumber = (byte) 0x01;

        SeResponse seResponse2 = this.processOpeningTestKif0xFFKey(key, sfi, recordNumber,
                responseOpenSession.getSingleResponse().getApduResponses(), null);

        assertEquals(2, seResponse2.getApduResponses().size());
        // Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        assertEquals(responseOpenSession.getSingleResponse().getApduResponses().get(0).getBytes(),
                seResponse2.getApduResponses().get(0).getBytes());
        assertEquals(
                responseOpenSession.getSingleResponse().getApduResponses().get(0).getStatusCode(),
                seResponse2.getApduResponses().get(0).getStatusCode());
        assertEquals(responseOpenSession.getSingleResponse().getApduResponses().get(0).getBytes(),
                seResponse2.getApduResponses().get(1).getBytes());
        assertEquals(
                responseOpenSession.getSingleResponse().getApduResponses().get(0).getStatusCode(),
                seResponse2.getApduResponses().get(1).getStatusCode());
    }

    @Test
    public void processOpeningTestKif0xFFKey0x02noCmdInside()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, ReaderTimeoutException, IllegalArgumentException {

        byte key = (byte) 0x02;
        this.setBeforeTest(key);
        byte sfi = (byte) 0x08;
        byte recordNumber = (byte) 0x01;

        SeResponse seResponse2 = this.processOpeningTestKif0xFFKey(key, sfi, recordNumber,
                responseOpenSession.getSingleResponse().getApduResponses(), null);

        assertEquals(2, seResponse2.getApduResponses().size());
        // Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        assertEquals(responseOpenSession.getSingleResponse().getApduResponses().get(0).getBytes(),
                seResponse2.getApduResponses().get(0).getBytes());
        assertEquals(
                responseOpenSession.getSingleResponse().getApduResponses().get(0).getStatusCode(),
                seResponse2.getApduResponses().get(0).getStatusCode());
        assertEquals(responseOpenSession.getSingleResponse().getApduResponses().get(0).getBytes(),
                seResponse2.getApduResponses().get(1).getBytes());
        assertEquals(
                responseOpenSession.getSingleResponse().getApduResponses().get(0).getStatusCode(),
                seResponse2.getApduResponses().get(1).getStatusCode());
    }


    @Test
    public void processOpeningTestKif0xFFKey0x03WithCmdInside()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, ReaderTimeoutException, IllegalArgumentException {

        byte key = (byte) 0x03;
        this.setBeforeTest(key);
        byte sfi = (byte) 0x08;
        byte recordNumber = (byte) 0x01;

        PoSendableInSession[] poCommandsInsideSession = new PoSendableInSession[1];

        poCommandsInsideSession[0] = new ReadRecordsCmdBuild(PoRevision.REV2_4, (byte) 0x08,
                recordNumber, false, (byte) 0x00);

        SeResponse seResponse2 = this.processOpeningTestKif0xFFKey(key, sfi, recordNumber,
                responseOpenSession.getSingleResponse().getApduResponses(),
                poCommandsInsideSession);

        assertEquals(3, seResponse2.getApduResponses().size());
        // Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        for (int i = 0; i < 3; i++) {
            assertEquals(
                    responseOpenSession.getSingleResponse().getApduResponses().get(0).getBytes(),
                    seResponse2.getApduResponses().get(i).getBytes());
            assertEquals(
                    responseOpenSession.getSingleResponse().getApduResponses().get(0)
                            .getStatusCode(),
                    seResponse2.getApduResponses().get(i).getStatusCode());
        }
    }

    @Test(expected = UnexpectedReaderException.class)
    public void processOpeningTestKif0xFFKey0x03WithCmdInsideUnexpectedReaderException()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, ReaderTimeoutException, IllegalArgumentException {

        byte key = (byte) 0x03;
        this.setBeforeTest(key);
        Mockito.when(poReader.transmit(Matchers.any(SeRequestSet.class)))
                .thenReturn(responseOpenSessionError);
        byte sfi = (byte) 0x08;
        byte recordNumber = (byte) 0x01;

        PoSendableInSession[] poCommandsInsideSession = new PoSendableInSession[1];

        poCommandsInsideSession[0] = new ReadRecordsCmdBuild(PoRevision.REV2_4, (byte) 0x08,
                recordNumber, false, (byte) 0x00);

        SeResponse seResponse2 = this.processOpeningTestKif0xFFKey(key, sfi, recordNumber,
                responseOpenSession.getSingleResponse().getApduResponses(),
                poCommandsInsideSession);

    }


    @Test(expected = IllegalArgumentException.class)
    public void processProceedingTestInconsitenteCommandException()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, ReaderTimeoutException, IllegalArgumentException {

        this.setBeforeTest(this.defaultKeyIndex);
        Mockito.when(csmSessionReader.transmit(Matchers.any(SeRequestSet.class)))
                .thenReturn(responseOpenSessionError);
        byte recordNumber = (byte) 0x01;

        PoSendableInSession[] poCommandsInsideSession = new PoSendableInSession[1];

        poCommandsInsideSession[0] = new ReadRecordsCmdBuild(PoRevision.REV2_4, (byte) 0x08,
                recordNumber, false, (byte) 0x00);

        SeResponse seResponse2 = this.poPlainSecrureSession
                .processPoCommands(Arrays.asList(poCommandsInsideSession));
    }

    @Test
    public void processProceedingTest()
            throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
            InvalidApduReaderException, ReaderTimeoutException, IllegalArgumentException {

        this.setBeforeTest(this.defaultKeyIndex);
        byte recordNumber = (byte) 0x01;

        PoSendableInSession[] poCommandsInsideSession = new PoSendableInSession[1];

        poCommandsInsideSession[0] = new ReadRecordsCmdBuild(PoRevision.REV2_4, (byte) 0x08,
                recordNumber, false, (byte) 0x00);

        SeResponse seResponse2 = this.poPlainSecrureSession
                .processPoCommands(Arrays.asList(poCommandsInsideSession));

        assertEquals(1, seResponse2.getApduResponses().size());
        // Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        assertEquals(responseOpenSession.getSingleResponse().getApduResponses().get(0).getBytes(),
                seResponse2.getApduResponses().get(0).getBytes());
        assertEquals(
                responseOpenSession.getSingleResponse().getApduResponses().get(0).getStatusCode(),
                seResponse2.getApduResponses().get(0).getStatusCode());
    }


    @Test(expected = InvalidApduReaderException.class)
    public void processClosingTestNoCmdInsideInvalidApduReaderException() throws Exception {

        this.setBeforeTest(this.defaultKeyIndex);
        PoModificationCommand[] poCommandsInsideSession = null;

        Mockito.when(poReader.transmit(Matchers.any(SeRequestSet.class)))
                .thenReturn(responseTerminalSessionSignatureError);
        Mockito.when(csmSessionReader.transmit(Matchers.any(SeRequestSet.class)))
                .thenReturn(responseTerminalSessionSignature);

        PoGetChallengeCmdBuild ratificationCommand =
                new PoGetChallengeCmdBuild(this.poPlainSecrureSession.getRevision());

        SeResponse seResponse2 = poPlainSecrureSession.processClosing(
                Arrays.asList(poCommandsInsideSession), null, ratificationCommand, true);
    }

    @Test
    public void processClosingTestNoCmdInside() throws Exception {

        this.setBeforeTest(this.defaultKeyIndex);
        // PoSendableInSession[] poCommandsInsideSession = null;

        Mockito.when(poReader.transmit(Matchers.any(SeRequestSet.class)))
                .thenReturn(responseTerminalSessionSignature);
        Mockito.when(csmSessionReader.transmit(Matchers.any(SeRequestSet.class)))
                .thenReturn(responseTerminalSessionSignature);

        PoGetChallengeCmdBuild ratificationCommand =
                new PoGetChallengeCmdBuild(this.poPlainSecrureSession.getRevision());

        SeResponse seResponse2 =
                poPlainSecrureSession.processClosing(null, null, ratificationCommand, true);
        // assertEquals(1, seResponse2.getApduResponses().size());
        // Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        assertEquals(responseTerminalSessionSignature.getSingleResponse().getApduResponses().get(0)
                .getBytes(), seResponse2.getApduResponses().get(0).getBytes());
        assertEquals(responseTerminalSessionSignature.getSingleResponse().getApduResponses().get(0)
                .getStatusCode(), seResponse2.getApduResponses().get(0).getStatusCode());
    }

    @Test
    public void processClosingTestWithCmdInside() throws Exception {

        this.setBeforeTest(this.defaultKeyIndex);
        byte counterNumber = (byte) 0x01;
        int decValue = 1;
        PoModificationCommand[] poModificationCommands = new PoModificationCommand[1];

        Mockito.when(poReader.transmit(Matchers.any(SeRequestSet.class)))
                .thenReturn(responseTerminalSessionSignature);
        Mockito.when(csmSessionReader.transmit(Matchers.any(SeRequestSet.class)))
                .thenReturn(responseTerminalSessionSignature);

        poModificationCommands[0] =
                new DecreaseCmdBuild(PoRevision.REV2_4, (byte) 0x08, counterNumber, decValue);
        PoGetChallengeCmdBuild ratificationCommand =
                new PoGetChallengeCmdBuild(this.poPlainSecrureSession.getRevision());

        SeResponse seResponse2 = poPlainSecrureSession.processClosing(
                Arrays.asList(poModificationCommands), null, ratificationCommand, true);
        assertEquals(2, seResponse2.getApduResponses().size());
        // Whitebox.getInternalState(seResponse2, "channelPreviouslyOpen").equals(true);
        assertNull(seResponse2.getFci());
        assertEquals(responseTerminalSessionSignature.getSingleResponse().getApduResponses().get(0)
                .getBytes(), seResponse2.getApduResponses().get(0).getBytes());
        assertEquals(responseTerminalSessionSignature.getSingleResponse().getApduResponses().get(0)
                .getStatusCode(), seResponse2.getApduResponses().get(0).getStatusCode());
        assertEquals(responseTerminalSessionSignature.getSingleResponse().getApduResponses().get(0)
                .getBytes(), seResponse2.getApduResponses().get(1).getBytes());
        assertEquals(responseTerminalSessionSignature.getSingleResponse().getApduResponses().get(0)
                .getStatusCode(), seResponse2.getApduResponses().get(1).getStatusCode());
    }


    // @Test
    // public void processIdentificationTest()
    // throws IOReaderException, UnexpectedReaderException, ChannelStateReaderException,
    // InvalidApduReaderException, ReaderTimeoutException, IllegalArgumentException {
    //
    // this.setBeforeTest(this.defaultKeyIndex);
    //
    // Mockito.when(poReader.transmit(Matchers.any(SeRequestSet.class))).thenReturn(responseFci);
    // Mockito.when(csmSessionReader.transmit(Matchers.any(SeRequestSet.class)))
    // .thenReturn(responseFci);
    // byte recordNumber = (byte) 0x01;
    //
    // PoSendableInSession[] poCommandsInsideSession = new PoSendableInSession[1];
    //
    // poCommandsInsideSession[0] = new ReadRecordsCmdBuild(PoRevision.REV2_4, (byte) 0x08,
    // recordNumber, false, (byte) 0x00);
    //
    // ApduResponse fciData = new ApduResponse(ByteBufferUtils.fromHex(
    // "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA153070A3C230C1410019000"),
    // null);
    //
    // // TODO ???
    // }

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

    private SeResponse processOpeningTestKif0xFFKey(byte keyIndex, byte sfi, byte recordNumber,
            List<ApduResponse> apduExpected, PoSendableInSession[] poCommandsInsideSession)
            throws IOReaderException, IllegalArgumentException {
        return poPlainSecrureSession.processOpening(null, keyIndex, sfi, recordNumber,
                poCommandsInsideSession != null ? Arrays.asList(poCommandsInsideSession) : null);

    }
}
