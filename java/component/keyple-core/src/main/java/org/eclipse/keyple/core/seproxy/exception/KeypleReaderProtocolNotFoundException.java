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
package org.eclipse.keyple.core.seproxy.exception;

/**
 * The exception {@code KeypleReaderNotFoundException} indicates that the current card protocol is
 * undetermined.
 */
public class KeypleReaderProtocolNotFoundException extends KeypleReaderException {

  /** @param identificationData the identification data used to identify the card */
  public KeypleReaderProtocolNotFoundException(String identificationData) {
    super(
        "The protocol of the card with identification data "
            + identificationData
            + " was not determined.");
  }
}
