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
package org.eclipse.keyple.calypso.command.po.parser;

import static org.assertj.core.api.Assertions.shouldHaveThrown;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WriteRecordRespParsTest {
    private static final String SW1SW2_KO = "6A82";
    private static final String SW1SW2_OK = "9000";

    @Test(expected = CalypsoPoCommandException.class)
    public void writeRecordRespPars_badStatus() throws CalypsoPoCommandException {
        WriteRecordRespPars writeRecordRespPars = new WriteRecordRespPars(
                new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_KO), null), null);
        writeRecordRespPars.checkStatus();
        shouldHaveThrown(CalypsoPoCommandException.class);
    }

    @Test
    public void writeRecordRespPars_goodStatus() throws CalypsoPoCommandException {
        WriteRecordRespPars writeRecordRespPars = new WriteRecordRespPars(
                new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_OK), null), null);
        writeRecordRespPars.checkStatus();
    }
}
