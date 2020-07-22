/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.command.po.parser.storedvalue;

import static org.junit.Assert.*;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Test;

public class SvDebitRespParsTest {

    @Test
    public void getSignatureLo_mode_compat() {
        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex("001122 9000"), null);
        SvDebitRespPars svDebitRespPars = new SvDebitRespPars(apduResponse, null);
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("001122"), svDebitRespPars.getSignatureLo());
    }

    @Test
    public void getSignatureLo_mode_rev3_2() {
        ApduResponse apduResponse =
                new ApduResponse(ByteArrayUtil.fromHex("001122334455 9000"), null);
        SvDebitRespPars svDebitRespPars = new SvDebitRespPars(apduResponse, null);
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("001122334455"),
                svDebitRespPars.getSignatureLo());
    }


    @Test(expected = IllegalStateException.class)
    public void getSignatureLo_bad_length() {
        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex("0011 9000"), null);
        SvDebitRespPars svDebitRespPars = new SvDebitRespPars(apduResponse, null);
    }
}
