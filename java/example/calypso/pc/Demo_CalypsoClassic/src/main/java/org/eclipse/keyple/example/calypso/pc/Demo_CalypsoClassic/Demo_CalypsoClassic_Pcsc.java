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
package org.eclipse.keyple.example.calypso.pc.Demo_CalypsoClassic;

import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.example.common.ReaderUtilities;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.pcsc.PcscReaderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo_CalypsoClassic_Pcsc {
  /**
   * This object is used to freeze the main thread while card operations are handle through the
   * observers callbacks. A call to the notify() method would end the program (not demonstrated
   * here).
   */
  private static final Object waitForEnd = new Object();

  /**
   * main program entry
   *
   * @param args the program arguments
   * @throws KeypleException setParameter exception
   * @throws InterruptedException thread exception
   */
  public static void main(String[] args) throws InterruptedException {
    Logger logger = LoggerFactory.getLogger(Demo_CalypsoClassic_Pcsc.class);

    /* Get the instance of the SeProxyService (Singleton pattern) */
    SeProxyService seProxyService = SeProxyService.getInstance();

    /* Assign PcscPlugin to the SeProxyService */
    ReaderPlugin readerPlugin = seProxyService.registerPlugin(new PcscPluginFactory());

    /* Setting up the transaction engine (implements Observer) */
    CalypsoClassicTransactionEngine transactionEngine = new CalypsoClassicTransactionEngine();

    /*
     * Get PO and SAM readers. Apply regulars expressions to reader names to select PO / SAM
     * readers. Use the getReader helper method from the transaction engine.
     */
    SeReader poReader = null;
    SeReader samReader = null;
    try {
      poReader = readerPlugin.getReader(ReaderUtilities.getContactlessReaderName());
      samReader = readerPlugin.getReader(ReaderUtilities.getContactReaderName());
    } catch (KeypleReaderNotFoundException e) {
      e.printStackTrace();
    }

    /* Both readers are expected not null */
    if (poReader == samReader || poReader == null || samReader == null) {
      throw new IllegalStateException("Bad PO/SAM setup");
    }

    logger.info("PO Reader  NAME = {}", poReader.getName());
    logger.info("SAM Reader  NAME = {}", samReader.getName());

    /* Set PcSc settings per reader */
    poReader.setParameter(PcscReaderConstants.PROTOCOL_KEY, PcscReaderConstants.PROTOCOL_VAL_T1);
    samReader.setParameter(PcscReaderConstants.PROTOCOL_KEY, PcscReaderConstants.PROTOCOL_VAL_T0);

    poReader.setParameter(
        PcscReaderConstants.TRANSMISSION_MODE_KEY,
        PcscReaderConstants.TRANSMISSION_MODE_VAL_CONTACTLESS);
    samReader.setParameter(
        PcscReaderConstants.TRANSMISSION_MODE_KEY,
        PcscReaderConstants.TRANSMISSION_MODE_VAL_CONTACTS);

    /*
     * PC/SC card access mode:
     *
     * The SAM is left in the SHARED mode (by default) to avoid automatic resets due to the
     * limited time between two consecutive exchanges granted by Windows.
     *
     * This point will be addressed in a coming release of the Keyple PcSc reader plugin.
     *
     * The PO reader is set to EXCLUSIVE mode to avoid side effects (on OS Windows 8+) during
     * the selection step that may result in session failures.
     *
     * See KEYPLE-CORE.PC.md file to learn more about this point.
     *
     */
    samReader.setParameter(PcscReaderConstants.MODE_KEY, PcscReaderConstants.MODE_VAL_SHARED);
    poReader.setParameter(PcscReaderConstants.MODE_KEY, PcscReaderConstants.MODE_VAL_SHARED);

    /* Set the PO reader protocol flag */
    poReader.addSeProtocolSetting(
        SeCommonProtocols.PROTOCOL_ISO14443_4,
        PcscProtocolSetting.PCSC_PROTOCOL_SETTING.get(SeCommonProtocols.PROTOCOL_ISO14443_4));
    poReader.addSeProtocolSetting(
        SeCommonProtocols.PROTOCOL_B_PRIME,
        PcscProtocolSetting.PCSC_PROTOCOL_SETTING.get(SeCommonProtocols.PROTOCOL_B_PRIME));

    samReader.addSeProtocolSetting(
        SeCommonProtocols.PROTOCOL_ISO7816_3,
        PcscProtocolSetting.PCSC_PROTOCOL_SETTING.get(SeCommonProtocols.PROTOCOL_ISO7816_3));

    /* Assign the readers to the Calypso transaction engine */
    transactionEngine.setReaders(poReader, samReader);

    /* Set terminal as Observer of the first reader */
    ((ObservableReader) poReader).addObserver((ObservableReader.ReaderObserver) transactionEngine);

    /* Set the default selection operation */
    ((ObservableReader) poReader)
        .setDefaultSelectionRequest(
            transactionEngine.preparePoSelection(),
            ObservableReader.NotificationMode.MATCHED_ONLY,
            ObservableReader.PollingMode.REPEATING);

    /* Wait for ever (exit with CTRL-C) */
    synchronized (waitForEnd) {
      waitForEnd.wait();
    }
  }
}
