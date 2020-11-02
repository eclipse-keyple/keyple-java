/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
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

/**
 * This POJO is used to transport data from a card obtained in response to a {@link
 * SelectionRequest}.
 *
 * <p>These data are the selection status and the responses ({@link SelectionStatus}), if any, to
 * the APDUs sent to the card (a list of {@link ApduResponse}).
 *
 * @since 1.0
 */
public final class SelectionResponse implements Serializable {

  private final SelectionStatus selectionStatus;
  private final CardResponse cardResponse;

  /**
   * Builds the SelectionResponse from the {@link SelectionStatus} and a {@link CardResponse} (list
   * of {@link ApduResponse}).
   *
   * <p>selectionStatus may be null (response to a non-selecting request)<br>
   * apduResponses may be empty but should not be null.
   *
   * @param selectionStatus A not null {@link SelectionStatus}.
   * @param cardResponse A {@link CardResponse} (must be not null).
   * @since 0.9
   */
  public SelectionResponse(SelectionStatus selectionStatus, CardResponse cardResponse) {

    this.selectionStatus = selectionStatus;
    this.cardResponse = cardResponse;
  }

  /**
   * Gets the selection status.
   *
   * @return A {@link SelectionStatus} (may be null).
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
  public CardResponse getCardResponse() {
    return cardResponse;
  }

  @Override
  public String toString() {
    return "SelectionResponse{"
        + "selectionStatus="
        + selectionStatus
        + ", cardResponse="
        + cardResponse
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SelectionResponse that = (SelectionResponse) o;

    if (!selectionStatus.equals(that.selectionStatus)) return false;
    return cardResponse != null
        ? cardResponse.equals(that.cardResponse)
        : that.cardResponse == null;
  }

  @Override
  public int hashCode() {
    int result = selectionStatus.hashCode();
    result = 31 * result + (cardResponse != null ? cardResponse.hashCode() : 0);
    return result;
  }
}
