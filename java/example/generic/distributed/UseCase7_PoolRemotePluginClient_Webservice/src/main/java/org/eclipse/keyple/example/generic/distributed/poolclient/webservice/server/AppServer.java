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
package org.eclipse.keyple.example.generic.distributed.poolclient.webservice.server;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.keyple.core.service.PoolPlugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.distributed.LocalServiceClient;
import org.eclipse.keyple.distributed.impl.PoolLocalServiceServerFactory;
import org.eclipse.keyple.example.generic.distributed.poolclient.webservice.util.CalypsoSmartCard;
import org.eclipse.keyple.plugin.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Example of a server side application. */
@ApplicationScoped
public class AppServer {

  private static final Logger logger = LoggerFactory.getLogger(AppServer.class);

  private PoolPlugin poolPlugin;

  /**
   * Initialize the server components :
   *
   * <ul>
   *   <li>A {@link PoolPlugin} with a {@link StubPoolPlugin} with a inserted card,
   *   <li>A {@link LocalServiceClient} with a sync node bind to a {@link
   *       org.eclipse.keyple.distributed.spi.SyncEndpointClient} endpoint.
   * </ul>
   */
  public void init() {

    // Init a local pool plugin.
    initStubPoolPlugin();

    // Init the local service using the associated factory.
    PoolLocalServiceServerFactory.builder()
        .withDefaultServiceName()
        .withSyncNode() // HTTP webservice needs a server sync node configuration
        .withPoolPlugins(poolPlugin.getName()) // use the registered ReaderPoolPlugin
        .getService();
  }

  /** Init a local pool plugin with a stub pool plugin and an inserted card */
  private void initStubPoolPlugin() {

    String STUB_PLUGIN_NAME = "stubPoolPlugin";
    String STUB_READER_NAME = "stubReader";
    String REFERENCE_GROUP = "group1";

    // Registers the plugin to the smart card service.
    poolPlugin =
        (StubPoolPlugin)
            SmartCardService.getInstance()
                .registerPlugin(new StubPoolPluginFactory(STUB_PLUGIN_NAME, null, null));

    // Plug manually to the plugin a local reader associated in a group reference.
    ((StubPoolPlugin) poolPlugin)
        .plugPoolReader(REFERENCE_GROUP, STUB_READER_NAME, new CalypsoSmartCard());

    // Retrieves the connected reader from the plugin.
    Reader nativeReader = poolPlugin.getReader(STUB_READER_NAME);

    // Activates the protocol ISO_14443_4 on the reader.
    nativeReader.activateProtocol(
        StubSupportedProtocols.ISO_14443_4.name(),
        ContactlessCardCommonProtocols.ISO_14443_4.name());

    logger.info(
        "Server - Local reader was configured with a STUB reader : {} in group reference : {}",
        nativeReader.getName(),
        REFERENCE_GROUP);
  }
}
