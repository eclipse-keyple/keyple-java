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

import org.eclipse.keyple.core.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.command.AbstractIso7816CommandBuilder;
import org.eclipse.keyple.core.seproxy.message.ApduRequest;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Abstract class for all PO command builders. */
public abstract class AbstractPoCommandBuilder<T extends AbstractPoResponseParser>
    extends AbstractIso7816CommandBuilder {

  /** common logger for all builders */
  protected static Logger logger = LoggerFactory.getLogger(AbstractPoCommandBuilder.class);

  /**
   * Constructor dedicated for the building of referenced Calypso commands
   *
   * @param commandRef a command reference from the Calypso command table
   * @param request the ApduRequest (the instruction byte will be overwritten)
   */
  public AbstractPoCommandBuilder(CalypsoPoCommand commandRef, ApduRequest request) {
    super(commandRef, request);
  }

  /**
   * Create the response parser matching the builder
   *
   * @param apduResponse the response data from the SE
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
   */
  public abstract boolean isSessionBufferUsed();
}
