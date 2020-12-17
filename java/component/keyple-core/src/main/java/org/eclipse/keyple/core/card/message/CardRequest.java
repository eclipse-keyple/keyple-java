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
 * This POJO wraps a list of APDUs to be sent to a card for which the logical channel has already
 * been opened.
 *
 * @see CardResponse
 * @since 0.9
 */
public final class CardRequest implements Serializable {

  private final List<ApduRequest> apduRequests;

  /**
   * Constructor.
   *
   * @param apduRequests a not empty list of {@link ApduRequest}
   * @since 0.9
   */
  public CardRequest(List<ApduRequest> apduRequests) {
    this.apduRequests = apduRequests;
  }

  /**
   * Gets the list of {@link ApduRequest}.
   *
   * @return a not empty list of {@link ApduRequest}.
   * @since 0.9
   */
  public List<ApduRequest> getApduRequests() {
    return apduRequests;
  }

  @Override
  public String toString() {
    return String.format("CardRequest:{REQUESTS = %s}", getApduRequests());
  }
}
