package biz.ixxi.calypso.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cna.sdk.calypso.commandset.ApduCommandBuilder;
import cna.sdk.calypso.commandset.CalypsoCommands;
import cna.sdk.calypso.commandset.RequestUtils;
import cna.sdk.calypso.commandset.ResponseUtils;
import cna.sdk.calypso.commandset.enumCmdReadRecords;
import cna.sdk.calypso.commandset.enumTagUtils;
import cna.sdk.calypso.commandset.dto.AID;
import cna.sdk.calypso.commandset.dto.CalypsoRequest;
import cna.sdk.calypso.commandset.dto.SamChallenge;
import cna.sdk.calypso.commandset.dto.EF;
import cna.sdk.calypso.commandset.dto.FCI;
import cna.sdk.calypso.commandset.dto.Record;
import cna.sdk.calypso.commandset.dto.SecureSession;
import cna.sdk.calypso.commandset.po.PoCommandBuilder;
import cna.sdk.calypso.commandset.po.builder.GetDataFciCmdBuild;
import cna.sdk.calypso.utils.LogUtils;
import cna.sdk.seproxy.APDURequest;
import cna.sdk.seproxy.ProxyReader;
import cna.sdk.seproxy.ReaderException;
import cna.sdk.seproxy.SERequest;
import cna.sdk.seproxy.SEResponse;

public class CommandSetServiceImplTest {
    Logger logger = LoggerFactory.getLogger(CommandSetServiceImplTest.class);

    @Test
    public void testGetDataFci() {

        ApduCommandBuilder session = new GetDataFciCmdBuild(PoCommandBuilder.defaultRevision );
        APDURequest request = session.getApduRequest();

        byte[] aidToRequest = null;
        boolean keepChannelOpen = true;
        List<APDURequest> listRequests = new ArrayList<>();
        listRequests.add(request);
        SERequest seRequest = new SERequest(aidToRequest, keepChannelOpen, listRequests);
        ProxyReader reader = Mockito.mock(ProxyReader.class);
        SEResponse response = null;
        try {
            response = reader.transmit(seRequest);
            logger.info(response.toString());
        } catch (ReaderException e) {

            e.printStackTrace();
        }
    }

    @Test
    public void testReadRecords() {

        // Select Application
        try {
            // get FCI
            CalypsoRequest calypsoRequest = new CalypsoRequest((byte) 0x00, CalypsoCommands.PO_GET_DATA_FCI, (byte) 0x00,
                    (byte) 0x4F, null, (byte) 0xff);
            APDURequest request = RequestUtils.constructAPDURequest(calypsoRequest);

            byte[] aidToRequest = null;
            boolean keepChannelOpen = true;
            List<APDURequest> listRequests = new ArrayList<>();
            listRequests.add(request);
            SERequest seRequest = new SERequest(aidToRequest, keepChannelOpen, listRequests);

            ProxyReader reader = Mockito.mock(ProxyReader.class);;
            SEResponse response = null;

            response = reader.transmit(seRequest);

            AID aid = ResponseUtils.toAID(response.getApduResponses().get(0).getbytes());
            logger.info("AID = " + aid.toString());

            // Select Application
            calypsoRequest = new CalypsoRequest((byte) 0x00, CalypsoCommands.PO_SELECT_APPLICATION, (byte) 0x04,
                    (byte) 0x00, aid.getValue(), (byte) 0x00);
            request = RequestUtils.constructAPDURequest(calypsoRequest);

            listRequests = new ArrayList<>();
            listRequests.add(request);
            seRequest = new SERequest(aidToRequest, keepChannelOpen, listRequests);

            response = reader.transmit(seRequest);

            FCI fci = ResponseUtils.toFCI(response.getApduResponses().get(0).getbytes());
            logger.info("Serial Number = "
                    + DatatypeConverter.printHexBinary(fci.getApplicationSN()));

            // Read Records of the file SFI = 04
            calypsoRequest = new CalypsoRequest((byte) 0x00, CalypsoCommands.PO_READ_RECORDS, (byte) 0x01, (byte) 0x24,
                    null);
            request = RequestUtils.constructAPDURequest(calypsoRequest);

            listRequests = new ArrayList<>();
            listRequests.add(request);
            seRequest = new SERequest(aidToRequest, keepChannelOpen, listRequests);

            response = reader.transmit(seRequest);

            List<Record> recData = ResponseUtils.toRecords(response.getApduResponses().get(0).getbytes(), enumCmdReadRecords.READ_RECORDS);
            logger.info("Recbyte = " + recData.toString());

        } catch (ReaderException e) {

            e.printStackTrace();

        }
    }

    @Test
    public void testGetSession() {

        try {

            // get FCI
            CalypsoRequest calypsoRequest = new CalypsoRequest((byte) 0x00, CalypsoCommands.PO_GET_DATA_FCI, (byte) 0x00,
                    (byte) 0x4F, null, (byte) 0xff);
            APDURequest request = RequestUtils.constructAPDURequest(calypsoRequest);

            byte[] aidToRequest = null;
            boolean keepChannelOpen = true;
            List<APDURequest> listRequests = new ArrayList<>();
            listRequests.add(request);
            SERequest seRequest = new SERequest(aidToRequest, keepChannelOpen, listRequests);

            ProxyReader reader = Mockito.mock(ProxyReader.class);
            SEResponse response = null;

            response = reader.transmit(seRequest);

            AID aid = ResponseUtils.toAID(response.getApduResponses().get(0).getbytes());
            logger.info("AID = " + aid.toString());

            // get List EF
            calypsoRequest = new CalypsoRequest((byte) 0x00, CalypsoCommands.PO_GET_DATA_FCI, (byte) 0x00, (byte) 0xC0,
                    (byte[]) null, (byte) 0x00);
            request = RequestUtils.constructAPDURequest(calypsoRequest);

            aidToRequest = null;
            keepChannelOpen = true;
            listRequests = new ArrayList<>();
            listRequests.add(request);
            seRequest = new SERequest(aidToRequest, keepChannelOpen, listRequests);

            response = reader.transmit(seRequest);
            List<EF> eflist = ResponseUtils.toEFList(response.getApduResponses().get(0).getbytes());
            for (EF ef : eflist) {
                logger.info("filetype = " + LogUtils.hexaToString(ef.getFileType()) + " lid = "
                        + LogUtils.hexaToString(ef.getLid()) + " sfi = " + ef.getSfi()
                        + " NumberRec = " + LogUtils.hexaToString(ef.getNumberRec()) + " RecSise = "
                        + LogUtils.hexaToString(ef.getRecSize()));
            }

            // Select Application
            calypsoRequest = new CalypsoRequest((byte) 0x00, CalypsoCommands.PO_SELECT_APPLICATION, (byte) 0x04,
                    (byte) 0x00, aid.getValue(), (byte) 0x00);
            request = RequestUtils.constructAPDURequest(calypsoRequest);

            listRequests = new ArrayList<>();
            listRequests.add(request);
            seRequest = new SERequest(aidToRequest, keepChannelOpen, listRequests);

            response = reader.transmit(seRequest);

            FCI fci = ResponseUtils.toFCI(response.getApduResponses().get(0).getbytes());
            logger.info("Serial Number = "
                    + DatatypeConverter.printHexBinary(fci.getApplicationSN()));

            // Select Diversifier
            calypsoRequest = new CalypsoRequest((byte) 0x80, CalypsoCommands.CSM_SELECT_DIVERSIFIER, (byte) 0x00,
                    (byte) 0x00, fci.getApplicationSN());
            request = RequestUtils.constructAPDURequest(calypsoRequest);

            listRequests = new ArrayList<>();
            listRequests.add(request);
            seRequest = new SERequest(aidToRequest, keepChannelOpen, listRequests);

            response = reader.transmit(seRequest);
            logger.info("Select Diversifier response = " + response.getApduResponses().get(0));

            // Get Challenge
            calypsoRequest = new CalypsoRequest((byte) 0x80, CalypsoCommands.CSM_GET_CHALLENGE, (byte) 0x00, (byte) 0x00,
                    null, (byte) 0x08);
            request = RequestUtils.constructAPDURequest(calypsoRequest);

            listRequests = new ArrayList<>();
            listRequests.add(request);
            seRequest = new SERequest(aidToRequest, keepChannelOpen, listRequests);

            response = reader.transmit(seRequest);
            SamChallenge challenge = ResponseUtils.toSamChallenge(response.getApduResponses().get(0).getbytes());
            logger.info("Random Number = "
                    + DatatypeConverter.printHexBinary(challenge.getRandomNumber()));

            // Open Secure Session
            calypsoRequest = new CalypsoRequest((byte) 0x94, CalypsoCommands.PO_OPEN_SESSION, (byte) 0x89, (byte) 0x10,
                    challenge.getRandomNumber());
            request = RequestUtils.constructAPDURequest(calypsoRequest);

            listRequests = new ArrayList<>();
            listRequests.add(request);
            seRequest = new SERequest(aidToRequest, keepChannelOpen, listRequests);

            response = reader.transmit(seRequest);

            SecureSession secureSession = ResponseUtils
                    .toSecureSessionRev2(response.getApduResponses().get(0).getbytes());
            logger.info("PO Session challenge = " + secureSession.getSessionChallenge() + " data = "
                    + LogUtils.hexaToString(secureSession.getOriginalData()) + " session ratified = "
                    + secureSession.isPreviousSessionRatified() + " manage secure session = "
                    + secureSession.isManageSecureSessionAuthorized() + " KIF = "
                    + LogUtils.hexaToString(secureSession.getKIF().getValue()) + " KVC = "
                    + LogUtils.hexaToString(secureSession.getKVC().getValue()) + " SecureSessionData = "
                    + LogUtils.hexaToString(secureSession.getSecureSessionData()));

            List<Byte> dataInDigestInit = new ArrayList<>();
            // dataInDigestInit[0] = (byte) 0x30;
            dataInDigestInit.add((secureSession.getKIF() == null) ? (byte) 0x30 : secureSession.getKIF().getValue());
            dataInDigestInit.add(secureSession.getKVC().getValue());
            dataInDigestInit.addAll(Arrays.asList(ArrayUtils.toObject(secureSession.getSecureSessionData())));

            // Digest Init
            calypsoRequest = new CalypsoRequest((byte) 0x94, CalypsoCommands.CSM_DIGEST_INIT, (byte) 0x00, (byte) 0x00,
                    ArrayUtils.toPrimitive(dataInDigestInit.toArray(new Byte[0])));
            request = RequestUtils.constructAPDURequest(calypsoRequest);

            listRequests = new ArrayList<>();
            listRequests.add(request);
            seRequest = new SERequest(aidToRequest, keepChannelOpen, listRequests);

            response = reader.transmit(seRequest);
            logger.info("Digest Init = " + response.getApduResponses().get(0));

        } catch (ReaderException e) {

            e.printStackTrace();
        }

    }
}
