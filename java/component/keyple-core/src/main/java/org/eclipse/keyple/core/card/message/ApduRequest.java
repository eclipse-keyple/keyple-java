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

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import org.eclipse.keyple.core.util.ByteArrayUtil;

/**
 * This POJO wraps a data set related to an ISO-7816 APDU.
 *
 * <ul>
 *   <li>A byte array containing the raw APDU data.
 *   <li>A flag indicating if the APDU is of type 4 (ingoing and outgoing data).
 *   <li>A set of integers corresponding to valid status codes in addition to the standard 9000h
 *       status word.
 * </ul>
 *
 * @since 0.9
 */
public final class ApduRequest implements Serializable {

  private final byte[] bytes;
  private final boolean case4;
  private Set<Integer> successfulStatusCodes;
  private String name;

  /**
   * Constructor.
   *
   * <p><code>bytes</code> contains the APDU command bytes to send to the card.<br>
   * The <code>case4</code> boolean is set to true to indicate that APDU has incoming and outgoing
   * data. It helps to handle cards that present a behaviour not compliant with ISO 7816-3 in
   * contacts mode (not returning the 61XYh status).<br>
   *
   * @param bytes A not empty byte array.
   * @param case4 True if the APDU is in case 4, false if not.
   * @since 0.9
   */
  public ApduRequest(byte[] bytes, boolean case4) {
    this.bytes = bytes;
    this.case4 = case4;
  }

  /**
   * The successfulStatusCodes list indicates which status words in the response should be
   * considered successful even though they are different from 9000h.
   *
   * @param successfulStatusCodes A not empty Set of Integer.
   * @return the object instance.
   * @since 1.0
   */
  public ApduRequest setSuccessfulStatusCodes(Set<Integer> successfulStatusCodes) {
    this.successfulStatusCodes = successfulStatusCodes;
    return this;
  }

  /**
   * Indicates if the APDU is type 4.
   *
   * @return True if the APDU is type 4, false if not.
   * @since 0.9
   */
  public boolean isCase4() {
    return case4;
  }

  /**
   * Name this APDU request.
   *
   * <p>This name String is dedicated to improving the readability of logs and should therefore only
   * be called conditionally (e.g. level &gt;= debug).
   *
   * @param name A not null String.
   * @return the object instance.
   * @since 0.9
   */
  public ApduRequest setName(final String name) {
    this.name = name;
    return this;
  }

  /**
   * Get the list of valid status codes for the request.
   *
   * @return A not null Set of Integer (can be empty).
   * @since 0.9
   */
  public Set<Integer> getSuccessfulStatusCodes() {
    return successfulStatusCodes;
  }

  /**
   * Gets the name of this APDU request if it has been defined (see setName).
   *
   * @return A String (may be null).
   * @since 0.9
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the APDU bytes to send to the card.
   *
   * @return A not null byte array.
   * @since 0.9
   */
  public byte[] getBytes() {
    return this.bytes;
  }

  @Override
  public String toString() {
    StringBuilder string;
    if (name == null) {
      name = "Unnamed";
    }
    string =
        new StringBuilder(
            "ApduRequest: NAME = \"" + name + "\", RAWDATA = " + ByteArrayUtil.toHex(bytes));
    if (isCase4()) {
      string.append(", case4");
    }
    if (successfulStatusCodes != null && !successfulStatusCodes.isEmpty()) {
      string.append(", additional successful status codes = ");
      Iterator<Integer> iterator = successfulStatusCodes.iterator();
      while (iterator.hasNext()) {
        string.append(String.format("%04X", iterator.next()));
        if (iterator.hasNext()) {
          string.append(", ");
        }
      }
    }
    return string.toString();
  }
}
