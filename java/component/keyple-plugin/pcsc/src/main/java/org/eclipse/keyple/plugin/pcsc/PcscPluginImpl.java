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
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler;
import org.eclipse.keyple.core.service.exception.KeypleReaderException;

/**
 * (package-private)<br>
 * Implementation of AbstractPcscPlugin suitable for platforms other than Windows.
 *
 * <p>Provides a createInstance method that receives a boolean as an argument to indicate that the
 * platform is MacOS. <br>
 * This information is used to create readers capable of handling the technical issues specific to
 * this platform.
 */
final class PcscPluginImpl extends AbstractPcscPlugin {

  /**
   * Singleton instance of SmartCardService 'volatile' qualifier ensures that read access to the
   * object will only be allowed once the object has been fully initialized.
   *
   * <p>This qualifier is required for "lazy-singleton" pattern with double-check method, to be
   * thread-safe.
   */
  private static volatile PcscPluginImpl instance; // NOSONAR: lazy-singleton pattern.

  private final boolean isOsMac;

  /**
   * (private)<br>
   * Constructor.
   */
  private PcscPluginImpl() {
    super();
    this.isOsMac = System.getProperty("os.name").toLowerCase().contains("mac");
  }

  /**
   * Gets the single instance of PcscPluginImpl.
   *
   * @return single instance of PcscPluginImpl
   * @throws KeypleReaderException if a reader error occurs
   * @since 0.9
   */
  static PcscPluginImpl getInstance() {
    if (instance == null) {
      synchronized (PcscPluginImpl.class) {
        if (instance == null) {
          instance = new PcscPluginImpl();
        }
      }
    }
    return instance;
  }

  /**
   * {@inheritDoc}
   *
   * @since 0.9
   */
  protected CardTerminals getCardTerminals() {
    return TerminalFactory.getDefault().terminals();
  }

  /**
   * Create a {@link PcscReaderMacOsImpl} or a {@link PcscReaderImpl} according to the identified
   * platform. {@inheritDoc}
   */
  @Override
  AbstractPcscReader createReader(
      String name,
      CardTerminal terminal,
      ReaderObservationExceptionHandler readerObservationExceptionHandler) {
    if (isOsMac) {
      return new PcscReaderMacOsImpl(name, terminal, readerObservationExceptionHandler);
    } else {
      return new PcscReaderImpl(name, terminal, readerObservationExceptionHandler);
    }
  }
}
