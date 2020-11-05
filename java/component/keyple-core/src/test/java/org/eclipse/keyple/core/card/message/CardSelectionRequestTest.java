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
public class CardSelectionRequestTest {

  // object to test
  CardSelectionRequest cardSelectionRequest;

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
    cardSelectionRequest = new CardSelectionRequest(selector, new CardRequest(apdus));
  }

  @Test
  public void testCardRequest() {
    assertNotNull(cardSelectionRequest);
  }

  @Test
  public void getSelector() {
    // test
    assertEquals(
        getSelector(selectionStatusCode).toString(),
        cardSelectionRequest.getCardSelector().toString());
  }

  @Test
  public void getApduRequests() {
    // test
    cardSelectionRequest = new CardSelectionRequest(getSelector(null), new CardRequest(apdus));
    assertArrayEquals(
        apdus.toArray(), cardSelectionRequest.getCardRequest().getApduRequests().toArray());
  }

  @Test
  public void getCardProtocol() {
    cardSelectionRequest =
        new CardSelectionRequest(getSelector(null), new CardRequest(new ArrayList<ApduRequest>()));
    assertEquals(cardProtocol, cardSelectionRequest.getCardSelector().getCardProtocol());
  }

  @Test
  public void getSuccessfulSelectionStatusCodes() {
    cardSelectionRequest =
        new CardSelectionRequest(
            getSelector(selectionStatusCode), new CardRequest(new ArrayList<ApduRequest>()));
    assertArrayEquals(
        selectionStatusCode.toArray(),
        cardSelectionRequest
            .getCardSelector()
            .getAidSelector()
            .getSuccessfulSelectionStatusCodes()
            .toArray());
  }

  @Test
  public void toStringNull() {
    cardSelectionRequest = new CardSelectionRequest(null, null);
    assertNotNull(cardSelectionRequest.toString());
  }

  /*
   * Constructors
   */
  @Test
  public void constructor1() {
    cardSelectionRequest = new CardSelectionRequest(getSelector(null), new CardRequest(apdus));
    assertEquals(getSelector(null).toString(), cardSelectionRequest.getCardSelector().toString());
    assertArrayEquals(
        apdus.toArray(), cardSelectionRequest.getCardRequest().getApduRequests().toArray());
    //
    assertEquals(
        ContactlessCardCommonProtocols.ISO_14443_4.name(),
        cardSelectionRequest.getCardSelector().getCardProtocol());
    assertNull(
        cardSelectionRequest
            .getCardSelector()
            .getAidSelector()
            .getSuccessfulSelectionStatusCodes());
  }

  @Test
  public void constructor2() {
    cardSelectionRequest = new CardSelectionRequest(getSelector(null), new CardRequest(apdus));
    assertEquals(getSelector(null).toString(), cardSelectionRequest.getCardSelector().toString());
    assertArrayEquals(
        apdus.toArray(), cardSelectionRequest.getCardRequest().getApduRequests().toArray());
    assertEquals(cardProtocol, cardSelectionRequest.getCardSelector().getCardProtocol());
    //
    assertNull(
        cardSelectionRequest
            .getCardSelector()
            .getAidSelector()
            .getSuccessfulSelectionStatusCodes());
  }

  @Test
  public void constructor2b() {
    cardSelectionRequest =
        new CardSelectionRequest(getSelector(selectionStatusCode), new CardRequest(apdus));
    assertEquals(
        getSelector(selectionStatusCode).toString(),
        cardSelectionRequest.getCardSelector().toString());
    assertArrayEquals(
        apdus.toArray(), cardSelectionRequest.getCardRequest().getApduRequests().toArray());
    assertEquals(
        ContactlessCardCommonProtocols.ISO_14443_4.name(),
        cardSelectionRequest.getCardSelector().getCardProtocol());
    //
    assertArrayEquals(
        selectionStatusCode.toArray(),
        cardSelectionRequest
            .getCardSelector()
            .getAidSelector()
            .getSuccessfulSelectionStatusCodes()
            .toArray());
  }

  @Test
  public void constructor3() {
    cardSelectionRequest =
        new CardSelectionRequest(getSelector(selectionStatusCode), new CardRequest(apdus));
    assertEquals(
        getSelector(selectionStatusCode).toString(),
        cardSelectionRequest.getCardSelector().toString());
    assertArrayEquals(
        apdus.toArray(), cardSelectionRequest.getCardRequest().getApduRequests().toArray());
    assertEquals(cardProtocol, cardSelectionRequest.getCardSelector().getCardProtocol());
    assertArrayEquals(
        selectionStatusCode.toArray(),
        cardSelectionRequest
            .getCardSelector()
            .getAidSelector()
            .getSuccessfulSelectionStatusCodes()
            .toArray());
  }

  /*
   * HELPERS FOR OTHERS TESTS SUITE
   */

  public static CardSelectionRequest getCardRequestSample() {

    List<ApduRequest> apdus = getAapduLists();
    Set<Integer> selectionStatusCode = ApduRequestTest.getASuccessFulStatusCode();
    return new CardSelectionRequest(getSelector(selectionStatusCode), new CardRequest(apdus));
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
