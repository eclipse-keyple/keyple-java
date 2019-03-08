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


import org.eclipse.keyple.calypso.command.sam.parser.session.SelectDiversifierRespPars;
import org.eclipse.keyple.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.message.ApduResponse;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SelectDiversifierRespParsTest {

    @Test
    public void selectDiversifierResp() {
        // We check here that the value returned by getApduResponse matches the value provided at
        // construct time
        ApduResponse apduResponse = new ApduResponse(new byte[] {(byte) 0x90, 0x00}, null);
        ApduResponse apduResponse1 = new ApduResponse(new byte[] {(byte) 0x80, 0x00}, null);

        AbstractApduResponseParser apduResponseParser =
                new org.eclipse.keyple.calypso.command.sam.parser.session.SelectDiversifierRespPars(
                        apduResponse);

        Assert.assertEquals(0x9000, apduResponseParser.getApduResponse().getStatusCode());

        apduResponseParser = new SelectDiversifierRespPars(apduResponse1);

        Assert.assertThat(apduResponseParser.getApduResponse().getStatusCode(), IsNot.not(0x9000));
    }
}
