/* **************************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.core.card.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.core.card.message.ApduRequest;
import org.eclipse.keyple.core.card.message.CardRequest;
import org.eclipse.keyple.core.card.message.SelectionRequest;
import org.eclipse.keyple.core.card.message.SelectionResponse;
import org.eclipse.keyple.core.service.exception.KeypleException;

/**
 * The AbstractCardSelectionRequest class combines a CardSelector with additional helper methods
 * useful to the selection process done in {@link CardSelection}.
 *
 * <p>This class may also be extended to add particular features specific to a card family.
 */
public abstract class AbstractCardSelectionRequest<T extends AbstractApduCommandBuilder> {
  protected final CardSelector cardSelector;

  /** optional command builder list of command to be executed following the selection process */
  private final List<T> commandBuilders = new ArrayList<T>();

  public AbstractCardSelectionRequest(CardSelector cardSelector) {
    this.cardSelector = cardSelector;
  }

  /**
   * Returns a selection CardRequest built from the information provided in the constructor and
   * possibly completed with the commandBuilders list
   *
   * @return the selection CardRequest
   */
  final SelectionRequest getSelectionRequest() {
    List<ApduRequest> cardSelectionApduRequests = new ArrayList<ApduRequest>();
    for (T commandBuilder : commandBuilders) {
      cardSelectionApduRequests.add(commandBuilder.getApduRequest());
    }
    return new SelectionRequest(cardSelector, new CardRequest(cardSelectionApduRequests));
  }

  public CardSelector getCardSelector() {
    return cardSelector;
  }

  /**
   * Add an additional {@link AbstractApduCommandBuilder} for the command to be executed after the
   * selection process if it succeeds.
   *
   * <p>If more than one {@link AbstractApduCommandBuilder} is added, all will be executed in the
   * order in which they were added.
   *
   * @param commandBuilder an {@link AbstractApduCommandBuilder}
   */
  protected final void addCommandBuilder(T commandBuilder) {
    commandBuilders.add(commandBuilder);
  }

  /** @return the current command builder list */
  protected final List<T> getCommandBuilders() {
    return commandBuilders;
  }

  /**
   * Virtual parse method
   *
   * @param selectionResponse the card response received
   * @return a {@link AbstractSmartCard}
   * @throws KeypleException if an error occurs while parsing the card response
   */
  protected abstract AbstractSmartCard parse(SelectionResponse selectionResponse);
}
