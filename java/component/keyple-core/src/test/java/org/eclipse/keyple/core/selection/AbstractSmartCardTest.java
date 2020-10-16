/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.selection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.ArrayList;
import org.eclipse.keyple.core.reader.message.AnswerToReset;
import org.eclipse.keyple.core.reader.message.ApduResponse;
import org.eclipse.keyple.core.reader.message.CardResponse;
import org.eclipse.keyple.core.reader.message.SelectionStatus;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class AbstractSmartCardTest {
  private static final String FCI_REV31 =
      "6F238409315449432E49434131A516BF0C13C708 0000000011223344 53070A3C23121410019000";
  private static final String ATR1 = "3B3F9600805A0080C120000012345678829000";

  @Test
  public void testHasAtr_true_HasFci_false_getAtrBytes() {
    TestSmartCard testSmartCard;
    AnswerToReset answerToReset = new AnswerToReset(ByteArrayUtil.fromHex(ATR1));
    SelectionStatus selectionStatus = new SelectionStatus(answerToReset, null, true);
    CardResponse cardResponse =
        new CardResponse(true, false, selectionStatus, new ArrayList<ApduResponse>());
    testSmartCard = new TestSmartCard(cardResponse);
    assertThat(testSmartCard.hasAtr()).isTrue();
    assertThat(testSmartCard.hasFci()).isFalse();
    assertThat(testSmartCard.getAtrBytes()).isEqualTo(ByteArrayUtil.fromHex(ATR1));
    try {
      // should raise an IllegalStateException
      testSmartCard.getFciBytes();
    } catch (IllegalStateException e) {
      return;
    }
    fail("Unexpected behaviour");
  }

  @Test
  public void testHasAtr_false_HasFci_true_getFciBytes() {
    TestSmartCard testSmartCard;
    ApduResponse fci = new ApduResponse(ByteArrayUtil.fromHex(FCI_REV31), null);
    SelectionStatus selectionStatus = new SelectionStatus(null, fci, true);
    CardResponse cardResponse =
        new CardResponse(true, false, selectionStatus, new ArrayList<ApduResponse>());
    testSmartCard = new TestSmartCard(cardResponse);
    assertThat(testSmartCard.hasAtr()).isFalse();
    assertThat(testSmartCard.hasFci()).isTrue();
    assertThat(testSmartCard.getFciBytes()).isEqualTo(ByteArrayUtil.fromHex(FCI_REV31));
    try {
      // should raise an IllegalStateException
      testSmartCard.getAtrBytes();
    } catch (IllegalStateException e) {
      return;
    }
    fail("Unexpected behaviour");
  }

  @Test
  public void testHasAtr_true_HasFci_true_getAtrBytes_getFciBytes() {
    TestSmartCard testSmartCard;
    AnswerToReset answerToReset = new AnswerToReset(ByteArrayUtil.fromHex(ATR1));
    ApduResponse fci = new ApduResponse(ByteArrayUtil.fromHex(FCI_REV31), null);
    SelectionStatus selectionStatus = new SelectionStatus(answerToReset, fci, true);
    CardResponse cardResponse =
        new CardResponse(true, false, selectionStatus, new ArrayList<ApduResponse>());
    testSmartCard = new TestSmartCard(cardResponse);
    assertThat(testSmartCard.hasAtr()).isTrue();
    assertThat(testSmartCard.hasFci()).isTrue();
    assertThat(testSmartCard.getAtrBytes()).isEqualTo(ByteArrayUtil.fromHex(ATR1));
    assertThat(testSmartCard.getFciBytes()).isEqualTo(ByteArrayUtil.fromHex(FCI_REV31));
  }

  private static class TestSmartCard extends AbstractSmartCard {
    protected TestSmartCard(CardResponse selectionResponse) {
      super(selectionResponse);
    }
  }
}
