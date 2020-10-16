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
import java.util.List;
import org.eclipse.keyple.core.util.ByteArrayUtil;

/**
 * This POJO is used to transport data from a secure element obtained in response to a {@link
 * SeRequest}..
 *
 * <p>It includes elements related to the selection of the SE.
 *
 * <ul>
 *   <li><code>logicalChannelIsOpen</code> tells if a logical channel is currently open.
 *   <li><code>channelPreviouslyOpen</code> tells if a logical channel wa already open prior the
 *       latest {@link SeRequest}.
 *   <li><code>selectionStatus</code>another POJO carrying ATR, FCI and a selection result boolean
 *       (may be null)
 * </ul>
 *
 * It also includes a list of {@link ApduResponse} corresponding to the list of {@link ApduRequest}
 * present in the original {@link SeRequest}.
 *
 * @since 0.9
 */
@SuppressWarnings("PMD.NPathComplexity")
public final class SeResponse implements Serializable {

  /**
   * is defined as true by the SE reader in case a logical channel was already open with the target
   * SE application.
   */
  private final boolean channelPreviouslyOpen;

  /** true if the channel is open */
  private final boolean logicalChannelIsOpen;

  /** POJO possibly including ATR, FCI and matching flag */
  private final SelectionStatus selectionStatus;

  /** List of {@link ApduResponse} returned by the selected SE application. */
  private final List<ApduResponse> apduResponses;

  /**
   * Constructor.
   *
   * <p>selectionStatus may be null (response to a non-selecting request)<br>
   * apduResponses may be empty but should not be null.
   *
   * @param logicalChannelIsOpen A boolean (true if the current channel is open).
   * @param channelPreviouslyOpen A boolean (true if the channel was previously open)
   * @param selectionStatus A nullable {@link SelectionStatus}.
   * @param apduResponses A list of {@link ApduResponse} (must be not null).
   * @since 0.9
   */
  public SeResponse(
      boolean logicalChannelIsOpen,
      boolean channelPreviouslyOpen,
      SelectionStatus selectionStatus,
      List<ApduResponse> apduResponses) {

    this.logicalChannelIsOpen = logicalChannelIsOpen;
    this.channelPreviouslyOpen = channelPreviouslyOpen;
    this.selectionStatus = selectionStatus;
    this.apduResponses = apduResponses;
  }

  /**
   * Tells if the channel was previously open.
   *
   * @return True or false.
   * @since 0.9
   */
  public boolean wasChannelPreviouslyOpen() {
    return channelPreviouslyOpen;
  }

  /**
   * Get the logical channel status
   *
   * @return True if the logical channel is open, false if not.
   * @since 0.9
   */
  public boolean isLogicalChannelOpen() {
    return logicalChannelIsOpen;
  }

  /**
   * Gets the selection status and its associated data.
   *
   * @return A nullable {@link SelectionStatus}.
   * @since 0.9
   */
  public SelectionStatus getSelectionStatus() {
    return this.selectionStatus;
  }

  /**
   * Gets the apdu responses.
   *
   * @return A list of {@link ApduResponse} (may be empty).
   * @since 0.9
   */
  public List<ApduResponse> getApduResponses() {
    return apduResponses;
  }

  @Override
  public String toString() {
    /*
     * getAtr() can return null, we must check it to avoid the call to getBytes() that would
     * raise an exception. In case of a null value, String.format prints "null" in the string,
     * the same is done here.
     */
    String string;
    if (selectionStatus != null) {
      string =
          String.format(
              "SeResponse:{RESPONSES = %s, ATR = %s, FCI = %s, HASMATCHED = %b CHANNELWASOPEN = %b "
                  + "LOGICALCHANNEL = %s}",
              getApduResponses(),
              selectionStatus.getAtr() == null
                  ? "null"
                  : ByteArrayUtil.toHex(selectionStatus.getAtr().getBytes()),
              selectionStatus.getFci() == null
                  ? "null"
                  : ByteArrayUtil.toHex(selectionStatus.getFci().getBytes()),
              selectionStatus.hasMatched(),
              wasChannelPreviouslyOpen(),
              logicalChannelIsOpen ? "OPEN" : "CLOSED");
    } else {
      string =
          String.format(
              "SeResponse:{RESPONSES = %s, ATR = null, FCI = null, HASMATCHED = false CHANNELWASOPEN = %b "
                  + "LOGICALCHANNEL = %s}",
              getApduResponses(),
              wasChannelPreviouslyOpen(),
              logicalChannelIsOpen ? "OPEN" : "CLOSED");
    }
    return string;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof SeResponse)) {
      return false;
    }

    SeResponse seResponse = (SeResponse) o;
    return seResponse.getSelectionStatus().equals(selectionStatus)
        && (seResponse.getApduResponses() == null
            ? apduResponses == null
            : seResponse.getApduResponses().equals(apduResponses))
        && seResponse.isLogicalChannelOpen() == logicalChannelIsOpen
        && seResponse.wasChannelPreviouslyOpen() == channelPreviouslyOpen;
  }

  @Override
  public int hashCode() {
    int hash = 17;
    hash = 31 * hash + (selectionStatus.getAtr() == null ? 0 : selectionStatus.getAtr().hashCode());
    hash = 7 * hash + (apduResponses == null ? 0 : this.apduResponses.hashCode());
    hash = 29 * hash + (this.channelPreviouslyOpen ? 1 : 0);
    hash = 37 * hash + (this.logicalChannelIsOpen ? 1 : 0);
    return hash;
  }
}
