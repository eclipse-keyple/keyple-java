/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
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

import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class RehabilitateRespParsTest {
    private static final byte[] SW1SW2_OK = ByteArrayUtil.fromHex("9000");
    private static final byte[] SW1SW2_KO = ByteArrayUtil.fromHex("6982");

    @Test
    public void rehabilitateRespPars_goodStatus() {
        RehabilitateRespPars parser =
                new RehabilitateRespPars(new ApduResponse(SW1SW2_OK, null), null);
        parser.checkStatus();
    }

    @Test(expected = CalypsoPoCommandException.class)
    public void rehabilitateRespPars_badStatus() {
        RehabilitateRespPars parser =
                new RehabilitateRespPars(new ApduResponse(SW1SW2_KO, null), null);
        parser.checkStatus();
    }
}
