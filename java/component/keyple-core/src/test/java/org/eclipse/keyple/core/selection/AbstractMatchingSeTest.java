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
package org.eclipse.keyple.core.selection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.eclipse.keyple.core.seproxy.message.AnswerToReset;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.message.SelectionStatus;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class AbstractMatchingSeTest {
    private static final String FCI_REV31 =
            "6F238409315449432E49434131A516BF0C13C708 0000000011223344 53070A3C23121410019000";
    private static final String ATR1 = "3B3F9600805A0080C120000012345678829000";

    @Test
    public void testGetTransmissionMode() {
        TestMatchingSe testMatchingSe;
        ApduResponse fci = new ApduResponse(ByteArrayUtil.fromHex(FCI_REV31), null);
        SelectionStatus selectionStatus = new SelectionStatus(null, fci, true);
        SeResponse seResponse = new SeResponse(true, false, selectionStatus, null);
        testMatchingSe = new TestMatchingSe(seResponse, TransmissionMode.CONTACTLESS);
        assertThat(testMatchingSe.getTransmissionMode()).isEqualTo(TransmissionMode.CONTACTLESS);
        testMatchingSe = new TestMatchingSe(seResponse, TransmissionMode.CONTACTS);
        assertThat(testMatchingSe.getTransmissionMode()).isEqualTo(TransmissionMode.CONTACTS);
    }

    @Test
    public void testHasAtr_true_HasFci_false_getAtrBytes() {
        TestMatchingSe testMatchingSe;
        AnswerToReset answerToReset = new AnswerToReset(ByteArrayUtil.fromHex(ATR1));
        SelectionStatus selectionStatus = new SelectionStatus(answerToReset, null, true);
        SeResponse seResponse = new SeResponse(true, false, selectionStatus, null);
        testMatchingSe = new TestMatchingSe(seResponse, TransmissionMode.CONTACTLESS);
        assertThat(testMatchingSe.hasAtr()).isTrue();
        assertThat(testMatchingSe.hasFci()).isFalse();
        assertThat(testMatchingSe.getAtrBytes()).isEqualTo(ByteArrayUtil.fromHex(ATR1));
        try {
            // should raise an IllegalStateException
            testMatchingSe.getFciBytes();
        } catch (IllegalStateException e) {
            return;
        }
        fail("Unexpected behaviour");
    }

    @Test
    public void testHasAtr_false_HasFci_true_getFciBytes() {
        TestMatchingSe testMatchingSe;
        ApduResponse fci = new ApduResponse(ByteArrayUtil.fromHex(FCI_REV31), null);
        SelectionStatus selectionStatus = new SelectionStatus(null, fci, true);
        SeResponse seResponse = new SeResponse(true, false, selectionStatus, null);
        testMatchingSe = new TestMatchingSe(seResponse, TransmissionMode.CONTACTLESS);
        assertThat(testMatchingSe.hasAtr()).isFalse();
        assertThat(testMatchingSe.hasFci()).isTrue();
        assertThat(testMatchingSe.getFciBytes()).isEqualTo(ByteArrayUtil.fromHex(FCI_REV31));
        try {
            // should raise an IllegalStateException
            testMatchingSe.getAtrBytes();
        } catch (IllegalStateException e) {
            return;
        }
        fail("Unexpected behaviour");
    }

    @Test
    public void testHasAtr_true_HasFci_true_getAtrBytes_getFciBytes() {
        TestMatchingSe testMatchingSe;
        AnswerToReset answerToReset = new AnswerToReset(ByteArrayUtil.fromHex(ATR1));
        ApduResponse fci = new ApduResponse(ByteArrayUtil.fromHex(FCI_REV31), null);
        SelectionStatus selectionStatus = new SelectionStatus(answerToReset, fci, true);
        SeResponse seResponse = new SeResponse(true, false, selectionStatus, null);
        testMatchingSe = new TestMatchingSe(seResponse, TransmissionMode.CONTACTLESS);
        assertThat(testMatchingSe.hasAtr()).isTrue();
        assertThat(testMatchingSe.hasFci()).isTrue();
        assertThat(testMatchingSe.getAtrBytes()).isEqualTo(ByteArrayUtil.fromHex(ATR1));
        assertThat(testMatchingSe.getFciBytes()).isEqualTo(ByteArrayUtil.fromHex(FCI_REV31));
    }

    private static class TestMatchingSe extends AbstractMatchingSe {
        protected TestMatchingSe(SeResponse selectionResponse, TransmissionMode transmissionMode) {
            super(selectionResponse, transmissionMode);
        }
    }
}
