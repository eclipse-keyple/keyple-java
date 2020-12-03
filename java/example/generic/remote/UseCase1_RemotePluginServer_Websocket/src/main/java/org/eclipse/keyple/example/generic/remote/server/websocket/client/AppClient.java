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
package org.eclipse.keyple.example.generic.remote.server.websocket.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.example.generic.remote.server.websocket.util.CalypsoSmartCard;
import org.eclipse.keyple.example.generic.remote.server.websocket.util.UserInputDataDto;
import org.eclipse.keyple.example.generic.remote.server.websocket.util.UserOutputDataDto;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.plugin.pcsc.PcscSupportedContactlessProtocols;
import org.eclipse.keyple.plugin.remote.LocalServiceClient;
import org.eclipse.keyple.plugin.remote.RemoteServiceParameters;
import org.eclipse.keyple.plugin.remote.impl.LocalServiceClientFactory;
import org.eclipse.keyple.plugin.remote.impl.LocalServiceClientUtils;
import org.eclipse.keyple.plugin.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Example of a client side application. */
@ApplicationScoped
public class AppClient {

  private static final Logger logger = LoggerFactory.getLogger(AppClient.class);

  /** The endpoint client */
  @Inject EndpointClient endpointClient;

  /** The local plugin */
  private Plugin plugin;

  /** The local reader */
  private Reader reader;

  /**
   * Initialize the client components :
   *
   * <ul>
   *   <li>A {@link StubPlugin} with a {@link StubReader} that accepts {@link
   *       org.eclipse.keyple.plugin.stub.StubSmartCard} based on protocol ISO14443_4,
   *   <li>A {@link LocalServiceClient} with an async node bind to a {@link
   *       org.eclipse.keyple.plugin.remote.spi.AsyncEndpointClient} endpoint.
   * </ul>
   */
  public void init() {

    // Init a local plugin and reader with a stub reader.
    initStubReader();

    // Init a local plugin and reader with a PCSC reader.
    // initPcscReader();

    // Init the local service using the associated factory.
    LocalServiceClientFactory.builder()
        .withDefaultServiceName()
        .withAsyncNode(endpointClient)
        .usingDefaultTimeout()
        .withoutReaderObservation()
        .getService();
  }

  /**
   * Executes a simple scenario : insert a card and invokes a remote service.
   *
   * @return true if the transaction was successful
   */
  public Boolean launchScenario() {

    // Builds the user input data if needed.
    UserInputDataDto userInputData = new UserInputDataDto().setUserId("test");

    // Builds the parameters to send to the server.
    RemoteServiceParameters params =
        RemoteServiceParameters.builder("EXECUTE_CALYPSO_SESSION_FROM_REMOTE_SELECTION", reader)
            .withUserInputData(userInputData)
            .build();

    // Retrieves the local service.
    LocalServiceClient localService = LocalServiceClientUtils.getLocalService();

    // Executes on the local reader the remote ticketing service having the id
    // "EXECUTE_CALYPSO_SESSION_FROM_REMOTE_SELECTION".
    UserOutputDataDto output = localService.executeRemoteService(params, UserOutputDataDto.class);

    return output.isSuccessful();
  }

  /** Init a local plugin and reader with a stub reader and an inserted card */
  private void initStubReader() {

    // Registers the plugin to the smart card service.
    plugin =
        SmartCardService.getInstance()
            .registerPlugin(new StubPluginFactory("stubPlugin", null, null));

    // Plug the reader manually to the plugin.
    ((StubPlugin) plugin).plugReader("stubReader", true);

    // Retrieves the connected reader from the plugin.
    reader = plugin.getReaders().values().iterator().next();

    // Activates the protocol ISO_14443_4 on the reader.
    reader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    // Insert a stub card manually on the reader.
    ((StubReader) reader).insertCard(new CalypsoSmartCard());

    logger.info(
        "Client - Local reader was configured with STUB reader : {} with a card", reader.getName());
  }

  /**
   * Init a local plugin and reader with a PCSC reader whose name is PCSC_READER_NAME and an
   * inserted card.
   */
  private void initPcscReader() {

    // Registers the plugin to the smart card service.
    plugin = SmartCardService.getInstance().registerPlugin(new PcscPluginFactory(null, null));

    if (plugin.getReaders().size() != 1) {
      throw new IllegalStateException(
          "For the matter of this example, we expect one and only one PCSC reader to be connected");
    }

    // Retrieves the connected reader from the plugin.
    reader = plugin.getReaders().values().iterator().next();

    if (!reader.isCardPresent()) {
      throw new IllegalStateException(
          "For the matter of this example, we expect a card to be present at the startup");
    }

    // Sets PCSC specific configuration to handle contactless.
    ((PcscReader) reader).setIsoProtocol(PcscReader.IsoProtocol.T1);

    // Activates the protocol ISO_14443_4 on the reader.
    reader.activateProtocol(
        PcscSupportedContactlessProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    logger.info(
        "Client - Local reader was configured with PCSC reader : {} with a card", reader.getName());
  }
}
