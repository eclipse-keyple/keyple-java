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
package org.eclipse.keyple.core.service.exception;

import java.util.List;
import org.eclipse.keyple.core.card.message.CardResponse;
import org.eclipse.keyple.core.card.message.CardSelectionResponse;

/**
 * The exception {@code KeypleReaderIOException} indicates that some IO operations with the reader
 * or the card have failed, generally when the reader was disconnected or card removed.
 */
public class KeypleReaderIOException extends KeypleReaderException {

  /*
   * CardResponse and list of CardSelectionResponse objects to carry partial responses in case of a breakdown
   * in communication with the card.
   */
  private CardResponse cardResponse;
  private List<CardSelectionResponse> cardSelectionResponses;

  /** @param message the message to identify the exception context */
  public KeypleReaderIOException(String message) {
    super(message);
  }

  /**
   * Encapsulates a lower level reader exception
   *
   * @param message message to identify the exception context
   * @param cause the cause
   */
  public KeypleReaderIOException(String message, Throwable cause) {
    super(message, cause);
  }

  public List<CardSelectionResponse> getCardSelectionResponses() {
    return cardSelectionResponses;
  }

  public void setCardSelectionResponses(List<CardSelectionResponse> cardSelectionResponses) {
    this.cardSelectionResponses = cardSelectionResponses;
  }

  public CardResponse getCardResponse() {
    return cardResponse;
  }

  public void setCardResponse(CardResponse cardResponse) {
    this.cardResponse = cardResponse;
  }
}
