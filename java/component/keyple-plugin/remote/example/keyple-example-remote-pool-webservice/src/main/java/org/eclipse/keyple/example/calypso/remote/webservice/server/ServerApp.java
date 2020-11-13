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
package org.eclipse.keyple.example.calypso.remote.webservice.server;

import java.util.concurrent.Executors;
import javax.enterprise.context.ApplicationScoped;

import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.ReaderPoolPlugin;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.core.util.NamedThreadFactory;
import org.eclipse.keyple.example.calypso.remote.webservice.client.ClientApp;
import org.eclipse.keyple.plugin.remote.PoolLocalServiceServer;
import org.eclipse.keyple.plugin.remote.impl.PoolLocalServiceServerFactory;
import org.eclipse.keyple.plugin.remote.impl.RemotePluginServerFactory;
import org.eclipse.keyple.plugin.stub.*;
import org.eclipse.keyple.remote.example.card.StubCalypsoClassic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Example of a server side app */
@ApplicationScoped
public class ServerApp {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerApp.class);

  ReaderPoolPlugin poolPlugin;
  Reader nativeReader;
  PoolLocalServiceServer localService;

  /**
   * Initialize the StubPoolPlugin
   */
  public void init() {
    initStubReader();

    localService = new PoolLocalServiceServerFactory()
            .builder()
            .withSyncNode()
            .withPoolPlugins(poolPlugin.getName())
            .getService();
  }

  /** Init Native Reader with a Stub plugin with a inserted card */
  private void initStubReader() {
    String STUB_PLUGIN_NAME = "stubPoolPlugin";
    String STUB_READER_NAME = "stubReader";
    String REFERENCE_GROUP = "group1";

    // register plugin
    poolPlugin = (StubPoolPlugin)
            SmartCardService.getInstance().registerPlugin(new StubPoolPluginFactory(STUB_PLUGIN_NAME));

    // configure native reader
    ((StubPoolPlugin)poolPlugin).plugStubPoolReader(REFERENCE_GROUP,STUB_READER_NAME, new StubCalypsoClassic());

    // retrieve the connected the reader
    nativeReader = poolPlugin.getReader(STUB_READER_NAME);

    // configure the procotol ISO_14443_4
    nativeReader.activateProtocol(
            StubSupportedProtocols.ISO_14443_4.name(),
            ContactlessCardCommonProtocols.ISO_14443_4.name());

    LOGGER.info(
            "Client - Native reader was configured with STUB reader : {} with a card",
            nativeReader.getName());
  }
}
