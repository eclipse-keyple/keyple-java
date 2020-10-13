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
package org.eclipse.keyple.core.seproxy.plugin.reader;

import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;

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
  ApduResponse openChannelForAid(SeSelector.AidSelector aidSelector);

  /** Closes the logical channel explicitly. */
  void closeLogicalChannel();
}
