/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.utils;

import org.keyple.calypso.commands.dto.*;
import org.keyple.calypso.commands.po.parser.GetDataFciRespPars;

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
    public static GetDataFciRespPars.FCI toFCI(byte[] apduResponse) {
        GetDataFciRespPars.StartupInformation startupInformation = null;
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
                startupInformation = new GetDataFciRespPars.StartupInformation(discretionaryData[0],
                        discretionaryData[1], discretionaryData[2], discretionaryData[3],
                        discretionaryData[4], discretionaryData[5], discretionaryData[6]);
            }
        }

        return new GetDataFciRespPars.FCI(dfName, fciProprietaryTemplate,
                fciIssuerDiscretionaryData, applicationSN, startupInformation);
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

        byte kif = apduResponse[9];
        byte kvc = apduResponse[10];
        int dataLength = apduResponse[11];
        byte[] data = subArray(apduResponse, 12, 12 + dataLength);

        return new SecureSession(
                new SecureSession.PoChallenge(subArray(apduResponse, 0, 3),
                        subArray(apduResponse, 3, 8)),
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

        byte kif = apduResponse[5];
        byte kvc = apduResponse[6];
        int dataLength = apduResponse[7];
        byte[] data = subArray(apduResponse, 8, 8 + dataLength);

        secureSession = new SecureSession(
                new SecureSession.PoChallenge(subArray(apduResponse, 0, 3),
                        subArray(apduResponse, 3, 4)),
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

        byte kvc = toKVCRev2(apduResponse);

        if (apduResponse.length < 6) {
            previousSessionRatified = false;
        }

        // TODO selecting record data without length ?

        secureSession = new SecureSession(
                new SecureSession.PoChallenge(subArray(apduResponse, 1, 4),
                        subArray(apduResponse, 4, 5)),
                previousSessionRatified, false, kvc, null, apduResponse);

        return secureSession;
    }

    /**
     * Method to get the KVC from the response in revision 2 mode.
     *
     * @param apduResponse the apdu response
     * @return a KVC byte
     */
    public static byte toKVCRev2(byte[] apduResponse) {
        // TODO: Check that part: I replaced a (null) KVC by a 0x00
        return apduResponse.length > 4 ? apduResponse[0] : 0x00;
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

    /**
     * Create a sub-array from an array
     *
     * @param source Source array
     * @param indexStart Start index
     * @param indexEnd End index
     * @return
     */
    public static byte[] subArray(byte[] source, int indexStart, int indexEnd) {
        byte[] res = new byte[indexEnd - indexStart];
        System.arraycopy(source, indexStart, res, 0, indexEnd - indexStart);
        return res;
    }
}
