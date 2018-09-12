/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.command.po.parser.session;

import java.nio.ByteBuffer;
import org.eclipse.keyple.command.po.PoRevision;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.util.ByteBufferUtils;

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
