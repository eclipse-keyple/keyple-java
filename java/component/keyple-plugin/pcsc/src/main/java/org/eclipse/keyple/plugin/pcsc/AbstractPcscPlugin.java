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

import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import org.eclipse.keyple.core.plugin.AbstractReader;
import org.eclipse.keyple.core.plugin.AbstractThreadedObservablePlugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.event.PluginObservationExceptionHandler;
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler;
import org.eclipse.keyple.core.service.exception.KeypleReaderException;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the {@link PcscPlugin} interface.<br>
 * (package-private)
 *
 * @since 0.9
 */
abstract class AbstractPcscPlugin extends AbstractThreadedObservablePlugin implements PcscPlugin {

  private static final Logger logger = LoggerFactory.getLogger(AbstractPcscPlugin.class);

  private String contactReaderRegexFilter;
  private String contactlessReaderRegexFilter;
  private PluginObservationExceptionHandler pluginObservationExceptionHandler;
  private ReaderObservationExceptionHandler readerObservationExceptionHandler;

  protected AbstractPcscPlugin() {
    super(PcscPluginFactory.PLUGIN_NAME);
    contactReaderRegexFilter = "";
    contactlessReaderRegexFilter = "";
  }

  /**
   * Fetch the list of connected native reader (from smartcard.io) and returns their names
   *
   * @return A {@link SortedSet} of {@link String}
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   * @since 0.9
   */
  @Override
  public SortedSet<String> fetchNativeReadersNames() {

    SortedSet<String> nativeReadersNames = new ConcurrentSkipListSet<String>();
    CardTerminals terminals = getCardTerminals();
    try {
      for (CardTerminal terminal : terminals.list()) {
        nativeReadersNames.add(terminal.getName());
      }
    } catch (CardException e) {
      if (e.getCause().toString().contains("SCARD_E_NO_READERS_AVAILABLE")) {
        logger.trace("No reader available.");
      } else {
        logger.trace(
            "[{}] fetchNativeReadersNames => Terminal list is not accessible. Exception: {}",
            this.getName(),
            e.getMessage());
        throw new KeypleReaderIOException("Could not access terminals list", e);
      }
    }
    return nativeReadersNames;
  }

  /**
   * Create a new instance of PcscReader.
   *
   * @param name A not empty String.
   * @param terminal A {@link CardTerminal} reference.
   * @param readerObservationExceptionHandler A not null reference to an object implementing {@link
   *     ReaderObservationExceptionHandler}
   * @return An instance of AbstractPcscReader
   */
  abstract AbstractPcscReader createReader(
      String name,
      CardTerminal terminal,
      ReaderObservationExceptionHandler readerObservationExceptionHandler);

  /**
   * Fetch all connected native readers (provided by smartcard.io) and returns a {@link Map} of
   * corresponding {@link AbstractReader} as value and a {@link String} containing the reader name
   * as key<br>
   * Returned {@link AbstractReader} are new instances.
   *
   * @return A {@link Map} (not null but possibly empty).
   * @throws KeypleReaderException if a reader error occurs
   * @since 0.9
   */
  @Override
  protected Map<String, Reader> initNativeReaders() {

    ConcurrentMap<String, Reader> nativeReaders = new ConcurrentHashMap<String, Reader>();

    // parse the current readers list to create the ProxyReader(s) associated with new reader(s)
    CardTerminals terminals = getCardTerminals();
    logger.trace("[{}] initNativeReaders => CardTerminal in list: {}", this.getName(), terminals);
    try {
      for (CardTerminal terminal : terminals.list()) {
        final PcscReader pcscReader =
            createReader(this.getName(), terminal, readerObservationExceptionHandler);
        nativeReaders.put(pcscReader.getName(), pcscReader);
      }
    } catch (CardException e) {
      if (e.getCause().toString().contains("SCARD_E_NO_READERS_AVAILABLE")) {
        logger.trace("No reader available.");
      } else {
        logger.trace(
            "[{}] Terminal list is not accessible. Exception: {}", this.getName(), e.getMessage());
        // throw new KeypleReaderIOException("Could not access terminals list", e); do not
        // propagate exception at the constructor will propagate it as a
        // KeypleRuntimeException

      }
    }
    return nativeReaders;
  }

  /**
   * Fetches the reader whose name is provided as an argument.
   *
   * <p>Returns the current reader if it is already listed.<br>
   * Creates and returns a new reader if not.
   *
   * @param name A String (should be not null).
   * @return A not null reference to a {@link Reader}.
   * @throws KeypleReaderNotFoundException if a reader is not found by its name
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   * @since 0.9
   */
  @Override
  protected Reader fetchNativeReader(String name) {

    // return the current reader if it is already listed
    Reader reader = readers.get(name);
    if (reader != null) {
      return reader;
    }
    /* parse the current PC/SC readers list to create the ProxyReader(s) associated with new reader(s) */
    CardTerminals terminals = getCardTerminals();
    try {
      for (CardTerminal terminal : terminals.list()) {
        if (terminal.getName().equals(name)) {
          logger.trace(
              "[{}] fetchNativeReader => CardTerminal in new PcscReader: {}",
              this.getName(),
              terminals);
          reader = createReader(this.getName(), terminal, readerObservationExceptionHandler);
        }
      }
    } catch (CardException e) {
      throw new KeypleReaderIOException("Could not access terminals list", e);
    }
    if (reader == null) {
      throw new KeypleReaderNotFoundException("Reader " + name + " not found!");
    }
    return reader;
  }

  /**
   * Returns a new CardTerminals object encapsulating the available terminals.
   *
   * @return A {@link CardTerminals} reference
   */
  protected abstract CardTerminals getCardTerminals();

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void setReaderNameFilter(boolean contactlessMode, String readerNameFilter) {

    Assert.getInstance().notEmpty(readerNameFilter, "readerNameFilter");

    if (contactlessMode) {
      contactlessReaderRegexFilter = readerNameFilter;
    } else {
      contactReaderRegexFilter = readerNameFilter;
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void setProtocolIdentificationRule(String readerProtocolName, String protocolRule) {
    PcscProtocolSetting.setProtocolIdentificationRule(readerProtocolName, protocolRule);
  }

  /**
   * (package-private)<br>
   * Attempts to determine the transmission mode of the reader whose name is provided.<br>
   * This determination is made by a test based on regular expressions provided by the application
   * in parameter to the plugin (see {@link #setReaderNameFilter(boolean, String)})
   *
   * @param readerName A string containing the reader name (must be not empty).
   * @return True if the reader is contactless, false if not.
   * @throws IllegalStateException if the mode of transmission could not be determined
   * @since 0.9
   */
  boolean isContactless(String readerName) {

    Pattern p;
    p = Pattern.compile(contactReaderRegexFilter);
    if (p.matcher(readerName).matches()) {
      return false;
    }
    p = Pattern.compile(contactlessReaderRegexFilter);
    if (p.matcher(readerName).matches()) {
      return true;
    }
    throw new IllegalStateException(
        "Unable to determine the transmission mode for reader " + readerName);
  }

  /**
   * (package-private) Sets the plugin observation exception handler
   *
   * @param pluginObservationExceptionHandler A not null reference to an object implementing {@link
   *     PluginObservationExceptionHandler}
   */
  void setPluginObservationExceptionHandler(
      PluginObservationExceptionHandler pluginObservationExceptionHandler) {
    this.pluginObservationExceptionHandler = pluginObservationExceptionHandler;
  }

  /**
   * (package-private) Sets the reader observation exception handler
   *
   * @param readerObservationExceptionHandler A not null reference to an object implementing {@link
   *     ReaderObservationExceptionHandler}
   */
  void setReaderObservationExceptionHandler(
      ReaderObservationExceptionHandler readerObservationExceptionHandler) {
    this.readerObservationExceptionHandler = readerObservationExceptionHandler;
  }

  /** {@inheritDoc} */
  @Override
  protected PluginObservationExceptionHandler getObservationExceptionHandler() {
    return pluginObservationExceptionHandler;
  }
}
