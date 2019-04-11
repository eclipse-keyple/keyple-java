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

public final class OpenSession31RespPars extends AbstractOpenSessionRespPars {

    public OpenSession31RespPars(ApduResponse response) {
        super(response, PoRevision.REV3_1);
    }

    @Override
    SecureSession toSecureSession(byte[] apduResponseData) {
        SecureSession secureSession;
        boolean previousSessionRatified = (apduResponseData[4] == (byte) 0x00);
        boolean manageSecureSessionAuthorized = false;

        byte kif = apduResponseData[5];
        byte kvc = apduResponseData[6];
        int dataLength = apduResponseData[7];
        byte[] data = Arrays.copyOfRange(apduResponseData, 8, 8 + dataLength);

        return new SecureSession(Arrays.copyOfRange(apduResponseData, 0, 3),
                Arrays.copyOfRange(apduResponseData, 3, 4), previousSessionRatified,
                manageSecureSessionAuthorized, kif, kvc, data, apduResponseData);
    }
}
