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
package org.eclipse.keyple.calypso.transaction.exception;

/**
 * Indicates that the "Stored Value" session is not authentic because the signature of the PO is
 * incorrect.
 */
public class CalypsoSvAuthenticationException extends CalypsoPoTransactionException {

  /** @param message the message to identify the exception context */
  public CalypsoSvAuthenticationException(String message) {
    super(message);
  }

  /**
   * Encapsulates lower level exception.
   *
   * @param message message to identify the exception context
   * @param cause the cause
   */
  public CalypsoSvAuthenticationException(String message, Throwable cause) {
    super(message, cause);
  }
}
