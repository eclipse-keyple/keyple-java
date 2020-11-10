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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import org.eclipse.keyple.core.service.exception.KeypleReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of AbstractPcscPlugin suitable for Windows platforms.
 *
 * <p>Windows 8/10 platforms have a problem in the management of the smart card service combined
 * with Java smartcard.io. <br>
 * The service is stopped when the last connected reader is removed; this prevents the detection of
 * any new connection (SCARD_E_NO_SERVICE CardException). To overcome this problem a hack using
 * reflexivity is used to reset internal variables of smartcard.io.
 */
public final class PcscPluginWinImpl extends AbstractPcscPlugin {

  private static final Logger logger = LoggerFactory.getLogger(PcscPluginWinImpl.class);

  /**
   * Singleton instance of SmartCardService 'volatile' qualifier ensures that read access to the
   * object will only be allowed once the object has been fully initialized.
   *
   * <p>This qualifier is required for "lazy-singleton" pattern with double-check method, to be
   * thread-safe.
   */
  private static volatile PcscPluginWinImpl instance; // NOSONAR: lazy-singleton pattern.

  /**
   * Gets the single instance of PcscPluginWinImpl.
   *
   * @return single instance of PcscPluginWinImpl
   * @throws KeypleReaderException if a reader error occurs
   * @since 0.9
   */
  static PcscPluginWinImpl getInstance() {
    if (instance == null) {
      synchronized (PcscPluginWinImpl.class) {
        if (instance == null) {
          instance = new PcscPluginWinImpl();
        }
      }
    }
    return instance;
  }

  /**
   * In the case of a Windows platform, the retrieval of the list of terminals is preceded by an
   * operation on fields internal to the smartcard.io classes intended to compensate for the loss of
   * smartcard service when the last reader is removed.<br>
   *
   * <p>The service it refers to is the Windows Smart Card service, also known as the smart card
   * resource manager. If you open the Services MMC console you'll see it there with the startup
   * type set to Manual (Trigger Start). In Windows 8 this service was changed to run only while a
   * smart card reader is attached to the system (to save resources) and the service is
   * automatically stopped when the last reader is removed. Stopping the service invalidates any
   * outstanding handles.
   *
   * <p>The native Windows solution is to call SCardAccessStartedEvent and use the handle it returns
   * to wait for the service to start before using SCardEstablishContext to connect to the resource
   * manager again.<br>
   * <a
   * href="https://bugs.openjdk.java.net/browse/JDK-8026326">https://bugs.openjdk.java.net/browse/JDK-8026326</a>
   * <br>
   * <a href="https://stackoverflow.com/a/17209132">https://stackoverflow.com/a/17209132</a><br>
   * {@inheritDoc}
   */
  protected CardTerminals getCardTerminals() {

    /* Some SONAR warnings have been disabled. This code should be reviewed carefully. */
    try {
      Class<?> pcscterminal;
      pcscterminal = Class.forName("sun.security.smartcardio.PCSCTerminals");
      Field contextId = pcscterminal.getDeclaredField("contextId");
      contextId.setAccessible(true); // NOSONAR

      if (contextId.getLong(pcscterminal) != 0L) {

        Class<?> pcsc = Class.forName("sun.security.smartcardio.PCSC");
        Method sCardEstablishContext =
            pcsc.getDeclaredMethod("SCardEstablishContext", Integer.TYPE);
        sCardEstablishContext.setAccessible(true); // NOSONAR

        Field sCardScopeUser = pcsc.getDeclaredField("SCARD_SCOPE_USER");
        sCardScopeUser.setAccessible(true); // NOSONAR

        long newId = (Long) sCardEstablishContext.invoke(pcsc, sCardScopeUser.getInt(pcsc));
        contextId.setLong(pcscterminal, newId); // NOSONAR

        // clear the terminals in cache
        TerminalFactory factory = TerminalFactory.getDefault();
        CardTerminals terminals = factory.terminals();

        Field fieldTerminals = pcscterminal.getDeclaredField("terminals");
        fieldTerminals.setAccessible(true); // NOSONAR
        ((Map) fieldTerminals.get(terminals)).clear();
      }
    } catch (Exception e) {
      logger.error("Unexpected exception.", e);
    }

    return TerminalFactory.getDefault().terminals();
  }

  /** {@inheritDoc} */
  @Override
  protected AbstractPcscReader createReader(String name, CardTerminal terminal) {
    return new PcscReaderImpl(name, terminal);
  }
}
