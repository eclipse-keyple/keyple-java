/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.po.parser;

import java.nio.ByteBuffer;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.util.ByteBufferUtils;

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
        // ratification: if the bit 0 of flag is set then the previous session has been ratified
        boolean previousSessionRatified = (flag & (1 << 0)) != 0;
        // secure session: if the bit 1 of flag is set then the secure session is authorized
        boolean manageSecureSessionAuthorized = (flag & (1 << 1)) != 0;

        byte kif = apduResponse.get(9);
        byte kvc = apduResponse.get(10);
        int dataLength = apduResponse.get(11);
        ByteBuffer data = ByteBufferUtils.subIndex(apduResponse, 12, 12 + dataLength);

        return new SecureSession(ByteBufferUtils.subIndex(apduResponse, 0, 3),
                ByteBufferUtils.subIndex(apduResponse, 3, 8), previousSessionRatified,
                manageSecureSessionAuthorized, kif, kvc, data, apduResponse);
    }
}
