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

/**
 * This POJO is used to transport the data related to the outcome of the card selection process.
 * These data are:
 *
 * <ul>
 *   <li>the status of the selection indicating if the card has matched it.
 *   <li>the card's Answer To Reset (ATR) if present.
 *   <li>the File Control Information (FCI) obtained in response to the SELECT command if present.
 * </ul>
 *
 * Note: when Answer To Reset or FCI are not present they are returned with a null value.
 *
 * @since 0.9
 */
public class SelectionStatus implements Serializable {
  private final AnswerToReset atr;
  private final ApduResponse fci;
  private final boolean isMatching;

  /**
   * Create a Selection Status with the 3 required elements.
   *
   * <ul>
   *   <li><code>atr</code> contains the ATR data from the card.<br>
   *   <li><code>fci</code> contains the APDU response to the SELECT command sent to the card during
   *       the selection process. The data part of this {@link ApduResponse} is the FCI<br>
   *   <li><code>isMatching</code> is True if the selection was successful, false otherwise.
   * </ul>
   *
   * Note: ATR and FCI are optional but cannot both be absent when the selection status is
   * successful.
   *
   * @param atr A not null {@link AnswerToReset} reference.
   * @param fci A nullable {@link ApduResponse} reference.
   * @param isMatching A boolean.
   * @since 0.9
   */
  public SelectionStatus(AnswerToReset atr, ApduResponse fci, boolean isMatching) {

    this.atr = atr;
    this.fci = fci;
    this.isMatching = isMatching;
  }

  /**
   * Gets the ATR coming from the card.
   *
   * <p>The ATR may not be available with certain types of readers, in this case this method returns
   * null.
   *
   * @return A nullable {@link AnswerToReset}.
   * @since 0.9
   */
  public AnswerToReset getAtr() {
    return atr;
  }

  /**
   * Gets the {@link ApduResponse} from the card to the <b>Selection Application</b> command.
   *
   * <p>The FCI is not available if the card has not been selected with an AID, in this case this
   * method returns null.
   *
   * @return A nullable {@link ApduResponse}.
   * @since 0.9
   */
  public ApduResponse getFci() {
    return fci;
  }

  /**
   * Tells if the selection process has been successful.
   *
   * @return True or False.
   * @since 0.9
   */
  public boolean hasMatched() {
    return isMatching;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SelectionStatus that = (SelectionStatus) o;

    if (isMatching != that.isMatching) return false;
    if (atr != null ? !atr.equals(that.atr) : that.atr != null) return false;
    return fci != null ? fci.equals(that.fci) : that.fci == null;
  }

  @Override
  public int hashCode() {
    int result = atr != null ? atr.hashCode() : 0;
    result = 31 * result + (fci != null ? fci.hashCode() : 0);
    result = 31 * result + (isMatching ? 1 : 0);
    return result;
  }
}
