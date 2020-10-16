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
package org.eclipse.keyple.core.command.exception;

import org.eclipse.keyple.core.command.CardCommand;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;

/**
 * This exception is the parent abstract class of all Keyple card APDU commands exceptions.
 *
 * @since 0.9
 */
public abstract class KeypleCardCommandException extends KeypleException {

  private final CardCommand command;

  private final Integer statusCode;

  /**
   * Constructor allowing to set the error message and the reference to the command
   *
   * @param message the message to identify the exception context (Should not be null)
   * @param command the command
   * @param statusCode the status code
   * @since 0.9
   */
  protected KeypleCardCommandException(String message, CardCommand command, Integer statusCode) {
    super(message);
    this.command = command;
    this.statusCode = statusCode;
  }

  /**
   * Gets the command
   *
   * @return A non null reference
   * @since 0.9
   */
  public CardCommand getCommand() {
    return command;
  }

  /**
   * Gets the status code
   *
   * @return A nullable reference
   * @since 0.9
   */
  public Integer getStatusCode() {
    return statusCode;
  }
}
