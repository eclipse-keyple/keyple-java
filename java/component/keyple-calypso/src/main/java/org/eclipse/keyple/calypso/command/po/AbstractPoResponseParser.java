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
package org.eclipse.keyple.calypso.command.po;

import org.eclipse.keyple.calypso.command.po.exception.*;
import org.eclipse.keyple.core.card.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.card.command.CardCommand;
import org.eclipse.keyple.core.card.command.exception.KeypleCardCommandException;
import org.eclipse.keyple.core.card.message.ApduResponse;

/**
 * Superclass for all PO command parsers.
 *
 * @since 0.9
 */
public abstract class AbstractPoResponseParser extends AbstractApduResponseParser {

  /**
   * The generic abstract constructor to build a parser of the APDU response.
   *
   * @param response response to parse
   * @param builder the reference of the builder that created the parser
   * @since 0.9
   */
  protected AbstractPoResponseParser(
      ApduResponse response, AbstractPoCommandBuilder<? extends AbstractPoResponseParser> builder) {
    super(response, builder);
  }

  /** {@inheritDoc} */
  @Override
  public final AbstractPoCommandBuilder<AbstractPoResponseParser> getBuilder() {
    return (AbstractPoCommandBuilder<AbstractPoResponseParser>) super.getBuilder();
  }

  /** {@inheritDoc} */
  @Override
  protected final KeypleCardCommandException buildCommandException(
      Class<? extends KeypleCardCommandException> exceptionClass,
      String message,
      CardCommand commandRef,
      Integer statusCode) {

    KeypleCardCommandException e;
    CalypsoPoCommand command = (CalypsoPoCommand) commandRef;
    if (exceptionClass == CalypsoPoAccessForbiddenException.class) {
      e = new CalypsoPoAccessForbiddenException(message, command, statusCode);
    } else if (exceptionClass == CalypsoPoDataAccessException.class) {
      e = new CalypsoPoDataAccessException(message, command, statusCode);
    } else if (exceptionClass == CalypsoPoDataOutOfBoundsException.class) {
      e = new CalypsoPoDataOutOfBoundsException(message, command, statusCode);
    } else if (exceptionClass == CalypsoPoIllegalArgumentException.class) {
      e = new CalypsoPoIllegalArgumentException(message, command);
    } else if (exceptionClass == CalypsoPoIllegalParameterException.class) {
      e = new CalypsoPoIllegalParameterException(message, command, statusCode);
    } else if (exceptionClass == CalypsoPoPinException.class) {
      e = new CalypsoPoPinException(message, command, statusCode);
    } else if (exceptionClass == CalypsoPoSecurityContextException.class) {
      e = new CalypsoPoSecurityContextException(message, command, statusCode);
    } else if (exceptionClass == CalypsoPoSecurityDataException.class) {
      e = new CalypsoPoSecurityDataException(message, command, statusCode);
    } else if (exceptionClass == CalypsoPoSessionBufferOverflowException.class) {
      e = new CalypsoPoSessionBufferOverflowException(message, command, statusCode);
    } else if (exceptionClass == CalypsoPoTerminatedException.class) {
      e = new CalypsoPoTerminatedException(message, command, statusCode);
    } else {
      e = new CalypsoPoUnknownStatusException(message, command, statusCode);
    }
    return e;
  }

  /** {@inheritDoc} */
  @Override
  public void checkStatus() {
    try {
      super.checkStatus();
    } catch (KeypleCardCommandException e) {
      throw (CalypsoPoCommandException) e;
    }
  }
}
