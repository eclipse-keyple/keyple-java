package cna.sdk.calypso.commandset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cna.sdk.calypso.commandset.dto.AID;
import cna.sdk.calypso.commandset.dto.EF;
import cna.sdk.calypso.commandset.dto.FCI;
import cna.sdk.calypso.commandset.dto.KIF;
import cna.sdk.calypso.commandset.dto.KVC;
import cna.sdk.calypso.commandset.dto.POChallenge;
import cna.sdk.calypso.commandset.dto.POHalfSessionSignature;
import cna.sdk.calypso.commandset.dto.PostponedData;
import cna.sdk.calypso.commandset.dto.Ratification;
import cna.sdk.calypso.commandset.dto.Record;
import cna.sdk.calypso.commandset.dto.SamChallenge;
import cna.sdk.calypso.commandset.dto.SamHalfSessionSignature;
import cna.sdk.calypso.commandset.dto.SecureSession;
import cna.sdk.calypso.commandset.dto.StartupInformation;
import cna.sdk.calypso.commandset.dto.TransactionCounter;
import cna.sdk.calypso.commandset.po.PoRevision;

/**
 * This class eases the parse of APDUResponses into objects.
 *
 * @author Ixxi
 */
public class ResponseUtils {

    /** The Constant logger. */
    static final Logger logger = LoggerFactory.getLogger(ResponseUtils.class);

    /**
     * Instantiates a new ResponseUtils.
     */
    private ResponseUtils() {

    }

    /**
     * Method to get the AID from the response.
     *
     * @param apduResponse
     *            the apdu response
     * @return the AID of current DF
     */
    public static AID toAID(byte[] apduResponse) {
        AID aid = null;
        byte firstResponseApdubyte = apduResponse[0];

        if (enumTagUtils.AID_OF_CURRENT_DF.getTagbyte2() == firstResponseApdubyte) {
            int le = apduResponse[1];
            aid = new AID(ArrayUtils.subarray(apduResponse, 2, 2 + le));
        }

        return aid;
    }

    /**
     * Method to get the list of EF contained in the Calypso application from
     * the response.
     *
     * @param apduResponse
     *            the apdu response
     * @return list of EF file
     */
    public static List<EF> toEFList(byte[] apduResponse) {
        List<EF> listOfEffile = new ArrayList<>();
        byte firstResponseApdubyte = apduResponse[0];

        if (enumTagUtils.LIST_OF_EF.getTagbyte2() == firstResponseApdubyte) {

            int le = apduResponse[1];

            int numberOfEfFiles = le / 8;
            int j = 0;

            for (int i = 0; i < numberOfEfFiles; i++) {

                byte[] lid = new byte[2];
                lid[0] = apduResponse[4 + j];
                lid[1] = apduResponse[5 + j];
                byte sfi = apduResponse[6 + j];
                byte fileType = apduResponse[7 + j];
                byte recSize = apduResponse[8 + j];
                byte numberRec = apduResponse[9 + j];
                EF ef = new EF(lid, enumSFI.getSfiByCode(sfi), fileType, recSize, numberRec);

                listOfEffile.add(ef);
                j = j + 8;
            }
        }

        return listOfEffile;
    }

    /**
     * Method to get the FCI from the response.
     *
     * @param apduResponse
     *            the apdu response
     * @return the FCI template
     */
    public static FCI toFCI(byte[] apduResponse) {
        StartupInformation startupInformation = null;
        byte firstResponseApdubyte = apduResponse[0];
        AID aid = null;
        byte[] fciProprietaryTemplate = null;
        byte[] fciIssuerDiscretionaryData = null;
        byte[] applicationSN = null;
        byte[] discretionaryData;

        if (enumTagUtils.FCI_TEMPLATE.getTagbyte2() == firstResponseApdubyte) {
            int aidLength = apduResponse[3];
            int fciTemplateLength = apduResponse[5 + aidLength];
            int fixedPartOfFciTemplate = fciTemplateLength - 22;
            int firstbyteAid = 6 + aidLength + fixedPartOfFciTemplate;
            int fciIssuerDiscretionaryDataLength = apduResponse[8 + aidLength + fixedPartOfFciTemplate];
            int firstbyteFciIssuerDiscretionaryData = 9 + aidLength + fixedPartOfFciTemplate;
            int applicationSNLength = apduResponse[10 + aidLength + fixedPartOfFciTemplate];
            int firstbyteApplicationSN = 11 + aidLength + fixedPartOfFciTemplate;
            int discretionaryDataLength = apduResponse[20 + aidLength + fixedPartOfFciTemplate];
            int firstbyteDiscretionaryData = 21 + aidLength + fixedPartOfFciTemplate;

            if (enumTagUtils.DF_NAME.getTagbyte2() == apduResponse[2]) {
                aid = new AID(ArrayUtils.subarray(apduResponse, 4, 4 + aidLength));
            }

            if (enumTagUtils.FCI_PROPRIETARY_TEMPLATE.getTagbyte2() == apduResponse[4 + aidLength]) {
                fciProprietaryTemplate = ArrayUtils.subarray(apduResponse, firstbyteAid,
                        firstbyteAid + fciTemplateLength);
            }

            if (enumTagUtils.FCI_ISSUER_DISCRETIONARY_DATA
                    .getTagbyte1() == apduResponse[6 + aidLength + fixedPartOfFciTemplate]
                    && (enumTagUtils.FCI_ISSUER_DISCRETIONARY_DATA
                            .getTagbyte2() == apduResponse[7 + aidLength + fixedPartOfFciTemplate])) {
                fciIssuerDiscretionaryData = ArrayUtils.subarray(apduResponse, firstbyteFciIssuerDiscretionaryData,
                        firstbyteFciIssuerDiscretionaryData + fciIssuerDiscretionaryDataLength);
            }

            if (enumTagUtils.APPLICATION_SERIAL_NUMBER
                    .getTagbyte2() == apduResponse[9 + aidLength + fixedPartOfFciTemplate]) {
                applicationSN = ArrayUtils.subarray(apduResponse, firstbyteApplicationSN,
                        firstbyteApplicationSN + applicationSNLength);
            }

            if (enumTagUtils.DISCREATIONARY_DATA
                    .getTagbyte2() == apduResponse[19 + aidLength + fixedPartOfFciTemplate]) {
                discretionaryData = ArrayUtils.subarray(apduResponse, firstbyteDiscretionaryData,
                        firstbyteDiscretionaryData + discretionaryDataLength);
                startupInformation = new StartupInformation(discretionaryData[0], discretionaryData[1],
                        discretionaryData[2], discretionaryData[3], discretionaryData[4], discretionaryData[5],
                        discretionaryData[6]);
            }
        }

        return new FCI(aid, fciProprietaryTemplate, fciIssuerDiscretionaryData, applicationSN, startupInformation);
    }

    /**
     * Method to get the challenge from the CSM response .
     *
     * @param apduResponse
     *            the apdu response
     * @return the SamChallenge
     */
    public static SamChallenge toSamChallenge(byte[] apduResponse) {
        return new SamChallenge(ArrayUtils.subarray(apduResponse, 0, 3), ArrayUtils.subarray(apduResponse, 3, 4));
    }

    /**
     * Method to get a Secure Session from the response in revision 3 mode.
     *
     * @param apduResponse
     *            the apdu response
     * @return a SecureSession
     */
    public static SecureSession toSecureSession(byte[] apduResponse) {
        SecureSession secureSession;
        boolean previousSessionRatified = true;
        boolean manageSecureSessionAuthorized = false;

        KIF kif = toKIF(apduResponse);
        KVC kvc = toKVC(apduResponse);
        int dataLength = apduResponse[7];
        byte[] data = ArrayUtils.subarray(apduResponse, 0, 8 + dataLength);

        secureSession = new SecureSession(
                new POChallenge(ArrayUtils.subarray(apduResponse, 0, 3), ArrayUtils.subarray(apduResponse, 3, 4)),
                previousSessionRatified, manageSecureSessionAuthorized, kif, kvc, data, apduResponse);
        logger.info("secure 3");
        return secureSession;
    }

    /**
     * Method to get a Secure Session from the response in revision 2 mode.
     *
     * @param apduResponse
     *            the apdu response
     * @return a SecureSession
     */
    public static SecureSession toSecureSessionRev2(byte[] apduResponse) {
        SecureSession secureSession;
        logger.info("secure 2.4");
        boolean previousSessionRatified = true;
        boolean manageSecureSessionAuthorized = false;

        KVC kvc = toKVCRev2(apduResponse);
        byte[] data;
        if (apduResponse.length > 5) {
            int dataLength = apduResponse[7];
            data = ArrayUtils.subarray(apduResponse, 0, 8 + dataLength);
            if ((byte) 0xFF == apduResponse[5]) {
                apduResponse[5] = 0x30;
            }
        } else {
            data = ArrayUtils.subarray(apduResponse, 0, 5);
        }
        secureSession = new SecureSession(
                new POChallenge(ArrayUtils.subarray(apduResponse, 1, 4), ArrayUtils.subarray(apduResponse, 4, 5)),
                previousSessionRatified, manageSecureSessionAuthorized, kvc, data, apduResponse);

        return secureSession;
    }

    /**
     * Method to get the SAM half session signature (the first half part of the
     * signature necessary to close the session properly) from the response.
     *
     * @param apduResponse
     *            the apdu response
     * @return a SamHalfSessionSignature
     */
    public static SamHalfSessionSignature toSamHalfSessionSignature(byte[] apduResponse) {
        return new SamHalfSessionSignature(apduResponse);
    }

    /**
     * Method to get the Records from the response.
     *
     * @param apduResponse
     *            the apdu response
     * @param cmd
     *            the command type
     * @return a Maps of Records
     */
    public static List<Record> toRecords(byte[] apduResponse, enumCmdReadRecords cmd) {
        List<enumCmdReadRecords> multipleRecords = Arrays.asList(new enumCmdReadRecords[] {
                enumCmdReadRecords.READ_RECORDS, enumCmdReadRecords.READ_RECORDS_FROM_EF_USING_SFI });
        List<enumCmdReadRecords> simpleRecords = Arrays.asList(new enumCmdReadRecords[] {
                enumCmdReadRecords.READ_ONE_RECORD, enumCmdReadRecords.READ_ONE_RECORD_FROM_EF_USING_SFI });
        List<Record> records = new ArrayList<>();
        if (simpleRecords.contains(cmd)) {
            records.add(new Record(apduResponse, 0));
        }
        if (multipleRecords.contains(cmd)) {
            int i = 0;
            while (i < apduResponse.length) {
                records.add(new Record(ArrayUtils.subarray(apduResponse, i + 2, i + 2 + apduResponse[i + 1]),
                        apduResponse[i]));
                // add data length to iterator
                i += apduResponse[i + 1];
                // add byte of data length to iterator
                i++;
                // add byte of num record to iterator
                i++;
            }
        }
        return records;
    }

    /**
     * Method to get the KVC from the response in revision 3 mode.
     *
     * @param apduResponse
     *            the apdu response
     * @return a KVC
     */
    public static KVC toKVC(byte[] apduResponse) {
        KVC kvcValue = null;
        if (apduResponse.length > 4)
            kvcValue = new KVC(apduResponse[6]);

        return kvcValue;
    }

    /**
     * Method to get the KVC from the response in revision 2 mode.
     *
     * @param apduResponse
     *            the apdu response
     * @return a KVC
     */
    public static KVC toKVCRev2(byte[] apduResponse) {
        KVC kvcValue = null;
        if (apduResponse.length > 4)
            kvcValue = new KVC(apduResponse[0]);

        return kvcValue;
    }

    /**
     * Method to get the KIF by getting the sixth byte from the response
     * relevant only if response.isSuccessful is true. if KIF = 0xFF then the
     * KIF is unknown
     *
     * @param response
     *            the response
     * @return a KIF
     */
    public static KIF toKIF(byte[] response) {
        if (response.length > 4) {
            KIF kif = new KIF(response[5]);

            if (kif.getValue() == (byte) 0xFF) {
                kif = new KIF((byte) 0x30);
            }
            return kif;
        } else
            return null;
    }

    /**
     * Method to set a boolean true or false depending on the use or not of the
     * sixth byte from the response Depending on the card's version. relevant
     * only if response.isSuccessful is true.
     *
     * @param apduResponse
     *            the apdu response
     * @param revision
     *            the revision
     * @return isPreviousSessionRatified
     */
    public static Ratification isPreviousSessionRatified(byte[] apduResponse, PoRevision revision) {
        byte flagRatified;
        Ratification ratification;
        boolean isPreviousSessionRatified = false;
        if ((PoRevision.REV3_2 == revision) || (PoRevision.REV3_1 == revision)) {
            flagRatified = apduResponse[5];
            if (flagRatified != 0x01) {
                isPreviousSessionRatified = true;
            }
        } else if (PoRevision.REV2_4 == revision && apduResponse.length < 6) {
            isPreviousSessionRatified = true;
        }

        ratification = new Ratification(isPreviousSessionRatified);
        return ratification;
    }

    /**
     * Method to get the number of transaction from the response and depending
     * on the card's revision relevant only if response.isSuccessful is true The
     * method parse the data from the response and get a byte [] to get the 3
     * bytes of the transaction counter. the counter is set by adding the value
     * of the 3 bytes.
     *
     * @param apduResponse
     *            the apdu response
     * @param revision
     *            the revision
     * @return transacCounter
     */
    public static TransactionCounter toTransactionCounter(byte[] apduResponse, PoRevision revision) {
        int transacCounter = 0;
        if ((PoRevision.REV3_2 == revision) || (PoRevision.REV3_1 == revision)) {
            byte[] transactionCounter = ArrayUtils.subarray(apduResponse, 1, 4);
            for (int i = 0; i < transactionCounter.length; i++) {
                transacCounter = transacCounter + transactionCounter[i];
            }
        } else if (PoRevision.REV2_4 == revision) {
            byte[] transactionCounter = ArrayUtils.subarray(apduResponse, 2, 5);
            for (int i = 0; i < transactionCounter.length; i++) {
                transacCounter = transacCounter + transactionCounter[i];
            }
        }

        return new TransactionCounter(transacCounter);
    }

    /**
     * Method to get the PO half session signature (the second half part of the
     * signature necessary to close the session properly) from the response.
     *
     * @param response
     *            the response
     * @return a POHalfSessionSignature
     */
    public static POHalfSessionSignature toPoHalfSessionSignature(byte[] response) {
        byte[] poHalfSessionSignatureTable = null;
        if (response.length == 8) {
            poHalfSessionSignatureTable = ArrayUtils.subarray(response, 4, response.length);
        } else if (response.length == 4) {
            poHalfSessionSignatureTable = ArrayUtils.subarray(response, 0, response.length);
        }

        return new POHalfSessionSignature(poHalfSessionSignatureTable);
    }

    /**
     * Method to get the post poned data from the response.
     *
     * @param response
     *            the response
     * @return a PostponedData
     */
    public static PostponedData toPostponedData(byte[] response) {
        byte[] responsePostponedDataTable;
        byte[] postponedDataTable = null;
        boolean hasPostponedData = false;

        if (response.length == 8) {
            hasPostponedData = true;
            responsePostponedDataTable = ArrayUtils.subarray(response, 0, 4);
            postponedDataTable = ArrayUtils.subarray(responsePostponedDataTable, 1, responsePostponedDataTable.length);
        }
        return new PostponedData(hasPostponedData, postponedDataTable);
    }
}
