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
package org.eclipse.keyple.core.selection;

import org.eclipse.keyple.core.reader.message.AnswerToReset;
import org.eclipse.keyple.core.reader.message.ApduResponse;
import org.eclipse.keyple.core.reader.message.CardResponse;

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
 */
public abstract class AbstractSmartCard {
  private final byte[] fciBytes;
  private final byte[] atrBytes;

  /**
   * Constructor.
   *
   * @param selectionResponse the response from the card
   */
  protected AbstractSmartCard(CardResponse selectionResponse) {
    ApduResponse fci = selectionResponse.getSelectionStatus().getFci();
    if (fci != null) {
      this.fciBytes = fci.getBytes();
    } else {
      this.fciBytes = null;
    }
    AnswerToReset atr = selectionResponse.getSelectionStatus().getAtr();
    if (atr != null) {
      this.atrBytes = atr.getBytes();
    } else {
      this.atrBytes = null;
    }
  }

  /** @return true if the matching card has an FCI */
  public boolean hasFci() {
    return fciBytes != null && fciBytes.length > 0;
  }

  /** @return true if the matching card has an ATR */
  public boolean hasAtr() {
    return atrBytes != null && atrBytes.length > 0;
  }

  /**
   * @return the FCI
   * @throws IllegalStateException if no FCI is available (see hasFci)
   */
  public byte[] getFciBytes() {
    if (hasFci()) {
      return fciBytes;
    }
    throw new IllegalStateException("No FCI is available in this AbstractSmartCard");
  }

  /**
   * @return the ATR
   * @throws IllegalStateException if no ATR is available (see hasAtr)
   */
  public byte[] getAtrBytes() {
    if (hasAtr()) {
      return atrBytes;
    }
    throw new IllegalStateException("No ATR is available in this AbstractSmartCard");
  }
}
