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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@RunWith(MockitoJUnitRunner.class)
public class ApduResponseTest {

  @Before
  public void setUp() {}

  @Test
  public void constructorSuccessFullResponse() {
    ApduResponse response = new ApduResponse(ByteArrayUtil.fromHex("FEDCBA98 9000h"), null);
    assertNotNull(response);
    assertEquals(0x9000, response.getStatusCode());
    assertEquals("FEDCBA989000", ByteArrayUtil.toHex(response.getBytes()));
    assertArrayEquals(ByteArrayUtil.fromHex("FEDCBA98"), response.getDataOut());
    assertTrue(response.isSuccessful());
  }

  @Test
  public void constructorSuccessFullResponseWithCustomCode() {
    ApduResponse response =
        new ApduResponse(ByteArrayUtil.fromHex("FEDCBA98 9005h"), getA9005CustomCode());
    assertNotNull(response);
    assertEquals(0x9005, response.getStatusCode());
    assertEquals("FEDCBA989005", ByteArrayUtil.toHex(response.getBytes()));
    assertArrayEquals(ByteArrayUtil.fromHex("FEDCBA98"), response.getDataOut());
    assertTrue(response.isSuccessful());
  }

  @Test
  public void constructorFailResponse() {
    ApduResponse response = new ApduResponse(ByteArrayUtil.fromHex("FEDCBA98 9004h"), null);
    assertNotNull(response);
    assertEquals("FEDCBA989004", ByteArrayUtil.toHex(response.getBytes()));
    assertArrayEquals(ByteArrayUtil.fromHex("FEDCBA98"), response.getDataOut());
    assertEquals(0x9004, response.getStatusCode());
    assertFalse(response.isSuccessful());
  }

  @Test
  public void constructorFailResponseWithCustomCode() {
    ApduResponse response =
        new ApduResponse(ByteArrayUtil.fromHex("FEDCBA98 9004h"), getA9005CustomCode());
    assertNotNull(response);
    assertEquals("FEDCBA989004", ByteArrayUtil.toHex(response.getBytes()));
    assertArrayEquals(ByteArrayUtil.fromHex("FEDCBA98"), response.getDataOut());
    assertEquals(0x9004, response.getStatusCode());
    assertFalse(response.isSuccessful());
  }

  /*
   * HELPERS
   */

  public static Set<Integer> getA9005CustomCode() {
    Set<Integer> successfulStatusCodes = new HashSet<Integer>();
    successfulStatusCodes.add(0x9005);
    return successfulStatusCodes;
  }

  static AnswerToReset getAAtr() {
    return new AnswerToReset(ByteArrayUtil.fromHex("3B8F8001804F0CA000000306030001000000006A"));
  }

  static ApduResponse getAFCI() {
    return new ApduResponse(ByteArrayUtil.fromHex("9000"), null);
  }

  static ApduResponse getSuccessfulResponse() {
    return new ApduResponse(ByteArrayUtil.fromHex("FEDCBA98 9000h"), null);
  }

  public static List<ApduResponse> getAListOfAPDUs() {
    List<ApduResponse> apdus = new ArrayList<ApduResponse>();
    apdus.add(getSuccessfulResponse());
    return apdus;
  }
}
