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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.eclipse.keyple.core.seproxy.CardSelector;
import org.eclipse.keyple.core.seproxy.util.ContactlessCardCommonProtocols;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@SuppressWarnings("PMD.SignatureDeclareThrowsException")
@RunWith(MockitoJUnitRunner.class)
public class CardRequestTest {

  // object to test
  CardRequest cardRequest;

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
    cardRequest = new CardRequest(selector, apdus);
  }

  @Test
  public void testCardRequest() {
    assertNotNull(cardRequest);
  }

  @Test
  public void getSelector() {
    // test
    assertEquals(
        getSelector(selectionStatusCode).toString(), cardRequest.getCardSelector().toString());
  }

  @Test
  public void getApduRequests() {
    // test
    cardRequest = new CardRequest(getSelector(null), apdus);
    assertArrayEquals(apdus.toArray(), cardRequest.getApduRequests().toArray());
  }

  @Test
  public void getCardProtocol() {
    cardRequest = new CardRequest(getSelector(null), new ArrayList<ApduRequest>());
    assertEquals(cardProtocol, cardRequest.getCardSelector().getCardProtocol());
  }

  @Test
  public void getSuccessfulSelectionStatusCodes() {
    cardRequest = new CardRequest(getSelector(selectionStatusCode), new ArrayList<ApduRequest>());
    assertArrayEquals(
        selectionStatusCode.toArray(),
        cardRequest
            .getCardSelector()
            .getAidSelector()
            .getSuccessfulSelectionStatusCodes()
            .toArray());
  }

  @Test
  public void toStringNull() {
    cardRequest = new CardRequest(null, null);
    assertNotNull(cardRequest.toString());
  }

  /*
   * Constructors
   */
  @Test
  public void constructor1() {
    cardRequest = new CardRequest(getSelector(null), apdus);
    assertEquals(getSelector(null).toString(), cardRequest.getCardSelector().toString());
    assertArrayEquals(apdus.toArray(), cardRequest.getApduRequests().toArray());
    //
    assertEquals(
        ContactlessCardCommonProtocols.ISO_14443_4.name(),
        cardRequest.getCardSelector().getCardProtocol());
    assertNull(cardRequest.getCardSelector().getAidSelector().getSuccessfulSelectionStatusCodes());
  }

  @Test
  public void constructor2() {
    cardRequest = new CardRequest(getSelector(null), apdus);
    assertEquals(getSelector(null).toString(), cardRequest.getCardSelector().toString());
    assertArrayEquals(apdus.toArray(), cardRequest.getApduRequests().toArray());
    assertEquals(cardProtocol, cardRequest.getCardSelector().getCardProtocol());
    //
    assertNull(cardRequest.getCardSelector().getAidSelector().getSuccessfulSelectionStatusCodes());
  }

  @Test
  public void constructor2b() {
    cardRequest = new CardRequest(getSelector(selectionStatusCode), apdus);
    assertEquals(
        getSelector(selectionStatusCode).toString(), cardRequest.getCardSelector().toString());
    assertArrayEquals(apdus.toArray(), cardRequest.getApduRequests().toArray());
    assertEquals(
        ContactlessCardCommonProtocols.ISO_14443_4.name(),
        cardRequest.getCardSelector().getCardProtocol());
    //
    assertArrayEquals(
        selectionStatusCode.toArray(),
        cardRequest
            .getCardSelector()
            .getAidSelector()
            .getSuccessfulSelectionStatusCodes()
            .toArray());
  }

  @Test
  public void constructor3() {
    cardRequest = new CardRequest(getSelector(selectionStatusCode), apdus);
    assertEquals(
        getSelector(selectionStatusCode).toString(), cardRequest.getCardSelector().toString());
    assertArrayEquals(apdus.toArray(), cardRequest.getApduRequests().toArray());
    assertEquals(cardProtocol, cardRequest.getCardSelector().getCardProtocol());
    assertArrayEquals(
        selectionStatusCode.toArray(),
        cardRequest
            .getCardSelector()
            .getAidSelector()
            .getSuccessfulSelectionStatusCodes()
            .toArray());
  }

  /*
   * HELPERS FOR OTHERS TESTS SUITE
   */

  public static CardRequest getCardRequestSample() {

    List<ApduRequest> apdus = getAapduLists();
    Set<Integer> selectionStatusCode = ApduRequestTest.getASuccessFulStatusCode();
    return new CardRequest(getSelector(selectionStatusCode), apdus);
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
