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
package org.eclipse.keyple.example.calypso.remote.webservice.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.plugin.pcsc.PcscSupportedProtocols;
import org.eclipse.keyple.plugin.remote.LocalServiceClient;
import org.eclipse.keyple.plugin.remote.RemoteServiceParameters;
import org.eclipse.keyple.plugin.remote.impl.LocalServiceClientFactory;
import org.eclipse.keyple.plugin.stub.*;
import org.eclipse.keyple.remote.example.common.TransactionResult;
import org.eclipse.keyple.remote.example.common.UserInfo;
import org.eclipse.keyple.remote.example.util.CalypsoSmartCard;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Example of a client side app */
@ApplicationScoped
public class ClientApp {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientApp.class);

  @Inject @RestClient RemotePluginClient clientEndpoint;

  LocalServiceClient nativeService;
  Plugin nativePlugin;
  Reader nativeReader;

  /**
   * Initialize the client components : {@link StubPlugin} with a {@link StubReader} that accepts
   * {@link org.eclipse.keyple.plugin.stub.StubSmartCard} based on protocol
   * SeCommonProtocols.PROTOCOL_ISO14443_4. Initialize the nativeSeService with a sync endpoint
   * {@link RemotePluginClient}
   */
  public void init() {

    // init native plugin and reader with a stub reader
    initStubReader();

    // init native plugin and reader with a pcsc reader
    // initPcscReader();

    // init native service
    nativeService =
        new LocalServiceClientFactory()
            .builder()
            .withSyncNode(clientEndpoint)
            .withoutReaderObservation()
            .getService();
  }

  /**
   * Execute a simple scenario : insert a card and invokes a remote service
   *
   * @return true if the transaction was successful
   */
  public Boolean launchScenario() {
    UserInfo user1 = new UserInfo().setUserId("test");

    // execute remote service
    TransactionResult output =
        nativeService.executeRemoteService(
            RemoteServiceParameters.builder(
                    "EXECUTE_CALYPSO_SESSION_FROM_REMOTE_SELECTION", nativeReader)
                .withUserInputData(user1)
                .build(),
            TransactionResult.class);

    return output.isSuccessful();
  }

  /** Init Native Reader with a Stub plugin with a inserted card */
  private void initStubReader() {
    String STUB_PLUGIN_NAME = "stubPlugin";
    String STUB_READER_NAME = "stubReader";

    // register plugin
    nativePlugin =
        SmartCardService.getInstance().registerPlugin(new StubPluginFactory(STUB_PLUGIN_NAME));

    // configure native reader
    ((StubPlugin) nativePlugin).plugStubReader(STUB_READER_NAME, true);

    // retrieve the connected the reader
    nativeReader = nativePlugin.getReaders().values().iterator().next();

    // configure the procotol ISO_14443_4
    nativeReader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    // insert a Stub card
    ((StubReader) nativeReader).insertCard(new CalypsoSmartCard());

    LOGGER.info(
        "Client - Native reader was configured with STUB reader : {} with a card",
        nativeReader.getName());
  }

  /**
   * Init Native Reader with a connected PCSC plugin whose name is PCSC_READER_NAME and an inserted
   * SE
   */
  private void initPcscReader() {

    // register plugin
    nativePlugin = SmartCardService.getInstance().registerPlugin(new PcscPluginFactory());

    if (nativePlugin.getReaders().size() != 1) {
      throw new IllegalStateException(
          "For the matter of this example, we expect one and only one pcsc reader to be connected");
    }

    // retrieve the connected the reader
    nativeReader = nativePlugin.getReaders().values().iterator().next();

    if (!nativeReader.isCardPresent()) {
      throw new IllegalStateException(
          "For the matter of this example, we expect a card to be present at the startup");
    }

    // configure pcsc specific configuration to handle contactless
    ((PcscReader) nativeReader).setIsoProtocol(PcscReader.IsoProtocol.T1);

    // configure the procotol settings
    nativeReader.activateProtocol(
        PcscSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    LOGGER.info(
        "Client - Native reader was configured with PCSC reader : {} with a card",
        nativeReader.getName());
  }
}
