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

import org.eclipse.keyple.core.service.*;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.PluginObservationExceptionHandler;
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler;
import org.eclipse.keyple.core.service.exception.KeypleException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.service.util.ContactCardCommonProtocols;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.example.calypso.local.common.PcscReaderUtilities;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.plugin.pcsc.PcscSupportedContactProtocols;
import org.eclipse.keyple.plugin.pcsc.PcscSupportedContactlessProtocols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo_CalypsoClassic_Pcsc {

  /**
   * This object is used to freeze the main thread while card operations are handle through the
   * observers callbacks. A call to the notify() method would end the program (not demonstrated
   * here).
   */
  private static final Object waitForEnd = new Object();

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

    /* Setting up the transaction engine (implements Observer) */
    CalypsoClassicTransactionEngine transactionEngine = new CalypsoClassicTransactionEngine();

    /*
     * Get PO and SAM readers. Apply regulars expressions to reader names to select PO / SAM
     * readers. Use the getReader helper method from the transaction engine.
     */
    Reader poReader = null;
    Reader samReader = null;
    poReader = plugin.getReader(PcscReaderUtilities.getContactlessReaderName());
    samReader = plugin.getReader(PcscReaderUtilities.getContactReaderName());

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

    // unregister plugin
    smartCardService.unregisterPlugin(plugin.getName());

    logger.info("Exit program.");
  }
}
