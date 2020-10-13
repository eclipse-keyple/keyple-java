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
package org.eclipse.keyple.core.selection;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.core.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.message.ApduRequest;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;

/**
 * The AbstractSeSelectionRequest class combines a SeSelector with additional helper methods useful
 * to the selection process done in {@link SeSelection}.
 *
 * <p>This class may also be extended to add particular features specific to a card family.
 */
public abstract class AbstractSeSelectionRequest<T extends AbstractApduCommandBuilder> {
  protected final SeSelector seSelector;

  /** optional command builder list of command to be executed following the selection process */
  private final List<T> commandBuilders = new ArrayList<T>();

  public AbstractSeSelectionRequest(SeSelector seSelector) {
    this.seSelector = seSelector;
  }

  /**
   * Returns a selection SeRequest built from the information provided in the constructor and
   * possibly completed with the commandBuilders list
   *
   * @return the selection SeRequest
   */
  final SeRequest getSelectionRequest() {
    List<ApduRequest> seSelectionApduRequests = new ArrayList<ApduRequest>();
    for (T commandBuilder : commandBuilders) {
      seSelectionApduRequests.add(commandBuilder.getApduRequest());
    }
    return new SeRequest(seSelector, seSelectionApduRequests);
  }

  public SeSelector getSeSelector() {
    return seSelector;
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
   * @param seResponse the card response received
   * @return a {@link AbstractMatchingSe}
   * @throws KeypleException if an error occurs while parsing the card response
   */
  protected abstract AbstractMatchingSe parse(SeResponse seResponse);
}
