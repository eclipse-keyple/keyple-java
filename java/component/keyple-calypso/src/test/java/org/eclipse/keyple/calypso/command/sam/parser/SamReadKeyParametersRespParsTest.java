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
package org.eclipse.keyple.calypso.command.sam.parser;

import static org.assertj.core.api.Assertions.shouldHaveThrown;
import static org.assertj.core.api.Java6Assertions.assertThat;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamCommandException;
import org.eclipse.keyple.calypso.command.sam.parser.security.SamReadKeyParametersRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SamReadKeyParametersRespParsTest {
    private static final String SW1SW2_KO = "6A00";
    private static final String SW1SW2_OK = "9000";
    private static final String KEY_PARAMETERS = "11223344";
    private static final String APDU_READ_KEY_PARAMS = KEY_PARAMETERS + SW1SW2_OK;

    @Test(expected = CalypsoSamCommandException.class)
    public void samReadKeyParametersRespPars_badStatus() throws CalypsoSamCommandException {
        SamReadKeyParametersRespPars samReadKeyParametersRespPars =
                new SamReadKeyParametersRespPars(
                        new ApduResponse(ByteArrayUtil.fromHex(SW1SW2_KO), null), null);
        samReadKeyParametersRespPars.checkStatus();
        shouldHaveThrown(CalypsoSamCommandException.class);
    }

    @Test
    public void samReadKeyParametersRespPars_goodStatus_getSignature()
            throws CalypsoSamCommandException {
        SamReadKeyParametersRespPars samReadKeyParametersRespPars =
                new SamReadKeyParametersRespPars(
                        new ApduResponse(ByteArrayUtil.fromHex(APDU_READ_KEY_PARAMS), null), null);
        samReadKeyParametersRespPars.checkStatus();
        assertThat(samReadKeyParametersRespPars.getKeyParameters())
                .isEqualTo(ByteArrayUtil.fromHex(KEY_PARAMETERS));
    }
}
