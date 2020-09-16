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

/**
 * This exception indicates that the status code is not referenced.
 *
 * @since 0.9
 */
public class KeypleSeCommandUnknownStatusException extends KeypleSeCommandException {

  /**
   * Constructor allowing to set a message, the command and the status code.
   *
   * @param message the message to identify the exception context (Should not be null)
   * @param command the SE command  (Should not be null)
   * @param statusCode the status code  (Should not be null)
   */
  public KeypleSeCommandUnknownStatusException(
      String message, SeCommand command, Integer statusCode) {
    super(message, command, statusCode);
  }
}
