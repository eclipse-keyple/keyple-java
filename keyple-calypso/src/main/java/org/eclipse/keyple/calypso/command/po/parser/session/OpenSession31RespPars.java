/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.calypso.command.po.parser.session;

import java.util.Arrays;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.seproxy.ApduResponse;

public class OpenSession31RespPars extends AbstractOpenSessionRespPars {

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
