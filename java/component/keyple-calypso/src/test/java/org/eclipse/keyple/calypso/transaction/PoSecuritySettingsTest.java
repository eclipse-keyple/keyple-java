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
package org.eclipse.keyple.calypso.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.keyple.calypso.transaction.PoTransaction.SessionSetting.*;
import org.eclipse.keyple.core.selection.SeResource;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.message.AnswerToReset;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.message.SelectionStatus;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class PoSecuritySettingsTest {
    public static String ATR1 = "3B001122805A0180D002030411223344829000";
    // The default KIF values for personalization, loading and debiting
    final byte DEFAULT_KIF_PERSO = (byte) 0x21;
    final byte DEFAULT_KIF_LOAD = (byte) 0x27;
    final byte DEFAULT_KIF_DEBIT = (byte) 0x30;
    // The default key record number values for personalization, loading and debiting
    // The actual value should be adjusted.
    final byte DEFAULT_KEY_RECORD_NUMBER_PERSO = (byte) 0x01;
    final byte DEFAULT_KEY_RECORD_NUMBER_LOAD = (byte) 0x02;
    final byte DEFAULT_KEY_RECORD_NUMBER_DEBIT = (byte) 0x03;

    @Test
    public void poSecuritySettings_nominal() {
        SeReader samReader = null;
        CalypsoSam calypsoSam = createCalypsoSam();
        SeResource<CalypsoSam> samResource = new SeResource<CalypsoSam>(samReader, calypsoSam);
        PoSecuritySettings poSecuritySettings =
                new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)//
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_PERSO, DEFAULT_KIF_PERSO)//
                        .sessionDefaultKif(AccessLevel.SESSION_LVL_LOAD, DEFAULT_KIF_LOAD)//
                        .sessionDefaultKif(
                                PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KIF_DEBIT)//
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_PERSO,
                                DEFAULT_KEY_RECORD_NUMBER_PERSO)//
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_LOAD,
                                DEFAULT_KEY_RECORD_NUMBER_LOAD)//
                        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_DEBIT,
                                DEFAULT_KEY_RECORD_NUMBER_DEBIT)
                        .build();
        assertThat(poSecuritySettings.getSessionDefaultKif(AccessLevel.SESSION_LVL_PERSO))
                .isEqualTo(DEFAULT_KIF_PERSO);
        assertThat(poSecuritySettings.getSessionDefaultKif(AccessLevel.SESSION_LVL_LOAD))
                .isEqualTo(DEFAULT_KIF_LOAD);
        assertThat(poSecuritySettings.getSessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT))
                .isEqualTo(DEFAULT_KIF_DEBIT);
    }

    private CalypsoSam createCalypsoSam() {

        SelectionStatus selectionStatus =
                new SelectionStatus(new AnswerToReset(ByteArrayUtil.fromHex(ATR1)), null, true);
        return new CalypsoSam(new SeResponse(true, true, selectionStatus, null),
                TransmissionMode.CONTACTS);
    }

    // TODO complete coverage
}
