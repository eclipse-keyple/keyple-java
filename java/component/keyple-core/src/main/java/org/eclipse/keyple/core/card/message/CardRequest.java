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
import org.eclipse.keyple.core.card.selection.CardSelector;

/**
 * List of APDU requests that will result in a {@link CardResponse}
 *
 * @see CardResponse
 */
public final class CardRequest implements Serializable {

  /** +/cardSelector is either an AID or an ATR regular expression */
  private final CardSelector cardSelector;

  /**
   * contains a group of APDUCommand to operate on the selected card application by the card reader.
   */
  private final List<ApduRequest> apduRequests;

  /**
   * The constructor called by a ProxyReader in order to open a logical channel, to send a set of
   * APDU commands to a card application, or both of them.
   *
   * @param cardSelector the CardSelector containing the selection information to process the card
   *     selection
   * @param apduRequests a optional list of {@link ApduRequest} to execute after a successful
   *     selection process
   */
  public CardRequest(CardSelector cardSelector, List<ApduRequest> apduRequests) {
    this.cardSelector = cardSelector;
    this.apduRequests = apduRequests;
  }

  /**
   * Constructor to be used when the card is already selected (without {@link CardSelector})
   *
   * @param apduRequests a list of ApudRequest
   */
  public CardRequest(List<ApduRequest> apduRequests) {
    this.cardSelector = null;
    this.apduRequests = apduRequests;
  }

  /**
   * Gets the card cardSelector.
   *
   * @return the current card cardSelector
   */
  public CardSelector getCardSelector() {
    return cardSelector;
  }

  /**
   * Gets the apdu requests.
   *
   * @return the group of APDUs to be transmitted to the card application for this instance of
   *     CardRequest.
   */
  public List<ApduRequest> getApduRequests() {
    return apduRequests;
  }

  @Override
  public String toString() {
    return String.format(
        "CardRequest:{REQUESTS = %s, SELECTOR = %s}", getApduRequests(), getCardSelector());
  }
}
