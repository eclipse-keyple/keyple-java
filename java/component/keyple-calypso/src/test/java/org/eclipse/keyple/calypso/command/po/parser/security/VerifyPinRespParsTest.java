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

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.shouldHaveThrown;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoPinException;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class VerifyPinRespParsTest {
    private static final String SW1SW2_KO = "6A82";
    private static final String SW1SW2_OK = "9000";
    private static final String ATTEMPTS_1 = "63C1";
    private static final String ATTEMPTS_2 = "63C2";
    private static final String PIN_BLOCKED = "6983";

    @Test
    public void verifyPinRespPars_goodStatus() {
        VerifyPinRespPars parser = new VerifyPinRespPars(
                new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_OK), null), null);
        parser.checkStatus();
        assertThat(parser.getRemainingAttemptCounter()).isEqualTo(3);
    }

    @Test(expected = CalypsoPoCommandException.class)
    public void verifyPinRespPars_badStatus() {
        VerifyPinRespPars parser = new VerifyPinRespPars(
                new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_KO), null), null);
        parser.checkStatus();
        shouldHaveThrown(CalypsoPoCommandException.class);
    }

    @Test(expected = IllegalStateException.class)
    public void verifyPinRespPars_badStatus_2() {
        VerifyPinRespPars parser = new VerifyPinRespPars(
                new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_KO), null), null);
        parser.getRemainingAttemptCounter();
        shouldHaveThrown(IllegalStateException.class);
    }

    @Test(expected = CalypsoPoPinException.class)
    public void verifyPinRespPars_attempts_1() {
        VerifyPinRespPars parser = new VerifyPinRespPars(
                new ApduResponse(ByteArrayUtil.fromHex(ATTEMPTS_1), null), null);
        assertThat(parser.getRemainingAttemptCounter()).isEqualTo(1);
        parser.checkStatus();
        shouldHaveThrown(CalypsoPoPinException.class);
    }

    @Test(expected = CalypsoPoPinException.class)
    public void verifyPinRespPars_attempts_2() {
        VerifyPinRespPars parser = new VerifyPinRespPars(
                new ApduResponse(ByteArrayUtil.fromHex(ATTEMPTS_2), null), null);
        assertThat(parser.getRemainingAttemptCounter()).isEqualTo(2);
        parser.checkStatus();
        shouldHaveThrown(CalypsoPoPinException.class);
    }

    @Test(expected = CalypsoPoPinException.class)
    public void verifyPinRespPars_pin_blocked() {
        VerifyPinRespPars parser = new VerifyPinRespPars(
                new ApduResponse(ByteArrayUtil.fromHex(PIN_BLOCKED), null), null);
        assertThat(parser.getRemainingAttemptCounter()).isEqualTo(0);
        parser.checkStatus();
        shouldHaveThrown(CalypsoPoPinException.class);
    }
}
