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

import org.eclipse.keyple.core.seproxy.exception.KeypleException;

/**
 * The exception {@code CalypsoPoTransactionException} is the parent abstract class of all Keyple PO
 * transaction functional exceptions.
 */
public abstract class CalypsoPoTransactionException extends KeypleException {

  /** @param message the message to identify the exception context */
  protected CalypsoPoTransactionException(String message) {
    super(message);
  }

  /**
   * Encapsulates a lower level PO transaction exception
   *
   * @param message message to identify the exception context
   * @param cause the cause
   */
  protected CalypsoPoTransactionException(String message, Throwable cause) {
    super(message, cause);
  }
}
