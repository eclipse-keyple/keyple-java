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
import java.util.List;

/**
 * This POJO wraps the data from a card obtained in response to a CardRequest.
 *
 * <p>These data are a list of {@link ApduResponse} corresponding to the list of {@link ApduRequest}
 * present in the original {@link CardRequest}.
 *
 * <p>The current logical channel status (open/closed) is provided by isLogicalChannelOpen.
 *
 * @since 0.9
 */
public final class CardResponse implements Serializable {

  private final boolean logicalChannelStatus;
  private final List<ApduResponse> apduResponses;

  /**
   * Constructor.
   *
   * @param logicalChannelStatus A boolean (true if the current channel is open).
   * @param apduResponses A list of {@link ApduResponse} (must be not null).
   * @since 0.9
   */
  public CardResponse(boolean logicalChannelStatus, List<ApduResponse> apduResponses) {

    this.logicalChannelStatus = logicalChannelStatus;
    this.apduResponses = apduResponses;
  }

  /**
   * Get the logical channel status
   *
   * @return True if the logical channel is open, false if not.
   * @since 0.9
   */
  public boolean isLogicalChannelOpen() {
    return logicalChannelStatus;
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
    return "CardResponse{"
        + "logicalChannelStatus="
        + logicalChannelStatus
        + ", apduResponses="
        + apduResponses
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CardResponse that = (CardResponse) o;

    if (logicalChannelStatus != that.logicalChannelStatus) return false;
    return apduResponses.equals(that.apduResponses);
  }

  @Override
  public int hashCode() {
    int result = (logicalChannelStatus ? 1 : 0);
    result = 31 * result + apduResponses.hashCode();
    return result;
  }
}
