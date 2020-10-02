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
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.example.common.ReaderUtilities;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
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
    ((PcscReader) poReader)
        .setTransmissionMode(TransmissionMode.CONTACTLESS)
        .setIsoProtocol(PcscReader.IsoProtocol.T1)
        .setSharingMode(PcscReader.SharingMode.SHARED);

    ((PcscReader) samReader)
        .setTransmissionMode(TransmissionMode.CONTACTS)
        .setIsoProtocol(PcscReader.IsoProtocol.T0)
        .setSharingMode(PcscReader.SharingMode.SHARED);

    /* Activate additional protocol */
    poReader.activateProtocol(SeCommonProtocols.PROTOCOL_B_PRIME);

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
