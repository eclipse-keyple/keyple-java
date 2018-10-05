/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.po.parser.session;

import java.nio.ByteBuffer;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.util.ByteBufferUtils;

public class OpenSession31RespPars extends AbstractOpenSessionRespPars {

    public OpenSession31RespPars(ApduResponse response) {
        super(response, PoRevision.REV3_1);
    }

    @Override
    SecureSession toSecureSession(ByteBuffer apduResponseData) {
        SecureSession secureSession;
        boolean previousSessionRatified = (apduResponseData.get(4) == (byte) 0x00);
        boolean manageSecureSessionAuthorized = false;

        byte kif = apduResponseData.get(5);
        byte kvc = apduResponseData.get(6);
        int dataLength = apduResponseData.get(7);
        ByteBuffer data = ByteBufferUtils.subIndex(apduResponseData, 8, 8 + dataLength);

        return new SecureSession(ByteBufferUtils.subIndex(apduResponseData, 0, 3),
                ByteBufferUtils.subIndex(apduResponseData, 3, 4), previousSessionRatified,
                manageSecureSessionAuthorized, kif, kvc, data, apduResponseData);
    }
}
