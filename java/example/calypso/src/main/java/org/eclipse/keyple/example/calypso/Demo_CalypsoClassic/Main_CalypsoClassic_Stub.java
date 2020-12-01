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
package org.eclipse.keyple.example.calypso.Demo_CalypsoClassic;

import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoIllegalArgumentException;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.AbstractDefaultSelectionsResponse;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.exception.KeyplePluginInstantiationException;
import org.eclipse.keyple.core.service.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.example.calypso.common.CalypsoClassicInfo;
import org.eclipse.keyple.example.calypso.common.StubCalypsoClassic;
import org.eclipse.keyple.example.calypso.common.StubSamCalypsoClassic;
import org.eclipse.keyple.plugin.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Calypso demonstration code consists in:
 *
 * <ol>
 *   <li>Setting up a sam reader configuration and adding an observer method ({@link
 *       CardReaderObserver#update})
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
public class Main_CalypsoClassic_Stub {

  private static Logger logger = LoggerFactory.getLogger(Main_CalypsoClassic_Stub.class);

  private static Plugin plugin;
  /**
   * main program entry
   *
   * @param args the program arguments
   * @throws InterruptedException thread exception
   * @throws KeyplePluginNotFoundException if the Stub plugin is not found
   * @throws KeyplePluginInstantiationException if the instantiation of the Stub plugin fails
   * @throws CalypsoPoIllegalArgumentException if an command argument is wrong
   */
  public static void main(String[] args) throws InterruptedException {

    /* Get the instance of the SmartCardService (Singleton pattern) */
    SmartCardService smartCardService = SmartCardService.getInstance();

    final String STUB_PLUGIN_NAME = "stub1";

    /* Register Stub plugin in the platform */
    plugin = smartCardService.registerPlugin(new StubPluginFactory(STUB_PLUGIN_NAME, null, null));

    StubReader poReader = initPoReader();

    StubReader samReader = initSamReader();

    /* Both readers are expected not null */
    if (poReader == samReader) {
      throw new IllegalStateException("Bad PO/SAM setup");
    }

    /* Setting up the observer on the PO Reader */
    CardReaderObserver poEventObserver = new CardReaderObserver();

    /* Assign readers to the Hoplink transaction engine */
    poEventObserver.setSamReader(samReader);

    /* Set the readerObserver as Observer of the PO reader */
    ((ObservableReader) poReader).addObserver(poEventObserver);

    /* Create 'virtual' Hoplink and SAM card */
    StubSmartCard calypsoStubCard = new StubCalypsoClassic();

    logger.info("Insert stub PO card.");
    poReader.insertCard(calypsoStubCard);

    Thread.sleep(1000);

    /* Remove card */
    logger.info("Remove stub SAM and PO cards.");

    poReader.removeCard();
    samReader.removeCard();

    // unregister plugin
    smartCardService.unregisterPlugin(plugin.getName());

    logger.info("Exit program.");

    System.exit(0);
  }

  private static StubReader initPoReader() {
    /*
     * Plug PO and SAM stub readers.
     */
    ((StubPlugin) plugin).plugReader("poReader", true);

    StubReader poReader = (StubReader) (plugin.getReader("poReader"));

    logger.info("PO Reader  NAME = {}", poReader.getName());

    /* Activate protocols */
    poReader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.INNOVATRON_B_PRIME_CARD.name());
    poReader.activateProtocol(
        StubSupportedProtocols.INNOVATRON_B_PRIME_CARD.name(),
        ContactlessCardCommonProtocols.INNOVATRON_B_PRIME_CARD.name());

    /* Set the default selection operation */
    poReader.setDefaultSelectionRequest(
        CardSelectionConfig.getPoDefaultCardSelection().getDefaultSelectionsRequest(),
        ObservableReader.NotificationMode.MATCHED_ONLY,
        ObservableReader.PollingMode.REPEATING);

    return poReader;
  }

  private static StubReader initSamReader() {
    // Get and configure the SAM reader
    ((StubPlugin) plugin).plugReader("samReader", true);

    StubReader samReader = (StubReader) (plugin.getReader("samReader"));
    logger.info("SAM Reader  NAME = {}", samReader.getName());

    StubSmartCard samSE = new StubSamCalypsoClassic();

    /* Insert the SAM into the SAM reader */
    logger.info("Insert stub SAM card.");
    samReader.insertCard(samSE);

    return samReader;
  }
}
