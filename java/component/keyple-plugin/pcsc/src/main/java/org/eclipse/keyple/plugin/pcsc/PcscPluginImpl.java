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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.plugin.AbstractThreadedObservablePlugin;
import org.eclipse.keyple.core.seproxy.plugin.reader.AbstractReader;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the {@link PcscPlugin} interface.<br>
 * (package-private)
 *
 * @since 0.9
 */
final class PcscPluginImpl extends AbstractThreadedObservablePlugin implements PcscPlugin {

  private static final Logger logger = LoggerFactory.getLogger(PcscPluginImpl.class);

  private boolean scardNoServiceHackNeeded;

  private String contactReaderRegexFilter = "";
  private String contactlessReaderRegexFilter = "";

  /**
   * Singleton instance of SeProxyService 'volatile' qualifier ensures that read access to the
   * object will only be allowed once the object has been fully initialized.
   *
   * <p>This qualifier is required for "lazy-singleton" pattern with double-check method, to be
   * thread-safe.
   */
  private static volatile PcscPluginImpl instance; // NOSONAR: lazy-singleton pattern.

  private PcscPluginImpl() {
    super(PcscPluginConstants.PLUGIN_NAME);
  }

  /**
   * Gets the single instance of PcscPlugin.
   *
   * @return single instance of PcscPlugin
   * @throws KeypleReaderException if a reader error occurs
   * @since 0.9
   */
  public static PcscPluginImpl getInstance() {
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
   * Fetch the list of connected native reader (from smartcard.io) and returns their names
   *
   * @return A {@link SortedSet} of {@link String}
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   * @since 0.9
   */
  @Override
  public SortedSet<String> fetchNativeReadersNames() {
    SortedSet<String> nativeReadersNames = new ConcurrentSkipListSet<String>();
    CardTerminals terminals = getCardTerminals();
    try {
      for (CardTerminal term : terminals.list()) {
        nativeReadersNames.add(term.getName());
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
  protected Map<String, SeReader> initNativeReaders() {
    ConcurrentMap<String, SeReader> nativeReaders = new ConcurrentHashMap<String, SeReader>();

    // activate a special processing for the "SCARD_E_NO_NO_SERVICE" exception (on Windows platforms
    // the removal of the last PC/SC reader stops the "Windows Smart Card service")
    String OS = System.getProperty("os.name").toLowerCase();
    scardNoServiceHackNeeded = OS.indexOf("win") >= 0;
    logger.info("System detected : {}", scardNoServiceHackNeeded);

    // parse the current readers list to create the ProxyReader(s) associated with new reader(s)
    CardTerminals terminals = getCardTerminals();
    logger.trace("[{}] initNativeReaders => CardTerminal in list: {}", this.getName(), terminals);
    try {
      for (CardTerminal term : terminals.list()) {
        final PcscReaderImpl pcscReader = new PcscReaderImpl(this.getName(), term);
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
        // org.eclipse.keyple.core.seproxy.exception.KeypleRuntimeException

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
   * @return A not null reference to a {@link SeReader}.
   * @throws KeypleReaderNotFoundException if a reader is not found by its name
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   * @since 0.9
   */
  @Override
  protected SeReader fetchNativeReader(String name) {
    // return the current reader if it is already listed
    SeReader seReader = readers.get(name);
    if (seReader != null) {
      return seReader;
    }
    // parse the current PC/SC readers list to create the ProxyReader(s) associated with new
    // reader(s)
    CardTerminals terminals = getCardTerminals();
    try {
      for (CardTerminal term : terminals.list()) {
        if (term.getName().equals(name)) {
          logger.trace(
              "[{}] fetchNativeReader => CardTerminal in new PcscReader: {}",
              this.getName(),
              terminals);
          seReader = new PcscReaderImpl(this.getName(), term);
        }
      }
    } catch (CardException e) {
      throw new KeypleReaderIOException("Could not access terminals list", e);
    }
    if (seReader == null) {
      throw new KeypleReaderNotFoundException("Reader " + name + " not found!");
    }
    return seReader;
  }

  private CardTerminals getCardTerminals() {
    if (scardNoServiceHackNeeded) {
      // This hack avoids the problem of stopping the Windows Smart Card service when removing the
      // last PC/SC reader.
      //
      // Some SONAR warnings have been disabled.
      try {
        Class<?> pcscterminal;
        pcscterminal = Class.forName("sun.security.smartcardio.PCSCTerminals");
        Field contextId = pcscterminal.getDeclaredField("contextId");
        contextId.setAccessible(true); // NOSONAR

        if (contextId.getLong(pcscterminal) != 0L) {
          Class<?> pcsc = Class.forName("sun.security.smartcardio.PCSC");
          Method SCardEstablishContext =
              pcsc.getDeclaredMethod("SCardEstablishContext", new Class[] {Integer.TYPE});
          SCardEstablishContext.setAccessible(true); // NOSONAR

          Field SCARD_SCOPE_USER = pcsc.getDeclaredField("SCARD_SCOPE_USER");
          SCARD_SCOPE_USER.setAccessible(true); // NOSONAR

          long newId =
              ((Long)
                      SCardEstablishContext.invoke(
                          pcsc, new Object[] {Integer.valueOf(SCARD_SCOPE_USER.getInt(pcsc))}))
                  .longValue();
          contextId.setLong(pcscterminal, newId); // NOSONAR

          // clear the terminals in cache
          TerminalFactory factory = TerminalFactory.getDefault();
          CardTerminals terminals = factory.terminals();
          Field fieldTerminals = pcscterminal.getDeclaredField("terminals");
          fieldTerminals.setAccessible(true); // NOSONAR
          Class<?> classMap = Class.forName("java.util.Map");
          Method clearMap = classMap.getDeclaredMethod("clear");

          clearMap.invoke(fieldTerminals.get(terminals));
        }
      } catch (Exception e) {
        logger.error("Unexpected exception.", e);
      }
    }

    return TerminalFactory.getDefault().terminals();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void setReaderNameFilter(TransmissionMode transmissionMode, String readerNameFilter) {
    Assert.getInstance().notNull(transmissionMode, "transmissionMode");
    Assert.getInstance().notEmpty(readerNameFilter, "readerNameFilter");
    if (TransmissionMode.CONTACTLESS.equals(transmissionMode)) {
      contactlessReaderRegexFilter = readerNameFilter;
    } else {
      contactReaderRegexFilter = readerNameFilter;
    }
  }

  /**
   * (package-private)<br>
   * Attempts to determine the transmission mode of the reader whose name is provided.<br>
   * This determination is made by a test based on regular expressions provided by the application
   * in parameter to the plugin (see {@link #setReaderNameFilter(TransmissionMode, String)})
   *
   * @param readerName A string containing the reader name (must be not empty).
   * @return The {@link TransmissionMode}.
   * @throws IllegalStateException if the mode of transmission could not be determined
   * @since 0.9
   */
  TransmissionMode findTransmissionMode(String readerName) {
    Pattern p;
    p = Pattern.compile(contactReaderRegexFilter);
    if (p.matcher(readerName).matches()) {
      return TransmissionMode.CONTACTS;
    }
    p = Pattern.compile(contactlessReaderRegexFilter);
    if (p.matcher(readerName).matches()) {
      return TransmissionMode.CONTACTLESS;
    }
    throw new IllegalStateException(
        "Unable to determine the transmission mode for reader " + readerName);
  }
}
