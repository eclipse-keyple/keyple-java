/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.po.parser;

import java.nio.ByteBuffer;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.ByteBufferUtils;

public class OpenSession31RespPars extends AbstractOpenSessionRespPars {

    public OpenSession31RespPars(ApduResponse response) {
        super(response, PoRevision.REV3_1);
    }

    @Override
    SecureSession toSecureSession(ByteBuffer apduResponse) {
        SecureSession secureSession;
        boolean previousSessionRatified = (apduResponse.get(4) == (byte) 0x01);
        boolean manageSecureSessionAuthorized = false;

        byte kif = apduResponse.get(5);
        byte kvc = apduResponse.get(6);
        int dataLength = apduResponse.get(7);
        ByteBuffer data = ByteBufferUtils.subIndex(apduResponse, 8, 8 + dataLength);

        return new SecureSession(ByteBufferUtils.subIndex(apduResponse, 0, 3),
                ByteBufferUtils.subIndex(apduResponse, 3, 4), previousSessionRatified,
                manageSecureSessionAuthorized, kif, kvc, data, apduResponse);
    }
}
