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
package org.eclipse.keyple.core.seproxy;

import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderProtocolNotSupportedException;

/**
 * SeReader interface
 *
 * <ul>
 *   <li>To retrieve the unique reader name
 *   <li>To check the SE presence.
 *   <li>To activate and deactivate SE protocols.
 * </ul>
 *
 * Interface used by applications processing SE.
 *
 * @since 0.9
 */
public interface SeReader extends ProxyElement {

  /**
   * Checks if is SE present.
   *
   * @return true if a Secure Element is present in the reader
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   * @since 0.9
   */
  boolean isSePresent();

  /**
   * Activates the provided SE protocol.
   *
   * <ul>
   *   <li>Ask the plugin to take this protocol into account if an SE using this protocol is
   *       identified during the selection phase.
   *   <li>Activates the detection of SEs using this protocol (if the plugin allows it).
   * </ul>
   *
   * @param seProtocol The protocol to activate (must be not null).
   * @throws KeypleReaderProtocolNotSupportedException if the protocol is not supported.
   * @since 1.0
   */
  void activateProtocol(String seProtocol);

  /**
   * Deactivates the provided SE protocol.
   *
   * <ul>
   *   <li>Ask the plugin to ignore this protocol if an SE using this protocol is identified during
   *       the selection phase.
   *   <li>Inhibits the detection of SEs using this protocol (if the plugin allows it).
   * </ul>
   *
   * @param seProtocol The protocol to deactivate (must be not null).
   * @since 1.0
   */
  void deactivateProtocol(String seProtocol);

  /**
   * Tells if the current card communication is contactless.
   *
   * @return True if the communication is contactless, false if not.
   * @since 1.0
   */
  boolean isContactless();
}
