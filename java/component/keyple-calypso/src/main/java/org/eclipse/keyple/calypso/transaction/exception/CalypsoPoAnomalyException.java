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

import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;

/**
 * Indicates an anomaly in the PO.<br>
 * This can occur if the PO is not Calypso compliant.
 */
public class CalypsoPoAnomalyException extends CalypsoPoTransactionException {

  /**
   * Encapsulates an unexpected {@link CalypsoPoCommandException} exception.
   *
   * @param message message to identify the exception context
   * @param cause the cause
   */
  public CalypsoPoAnomalyException(String message, CalypsoPoCommandException cause) {
    super(message, cause);
  }

  /** @return the unexpected cause {@link CalypsoPoCommandException} */
  @Override
  public synchronized CalypsoPoCommandException getCause() {
    return (CalypsoPoCommandException) super.getCause();
  }
}
