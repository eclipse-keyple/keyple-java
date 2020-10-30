/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.remotese.exception;

import org.eclipse.keyple.core.service.exception.KeypleException;

/** Exception used when the communication with the other terminal has failed */
public class KeypleRemoteException extends KeypleException {

  public KeypleRemoteException(String message, Throwable cause) {
    super(message, cause);
  }

  public KeypleRemoteException(String message) {
    super(message);
  }
}
