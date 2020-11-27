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
import org.eclipse.keyple.core.card.message.CardSelectionRequest;
import org.eclipse.keyple.core.card.message.CardSelectionResponse;
import org.eclipse.keyple.core.service.exception.KeypleException;

/**
 * This class provide means to create a CardSelectionRequest and analyse its result.
 *
 * <p>It embeds a {@link CardSelector} provided at construction and offers methods to manage
 * additional APDU command builders.<br>
 * The resulting {@link CardSelectionRequest} will be used as a selection case in the general
 * selection process implemented in {@link CardSelectionsService}.
 *
 * <p>This class can also be extended to add specific features to a family of cards and create
 * specific instances of {@link AbstractSmartCard} in return for the parse method.
 *
 * @since 0.9
 */
public abstract class AbstractCardSelection<T extends AbstractApduCommandBuilder> {
  protected final CardSelector cardSelector;

  private final List<T> commandBuilders;

  /**
   * (protected)<br>
   * Constructor.
   *
   * @param cardSelector A not null {@link CardSelector}.
   * @since 0.9
   */
  protected AbstractCardSelection(CardSelector cardSelector) {
    this.cardSelector = cardSelector;
    commandBuilders = new ArrayList<T>();
  }

  /**
   * Returns a {@link CardSelectionRequest} built from the information provided in the constructor
   * and possibly completed with APDUs from the command builders list optionally added.
   *
   * @return A not null {@link CardSelectionRequest}
   * @since 0.9
   */
  final CardSelectionRequest getCardSelectionRequest() {
    List<ApduRequest> cardSelectionApduRequests = new ArrayList<ApduRequest>();
    for (T commandBuilder : commandBuilders) {
      cardSelectionApduRequests.add(commandBuilder.getApduRequest());
    }
    return new CardSelectionRequest(cardSelector, new CardRequest(cardSelectionApduRequests));
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
   * @since 0.9
   */
  protected final void addCommandBuilder(T commandBuilder) {
    commandBuilders.add(commandBuilder);
  }

  /**
   * Gets the list of command builders.
   *
   * @return the current command builder list
   * @since 0.9
   */
  protected final List<T> getCommandBuilders() {
    return commandBuilders;
  }

  /**
   * Parsing method to be implemented in a card specific extension.
   *
   * <p>It returns an instance of {@link AbstractSmartCard} created from the data collected in the
   * selection step (FCI, other data if any).
   *
   * @param cardSelectionResponse the card response received
   * @return a {@link AbstractSmartCard}
   * @throws KeypleException if an error occurs while parsing the card response
   * @since 0.9
   */
  protected abstract AbstractSmartCard parse(CardSelectionResponse cardSelectionResponse);
}
