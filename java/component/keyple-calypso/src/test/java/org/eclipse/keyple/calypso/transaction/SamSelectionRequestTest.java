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
package org.eclipse.keyple.calypso.transaction;

import static org.assertj.core.api.Java6Assertions.assertThat;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.core.seproxy.message.AnswerToReset;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.message.SelectionStatus;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class SamSelectionRequestTest {
    private static final String REVISION = "C1";
    private static final String SN = "11223344";
    private static final String ATR = "3B3F9600805A4880" + REVISION + "205017" + SN + "829000";

    @Test
    public void samSelectionRequest_parse() {
        SamSelector samSelector =
                SamSelector.builder().seProtocol(SeCommonProtocols.PROTOCOL_ISO7816_3)
                        .samRevision(SamRevision.AUTO).build();
        SamSelectionRequest samSelectionRequest = new SamSelectionRequest(samSelector);
        SelectionStatus selectionStatus =
                new SelectionStatus(new AnswerToReset(ByteArrayUtil.fromHex(ATR)), null, true);
        CalypsoSam calypsoSam =
                samSelectionRequest.parse(new SeResponse(true, true, selectionStatus, null));
        // minimal checks on the CalypsoSam result
        assertThat(calypsoSam.getSamRevision()).isEqualTo(SamRevision.C1);
        assertThat(calypsoSam.getSerialNumber()).isEqualTo(ByteArrayUtil.fromHex(SN));
    }
}
