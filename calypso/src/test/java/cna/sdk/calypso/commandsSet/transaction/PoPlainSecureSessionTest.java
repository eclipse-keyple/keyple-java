package cna.sdk.calypso.commandsSet.transaction;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cna.sdk.calypso.commandset.ApduCommandBuilder;
import cna.sdk.calypso.commandset.enumCmdReadRecords;
import cna.sdk.calypso.commandset.enumCmdWriteRecords;
import cna.sdk.calypso.commandset.enumSFI;
import cna.sdk.calypso.commandset.po.PoCommandBuilder;
import cna.sdk.calypso.commandset.po.PoRevision;
import cna.sdk.calypso.commandset.po.SendableInSession;
import cna.sdk.calypso.commandset.po.builder.CloseSessionCmdBuild;
import cna.sdk.calypso.commandset.po.builder.GetAIDCmdBuild;
import cna.sdk.calypso.commandset.po.builder.OpenSessionCmdBuild;
import cna.sdk.calypso.commandset.po.builder.PoGetChallengeCmdBuild;
import cna.sdk.calypso.commandset.po.builder.ReadRecordsCmdBuild;
import cna.sdk.calypso.commandset.po.builder.UpdateRecordCmdBuild;
import cna.sdk.calypso.transaction.PoPlainSecureSession;
import cna.sdk.plugin.smartcardio.SmartCardIOPlugin;
import cna.sdk.seproxy.APDURequest;
import cna.sdk.seproxy.APDUResponse;
import cna.sdk.seproxy.ProxyReader;
import cna.sdk.seproxy.ReaderException;
import cna.sdk.seproxy.ReadersPlugin;
import cna.sdk.seproxy.SERequest;
import cna.sdk.seproxy.SEResponse;

public class PoPlainSecureSessionTest {

    static final Logger logger = LoggerFactory.getLogger(PoPlainSecureSessionTest.class);

    ProxyReader poReader = Mockito.mock(ProxyReader.class);

    ProxyReader csmSessionReader = Mockito.mock(ProxyReader.class);

    byte[] samchallenge;

    public void init() {
        ReadersPlugin plugin = new SmartCardIOPlugin();

        for (ProxyReader el : plugin.getReaders()) {
            if ("CalypsoReader".equals(el.getName())) {
                logger.info("poReader");
                poReader = el;
            } else {
                logger.info("csmReader");
                csmSessionReader = el;
            }
        }
    }

    // @Test
    // public void processIdentificationTest() throws ReaderException {
    // byte[] challenge = { 0x01, 0x02, 0x03, 0x04 };
    // byte[] dataFci = { 0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52,
    // 0x2E, 0x49, 0x43, 0x41, (byte) 0xA5,
    // 0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00,
    // 0x27, 0x4A, (byte) 0x9A,
    // (byte) 0xB8, 0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10, 0x01 };
    // byte[] statusCode = { (byte) 0x90, 0x00 };
    // byte[] diversifier = { (byte) 0xAA, (byte) 0xBB };
    //
    // List<APDUResponse> apdu = new ArrayList<>();
    // List<APDUResponse> apduCSM = new ArrayList<>();
    // List<APDUResponse> apduCSM2 = new ArrayList<>();
    //
    // apdu.add(new APDUResponse(dataFci, true, statusCode));
    // apduCSM2.add(new APDUResponse(challenge, true, statusCode));
    // apduCSM.add(new APDUResponse(diversifier, true, statusCode));
    //
    // SEResponse seResponsePO = new SEResponse(null, apdu.get(0), apdu);
    // SEResponse seResponseCSM1 = new SEResponse(null, null, apduCSM);
    // SEResponse seResponseCSM2 = new SEResponse(null, null, apduCSM2);
    //
    // PoPlainSecureSession poPlainSecureSession = new
    // PoPlainSecureSession(poReader, csmSessionReader, null);
    //
    // Mockito.when(poReader.transmit(any(SERequest.class))).thenReturn(seResponsePO);
    // Mockito.when(csmSessionReader.transmit(any(SERequest.class))).thenReturn(seResponseCSM1,
    // seResponseCSM2);
    //
    // logger.info("+++++++++++++++++ IDENTIFICATION test ++++++++++++++");
    // SEResponse seResponse1 =
    // poPlainSecureSession.processIdentification(null,);
    //
    // assertEquals(3, seResponse1.getApduResponses().size());
    // assertArrayEquals(
    // new byte[] { 0x6F, 0x22, (byte) 0x84, 0x08, 0x33, 0x4D, 0x54, 0x52, 0x2E,
    // 0x49, 0x43, 0x41, (byte) 0xA5,
    // 0x16, (byte) 0xBF, 0x0C, 0x13, (byte) 0xC7, 0x08, 0x00, 0x00, 0x00, 0x00,
    // 0x27, 0x4A,
    // (byte) 0x9A, (byte) 0xB8, 0x53, 0x07, 0x0A, 0x3C, 0x11, 0x32, 0x14, 0x10,
    // 0x01 },
    // seResponse1.getApduResponses().get(0).getbytes());
    // assertArrayEquals(new byte[] { (byte) 0xAA, (byte) 0xBB },
    // seResponse1.getApduResponses().get(1).getbytes());
    // assertArrayEquals(new byte[] { 0x01, 0x02, 0x03, 0x04 },
    // seResponse1.getApduResponses().get(2).getbytes());
    //
    // }

    @Test
    public void processOpeningTest_no_insideCommand() throws ReaderException {
        byte[] challenge = { 0x01, 0x02, 0x03, 0x04 };
        byte[] dataOpen = { 0x7E, (byte) 0x8F, 0x05, 0x75, 0x01A, 0x00, 0x00, 0x00, 0x00, 0x00 };
        byte[] statusCode = { (byte) 0x90, 0x00 };

        List<APDUResponse> apdu = new ArrayList<>();
        List<APDUResponse> apduCSM = new ArrayList<>();
        List<APDUResponse> apduCSM2 = new ArrayList<>();

        apdu.add(new APDUResponse(dataOpen, true, statusCode));
        apduCSM.add(new APDUResponse(null, true, statusCode));
        apduCSM2.add(new APDUResponse(challenge, true, statusCode));

        SEResponse seResponsePO = new SEResponse(true, null, apdu);
        SEResponse seResponseCSM1 = new SEResponse(true, null, apduCSM);
        new SEResponse(true, null, apduCSM2);

        byte key = 0x03;
        byte record = 0x01;
        byte sfi = 0x08;

        OpenSessionCmdBuild openCommand = new OpenSessionCmdBuild(PoRevision.REV2_4, key, samchallenge, sfi, record);
        PoPlainSecureSession poPlainSecureSession = new PoPlainSecureSession(poReader, csmSessionReader, null);
        SendableInSession[] poCommandsInsideSession = new SendableInSession[1];

        Mockito.when(poReader.transmit(any(SERequest.class))).thenReturn(seResponsePO);
        Mockito.when(csmSessionReader.transmit(any(SERequest.class))).thenReturn(seResponseCSM1);
        poCommandsInsideSession[0] = new ReadRecordsCmdBuild(PoRevision.REV2_4, record, enumSFI.EVENT_LOG_FILE.getSfi(),
                (byte) 0x00);
        logger.info("+++++++++++++++++    OPENING test no command  ++++++++++++++");

        SEResponse seResponse2 = poPlainSecureSession.processOpening(openCommand, null);

        assertEquals(2, seResponse2.getApduResponses().size());
        assertArrayEquals(new byte[] { 0x7E, (byte) 0x8F, 0x05, 0x75, 0x01A, 0x00, 0x00, 0x00, 0x00, 0x00 },
                seResponse2.getApduResponses().get(0).getbytes());
        assertArrayEquals(new byte[] { (byte) 0x90, 0x00 }, seResponse2.getApduResponses().get(1).getStatusCode());
    }

    @Test
    public void processOpeningTest_with_insideCommand() throws ReaderException {
        byte[] challenge = { 0x01, 0x02, 0x03, 0x04 };
        byte[] dataOpen = { 0x7E, (byte) 0x8F, 0x05, 0x75, 0x01, 0x00, 0x00, 0x00 };
        byte[] statusCode = { (byte) 0x90, 0x00 };
        byte[] dataRead = { 0x01, (byte) 0x02, 0x03, 0x75, 0x05, (byte) 0x80, (byte) 0x84, 0x14 };

        List<APDUResponse> apduOpen = new ArrayList<>();
        List<APDUResponse> apduRead = new ArrayList<>();
        List<APDUResponse> apduCSM = new ArrayList<>();
        List<APDUResponse> apduCSM2 = new ArrayList<>();

        apduOpen.add(new APDUResponse(dataOpen, true, statusCode));
        apduRead.add(new APDUResponse(dataRead, true, statusCode));
        apduCSM.add(new APDUResponse(null, true, statusCode));
        apduCSM2.add(new APDUResponse(challenge, true, statusCode));

        SEResponse seResponsePO = new SEResponse(true, null, apduOpen);
        SEResponse seResponsePoRead = new SEResponse(true, null, apduRead);
        SEResponse seResponseCSM1 = new SEResponse(true, null, apduCSM);
        SEResponse seResponseCsmUpdate = new SEResponse(true, null, apduCSM2);

        byte key = 0x03;
        byte record = 0x01;
        byte sfi = 0x08;

        OpenSessionCmdBuild openCommand = new OpenSessionCmdBuild(PoRevision.REV2_4, key, samchallenge, sfi, record);
        PoPlainSecureSession poPlainSecureSession = new PoPlainSecureSession(poReader, csmSessionReader, null);
        SendableInSession[] poCommandsInsideSession = new SendableInSession[1];

        Mockito.when(poReader.transmit(any(SERequest.class))).thenReturn(seResponsePO, seResponsePoRead);
        Mockito.when(csmSessionReader.transmit(any(SERequest.class))).thenReturn(seResponseCSM1, seResponseCsmUpdate,
                seResponseCsmUpdate);
        poCommandsInsideSession[0] = new ReadRecordsCmdBuild(PoRevision.REV2_4, record, enumSFI.EVENT_LOG_FILE.getSfi(),
                (byte) 0x00);

        logger.info("+++++++++++++++++    OPENING  test with command ++++++++++++++");
        SEResponse seResponse = poPlainSecureSession.processOpening(openCommand, poCommandsInsideSession);

        assertEquals(5, seResponse.getApduResponses().size());
        assertArrayEquals(new byte[] { 0x7E, (byte) 0x8F, 0x05, 0x75, 0x01, 0x00, 0x00, 0x00 },
                seResponse.getApduResponses().get(0).getbytes());
        assertArrayEquals(new byte[] { (byte) 0x90, 0x00 }, seResponse.getApduResponses().get(1).getStatusCode());
        assertArrayEquals(new byte[] { 0x01, (byte) 0x02, 0x03, 0x75, 0x05, (byte) 0x80, (byte) 0x84, 0x14 },
                seResponse.getApduResponses().get(2).getbytes());
        assertArrayEquals(new byte[] { (byte) 0x90, 0x00 }, seResponse.getApduResponses().get(3).getStatusCode());
        assertArrayEquals(new byte[] { (byte) 0x90, 0x00 }, seResponse.getApduResponses().get(4).getStatusCode());
    }

    @Test
    public void processProceedingTest() throws ReaderException {

        byte[] statusCode = { (byte) 0x90, 0x00 };
        byte[] dataRead = { 0x01, (byte) 0x02, 0x03, 0x75, 0x05, (byte) 0x80, (byte) 0x84, 0x14 };

        List<APDUResponse> apduOk = new ArrayList<>();
        List<APDUResponse> apduRead = new ArrayList<>();
        List<APDUResponse> apduCSM = new ArrayList<>();

        apduOk.add(new APDUResponse(null, true, statusCode));
        apduRead.add(new APDUResponse(dataRead, true, statusCode));
        apduCSM.add(new APDUResponse(null, true, statusCode));

        SEResponse seResponsePO = new SEResponse(true, null, apduOk);
        SEResponse seResponsePoRead = new SEResponse(true, null, apduRead);
        SEResponse seResponseCSM1 = new SEResponse(true, null, apduCSM);

        byte record = 0x01;
        byte[] newData = { 0x01, 0x01, 0x01, 0x01 };

        PoPlainSecureSession poPlainSecureSession = new PoPlainSecureSession(poReader, csmSessionReader, null);
        SendableInSession[] poCommandsInsideSession = new SendableInSession[2];

        Mockito.when(poReader.transmit(any(SERequest.class))).thenReturn(seResponsePO, seResponsePoRead);
        Mockito.when(csmSessionReader.transmit(any(SERequest.class))).thenReturn(seResponseCSM1);

        poCommandsInsideSession[1] = new ReadRecordsCmdBuild(PoRevision.REV2_4, record, enumSFI.EVENT_LOG_FILE.getSfi(),
                (byte) 0x00);
        poCommandsInsideSession[0] = new UpdateRecordCmdBuild(PoRevision.REV2_4, record,
                enumSFI.EVENT_LOG_FILE.getSfi(), newData);

        logger.info("+++++++++++++++++    PROCEEDING   ++++++++++++++");

        SEResponse seResponse = poPlainSecureSession.processProceeding(poCommandsInsideSession);

        assertEquals(6, seResponse.getApduResponses().size());
        assertArrayEquals(new byte[] { (byte) 0x90, 0x00 }, seResponse.getApduResponses().get(0).getStatusCode());
        assertArrayEquals(new byte[] { (byte) 0x90, 0x00 }, seResponse.getApduResponses().get(1).getStatusCode());
        assertArrayEquals(new byte[] { (byte) 0x90, 0x00 }, seResponse.getApduResponses().get(2).getStatusCode());
        assertArrayEquals(new byte[] { 0x01, (byte) 0x02, 0x03, 0x75, 0x05, (byte) 0x80, (byte) 0x84, 0x14 },
                seResponse.getApduResponses().get(3).getbytes());
        assertArrayEquals(new byte[] { (byte) 0x90, 0x00 }, seResponse.getApduResponses().get(4).getStatusCode());
        assertArrayEquals(new byte[] { (byte) 0x90, 0x00 }, seResponse.getApduResponses().get(5).getStatusCode());

    }

    @Test
    public void processClosingTest() throws ReaderException {

        byte[] statusCode = { (byte) 0x90, 0x00 };
        byte[] poChallenge = { (byte) 0xA1, (byte) 0xB2, (byte) 0xC3, (byte) 0x85 };
        byte[] samChallenge = { 0x01, (byte) 0x02, 0x03, 0x75 };


        ApduCommandBuilder getAidRequest = new GetAIDCmdBuild(PoCommandBuilder.defaultRevision);
        List<APDURequest> listRequests = new ArrayList<>();
        listRequests.add(getAidRequest.getApduRequest());
        SERequest seRequest = new SERequest(null, true, listRequests);

        List<APDUResponse> apduPoChall = new ArrayList<>();
        List<APDUResponse> apduCsmChall = new ArrayList<>();
        List<APDUResponse> apduCsmOk = new ArrayList<>();

        apduPoChall.add(new APDUResponse(poChallenge, true, statusCode));
        apduCsmOk.add(new APDUResponse(null, true, statusCode));
        apduCsmChall.add(new APDUResponse(samChallenge, true, statusCode));

        SEResponse seResponsePO = new SEResponse(true, null, apduPoChall);
        SEResponse seResponseCSM1 = new SEResponse(true, null, apduCsmChall);
        SEResponse seResponseCSM2 = new SEResponse(true, null, apduCsmOk);

        PoPlainSecureSession poPlainSecureSession = new PoPlainSecureSession(poReader, csmSessionReader, null);
        Mockito.when(poReader.transmit(seRequest)).thenReturn(seResponsePO);
        poPlainSecureSession.processIdentification(null, null);
        Mockito.when(poReader.transmit(any(SERequest.class))).thenReturn(seResponsePO);
        Mockito.when(csmSessionReader.transmit(any(SERequest.class))).thenReturn(seResponseCSM1, seResponseCSM2);

        CloseSessionCmdBuild closeCommand = new CloseSessionCmdBuild(PoRevision.REV2_4, true, samChallenge);
        PoGetChallengeCmdBuild poGetChallengeCmdBuild = new PoGetChallengeCmdBuild(PoRevision.REV2_4);

        logger.info("+++++++++++++++++    CLOSING   ++++++++++++++");
        SEResponse mapSeResponse = poPlainSecureSession.processClosing(null, closeCommand, poGetChallengeCmdBuild);

    }
}
