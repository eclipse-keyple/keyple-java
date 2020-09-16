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

import org.eclipse.keyple.core.command.SeCommand;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;

/**
 * This exception is the parent abstract class of all Keyple SE APDU commands exceptions.
 *
 * @since 0.9
 */
public abstract class KeypleSeCommandException extends KeypleException {

  /**
   * The SE command raising the exception
   *
   * @since 0.9
   */
  private final SeCommand command;

  /**
   * The status code (optional)
   *
   * @since 0.9
   */
  private final Integer statusCode;

  /**
   * Constructor allowing to set the error message and the reference to the command
   *
   * @param message the message to identify the exception context (Should not be null)
   * @param command the command (Should not be null)
   * @param statusCode the status code
   *
   * @since 0.9
   */
  protected KeypleSeCommandException(String message, SeCommand command, Integer statusCode) {
    super(message);
    this.command = command;
    this.statusCode = statusCode;
  }

  /**
   * Get the command
   * @return A non null reference
   *
   * @since 0.9
   */
  public SeCommand getCommand() {
    return command;
  }

  /**
   *  Get the status code
   *
   * @return A nullable reference
   *
   * @since 0.9
   */
  public Integer getStatusCode() {
    return statusCode;
  }
}
