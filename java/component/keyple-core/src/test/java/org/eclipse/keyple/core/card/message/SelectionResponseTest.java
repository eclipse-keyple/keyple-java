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
package org.eclipse.keyple.core.card.message;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SelectionResponseTest {

  @Test
  public void constructorSuccessfullResponseMatch() {

    SelectionResponse response =
        new SelectionResponse(
            new SelectionStatus(ApduResponseTest.getAAtr(), ApduResponseTest.getAFCI(), true),
            new CardResponse(true, ApduResponseTest.getAListOfAPDUs()));
    Assert.assertNotNull(response);
    Assert.assertArrayEquals(
        ApduResponseTest.getAListOfAPDUs().toArray(),
        response.getCardResponse().getApduResponses().toArray());
    Assert.assertEquals(ApduResponseTest.getAAtr(), response.getSelectionStatus().getAtr());
    Assert.assertEquals(ApduResponseTest.getAFCI(), response.getSelectionStatus().getFci());
    Assert.assertTrue(response.getSelectionStatus().hasMatched());
  }

  @Test
  public void constructorSuccessfullResponseNoMatch() {

    SelectionResponse response =
        new SelectionResponse(
            new SelectionStatus(ApduResponseTest.getAAtr(), ApduResponseTest.getAFCI(), false),
            new CardResponse(true, ApduResponseTest.getAListOfAPDUs()));
    Assert.assertNotNull(response);
    Assert.assertArrayEquals(
        ApduResponseTest.getAListOfAPDUs().toArray(),
        response.getCardResponse().getApduResponses().toArray());
    Assert.assertEquals(ApduResponseTest.getAAtr(), response.getSelectionStatus().getAtr());
    Assert.assertEquals(ApduResponseTest.getAFCI(), response.getSelectionStatus().getFci());
    Assert.assertFalse(response.getSelectionStatus().hasMatched());
  }

  @Test
  public void constructorATRNull() {
    SelectionResponse response =
        new SelectionResponse(
            new SelectionStatus(null, ApduResponseTest.getAFCI(), true),
            new CardResponse(true, ApduResponseTest.getAListOfAPDUs()));
    Assert.assertNotNull(response);
  }

  @Test
  public void constructorFCINull() {
    SelectionResponse response =
        new SelectionResponse(
            new SelectionStatus(ApduResponseTest.getAAtr(), null, true),
            new CardResponse(true, ApduResponseTest.getAListOfAPDUs()));
    Assert.assertNotNull(response);
  }

  /*
   * HELPERS
   */

  public static SelectionStatus getASelectionStatus() {
    return new SelectionStatus(ApduResponseTest.getAAtr(), ApduResponseTest.getAFCI(), true);
  }

  public static CardResponse getACardResponse() {
    return new CardResponse(true, ApduResponseTest.getAListOfAPDUs());
  }
}
