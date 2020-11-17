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

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.ReaderPoolPlugin;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.example.calypso.remote.webservice.util.CalypsoSmartCard;
import org.eclipse.keyple.plugin.remote.PoolLocalServiceServer;
import org.eclipse.keyple.plugin.remote.impl.PoolLocalServiceServerFactory;
import org.eclipse.keyple.plugin.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Server configuration for the {@link PoolLocalServiceServer} example. */
@ApplicationScoped
public class ServerConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfiguration.class);

  ReaderPoolPlugin poolPlugin;
  PoolLocalServiceServer localService;

  /** Initialize the {@link StubPoolPlugin} and the {@link PoolLocalServiceServer} */
  public void init() {
    /*
     * Initiliaze the StubPoolPlugin as the ReaderPoolPlugin
     */
    initStubPoolPlugin();

    /*
     * Initialize the PoolLocalServiceServer
     */
    localService =
        new PoolLocalServiceServerFactory()
            .builder()
            .withSyncNode() // HTTP webservice needs a server sync node configuration
            .withPoolPlugins(poolPlugin.getName()) // use the registered ReaderPoolPlugin
            .getService();
  }

  /** Init {@link ReaderPoolPlugin} with a {@link StubPoolPlugin} with a inserted card */
  private void initStubPoolPlugin() {
    String STUB_PLUGIN_NAME = "stubPoolPlugin";
    String STUB_READER_NAME = "stubReader";
    String REFERENCE_GROUP = "group1";

    // register a StubPoolPlugin
    poolPlugin =
        (StubPoolPlugin)
            SmartCardService.getInstance()
                .registerPlugin(new StubPoolPluginFactory(STUB_PLUGIN_NAME));

    // plug a native reader associated in a group reference
    ((StubPoolPlugin) poolPlugin)
        .plugStubPoolReader(REFERENCE_GROUP, STUB_READER_NAME, new CalypsoSmartCard());

    // retrieve the connected reader
    Reader nativeReader = poolPlugin.getReader(STUB_READER_NAME);

    // configure the procotol ISO_14443_4
    nativeReader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    LOGGER.info(
        "Server - a native reader was configured with a STUB reader : {} in group reference : {}",
        nativeReader.getName(),
        REFERENCE_GROUP);
  }
}
