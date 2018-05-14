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

public class OpenSession24RespPars extends AbstractOpenSessionRespPars {

    public OpenSession24RespPars(ApduResponse response) {
        super(response, PoRevision.REV2_4);
    }

    @Override
    SecureSession toSecureSession(ByteBuffer apduResponse) {
        return createSecureSession(apduResponse);
    }

    public static SecureSession createSecureSession(ByteBuffer apduResponse) {
        boolean previousSessionRatified = true;

        byte kvc = toKVCRev2(apduResponse);

        if (apduResponse.limit() < 6) {
            previousSessionRatified = false;
        }

        return new SecureSession(ByteBufferUtils.subIndex(apduResponse, 1, 4),
                ByteBufferUtils.subIndex(apduResponse, 4, 5), previousSessionRatified, false, kvc,
                null, apduResponse);
    }
}
