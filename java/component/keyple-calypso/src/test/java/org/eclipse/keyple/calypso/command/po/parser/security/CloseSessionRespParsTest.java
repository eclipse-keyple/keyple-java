/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.calypso.command.po.parser.security;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.core.card.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.card.message.ApduResponse;
import org.eclipse.keyple.core.card.message.CardResponse;
import org.eclipse.keyple.core.card.message.SelectionStatus;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CloseSessionRespParsTest {

  @Test
  public void closeSessionRespPars() {
    byte[] response = {0x4D, (byte) 0xBD, (byte) 0xC9, 0x60, (byte) 0x90, 0x00};
    List<ApduResponse> responses = new ArrayList<ApduResponse>();
    ApduResponse apduResponse = new ApduResponse(response, null);
    responses.add(apduResponse);
    CardResponse cardResponse =
        new CardResponse(
            true,
            true,
            new SelectionStatus(null, new ApduResponse(ByteArrayUtil.fromHex("9000"), null), true),
            responses);

    AbstractApduResponseParser apduResponseParser =
        new CloseSessionRespPars(cardResponse.getApduResponses().get(0), null);
    Assert.assertArrayEquals(response, apduResponseParser.getApduResponse().getBytes());
  }

  @Test
  public void TestToPOHalfSessionSignature() {

    byte[] apduResponse =
        new byte[] {(byte) 0x4D, (byte) 0xBD, (byte) 0xC9, 0x60, (byte) 0x90, 0x00};
    byte[] apduResponseCaseTwo =
        new byte[] {
          (byte) 0xA8,
          0x31,
          (byte) 0xC3,
          0x3E,
          (byte) 0xA7,
          0x21,
          (byte) 0xC2,
          0x2E,
          (byte) 0x90,
          0x00
        };
    byte[] apduResponseCaseThree = new byte[] {(byte) 0xA8, 0x31, (byte) 0xC3, (byte) 0x90, 0x00};

    byte[] sessionSignature = new byte[] {(byte) 0x4D, (byte) 0xBD, (byte) 0xC9, 0x60};
    byte[] sessionSignatureCaseTwo = new byte[] {(byte) 0xA7, 0x21, (byte) 0xC2, 0x2E};

    { // Case Length = 4
      CloseSessionRespPars pars =
          new CloseSessionRespPars(new ApduResponse(apduResponse, null), null);
      Assert.assertArrayEquals(sessionSignature, pars.getSignatureLo());
    }

    { // Case Length = 8
      CloseSessionRespPars pars =
          new CloseSessionRespPars(new ApduResponse(apduResponseCaseTwo, null), null);
      Assert.assertArrayEquals(sessionSignatureCaseTwo, pars.getSignatureLo());
    }

    { // Case Other
      try {
        CloseSessionRespPars pars =
            new CloseSessionRespPars(new ApduResponse(apduResponseCaseThree, null), null);
        Assert.fail();
      } catch (IllegalArgumentException ex) {
        /* expected case */
      }
    }
  }

  @Test
  public void existingTestConverted() {
    CloseSessionRespPars parser =
        new CloseSessionRespPars(new ApduResponse(ByteArrayUtil.fromHex("9000h"), null), null);
    // This assert wasn't passing
    Assert.assertEquals("", ByteArrayUtil.toHex(parser.getSignatureLo()));
    Assert.assertEquals("", ByteArrayUtil.toHex(parser.getPostponedData()));
  }

  @Test // Calypso / page 105 / Example command aborting a session:
  public void abortingASession() {
    CloseSessionRespPars parser =
        new CloseSessionRespPars(
            new ApduResponse(ByteArrayUtil.fromHex("FEDCBA98 9000h"), null), null);
  }

  @Test // Calypso / page 105 / Example command, Lc=4, without postponed data:
  public void lc4withoutPostponedData() {
    CloseSessionRespPars parser =
        new CloseSessionRespPars(
            new ApduResponse(ByteArrayUtil.fromHex("FEDCBA98 9000h"), null), null);
    Assert.assertEquals("FEDCBA98", ByteArrayUtil.toHex(parser.getSignatureLo()));
    Assert.assertEquals("", ByteArrayUtil.toHex(parser.getPostponedData()));
  }

  @Test // Calypso / page 105 / Example command, Lc=4, with postponed data:
  public void lc4WithPostponedData() {
    CloseSessionRespPars parser =
        new CloseSessionRespPars(
            new ApduResponse(ByteArrayUtil.fromHex("04 345678 FEDCBA98 9000h"), null), null);
    Assert.assertEquals("FEDCBA98", ByteArrayUtil.toHex(parser.getSignatureLo()));
    Assert.assertEquals("345678", ByteArrayUtil.toHex(parser.getPostponedData()));
  }
}
