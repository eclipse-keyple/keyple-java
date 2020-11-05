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
package org.eclipse.keyple.calypso.command.sam;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.calypso.command.sam.exception.*;
import org.eclipse.keyple.core.card.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.card.command.CardCommand;
import org.eclipse.keyple.core.card.command.exception.KeypleCardCommandException;
import org.eclipse.keyple.core.card.message.ApduResponse;

public abstract class AbstractSamResponseParser extends AbstractApduResponseParser {

  protected static final Map<Integer, StatusProperties> STATUS_TABLE;

  static {
    Map<Integer, StatusProperties> m =
        new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
    m.put(
        0x6D00,
        new StatusProperties("Instruction unknown.", CalypsoSamIllegalParameterException.class));
    m.put(
        0x6E00,
        new StatusProperties("Class not supported.", CalypsoSamIllegalParameterException.class));
    STATUS_TABLE = m;
  }

  /** {@inheritDoc} */
  @Override
  protected Map<Integer, StatusProperties> getStatusTable() {
    return STATUS_TABLE;
  }

  /**
   * Constructor to build a parser of the APDU response.
   *
   * @param response response to parse
   * @param builder the reference of the builder that created the parser
   */
  public AbstractSamResponseParser(
      ApduResponse response,
      AbstractSamCommandBuilder<? extends AbstractSamResponseParser> builder) {
    super(response, builder);
  }

  /** {@inheritDoc} */
  @Override
  public final AbstractSamCommandBuilder<AbstractSamResponseParser> getBuilder() {
    return (AbstractSamCommandBuilder<AbstractSamResponseParser>) super.getBuilder();
  }

  /** {@inheritDoc} */
  @Override
  protected final KeypleCardCommandException buildCommandException(
      Class<? extends KeypleCardCommandException> exceptionClass,
      String message,
      CardCommand commandRef,
      Integer statusCode) {

    KeypleCardCommandException e;
    CalypsoSamCommand command = (CalypsoSamCommand) commandRef;
    if (exceptionClass == CalypsoSamAccessForbiddenException.class) {
      e = new CalypsoSamAccessForbiddenException(message, command, statusCode);
    } else if (exceptionClass == CalypsoSamCounterOverflowException.class) {
      e = new CalypsoSamCounterOverflowException(message, command, statusCode);
    } else if (exceptionClass == CalypsoSamDataAccessException.class) {
      e = new CalypsoSamDataAccessException(message, command, statusCode);
    } else if (exceptionClass == CalypsoSamIllegalArgumentException.class) {
      e = new CalypsoSamIllegalArgumentException(message, command);
    } else if (exceptionClass == CalypsoSamIllegalParameterException.class) {
      e = new CalypsoSamIllegalParameterException(message, command, statusCode);
    } else if (exceptionClass == CalypsoSamIncorrectInputDataException.class) {
      e = new CalypsoSamIncorrectInputDataException(message, command, statusCode);
    } else if (exceptionClass == CalypsoSamSecurityDataException.class) {
      e = new CalypsoSamSecurityDataException(message, command, statusCode);
    } else {
      e = new CalypsoSamUnknownStatusException(message, command, statusCode);
    }
    return e;
  }

  /** {@inheritDoc} */
  @Override
  public void checkStatus() {
    try {
      super.checkStatus();
    } catch (KeypleCardCommandException e) {
      throw (CalypsoSamCommandException) e;
    }
  }
}
