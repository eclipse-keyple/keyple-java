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
import org.eclipse.keyple.core.card.selection.CardSelector;

/**
 * This POJO, combining a {@link CardSelector} and a {@link CardRequest}, is used to define a
 * selection case.
 *
 * <p>The {@link CardSelector} is used to select a particular smart card, the optinal {@link
 * CardRequest} contains additional commands to be sent to the card when the selection is
 * successful. <br>
 * The {@link CardSelector} and {@link CardRequest} must be carefully defined in order to send the
 * right commands to the right card during the selection process. <br>
 * Can be used in conjunction with the default selection process to optimize the start of a
 * ticketing process.
 *
 * @see CardSelectionResponse
 * @since 1.0
 */
public final class CardSelectionRequest implements Serializable {

  private final CardSelector cardSelector;
  private final CardRequest cardRequest;

  /**
   * Constructor.
   *
   * <p>Builds a request to open a logical channel without sending additional APDUs.
   *
   * <p>The cardRequest field is set to null.
   *
   * @param cardSelector a not null {@link CardSelector}
   * @since 1.0
   */
  public CardSelectionRequest(CardSelector cardSelector) {
    this.cardSelector = cardSelector;
    this.cardRequest = null;
  }

  /**
   * Constructor.
   *
   * <p>Builds a request to open a logical channel and send additional APDUs.
   *
   * @param cardSelector a not null {@link CardSelector}
   * @param cardRequest a not empty {@link CardRequest}
   * @since 1.0
   */
  public CardSelectionRequest(CardSelector cardSelector, CardRequest cardRequest) {
    this.cardSelector = cardSelector;
    this.cardRequest = cardRequest;
  }

  /**
   * Gets the {@link CardSelector}
   *
   * @return a not null {@link CardSelector}
   * @since 1.0
   */
  public CardSelector getCardSelector() {
    return cardSelector;
  }

  /**
   * Gets the {@link CardRequest}
   *
   * @return a {@link CardRequest} or null if it has not been defined
   * @since 1.0
   */
  public CardRequest getCardRequest() {
    return cardRequest;
  }

  @Override
  public String toString() {
    return String.format(
        "CardSelectionRequest:{SELECTOR = %s, CARDREQUEST = %s}",
        getCardSelector(), getCardRequest());
  }
}
