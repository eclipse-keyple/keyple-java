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
package org.eclipse.keyple.core.service.exception;

/** Indicates that the current card protocol is not supported by the plugin. */
public class KeypleReaderProtocolNotSupportedException extends KeypleReaderException {

  /** @param cardProtocol the identification data used to identify the card */
  public KeypleReaderProtocolNotSupportedException(String cardProtocol) {
    super("The card protocol " + cardProtocol + " is not supported.");
  }
}
