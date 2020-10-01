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

import java.io.Serializable;
import java.util.Arrays;

/**
 * This POJO carries the data retrieved from the card after the power on sequence.
 *
 * <p>In the case of an ISO-7816 card (with contacts), these data are the ATR (Answer To Reset)
 * bytes as defined by the standard.<br>
 * However, in the case of another type of card, this data may be specific to the card reader (e.g.
 * reconstructed ATR for a PC/SC reader, low-level protocol information for other card readers,
 * etc).
 *
 * <p>(May be enhanced to provide analysis methods)
 *
 * @since 0.9
 */
public class AnswerToReset implements Serializable {
  private final byte[] atrBytes;

  /**
   * Build from a byte array containing the power on data.
   *
   * @param atrBytes A not null byte array.
   */
  public AnswerToReset(byte[] atrBytes) {
    this.atrBytes = atrBytes;
  }

  /**
   * Gets the data
   *
   * @return A not null byte array.
   */
  public byte[] getBytes() {
    return atrBytes;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof AnswerToReset)) {
      return false;
    }

    AnswerToReset atr = (AnswerToReset) o;
    return Arrays.equals(atr.getBytes(), this.atrBytes);
  }

  @Override
  public int hashCode() {
    int hash = 17;
    hash = 19 * hash + (atrBytes == null ? 0 : Arrays.hashCode(atrBytes));
    return hash;
  }
}
