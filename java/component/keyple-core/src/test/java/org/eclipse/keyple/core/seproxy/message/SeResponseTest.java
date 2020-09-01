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
package org.eclipse.keyple.core.seproxy.message;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.SignatureDeclareThrowsException"})
@RunWith(MockitoJUnitRunner.class)
public class SeResponseTest {

  @Test
  public void constructorSuccessfullResponseMatch() {

    SeResponse response =
        new SeResponse(
            true,
            true,
            new SelectionStatus(ApduResponseTest.getAAtr(), ApduResponseTest.getAFCI(), true),
            ApduResponseTest.getAListOfAPDUs());
    Assert.assertNotNull(response);
    Assert.assertArrayEquals(
        ApduResponseTest.getAListOfAPDUs().toArray(), response.getApduResponses().toArray());
    Assert.assertTrue(response.wasChannelPreviouslyOpen());
    Assert.assertEquals(ApduResponseTest.getAAtr(), response.getSelectionStatus().getAtr());
    Assert.assertEquals(ApduResponseTest.getAFCI(), response.getSelectionStatus().getFci());
    Assert.assertTrue(response.getSelectionStatus().hasMatched());
  }

  @Test
  public void constructorSuccessfullResponseNoMatch() {

    SeResponse response =
        new SeResponse(
            true,
            true,
            new SelectionStatus(ApduResponseTest.getAAtr(), ApduResponseTest.getAFCI(), false),
            ApduResponseTest.getAListOfAPDUs());
    Assert.assertNotNull(response);
    Assert.assertArrayEquals(
        ApduResponseTest.getAListOfAPDUs().toArray(), response.getApduResponses().toArray());
    Assert.assertTrue(response.wasChannelPreviouslyOpen());
    Assert.assertEquals(ApduResponseTest.getAAtr(), response.getSelectionStatus().getAtr());
    Assert.assertEquals(ApduResponseTest.getAFCI(), response.getSelectionStatus().getFci());
    Assert.assertFalse(response.getSelectionStatus().hasMatched());
  }

  @Test
  public void constructorATRNull() {
    SeResponse response =
        new SeResponse(
            true,
            true,
            new SelectionStatus(null, ApduResponseTest.getAFCI(), true),
            ApduResponseTest.getAListOfAPDUs());
    Assert.assertNotNull(response);
  }

  @Test
  public void constructorFCINull() {
    SeResponse response =
        new SeResponse(
            true,
            true,
            new SelectionStatus(ApduResponseTest.getAAtr(), null, true),
            ApduResponseTest.getAListOfAPDUs());
    Assert.assertNotNull(response);
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructorFCIAndATRNull() {
    SeResponse response =
        new SeResponse(
            true, true, new SelectionStatus(null, null, true), ApduResponseTest.getAListOfAPDUs());
  }

  /*
   * HELPERS
   */

  public static SeResponse getASeResponse() {
    return new SeResponse(
        true,
        true,
        new SelectionStatus(ApduResponseTest.getAAtr(), ApduResponseTest.getAFCI(), true),
        ApduResponseTest.getAListOfAPDUs());
  }
}
