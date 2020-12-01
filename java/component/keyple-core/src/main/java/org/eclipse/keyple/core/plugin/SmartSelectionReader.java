/* **************************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.plugin;

import org.eclipse.keyple.core.card.message.ProxyReader;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;

/**
 * Interface implemented by readers able to handle natively the card selection process.
 *
 * <p>Android OMAPI based readers are for example able to do this.
 */
public interface SmartSelectionReader extends ProxyReader {

  /**
   * Opens a logical channel for the provided AID
   *
   * <p>The <b>dfName</b> is provided as a byte array. It can be set to null to activate the basic
   * channel opening defined by the OMAPI. <br>
   * The bit mask indicates the ISO defined condition to retrieve the selection data.
   *
   * @param dfName A byte array or null
   * @param isoControlMask bit mask from
   * @return A byte array containing the card answer to selection
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   */
  byte[] openChannelForAid(byte[] dfName, byte isoControlMask);

  /** Closes the logical channel explicitly. */
  void closeLogicalChannel();
}
