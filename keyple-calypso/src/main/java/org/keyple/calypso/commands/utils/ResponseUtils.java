/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.utils;

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
     * Get the value of the bit
     *
     * @param b Input byte
     * @param p Bit position in the byte
     * @return true if the bit is set
     */
    public static boolean isBitSet(byte b, int p) {
        return (1 == ((b >> p) & 1));
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
