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

import static org.eclipse.keyple.calypso.command.sam.SamRevision.*;
import static org.junit.Assert.*;
import org.eclipse.keyple.calypso.transaction.sam.CalypsoSam;
import org.eclipse.keyple.calypso.transaction.sam.SamSelectionRequest;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.message.AnswerToReset;
import org.eclipse.keyple.seproxy.message.SeResponse;
import org.eclipse.keyple.seproxy.message.SelectionStatus;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.Test;



public class CalypsoSamTest {
    /** basic CalypsoSam test: nominal ATR parsing */
    @Test
    public void test_CalypsoSam_1() {
        SeSelector seSelector = new SeSelector(null, null, "Dummy SeSelector");
        SamSelectionRequest samSelectionRequest = new SamSelectionRequest(seSelector,
                ChannelState.KEEP_OPEN, ContactlessProtocols.PROTOCOL_ISO14443_4);
        SelectionStatus selectionStatus = new SelectionStatus(
                new AnswerToReset(ByteArrayUtils.fromHex("3B001122805A0180D002030411223344829000")),
                null, true);
        CalypsoSam calypsoSam = new CalypsoSam(new SeResponse(true, true, selectionStatus, null),
                "Dummy SeSlector");
        assertEquals(S1D, calypsoSam.getSamRevision());
        assertEquals((byte) 0x80, calypsoSam.getApplicationType());
        assertEquals((byte) 0xD0, calypsoSam.getApplicationSubType());
        assertEquals((byte) 0x01, calypsoSam.getPlatform());
        assertEquals((byte) 0x02, calypsoSam.getSoftwareIssuer());
        assertEquals((byte) 0x03, calypsoSam.getSoftwareVersion());
        assertEquals((byte) 0x04, calypsoSam.getSoftwareRevision());
        assertEquals("11223344", ByteArrayUtils.toHex(calypsoSam.getSerialNumber()));
    }

    /* S1D D1 */
    @Test
    public void test_CalypsoSam_2() {
        SeSelector seSelector = new SeSelector(null, null, "Dummy SeSelector");
        SamSelectionRequest samSelectionRequest = new SamSelectionRequest(seSelector,
                ChannelState.KEEP_OPEN, ContactlessProtocols.PROTOCOL_ISO14443_4);
        SelectionStatus selectionStatus = new SelectionStatus(
                new AnswerToReset(ByteArrayUtils.fromHex("3B001122805A0180D102030411223344829000")),
                null, true);
        CalypsoSam calypsoSam = new CalypsoSam(new SeResponse(true, true, selectionStatus, null),
                "Dummy SeSlector");
        assertEquals(S1D, calypsoSam.getSamRevision());
        assertEquals((byte) 0xD1, calypsoSam.getApplicationSubType());
    }

    /* S1D D2 */
    @Test
    public void test_CalypsoSam_3() {
        SeSelector seSelector = new SeSelector(null, null, "Dummy SeSelector");
        SamSelectionRequest samSelectionRequest = new SamSelectionRequest(seSelector,
                ChannelState.KEEP_OPEN, ContactlessProtocols.PROTOCOL_ISO14443_4);
        SelectionStatus selectionStatus = new SelectionStatus(
                new AnswerToReset(ByteArrayUtils.fromHex("3B001122805A0180D202030411223344829000")),
                null, true);
        CalypsoSam calypsoSam = new CalypsoSam(new SeResponse(true, true, selectionStatus, null),
                "Dummy SeSlector");
        assertEquals(S1D, calypsoSam.getSamRevision());
        assertEquals((byte) 0xD2, calypsoSam.getApplicationSubType());
    }

    /* C1 */
    @Test
    public void test_CalypsoSam_4() {
        SeSelector seSelector = new SeSelector(null, null, "Dummy SeSelector");
        SamSelectionRequest samSelectionRequest = new SamSelectionRequest(seSelector,
                ChannelState.KEEP_OPEN, ContactlessProtocols.PROTOCOL_ISO14443_4);
        SelectionStatus selectionStatus = new SelectionStatus(
                new AnswerToReset(ByteArrayUtils.fromHex("3B001122805A0180C102030411223344829000")),
                null, true);
        CalypsoSam calypsoSam = new CalypsoSam(new SeResponse(true, true, selectionStatus, null),
                "Dummy SeSlector");
        assertEquals(C1, calypsoSam.getSamRevision());
        assertEquals((byte) 0xC1, calypsoSam.getApplicationSubType());
    }

    /* E1 */
    @Test
    public void test_CalypsoSam_5() {
        SeSelector seSelector = new SeSelector(null, null, "Dummy SeSelector");
        SamSelectionRequest samSelectionRequest = new SamSelectionRequest(seSelector,
                ChannelState.KEEP_OPEN, ContactlessProtocols.PROTOCOL_ISO14443_4);
        SelectionStatus selectionStatus = new SelectionStatus(
                new AnswerToReset(ByteArrayUtils.fromHex("3B001122805A0180E102030411223344829000")),
                null, true);
        CalypsoSam calypsoSam = new CalypsoSam(new SeResponse(true, true, selectionStatus, null),
                "Dummy SeSlector");
        assertEquals(S1E, calypsoSam.getSamRevision());
        assertEquals((byte) 0xE1, calypsoSam.getApplicationSubType());
    }

    /* Unrecognized E2 */
    @Test(expected = IllegalStateException.class)
    public void test_CalypsoSam_6() {
        SeSelector seSelector = new SeSelector(null, null, "Dummy SeSelector");
        SamSelectionRequest samSelectionRequest = new SamSelectionRequest(seSelector,
                ChannelState.KEEP_OPEN, ContactlessProtocols.PROTOCOL_ISO14443_4);
        SelectionStatus selectionStatus = new SelectionStatus(
                new AnswerToReset(ByteArrayUtils.fromHex("3B001122805A0180E202030411223344829000")),
                null, true);
        CalypsoSam calypsoSam = new CalypsoSam(new SeResponse(true, true, selectionStatus, null),
                "Dummy SeSlector");
    }

    /* Bad Calypso SAM ATR (0000 instead of 9000) */
    @Test(expected = IllegalStateException.class)
    public void test_CalypsoSam_7() {
        SeSelector seSelector = new SeSelector(null, null, "Dummy SeSelector");
        SamSelectionRequest samSelectionRequest = new SamSelectionRequest(seSelector,
                ChannelState.KEEP_OPEN, ContactlessProtocols.PROTOCOL_ISO14443_4);
        SelectionStatus selectionStatus = new SelectionStatus(
                new AnswerToReset(ByteArrayUtils.fromHex("3B001122805A0180E202030411223344820000")),
                null, true);
        CalypsoSam calypsoSam = new CalypsoSam(new SeResponse(true, true, selectionStatus, null),
                "Dummy SeSlector");
    }

    /* Bad Calypso SAM ATR (empty array) */
    @Test(expected = IllegalStateException.class)
    public void test_CalypsoSam_8() {
        SeSelector seSelector = new SeSelector(null, null, "Dummy SeSelector");
        SamSelectionRequest samSelectionRequest = new SamSelectionRequest(seSelector,
                ChannelState.KEEP_OPEN, ContactlessProtocols.PROTOCOL_ISO14443_4);
        SelectionStatus selectionStatus =
                new SelectionStatus(new AnswerToReset(ByteArrayUtils.fromHex("")), null, true);
        CalypsoSam calypsoSam = new CalypsoSam(new SeResponse(true, true, selectionStatus, null),
                "Dummy SeSlector");
    }
}
