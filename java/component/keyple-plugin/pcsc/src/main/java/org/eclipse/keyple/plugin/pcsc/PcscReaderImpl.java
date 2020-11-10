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
package org.eclipse.keyple.plugin.pcsc;

import javax.smartcardio.*;
import org.eclipse.keyple.core.plugin.reader.AbstractObservableLocalReader;
import org.eclipse.keyple.core.plugin.reader.SmartInsertionReader;
import org.eclipse.keyple.core.plugin.reader.SmartRemovalReader;
import org.eclipse.keyple.core.service.Reader;

/**
 * Package private class implementing the {@link Reader} interface for PC/SC based readers.
 *
 * <p>A PC/SC reader is observable ({@link AbstractObservableLocalReader}), autonomous to detect the
 * insertion of cards ({@link SmartInsertionReader}, able to detect the removal of a card prior an
 * attempt to communicate with it ({@link SmartRemovalReader} and has specific settings ({@link
 * PcscReader}.
 *
 * @since 0.9
 */
final class PcscReaderImpl extends AbstractPcscReader {

  /**
   * This constructor should only be called by PcscPlugin PCSC reader parameters are initialized
   * with their default values as defined in setParameter.
   *
   * @param pluginName the name of the plugin
   * @param terminal the PC/SC terminal
   * @since 0.9
   */
  protected PcscReaderImpl(String pluginName, CardTerminal terminal) {
    super(pluginName, terminal);
  }
}
