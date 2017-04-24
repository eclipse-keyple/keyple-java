package biz.ixxi.calypso.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import biz.ixxi.calypso.service.CommandSetService;
import biz.ixxi.calypso.service.ReaderPoService;
import biz.ixxi.calypso.service.ReaderSamService;
import cna.sdk.calypso.commandset.ApduResponseParser;
import cna.sdk.calypso.commandset.ResponseUtils;
import cna.sdk.calypso.commandset.enumCmdReadRecords;
import cna.sdk.calypso.commandset.enumCmdWriteRecords;
import cna.sdk.calypso.commandset.enumSFI;
import cna.sdk.calypso.commandset.dto.AID;
import cna.sdk.calypso.commandset.dto.Record;
import cna.sdk.calypso.commandset.po.PoRevision;
import cna.sdk.calypso.commandset.po.SendableInSession;
import cna.sdk.calypso.commandset.po.builder.CloseSessionCmdBuild;
import cna.sdk.calypso.commandset.po.builder.OpenSessionCmdBuild;
import cna.sdk.calypso.commandset.po.builder.PoGetChallengeCmdBuild;
import cna.sdk.calypso.commandset.po.builder.ReadRecordsCmdBuild;
import cna.sdk.calypso.commandset.po.builder.UpdateRecordCmdBuild;
import cna.sdk.calypso.commandset.po.parser.CloseSessionRespPars;
import cna.sdk.calypso.commandset.po.parser.OpenSessionRespPars;
import cna.sdk.calypso.commandset.po.parser.ReadRecordsRespPars;
import cna.sdk.calypso.transaction.PoPlainSecureSession;
import cna.sdk.seproxy.APDURequest;
import cna.sdk.seproxy.APDUResponse;
import cna.sdk.seproxy.ReaderException;
import cna.sdk.seproxy.SERequest;
import cna.sdk.seproxy.SEResponse;

@Service("commandSetService")
public class CommandSetServiceImpl implements CommandSetService {

    static final Logger logger = LoggerFactory.getLogger(CommandSetServiceImpl.class);

    @Inject
    ReaderSamService readerSamService;

    @Inject
    ReaderPoService readerPoService;

    @Inject
    private Environment env;

    PoRevision revision;

    PoPlainSecureSession poPlainSecureSession;

    boolean sessionOpened = false;

    @PostConstruct
    private void init() {
        poPlainSecureSession = new PoPlainSecureSession(readerPoService.getReader(), readerSamService.getReader(),
                null);
        AID aid = new AID(null);
        if (StringUtils.isNotBlank(env.getProperty("calypso.force.aid"))) {
            aid = new AID(env.getProperty("calypso.force.aid").trim().getBytes());
        }
        try {
            poPlainSecureSession.processIdentification(aid.getValue(), null);
        } catch (ReaderException e) {
            logger.error("Error, identifing card", e);
        }
    }

    @Override
    public List<String> openSession(byte sfiToSelect) {
        logger.info("openning session from sfi " + sfiToSelect);

        List<String> list = new ArrayList<>();
        revision = getVersion();

        AID aid = new AID(null);
        if (StringUtils.isNotBlank(env.getProperty("calypso.force.aid"))) {
            aid = new AID(env.getProperty("calypso.force.aid").trim().getBytes());
        }
        try {
            SEResponse seResponse = poPlainSecureSession.processIdentification(aid.getValue(), null);

            // sam Challenge is the last response
            byte[] samChallenge = seResponse.getApduResponses().get(seResponse.getApduResponses().size() - 1)
                    .getbytes();
            byte keyIndex = 0x03;
            byte recordNumberToRead = 0x01;

            OpenSessionCmdBuild openCommand = new OpenSessionCmdBuild(revision, keyIndex, samChallenge, sfiToSelect,
                    recordNumberToRead);
            SEResponse seResponse2 = poPlainSecureSession.processOpening(openCommand, null);

            for (APDUResponse response : seResponse2.getApduResponses()) {
                ApduResponseParser apduResponseParser = new OpenSessionRespPars(response, revision);
                list.add(apduResponseParser.getStatusInformation());
            }
            sessionOpened = true;
        } catch (ReaderException e) {
            logger.error("Error, openning session", e);

        }
        return list;
    }

    @Override
    public String closeSession() throws ReaderException {
        logger.info("Close session");
        CloseSessionCmdBuild closeCommand = new CloseSessionCmdBuild(revision, true, null);
        PoGetChallengeCmdBuild poGetChallengeCmdBuild = new PoGetChallengeCmdBuild(revision);
        SEResponse seResponse = poPlainSecureSession.processClosing(null, closeCommand, poGetChallengeCmdBuild);
        ApduResponseParser apduResponseParser = new CloseSessionRespPars(seResponse.getApduResponses().get(0));
        sessionOpened = false;
        return apduResponseParser.getStatusInformation();
    }

    @Override
    public String cancelSession() throws ReaderException {

        CloseSessionCmdBuild closeCommand = new CloseSessionCmdBuild(revision, true, null);

        List<APDURequest> listRequests = new ArrayList<>();
        listRequests.add(closeCommand.getApduRequest());
        SERequest seRequest = new SERequest(null, true, listRequests);
        SEResponse seResponse = readerPoService.transmit(seRequest);
        ApduResponseParser apduCommandBuilder = new CloseSessionRespPars(seResponse.getApduResponses().get(0));
        sessionOpened = false;
        return apduCommandBuilder.getStatusInformation();

    }

    @Override
    public PoRevision getVersion() {
        return poPlainSecureSession.getRevision();

    }

    // @Override
    // public void digestUpdate(byte[] bytes) {
    // logger.info("\n\n ******** DIGEST UPDATE ********\n");
    //
    // ApduCommandBuilder apduCommandBuilder = new DigestUpdateCmdBuild(null,
    // false, bytes);
    // APDURequest request = apduCommandBuilder.getRequest();
    // byte[] aidToRequest = null;
    // boolean keepChannelOpen = true;
    // List<APDURequest> listRequests = new ArrayList<>();
    // listRequests.add(request);
    // SERequest seRequest = new SERequest(aidToRequest, keepChannelOpen,
    // listRequests);
    // readerSamService.transmit(seRequest);
    // }

    @Override
    public List<Record> readRecord(PoRevision revision, byte recordNumber, enumSFI sfi, enumCmdReadRecords cmd)
            throws ReaderException {
        logger.info("\n\n                                                 ******** READ RECORD ********\n");
        logger.info("reading sfi " + sfi + " from record " + recordNumber + " with command " + cmd.getName());

        SendableInSession[] poCommandsInsideSession = new SendableInSession[1];

        poCommandsInsideSession[0] = new ReadRecordsCmdBuild(revision, recordNumber, sfi.getSfi(), (byte) 0x00);
        SEResponse response = poPlainSecureSession.processProceeding(poCommandsInsideSession);

        List<Record> record = ResponseUtils.toRecords(response.getApduResponses().get(0).getbytes(), cmd);

        ReadRecordsRespPars readRecodRespPars = new ReadRecordsRespPars(response.getApduResponses().get(0), cmd);
        String msg = readRecodRespPars.getStatusInformation();
        logger.info("message d'information " + msg + " readRecords");

        return record;
    }

    @Override
    public void updateRecord(String stringRecordToWrite, byte recordNumber, enumSFI sfi, enumCmdWriteRecords cmd)
            throws ReaderException {
        logger.info("\n\n                                                 ******** UPDATE RECORD ********\n");
        logger.info("writing " + stringRecordToWrite + " on sfi " + sfi + " from record " + recordNumber
                + " with command " + cmd.getName());

        SendableInSession[] poCommandsInsideSession = new SendableInSession[1];

        poCommandsInsideSession[0] = new UpdateRecordCmdBuild(revision, recordNumber, sfi.getSfi(),
                stringRecordToWrite.getBytes());
        poPlainSecureSession.processProceeding(poCommandsInsideSession);

    }

    @Override
    public String isCardPresent() {
        return BooleanUtils.toStringTrueFalse(readerPoService.getReader().isSEPresent());
    }

    public PoPlainSecureSession getPoPlainSecureSession() {
        return poPlainSecureSession;
    }

}
