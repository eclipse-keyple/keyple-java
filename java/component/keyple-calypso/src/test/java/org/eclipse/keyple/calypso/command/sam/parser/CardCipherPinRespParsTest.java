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
package org.eclipse.keyple.calypso.command.sam.parser;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.shouldHaveThrown;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamCommandException;
import org.eclipse.keyple.calypso.command.sam.parser.security.CardCipherPinRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class CardCipherPinRespParsTest {
    private static final String SW1SW2_KO = "6A82";
    private static final String SW1SW2_OK = "9000";
    private static final String CIPHERED_DATA = "1122334455667788";

    @Test
    public void cardCipherPinRespPars_goodStatus() {
        CardCipherPinRespPars parser = new CardCipherPinRespPars(
                new ApduResponse(ByteArrayUtil.fromHex(CIPHERED_DATA + SW1SW2_OK), null), null);
        parser.checkStatus();
        assertThat(parser.getCipheredData()).isEqualTo(ByteArrayUtil.fromHex(CIPHERED_DATA));
    }

    @Test(expected = CalypsoSamCommandException.class)
    public void cardCipherPinRespPars_badStatus() {
        CardCipherPinRespPars parser = new CardCipherPinRespPars(
                new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_KO), null), null);
        parser.checkStatus();
        shouldHaveThrown(CalypsoSamCommandException.class);
    }
}
