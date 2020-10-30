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

import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoIllegalArgumentException;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.exception.KeyplePluginInstantiationException;
import org.eclipse.keyple.core.service.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.example.common.calypso.stub.StubCalypsoClassic;
import org.eclipse.keyple.example.common.calypso.stub.StubSamCalypsoClassic;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubPluginFactory;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.plugin.stub.StubSecureElement;
import org.eclipse.keyple.plugin.stub.StubSupportedProtocols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo_CalypsoClassic_Stub {

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
    final Logger logger = LoggerFactory.getLogger(Demo_CalypsoClassic_Stub.class);

    /* Get the instance of the SmartCardService (Singleton pattern) */
    SmartCardService smartCardService = SmartCardService.getInstance();

    final String STUB_PLUGIN_NAME = "stub1";

    /* Register Stub plugin in the platform */
    Plugin stubPlugin = smartCardService.registerPlugin(new StubPluginFactory(STUB_PLUGIN_NAME));

    /* Setting up the transaction engine (implements Observer) */
    CalypsoClassicTransactionEngine transactionEngine = new CalypsoClassicTransactionEngine();

    /*
     * Plug PO and SAM stub readers.
     */
    ((StubPlugin) stubPlugin).plugStubReader("poReader", true);
    ((StubPlugin) stubPlugin).plugStubReader("samReader", true);

    StubReader poReader = null;
    StubReader samReader = null;
    try {
      poReader = (StubReader) (stubPlugin.getReader("poReader"));
      samReader = (StubReader) (stubPlugin.getReader("samReader"));
    } catch (KeypleReaderNotFoundException e) {
      e.printStackTrace();
    }

    /* Both readers are expected not null */
    if (poReader == samReader || poReader == null || samReader == null) {
      throw new IllegalStateException("Bad PO/SAM setup");
    }

    logger.info("PO Reader  NAME = {}", poReader.getName());
    logger.info("SAM Reader  NAME = {}", samReader.getName());

    /* Activate protocols */
    poReader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.CALYPSO_OLD_CARD_PRIME.name());
    poReader.activateProtocol(
        StubSupportedProtocols.CALYPSO_OLD_CARD_PRIME.name(),
        ContactlessCardCommonProtocols.CALYPSO_OLD_CARD_PRIME.name());

    /* Assign readers to the Hoplink transaction engine */
    transactionEngine.setReaders(poReader, samReader);

    /* Create 'virtual' Hoplink and SAM card */
    StubSecureElement calypsoStubCard = new StubCalypsoClassic();
    StubSecureElement samSE = new StubSamCalypsoClassic();

    /* Insert the SAM into the SAM reader */
    logger.info("Insert stub SAM card.");
    samReader.insertSe(samSE);

    /* Set the default selection operation */
    poReader.setDefaultSelectionRequest(
        transactionEngine.preparePoSelection(),
        ObservableReader.NotificationMode.MATCHED_ONLY,
        ObservableReader.PollingMode.REPEATING);

    /* Set the transactionEngine as Observer of the PO reader */
    poReader.addObserver(transactionEngine);

    logger.info("Insert stub PO card.");
    poReader.insertSe(calypsoStubCard);

    Thread.sleep(1000);

    /* Remove card */
    logger.info("Remove stub SAM and PO cards.");

    poReader.removeSe();
    samReader.removeSe();

    logger.info("END.");

    System.exit(0);
  }
}
