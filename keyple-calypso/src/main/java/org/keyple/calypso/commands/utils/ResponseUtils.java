/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.utils;

import java.nio.ByteBuffer;
import org.keyple.calypso.commands.po.parser.GetDataFciRespPars;
import org.keyple.seproxy.ByteBufferUtils;

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
    public static GetDataFciRespPars.FCI toFCI(ByteBuffer apduResponse) {
        GetDataFciRespPars.StartupInformation startupInformation = null;
        byte firstResponseApdubyte = apduResponse.get(0);
        ByteBuffer dfName = null;
        ByteBuffer fciProprietaryTemplate = null;
        ByteBuffer fciIssuerDiscretionaryData = null;
        ByteBuffer applicationSN = null;
        ByteBuffer discretionaryData;

        if ((byte) 0x6F == firstResponseApdubyte) {
            int aidLength = apduResponse.get(3);
            int fciTemplateLength = apduResponse.get(5 + aidLength);
            int fixedPartOfFciTemplate = fciTemplateLength - 22;
            int firstbyteAid = 6 + aidLength + fixedPartOfFciTemplate;
            int fciIssuerDiscretionaryDataLength =
                    apduResponse.get(8 + aidLength + fixedPartOfFciTemplate);
            int firstbyteFciIssuerDiscretionaryData = 9 + aidLength + fixedPartOfFciTemplate;
            int applicationSNLength = apduResponse.get(10 + aidLength + fixedPartOfFciTemplate);
            int firstbyteApplicationSN = 11 + aidLength + fixedPartOfFciTemplate;
            int discretionaryDataLength = apduResponse.get(20 + aidLength + fixedPartOfFciTemplate);
            int firstbyteDiscretionaryData = 21 + aidLength + fixedPartOfFciTemplate;

            if ((byte) 0x84 == apduResponse.get(2)) {
                dfName = ByteBufferUtils.subIndex(apduResponse, 4, 4 + aidLength);
            }

            if ((byte) 0xA5 == apduResponse.get(4 + aidLength)) {
                fciProprietaryTemplate = ByteBufferUtils.subIndex(apduResponse, firstbyteAid,
                        firstbyteAid + fciTemplateLength);
            }

            if ((byte) 0xBF == apduResponse.get(6 + aidLength + fixedPartOfFciTemplate)
                    && ((byte) 0x0C == apduResponse.get(7 + aidLength + fixedPartOfFciTemplate))) {
                fciIssuerDiscretionaryData = ByteBufferUtils.subIndex(apduResponse,
                        firstbyteFciIssuerDiscretionaryData,
                        firstbyteFciIssuerDiscretionaryData + fciIssuerDiscretionaryDataLength);
            }

            if ((byte) 0xC7 == apduResponse.get(9 + aidLength + fixedPartOfFciTemplate)) {
                applicationSN = ByteBufferUtils.subIndex(apduResponse, firstbyteApplicationSN,
                        firstbyteApplicationSN + applicationSNLength);
            }

            if ((byte) 0x53 == apduResponse.get(19 + aidLength + fixedPartOfFciTemplate)) {
                discretionaryData =
                        ByteBufferUtils.subIndex(apduResponse, firstbyteDiscretionaryData,
                                firstbyteDiscretionaryData + discretionaryDataLength);
                startupInformation =
                        new GetDataFciRespPars.StartupInformation(discretionaryData.get(0),
                                discretionaryData.get(1), discretionaryData.get(2),
                                discretionaryData.get(3), discretionaryData.get(4),
                                discretionaryData.get(5), discretionaryData.get(6));
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
