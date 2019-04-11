/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.command.po.parser.security;

import java.util.Arrays;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.seproxy.message.ApduResponse;

public final class OpenSession24RespPars extends AbstractOpenSessionRespPars {

    public OpenSession24RespPars(ApduResponse response) {
        super(response, PoRevision.REV2_4);
    }

    @Override
    SecureSession toSecureSession(byte[] apduResponseData) {
        return createSecureSession(apduResponseData);
    }

    public static SecureSession createSecureSession(byte[] apduResponseData) {
        boolean previousSessionRatified;

        /**
         * In rev 2.4 mode, the response to the Open Secure Session command is as follows:
         * <p>
         * <code>KK CC CC CC CC [RR RR] [NN..NN]</code>
         * <p>
         * Where:
         * <ul>
         * <li><code>KK</code> = KVC byte CC</li>
         * <li><code>CC CC CC CC</code> = PO challenge</li>
         * <li><code>RR RR</code> = ratification bytes (may be absent)</li>
         * <li><code>NN..NN</code> = record data (29 bytes)</li>
         * </ul>
         * Legal length values are:
         * <ul>
         * <li>5: ratified, 1-byte KCV, 4-byte challenge, no data</li>
         * <li>34: ratified, 1-byte KCV, 4-byte challenge, 29 bytes of data</li>
         * <li>7: not ratified (2 ratification bytes), 1-byte KCV, 4-byte challenge, no data</li>
         * <li>35 not ratified (2 ratification bytes), 1-byte KCV, 4-byte challenge, 29 bytes of
         * data</li>
         * </ul>
         */

        byte[] data = null;

        switch (apduResponseData.length) {
            case 5:
                previousSessionRatified = true;
                break;
            case 34:
                previousSessionRatified = true;
                data = Arrays.copyOfRange(apduResponseData, 5, 34);
                break;
            case 7:
                previousSessionRatified = false;
                break;
            case 36:
                previousSessionRatified = false;
                data = Arrays.copyOfRange(apduResponseData, 7, 36);
                break;
            default:
                throw new IllegalStateException(
                        "Bad response length to Open Secure Session: " + apduResponseData.length);
        }

        byte kvc = apduResponseData[0];

        return new SecureSession(Arrays.copyOfRange(apduResponseData, 1, 4),
                Arrays.copyOfRange(apduResponseData, 4, 5), previousSessionRatified, false, kvc,
                data, apduResponseData);
    }
}
