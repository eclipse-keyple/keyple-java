package biz.ixxi.calypso.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import biz.ixxi.calypso.service.EFService;
import biz.ixxi.calypso.service.ReaderPoService;
import cna.sdk.calypso.commandset.ResponseUtils;
import cna.sdk.calypso.commandset.enumCmdReadRecords;
import cna.sdk.calypso.commandset.enumSFI;
import cna.sdk.calypso.commandset.enumTagUtils;
import cna.sdk.calypso.commandset.dto.EF;
import cna.sdk.calypso.commandset.dto.Record;
import cna.sdk.calypso.commandset.po.PoRevision;
import cna.sdk.calypso.commandset.po.SendableInSession;
import cna.sdk.calypso.commandset.po.builder.GetDataFciCmdBuild;
import cna.sdk.calypso.commandset.po.builder.GetListEfCmdBuild;
import cna.sdk.calypso.commandset.po.builder.ReadRecordsCmdBuild;
import cna.sdk.calypso.commandset.po.parser.ReadRecordsRespPars;
import cna.sdk.calypso.utils.LogUtils;
import cna.sdk.seproxy.APDUResponse;
import cna.sdk.seproxy.ReaderException;
import cna.sdk.seproxy.SEResponse;

@Service("efService")
public class EFServiceImpl implements EFService {

    static final Logger logger = LoggerFactory.getLogger(EFServiceImpl.class);

    @Inject
    ReaderPoService readerPoService;

    @Inject
    CommandSetServiceImpl commandSet;

    @Override
    public List<EF> listEF() {
        logger.info("\n\n                                          ******** LIST EF ********\n");

        List<EF> listEF = new ArrayList<>();
        // get List EF
        // CalypsoRequest calypsoRequest = new CalypsoRequest((byte) 0x00,
        // CalypsoCommands.PO_GetData, (byte) 0x00,
        // (byte) 0xC0, null, (byte) 0x00);
        // APDURequest request =
        // RequestUtils.constructAPDURequest(calypsoRequest);
        SendableInSession apduCommandBuilder = new GetListEfCmdBuild(commandSet.getVersion());
        APDUResponse response;
        if (!commandSet.sessionOpened) {
            response = readerPoService.transmit(apduCommandBuilder.getAPDURequest());
        } else {
            try {
                response = commandSet.getPoPlainSecureSession()
                        .processProceeding(new SendableInSession[] { apduCommandBuilder }).getApduResponses().get(0);
            } catch (ReaderException e) {
                return null;
            }
        }
        List<EF> eflist = ResponseUtils.toEFList(response.getbytes());
        for (EF ef : eflist) {
            logger.info("filetype = " + LogUtils.hexaToString(ef.getFileType()) + " filetype name = "
                    + ef.getFileTypeName() + " lid = " + LogUtils.hexaToString(ef.getLid()) + " sfi = "
                    + ef.getSfi() + " NumberRec = " + LogUtils.hexaToString(ef.getNumberRec())
                    + " RecSise = " + LogUtils.hexaToString(ef.getRecSize()));
        }

        listEF.addAll(eflist);

        return listEF;

    }

    @Override
    public List<Record> readRecords(EF file) {

        PoRevision revision = commandSet.getVersion();
        enumCmdReadRecords cmd = enumCmdReadRecords.READ_ONE_RECORD_FROM_EF_USING_SFI;
        byte numRec = 0x01;

        SendableInSession session = new ReadRecordsCmdBuild(revision, numRec, file.getSfi().getSfi(), (byte) 0x00);
        try {
            APDUResponse response = commandSet.getPoPlainSecureSession()
                    .processProceeding(new SendableInSession[] { session }).getApduResponses().get(0);
            ReadRecordsRespPars parser = new ReadRecordsRespPars(response, cmd);

            return parser.getRecords();
        } catch (ReaderException e) {
            return null;
        }
    }

    @Override
    public List<Record> readAllRecords(byte sfi) {

        PoRevision revision = commandSet.getVersion();
        enumCmdReadRecords cmd = enumCmdReadRecords.READ_RECORDS_FROM_EF_USING_SFI;
        byte numRec = 0x01;

        SendableInSession session = new ReadRecordsCmdBuild(revision, numRec, enumSFI.getSfiByCode(sfi).getSfi(), (byte) 0x00);
        try {
            SEResponse response = commandSet.getPoPlainSecureSession()
                    .processProceeding(new SendableInSession[] { session });
            ReadRecordsRespPars parser = new ReadRecordsRespPars(response.getApduResponses().get(0), cmd);

            return parser.getRecords();
        } catch (ReaderException e) {
            return null;
        }
    }

    @Override
    public String getData(enumTagUtils dataType) {
        SendableInSession session = new GetDataFciCmdBuild(commandSet.getVersion());
        try {
            APDUResponse response = commandSet.getPoPlainSecureSession()
                    .processProceeding(new SendableInSession[] { session }).getApduResponses().get(0);

            return DatatypeConverter.printHexBinary(response.getbytes());
        } catch (ReaderException e) {
            return null;
        }
    }

}
