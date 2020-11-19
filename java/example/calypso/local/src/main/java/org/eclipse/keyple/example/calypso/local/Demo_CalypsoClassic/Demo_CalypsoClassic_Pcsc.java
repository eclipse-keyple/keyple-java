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
package org.eclipse.keyple.example.calypso.local.Demo_CalypsoClassic;

import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.AbstractDefaultSelectionsResponse;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.PluginObservationExceptionHandler;
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler;
import org.eclipse.keyple.core.service.exception.KeypleException;
import org.eclipse.keyple.core.service.util.ContactCardCommonProtocols;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.example.calypso.local.Demo_CalypsoClassic.PoReaderConfig.PoReaderObserver;
import org.eclipse.keyple.example.calypso.local.common.CalypsoClassicInfo;
import org.eclipse.keyple.example.calypso.local.common.PcscReaderUtils;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.plugin.pcsc.PcscSupportedContactProtocols;
import org.eclipse.keyple.plugin.pcsc.PcscSupportedContactlessProtocols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Calypso demonstration code consists in:
 *
 * <ol>
 *   <li>Setting up a sam reader configuration and adding an observer method ({@link
 *       PoReaderConfig#getObserver()#update})
 *   <li>Starting a card operation when a PO presence is notified (processSeMatch
 *       operateSeTransaction)
 *   <li>Opening a logical channel with the SAM (C1 SAM is expected) see ({@link
 *       CalypsoClassicInfo#SAM_C1_ATR_REGEX SAM_C1_ATR_REGEX})
 *   <li>Attempting to open a logical channel with the PO with 3 options:
 *       <ul>
 *         <li>Selecting with a fake AID (1)
 *         <li>Selecting with the Calypso AID and reading the event log file
 *         <li>Selecting with a fake AID (2)
 *       </ul>
 *   <li>Display {@link AbstractDefaultSelectionsResponse} data
 *   <li>If the Calypso selection succeeded, do a Calypso transaction
 *       ({doCalypsoReadWriteTransaction(PoTransaction, ApduResponse, boolean)}
 *       doCalypsoReadWriteTransaction}).
 * </ol>
 *
 * <p>The Calypso transactions demonstrated here shows the Keyple API in use with Calypso card (PO
 * and SAM).
 *
 * <p>Read the doc of each methods for further details.
 */
public class Demo_CalypsoClassic_Pcsc {

  static class ExceptionHandlerImpl
      implements PluginObservationExceptionHandler, ReaderObservationExceptionHandler {
    final Logger logger = LoggerFactory.getLogger(ExceptionHandlerImpl.class);

    @Override
    public void onPluginObservationError(String pluginName, Throwable throwable) {
      logger.error("An unexpected plugin error occurred: {}", pluginName, throwable);
      synchronized (waitForEnd) {
        waitForEnd.notifyAll();
      }
    }

    @Override
    public void onReaderObservationError(
        String pluginName, String readerName, Throwable throwable) {
      logger.error("An unexpected reader error occurred: {}:{}", pluginName, readerName, throwable);
      synchronized (waitForEnd) {
        waitForEnd.notifyAll();
      }
    }
  }

  /**
   * main program entry
   *
   * @param args the program arguments
   * @throws KeypleException setParameter exception
   * @throws InterruptedException thread exception
   */
  public static void main(String[] args) throws InterruptedException {
    final Logger logger = LoggerFactory.getLogger(Demo_CalypsoClassic_Pcsc.class);

    /* Get the instance of the SmartCardService (Singleton pattern) */
    SmartCardService smartCardService = SmartCardService.getInstance();

    ExceptionHandlerImpl exceptionHandlerImpl = new ExceptionHandlerImpl();

    /* Assign PcscPlugin to the SmartCardService */
    Plugin plugin =
        smartCardService.registerPlugin(
            new PcscPluginFactory(exceptionHandlerImpl, exceptionHandlerImpl));

    /*
     * Get PO and SAM readers. Apply regulars expressions to reader names to select PO / SAM
     * readers. Use the getReader helper method from the transaction engine.
     */
    Reader poReader = plugin.getReader(PcscReaderUtils.getContactlessReaderName());
    Reader samReader = plugin.getReader(PcscReaderUtils.getContactReaderName());

    /* Both readers are expected not null */
    if (poReader == samReader || poReader == null || samReader == null) {
      throw new IllegalStateException("Bad PO/SAM setup");
    }

    logger.info("PO Reader  NAME = {}", poReader.getName());
    logger.info("SAM Reader  NAME = {}", samReader.getName());

    /* Set PcSc settings per reader */
    ((PcscReader) poReader)
        .setContactless(true)
        .setIsoProtocol(PcscReader.IsoProtocol.T1)
        .setSharingMode(PcscReader.SharingMode.SHARED);

    ((PcscReader) samReader)
        .setContactless(false)
        .setIsoProtocol(PcscReader.IsoProtocol.T0)
        .setSharingMode(PcscReader.SharingMode.SHARED);

    /* Activate protocols */
    poReader.activateProtocol(
        PcscSupportedContactlessProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());
    poReader.activateProtocol(
        PcscSupportedContactlessProtocols.INNOVATRON_B_PRIME_CARD.name(),
        ContactlessCardCommonProtocols.INNOVATRON_B_PRIME_CARD.name());

    samReader.activateProtocol(
        PcscSupportedContactProtocols.ISO_7816_3.name(),
        ContactCardCommonProtocols.ISO_7816_3.name());

    /* Setting up the reader observer on the po Reader */
    PoReaderObserver poReaderObserver = PoReaderConfig.getObserver();

    poReaderObserver.setSamReader(samReader);

    /* Set terminal as Observer of the first reader */
    ((ObservableReader) poReader).addObserver(poReaderObserver);

    /* Set the default selection operation */
    ((ObservableReader) poReader)
        .setDefaultSelectionRequest(
            PoReaderConfig.getPoCardSelection().getSelectionOperation(),
            ObservableReader.NotificationMode.MATCHED_ONLY,
            ObservableReader.PollingMode.REPEATING);

    /* Wait for ever (exit with CTRL-C) */
    synchronized (waitForEnd) {
      waitForEnd.wait();
    }

    // unregister plugin
    smartCardService.unregisterPlugin(plugin.getName());

    logger.info("Exit program.");
  }

  /**
   * This object is used to freeze the main thread while card operations are handle through the
   * observers callbacks. A call to the notify() method would end the program (not demonstrated
   * here).
   */
  private static final Object waitForEnd = new Object();
}
