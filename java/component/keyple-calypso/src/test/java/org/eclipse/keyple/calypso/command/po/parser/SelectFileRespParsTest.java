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
package org.eclipse.keyple.calypso.command.po.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.shouldHaveThrown;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class SelectFileRespParsTest {
    private static final String SW1SW2_KO = "6A82";
    private static final String SW1SW2_OK = "9000";
    private static final String ACCESS_CONDITIONS_3F00 = "10100000";
    private static final String KEY_INDEXES_3F00 = "01030101";
    private static final String PROPRIETARY_INFORMATION =
            "0001000000" + ACCESS_CONDITIONS_3F00 + KEY_INDEXES_3F00 + "00777879616770003F00";
    private static final String PO_SELECT_FILE_3F00_RSP =
            "8517" + PROPRIETARY_INFORMATION + SW1SW2_OK;

    @Test(expected = CalypsoPoCommandException.class)
    public void selectFileRespParsTest_badStatus() throws CalypsoPoCommandException {
        SelectFileRespPars selectFileRespPars = new SelectFileRespPars(
                new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_KO), null), null);
        selectFileRespPars.checkStatus();
        shouldHaveThrown(CalypsoPoCommandException.class);
    }

    @Test
    public void selectFileRespParsTest_goodStatus() throws CalypsoPoCommandException {
        SelectFileRespPars selectFileRespPars = new SelectFileRespPars(
                new ApduResponse(ByteArrayUtil.fromHex(PO_SELECT_FILE_3F00_RSP), null), null);
        selectFileRespPars.checkStatus();
    }

    @Test
    public void selectFileRespParsTest_getProprietaryInformation() {
        SelectFileRespPars selectFileRespPars = new SelectFileRespPars(
                new ApduResponse(ByteArrayUtil.fromHex(PO_SELECT_FILE_3F00_RSP), null), null);
        assertThat(selectFileRespPars.getProprietaryInformation())
                .isEqualTo(ByteArrayUtil.fromHex(PROPRIETARY_INFORMATION));
    }
}
