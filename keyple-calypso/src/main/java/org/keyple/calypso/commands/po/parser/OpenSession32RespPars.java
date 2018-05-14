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
import org.keyple.calypso.commands.utils.ApduUtils;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.ByteBufferUtils;

public class OpenSession32RespPars extends AbstractOpenSessionRespPars {

    public OpenSession32RespPars(ApduResponse response) {
        super(response, PoRevision.REV3_2);
    }

    /**
     * Method to get a Secure Session from the response in revision 3.2 mode.
     *
     * @param apduResponse the apdu response
     * @return a SecureSession
     */
    SecureSession toSecureSession(ByteBuffer apduResponse) {
        return createSecureSession(apduResponse);
    }

    public static SecureSession createSecureSession(ByteBuffer apduResponse) {

        byte flag = apduResponse.get(8);
        boolean previousSessionRatified = ApduUtils.isBitSet(flag, 0x00);
        boolean manageSecureSessionAuthorized = ApduUtils.isBitSet(flag, 1);

        byte kif = apduResponse.get(9);
        byte kvc = apduResponse.get(10);
        int dataLength = apduResponse.get(11);
        ByteBuffer data = ByteBufferUtils.subIndex(apduResponse, 12, 12 + dataLength);

        return new SecureSession(ByteBufferUtils.subIndex(apduResponse, 0, 3),
                ByteBufferUtils.subIndex(apduResponse, 3, 8), previousSessionRatified,
                manageSecureSessionAuthorized, kif, kvc, data, apduResponse);
    }
}
