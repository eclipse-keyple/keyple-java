/* **************************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.calypso.transaction;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamCommandException;
import org.eclipse.keyple.core.seproxy.message.AnswerToReset;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.message.SelectionStatus;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class SamSelectionRequestTest {
  private static final String REVISION = "C1";
  private static final String SN_STR = "11223344";
  private static final String ATR_STR =
      "3B3F9600805A4880" + REVISION + "205017" + SN_STR + "829000";
  private static final String UNLOCK_DATA_STR = "00112233445566778899AABBCCDDEEFF";
  private static final byte[] SW1SW2_OK = ByteArrayUtil.fromHex("9000");
  private static final byte[] SW1SW2_CONDITIONS_NOT_SATISFIED = ByteArrayUtil.fromHex("6985");
  private static final byte[] SN = ByteArrayUtil.fromHex(SN_STR);
  private static final byte[] ATR = ByteArrayUtil.fromHex(ATR_STR);
  private static final byte[] UNLOCK_DATA = ByteArrayUtil.fromHex(UNLOCK_DATA_STR);
  private final ApduResponse UNLOCK_APDU_RESPONSE_OK = new ApduResponse(SW1SW2_OK, null);
  private final ApduResponse UNLOCK_APDU_RESPONSE_KO =
      new ApduResponse(SW1SW2_CONDITIONS_NOT_SATISFIED, null);

  @Test
  public void samSelectionRequest_parse() {
    SamSelector samSelector =
        SamSelector.builder() //
            .samRevision(SamRevision.AUTO) //
            .build();
    SamSelectionRequest samSelectionRequest = new SamSelectionRequest(samSelector);
    SelectionStatus selectionStatus = new SelectionStatus(new AnswerToReset(ATR), null, true);
    CalypsoSam calypsoSam =
        samSelectionRequest.parse(new SeResponse(true, true, selectionStatus, null));
    // minimal checks on the CalypsoSam result
    assertThat(calypsoSam.getSamRevision()).isEqualTo(SamRevision.C1);
    assertThat(calypsoSam.getSerialNumber()).isEqualTo(SN);
  }

  @Test
  public void samSelectionRequest_unlock_ok() {
    SamSelector samSelector =
        SamSelector.builder() //
            .samRevision(SamRevision.AUTO) //
            .unlockData(UNLOCK_DATA)
            .build();
    SamSelectionRequest samSelectionRequest = new SamSelectionRequest(samSelector);
    SelectionStatus selectionStatus = new SelectionStatus(new AnswerToReset(ATR), null, true);
    List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
    apduResponses.add(UNLOCK_APDU_RESPONSE_OK);
    CalypsoSam calypsoSam =
        samSelectionRequest.parse(new SeResponse(true, true, selectionStatus, apduResponses));
    // minimal checks on the CalypsoSam result
    assertThat(calypsoSam.getSamRevision()).isEqualTo(SamRevision.C1);
    assertThat(calypsoSam.getSerialNumber()).isEqualTo(SN);
  }

  @Test(expected = CalypsoSamCommandException.class)
  public void samSelectionRequest_unlock_ko() {
    SamSelector samSelector =
        SamSelector.builder() //
            .samRevision(SamRevision.AUTO) //
            .unlockData(UNLOCK_DATA)
            .build();
    SamSelectionRequest samSelectionRequest = new SamSelectionRequest(samSelector);
    SelectionStatus selectionStatus = new SelectionStatus(new AnswerToReset(ATR), null, true);
    List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
    apduResponses.add(UNLOCK_APDU_RESPONSE_KO);
    CalypsoSam calypsoSam =
        samSelectionRequest.parse(new SeResponse(true, true, selectionStatus, apduResponses));
  }
}
