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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SelectionRequestTest {

  // object to test
  SelectionRequest selectionRequest;

  public List<ApduRequest> getApdus() {
    return apdus;
  }

  // attributes
  List<ApduRequest> apdus;
  String cardProtocol;
  Set<Integer> selectionStatusCode;
  CardSelector selector;

  @Before
  public void setUp() {

    apdus = getAapduLists();
    cardProtocol = getAString();
    selectionStatusCode = ApduRequestTest.getASuccessFulStatusCode();
    selector = getSelector(selectionStatusCode);
    selectionRequest = new SelectionRequest(selector, new CardRequest(apdus));
  }

  @Test
  public void testCardRequest() {
    assertNotNull(selectionRequest);
  }

  @Test
  public void getSelector() {
    // test
    assertEquals(
        getSelector(selectionStatusCode).toString(), selectionRequest.getCardSelector().toString());
  }

  @Test
  public void getApduRequests() {
    // test
    selectionRequest = new SelectionRequest(getSelector(null), new CardRequest(apdus));
    assertArrayEquals(
        apdus.toArray(), selectionRequest.getCardRequest().getApduRequests().toArray());
  }

  @Test
  public void getCardProtocol() {
    selectionRequest =
        new SelectionRequest(getSelector(null), new CardRequest(new ArrayList<ApduRequest>()));
    assertEquals(cardProtocol, selectionRequest.getCardSelector().getCardProtocol());
  }

  @Test
  public void getSuccessfulSelectionStatusCodes() {
    selectionRequest =
        new SelectionRequest(
            getSelector(selectionStatusCode), new CardRequest(new ArrayList<ApduRequest>()));
    assertArrayEquals(
        selectionStatusCode.toArray(),
        selectionRequest
            .getCardSelector()
            .getAidSelector()
            .getSuccessfulSelectionStatusCodes()
            .toArray());
  }

  @Test
  public void toStringNull() {
    selectionRequest = new SelectionRequest(null, null);
    assertNotNull(selectionRequest.toString());
  }

  /*
   * Constructors
   */
  @Test
  public void constructor1() {
    selectionRequest = new SelectionRequest(getSelector(null), new CardRequest(apdus));
    assertEquals(getSelector(null).toString(), selectionRequest.getCardSelector().toString());
    assertArrayEquals(
        apdus.toArray(), selectionRequest.getCardRequest().getApduRequests().toArray());
    //
    assertEquals(
        ContactlessCardCommonProtocols.ISO_14443_4.name(),
        selectionRequest.getCardSelector().getCardProtocol());
    assertNull(
        selectionRequest.getCardSelector().getAidSelector().getSuccessfulSelectionStatusCodes());
  }

  @Test
  public void constructor2() {
    selectionRequest = new SelectionRequest(getSelector(null), new CardRequest(apdus));
    assertEquals(getSelector(null).toString(), selectionRequest.getCardSelector().toString());
    assertArrayEquals(
        apdus.toArray(), selectionRequest.getCardRequest().getApduRequests().toArray());
    assertEquals(cardProtocol, selectionRequest.getCardSelector().getCardProtocol());
    //
    assertNull(
        selectionRequest.getCardSelector().getAidSelector().getSuccessfulSelectionStatusCodes());
  }

  @Test
  public void constructor2b() {
    selectionRequest =
        new SelectionRequest(getSelector(selectionStatusCode), new CardRequest(apdus));
    assertEquals(
        getSelector(selectionStatusCode).toString(), selectionRequest.getCardSelector().toString());
    assertArrayEquals(
        apdus.toArray(), selectionRequest.getCardRequest().getApduRequests().toArray());
    assertEquals(
        ContactlessCardCommonProtocols.ISO_14443_4.name(),
        selectionRequest.getCardSelector().getCardProtocol());
    //
    assertArrayEquals(
        selectionStatusCode.toArray(),
        selectionRequest
            .getCardSelector()
            .getAidSelector()
            .getSuccessfulSelectionStatusCodes()
            .toArray());
  }

  @Test
  public void constructor3() {
    selectionRequest =
        new SelectionRequest(getSelector(selectionStatusCode), new CardRequest(apdus));
    assertEquals(
        getSelector(selectionStatusCode).toString(), selectionRequest.getCardSelector().toString());
    assertArrayEquals(
        apdus.toArray(), selectionRequest.getCardRequest().getApduRequests().toArray());
    assertEquals(cardProtocol, selectionRequest.getCardSelector().getCardProtocol());
    assertArrayEquals(
        selectionStatusCode.toArray(),
        selectionRequest
            .getCardSelector()
            .getAidSelector()
            .getSuccessfulSelectionStatusCodes()
            .toArray());
  }

  /*
   * HELPERS FOR OTHERS TESTS SUITE
   */

  public static SelectionRequest getCardRequestSample() {

    List<ApduRequest> apdus = getAapduLists();
    Set<Integer> selectionStatusCode = ApduRequestTest.getASuccessFulStatusCode();
    return new SelectionRequest(getSelector(selectionStatusCode), new CardRequest(apdus));
  }

  static List<ApduRequest> getAapduLists() {
    List<ApduRequest> apdus;
    apdus = new ArrayList<ApduRequest>();
    apdus.add(ApduRequestTest.getApduSample());
    apdus.add(ApduRequestTest.getApduSample());
    return apdus;
  }

  static String getAString() {
    return ContactlessCardCommonProtocols.ISO_14443_4.name();
  }

  static CardSelector getSelector(Set<Integer> selectionStatusCode) {
    /*
     * We can use a fake AID here because it is not fully interpreted, the purpose of this unit
     * test is to verify the proper format of the request.
     */
    CardSelector.AidSelector aidSelector =
        CardSelector.AidSelector.builder().aidToSelect("AABBCCDDEEFF").build();
    if (selectionStatusCode != null) {
      for (int statusCode : selectionStatusCode) {
        aidSelector.addSuccessfulStatusCode(statusCode);
      }
    }
    CardSelector cardSelector =
        CardSelector.builder().cardProtocol(getAString()).aidSelector(aidSelector).build();
    return cardSelector;
  }
}
