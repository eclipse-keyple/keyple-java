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
package org.eclipse.keyple.core.plugin.reader;

import org.eclipse.keyple.core.card.message.ApduResponse;
import org.eclipse.keyple.core.card.message.ProxyReader;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.reader.exception.KeypleReaderIOException;

/**
 * Interface implemented by readers able to handle natively the card selection process (e.g. Android
 * OMAPI readers).
 */
public interface SmartSelectionReader extends ProxyReader {

  /**
   * Opens a logical channel for the provided AID
   *
   * @param aidSelector the selection data
   * @return an ApduResponse containing the card answer to selection
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   */
  ApduResponse openChannelForAid(CardSelector.AidSelector aidSelector);

  /** Closes the logical channel explicitly. */
  void closeLogicalChannel();
}
