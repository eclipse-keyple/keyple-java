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
 * The exception {@code KeypleSeCommandUnknownStatusException} indicates that the status code is not
 * referenced.
 */
public class KeypleSeCommandUnknownStatusException extends KeypleSeCommandException {

  /**
   * @param message the message to identify the exception context
   * @param command the SE command
   * @param statusCode the status code
   */
  public KeypleSeCommandUnknownStatusException(
      String message, SeCommand command, Integer statusCode) {
    super(message, command, statusCode);
  }
}
