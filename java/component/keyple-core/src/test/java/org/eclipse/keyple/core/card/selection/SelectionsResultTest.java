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
package org.eclipse.keyple.core.card.selection;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.Map;
import org.eclipse.keyple.core.card.message.ApduResponse;
import org.eclipse.keyple.core.card.message.CardSelectionResponse;
import org.eclipse.keyple.core.card.message.SelectionStatus;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class SelectionsResultTest {
  private static final String FCI1 =
      "6F238409315449432E49434131A516BF0C13C708 0000000011223344 53070A3C23121410019000";
  private static final String FCI2 =
      "6F238409315449432E49434131A516BF0C13C708 0000000055667788 53070A3C23121410019000";

  @Test
  public void testSelectionsResult() {
    SelectionsResult selectionsResult = new SelectionsResult();
    ApduResponse fci1 = new ApduResponse(ByteArrayUtil.fromHex(FCI1), null);
    SelectionStatus selectionStatus1 = new SelectionStatus(null, fci1, true);
    CardSelectionResponse cardSelectionResponse1 =
        new CardSelectionResponse(selectionStatus1, null);
    ApduResponse fci2 = new ApduResponse(ByteArrayUtil.fromHex(FCI2), null);
    SelectionStatus selectionStatus2 = new SelectionStatus(null, fci2, true);
    CardSelectionResponse cardSelectionResponse2 =
        new CardSelectionResponse(selectionStatus2, null);
    TestSmartCard testSmartCard1 = new TestSmartCard(cardSelectionResponse1);
    TestSmartCard testSmartCard2 = new TestSmartCard(cardSelectionResponse2);
    selectionsResult.addSmartCard(0, testSmartCard1, false);
    selectionsResult.addSmartCard(2, testSmartCard2, true);
    assertThat(selectionsResult.hasActiveSelection()).isTrue();
    AbstractSmartCard activeSmartCard = selectionsResult.getActiveSmartCard();
    assertThat(activeSmartCard).isEqualTo(testSmartCard2);
    assertThat(selectionsResult.getActiveSelectionIndex()).isEqualTo(2);
    assertThat(selectionsResult.getSmartCard(0)).isEqualTo(testSmartCard1);
    assertThat(selectionsResult.getSmartCard(2)).isEqualTo(testSmartCard2);
    assertThat(selectionsResult.hasSelectionMatched(0)).isTrue();
    assertThat(selectionsResult.hasSelectionMatched(1)).isFalse();
    assertThat(selectionsResult.hasSelectionMatched(2)).isTrue();
    Map<Integer, AbstractSmartCard> matchingSelections = selectionsResult.getSmartCards();
    assertThat(matchingSelections.get(0)).isEqualTo(testSmartCard1);
    assertThat(matchingSelections.get(1)).isEqualTo(null);
    assertThat(matchingSelections.get(2)).isEqualTo(testSmartCard2);
  }

  private static class TestSmartCard extends AbstractSmartCard {
    protected TestSmartCard(CardSelectionResponse cardSelectionResponse) {
      super(cardSelectionResponse);
    }
  }
}
