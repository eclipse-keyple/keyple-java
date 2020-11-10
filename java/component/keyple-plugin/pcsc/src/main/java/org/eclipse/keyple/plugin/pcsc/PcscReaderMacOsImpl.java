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
package org.eclipse.keyple.plugin.pcsc;

import javax.smartcardio.CardTerminal;
import org.eclipse.keyple.core.plugin.reader.WaitForCardInsertionNonBlocking;

/**
 * (package-private)<br>
 * Implementation of {@link AbstractPcscReader} for all non-MacOS platforms.
 *
 * <p>Implements {@link WaitForCardInsertionNonBlocking} to enable non-blocking detection of card
 * insertion.
 *
 * @since 0.9
 */
final class PcscReaderMacOsImpl extends AbstractPcscReader
    implements WaitForCardInsertionNonBlocking {

  /**
   * This constructor should only be called by a PcscPlugin on macOS platforms.
   *
   * @param pluginName the name of the plugin
   * @param terminal the PC/SC terminal
   * @since 1.0
   */
  protected PcscReaderMacOsImpl(String pluginName, CardTerminal terminal) {
    super(pluginName, terminal);
  }
}
