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
package org.eclipse.keyple.plugin.remote.exception;

import org.eclipse.keyple.core.service.exception.KeypleException;

/**
 * The exception {@code KeypleRemoteCommunicationException} indicates that an external error occurs
 * during the communication between nodes.
 */
public class KeypleRemoteCommunicationException extends KeypleException {

  /** @param message message to identify the exception context */
  public KeypleRemoteCommunicationException(String message) {
    super(message);
  }

  /**
   * Encapsulates a lower level external exception
   *
   * @param message message to identify the exception context
   * @param cause the cause
   */
  public KeypleRemoteCommunicationException(String message, Throwable cause) {
    super(message, cause);
  }
}
