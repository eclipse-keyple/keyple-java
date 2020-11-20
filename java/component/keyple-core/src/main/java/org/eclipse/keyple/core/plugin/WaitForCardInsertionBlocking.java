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

import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;

/**
 * Interface to be implemented by readers that are autonomous in the management of waiting for the
 * insertion of a card and that provide a method to wait for it indefinitely.
 *
 * <p>A typical example of readers conforming to this mode of operation are PC/SC type readers
 * capable of performing RF polling without waiting for a command from the application.
 */
public interface WaitForCardInsertionBlocking extends ObservableReaderNotifier {
  /**
   * Waits for a card. Returns true if a card is detected, false it was interrupted or if the reader is no longer able to detect a card.
   *
   * <p>This method must be implemented by the plugin's reader class when it implements the {@link
   * WaitForCardInsertionBlocking} interface.
   *
   * <p>Returns false if no card is detected.
   *
   * @return presence status
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   *     (disconnection)
   */
  boolean waitForCardPresent();

  /** Interrupts the waiting of a card */
  void stopWaitForCard();
}
