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

/** Indicates that the SAM is revoked. */
public class CalypsoSamRevokedException extends CalypsoPoTransactionException {

  /** @param message the message to identify the exception context */
  public CalypsoSamRevokedException(String message) {
    super(message);
  }
}
