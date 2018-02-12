/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.utils;

import java.util.ArrayList;
import java.util.List;
import org.keyple.calypso.commands.dto.FCI;
import org.keyple.calypso.commands.dto.KIF;
import org.keyple.calypso.commands.dto.KVC;
import org.keyple.calypso.commands.dto.POChallenge;
import org.keyple.calypso.commands.dto.POHalfSessionSignature;
import org.keyple.calypso.commands.dto.Record;
import org.keyple.calypso.commands.dto.SecureSession;
import org.keyple.calypso.commands.dto.StartupInformation;

/**
 * This class eases the parse of APDUResponses into objects.
 *
 * @author Ixxi
 */
public class ResponseUtils {

    /**
     * Instantiates a new ResponseUtils.
     */
    private ResponseUtils() {

    }

    /**
     * Method to get the FCI from the response.
     *
     * @param apduResponse the apdu response
     * @return the FCI template
     */
    public static FCI toFCI(byte[] apduResponse) {
        StartupInformation startupInformation = null;
        byte firstResponseApdubyte = apduResponse[0];
        byte[] dfName = null;
        byte[] fciProprietaryTemplate = null;
        byte[] fciIssuerDiscretionaryData = null;
        byte[] applicationSN = null;
        byte[] discretionaryData;

        if ((byte) 0x6F == firstResponseApdubyte) {
            int aidLength = apduResponse[3];
            int fciTemplateLength = apduResponse[5 + aidLength];
            int fixedPartOfFciTemplate = fciTemplateLength - 22;
            int firstbyteAid = 6 + aidLength + fixedPartOfFciTemplate;
            int fciIssuerDiscretionaryDataLength =
                    apduResponse[8 + aidLength + fixedPartOfFciTemplate];
            int firstbyteFciIssuerDiscretionaryData = 9 + aidLength + fixedPartOfFciTemplate;
            int applicationSNLength = apduResponse[10 + aidLength + fixedPartOfFciTemplate];
            int firstbyteApplicationSN = 11 + aidLength + fixedPartOfFciTemplate;
            int discretionaryDataLength = apduResponse[20 + aidLength + fixedPartOfFciTemplate];
            int firstbyteDiscretionaryData = 21 + aidLength + fixedPartOfFciTemplate;

            if ((byte) 0x84 == apduResponse[2]) {
                dfName = subArray(apduResponse, 4, 4 + aidLength);
            }

            if ((byte) 0xA5 == apduResponse[4 + aidLength]) {
                fciProprietaryTemplate =
                        subArray(apduResponse, firstbyteAid, firstbyteAid + fciTemplateLength);
            }

            if ((byte) 0xBF == apduResponse[6 + aidLength + fixedPartOfFciTemplate]
                    && ((byte) 0x0C == apduResponse[7 + aidLength + fixedPartOfFciTemplate])) {
                fciIssuerDiscretionaryData = subArray(apduResponse,
                        firstbyteFciIssuerDiscretionaryData,
                        firstbyteFciIssuerDiscretionaryData + fciIssuerDiscretionaryDataLength);
            }

            if ((byte) 0xC7 == apduResponse[9 + aidLength + fixedPartOfFciTemplate]) {
                applicationSN = subArray(apduResponse, firstbyteApplicationSN,
                        firstbyteApplicationSN + applicationSNLength);
            }

            if ((byte) 0x53 == apduResponse[19 + aidLength + fixedPartOfFciTemplate]) {
                discretionaryData = subArray(apduResponse, firstbyteDiscretionaryData,
                        firstbyteDiscretionaryData + discretionaryDataLength);
                startupInformation = new StartupInformation(discretionaryData[0],
                        discretionaryData[1], discretionaryData[2], discretionaryData[3],
                        discretionaryData[4], discretionaryData[5], discretionaryData[6]);
            }
        }

        return new FCI(dfName, fciProprietaryTemplate, fciIssuerDiscretionaryData, applicationSN,
                startupInformation);
    }

    /**
     * Method to get a Secure Session from the response in revision 3.2 mode.
     *
     * @param apduResponse the apdu response
     * @return a SecureSession
     */
    public static SecureSession toSecureSessionRev32(byte[] apduResponse) {

        byte flag = apduResponse[8];
        boolean previousSessionRatified = isBitEqualsOne(flag, 0x00);
        boolean manageSecureSessionAuthorized = isBitEqualsOne(flag, 1);

        KIF kif = new KIF(apduResponse[9]);
        KVC kvc = new KVC(apduResponse[10]);
        int dataLength = apduResponse[11];
        byte[] data = subArray(apduResponse, 12, 12 + dataLength);

        return new SecureSession(
                new POChallenge(subArray(apduResponse, 0, 3), subArray(apduResponse, 3, 8)),
                previousSessionRatified, manageSecureSessionAuthorized, kif, kvc, data,
                apduResponse);
    }

    /**
     * Method to get a Secure Session from the response in revision 3 mode.
     *
     * @param apduResponse the apdu response
     * @return a SecureSession
     */
    public static SecureSession toSecureSessionRev3(byte[] apduResponse) {
        SecureSession secureSession;
        boolean previousSessionRatified = apduResponse[4] == (byte) 0x01 ? true : false;
        boolean manageSecureSessionAuthorized = false;

        KIF kif = new KIF(apduResponse[5]);
        KVC kvc = new KVC(apduResponse[6]);
        int dataLength = apduResponse[7];
        byte[] data = subArray(apduResponse, 8, 8 + dataLength);

        secureSession = new SecureSession(
                new POChallenge(subArray(apduResponse, 0, 3), subArray(apduResponse, 3, 4)),
                previousSessionRatified, manageSecureSessionAuthorized, kif, kvc, data,
                apduResponse);
        return secureSession;
    }

    /**
     * Method to get a Secure Session from the response in revision 2 mode.
     *
     * @param apduResponse the apdu response
     * @return a SecureSession
     */
    public static SecureSession toSecureSessionRev2(byte[] apduResponse) {
        SecureSession secureSession;
        boolean previousSessionRatified = true;
        boolean manageSecureSessionAuthorized = false;

        KVC kvc = toKVCRev2(apduResponse);
        byte[] data = null;

        if (apduResponse.length < 6) {
            previousSessionRatified = false;
        }

        // TODO selecting record data without length ?

        secureSession = new SecureSession(
                new POChallenge(subArray(apduResponse, 1, 4), subArray(apduResponse, 4, 5)),
                previousSessionRatified, manageSecureSessionAuthorized, kvc, data, apduResponse);

        return secureSession;
    }

    /**
     * Method to get the Records from the response.
     *
     * @param apduResponse the apdu response
     * @param oneRecordOnly the one record only
     * @return a Maps of Records
     */
    public static List<Record> toRecords(byte[] apduResponse, boolean oneRecordOnly) {
        List<Record> records = new ArrayList<Record>();
        if (oneRecordOnly) {
            records.add(new Record(apduResponse, 0));
        } else {
            int i = 0;
            while (i < apduResponse.length) {
                if (i + 2 + apduResponse[i + 1] > apduResponse.length - 1) {
                    records.add(new Record(subArray(apduResponse, i + 2, apduResponse.length - 1),
                            apduResponse[i]));
                } else {
                    records.add(
                            new Record(subArray(apduResponse, i + 2, i + 2 + apduResponse[i + 1]),
                                    apduResponse[i]));
                }
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
     * Method to get the KVC from the response in revision 2 mode.
     *
     * @param apduResponse the apdu response
     * @return a KVC
     */
    public static KVC toKVCRev2(byte[] apduResponse) {
        KVC kvcValue = null;
        if (apduResponse.length > 4) {
            kvcValue = new KVC(apduResponse[0]);
        }

        return kvcValue;
    }

    /**
     * Method to get the PO half session signature (the second half part of the signature necessary
     * to close the session properly) from the response.
     *
     * @param response the response
     * @return a POHalfSessionSignature
     */
    public static POHalfSessionSignature toPoHalfSessionSignature(byte[] response) {
        byte[] poHalfSessionSignatureTable = null;
        byte[] postponedData = null;
        if (response.length == 8) {
            poHalfSessionSignatureTable = subArray(response, 4, response.length);
            postponedData = subArray(response, 0, 4);
        } else if (response.length == 4) {
            poHalfSessionSignatureTable = subArray(response, 0, response.length);
        }

        return new POHalfSessionSignature(poHalfSessionSignatureTable, postponedData);
    }

    /**
     * Checks if is bit equals one.
     *
     * @param thebyte the thebyte
     * @param position the position
     * @return true, if is bit equals one
     */
    private static boolean isBitEqualsOne(byte thebyte, int position) {
        return (1 == ((thebyte >> position) & 1));
    }

    private static byte[] subArray(byte[] source, int indexStart, int indexEnd) {
        byte[] res = new byte[indexEnd - indexStart];
        System.arraycopy(source, indexStart, res, 0, indexEnd - indexStart);
        return res;
    }
}
