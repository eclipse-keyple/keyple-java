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
package org.eclipse.keyple.core.card.selection;

import org.eclipse.keyple.core.card.message.AnswerToReset;
import org.eclipse.keyple.core.card.message.ApduResponse;
import org.eclipse.keyple.core.card.message.CardSelectionResponse;

/**
 * AbstractSmartCard is the class to manage the elements of the result of a selection.
 *
 * <p>This class should be extended for the management of specific card.<br>
 * Nevertheless it gives access to the generic parameters common to all cards which are:
 *
 * <ul>
 *   <li>the FCI (response to select command)
 *   <li>the ATR (card's answer to reset)
 * </ul>
 *
 * when they are available.
 *
 * @since 0.9
 */
public abstract class AbstractSmartCard {
  private final byte[] fciBytes;
  private final byte[] atrBytes;

  /**
   * Constructor.
   *
   * @param cardSelectionResponse the response from the card
   * @since 0.9
   */
  protected AbstractSmartCard(CardSelectionResponse cardSelectionResponse) {
    ApduResponse fci = cardSelectionResponse.getSelectionStatus().getFci();
    if (fci != null) {
      this.fciBytes = fci.getBytes();
    } else {
      this.fciBytes = null;
    }
    AnswerToReset atr = cardSelectionResponse.getSelectionStatus().getAtr();
    if (atr != null) {
      this.atrBytes = atr.getBytes();
    } else {
      this.atrBytes = null;
    }
  }

  /**
   * Tells if the card provided a FCI
   *
   * @return true if the card has an FCI
   * @since 0.9
   */
  public boolean hasFci() {
    return fciBytes != null && fciBytes.length > 0;
  }

  /**
   * Tells if the card provided an ATR
   *
   * @return true if the card has an ATR
   * @since 0.9
   */
  public boolean hasAtr() {
    return atrBytes != null && atrBytes.length > 0;
  }

  /**
   * Gets the FCI
   *
   * @return the FCI as a not null byte array
   * @throws IllegalStateException if no FCI is available (see hasFci)
   * @since 0.9
   */
  public byte[] getFciBytes() {
    if (hasFci()) {
      return fciBytes;
    }
    throw new IllegalStateException("No FCI is available in this AbstractSmartCard");
  }

  /**
   * Gets the ATR
   *
   * @return the ATR as a not null byte array
   * @throws IllegalStateException if no ATR is available (see hasAtr)
   * @since 0.9
   */
  public byte[] getAtrBytes() {
    if (hasAtr()) {
      return atrBytes;
    }
    throw new IllegalStateException("No ATR is available in this AbstractSmartCard");
  }
}
