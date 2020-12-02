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
package org.eclipse.keyple.calypso.command.po;

import org.eclipse.keyple.core.card.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.card.command.AbstractIso7816CommandBuilder;
import org.eclipse.keyple.core.card.message.ApduRequest;
import org.eclipse.keyple.core.card.message.ApduResponse;

/**
 * Abstract class for all PO command builders
 *
 * @since 0.9
 */
public abstract class AbstractPoCommandBuilder<T extends AbstractPoResponseParser>
    extends AbstractIso7816CommandBuilder {

  /**
   * Constructor dedicated for the building of referenced Calypso commands
   *
   * @param commandRef a command reference from the Calypso command table
   * @param request the ApduRequest (the instruction byte will be overwritten)
   * @since 0.9
   */
  protected AbstractPoCommandBuilder(CalypsoPoCommand commandRef, ApduRequest request) {
    super(commandRef, request);
  }

  /**
   * Create the response parser matching the builder
   *
   * @param apduResponse the response data from the the card
   * @return an {@link AbstractApduResponseParser}
   */
  public abstract T createResponseParser(ApduResponse apduResponse);

  /** {@inheritDoc} */
  @Override
  public CalypsoPoCommand getCommandRef() {
    return (CalypsoPoCommand) commandRef;
  }

  /**
   * Indicates if the session buffer is used when executing this command.
   *
   * <p>Allows the management of the overflow of this buffer.
   *
   * @return true if this command uses the session buffer
   * @since 0.9
   */
  public abstract boolean isSessionBufferUsed();
}
