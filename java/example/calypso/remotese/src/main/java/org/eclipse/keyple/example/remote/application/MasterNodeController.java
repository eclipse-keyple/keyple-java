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
package org.eclipse.keyple.example.remote.application;

import java.io.IOException;
import org.eclipse.keyple.calypso.transaction.SamResourceManager;
import org.eclipse.keyple.calypso.transaction.SamResourceManagerFactory;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginInstantiationException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.example.common.calypso.stub.StubSamCalypsoClassic;
import org.eclipse.keyple.plugin.remotese.pluginse.MasterAPI;
import org.eclipse.keyple.plugin.remotese.pluginse.RemoteSePlugin;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReader;
import org.eclipse.keyple.plugin.remotese.transport.DtoNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.ClientNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.ServerNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubPluginFactory;
import org.eclipse.keyple.plugin.stub.StubProtocolSetting;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.plugin.stub.StubSecureElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MasterNodeController is the Master Device that controls remotely native readers. In this example,
 * Slave nodes listen for Calypso Portable Object (PO) while the SAM resource is connected to the
 * Master Node. A Slave Terminal delegates control of one of its native reader to the Master. In
 * response a {@link VirtualReader} is created and accessible via {@link RemoteSePlugin} like any
 * local reader
 */
public class MasterNodeController {

  private static final Logger logger = LoggerFactory.getLogger(MasterNodeController.class);

  private SamResourceManager samResourceManager;

  // Master API reference
  private MasterAPI masterAPI;

  // DtoNode used as to send and receive KeypleDto to Slaves
  private DtoNode node;

  private static final int MAX_BLOCKING_TIME = 1000; // 1 second

  public static long RPC_TIMEOUT = 20000;

  public static String STUB_MASTER = "stubMaster";

  public static String REMOTESE_PLUGIN_NAME = "RemoteSePlugin";

  /**
   * Constructor of the MasterNodeController.
   *
   * <p>Starts a new thread that can be server or client
   *
   * @param transportFactory : type of transport used (websocket, webservice...)
   * @param isServer : is Master the server?
   */
  public MasterNodeController(
      final TransportFactory transportFactory, Boolean isServer, final String clientNodeId) {

    logger.info("*****************************************************************************");
    logger.info("Create MasterNodeController  ");
    logger.info("*****************************************************************************");
    if (isServer) {
      // Master is server, start Server and wait for Slave Clients
      try {
        node = transportFactory.getServer();

        // start server in a new thread
        new Thread() {
          @Override
          public void run() {
            ((ServerNode) node).start();
            logger.info("{} Waits for remote connections", node.getNodeId());
          }
        }.start();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {

      node = transportFactory.getClient(clientNodeId);

      ((ClientNode) node).connect(null);
    }
  }

  /** Initiate {@link MasterAPI} with both ingoing and outcoming {@link DtoNode} */
  public void boot() {
    try {
      /*
       * Configure the SAM Resource Manager
       */
      ReaderPlugin samStubPlugin =
          SeProxyService.getInstance().registerPlugin(new StubPluginFactory(STUB_MASTER));

      /* Plug the SAM stub reader. */
      ((StubPlugin) samStubPlugin).plugStubReader("samReader", true);

      SeReader samReader = samStubPlugin.getReader("samReader");

      samReader.addSeProtocolSetting(
          SeCommonProtocols.PROTOCOL_ISO7816_3,
          StubProtocolSetting.STUB_PROTOCOL_SETTING.get(SeCommonProtocols.PROTOCOL_ISO7816_3));

      /* Create 'virtual' and insert a Calypso SAM */
      StubSecureElement calypsoSamStubSe = new StubSamCalypsoClassic();

      ((StubReader) samReader).insertSe(calypsoSamStubSe);
      logger.info("Stub SAM inserted");

      /*
       * Configure a Sam Resource Manager
       */
      samResourceManager = SamResourceManagerFactory.instantiate(samStubPlugin, ".*");

      /*
       * Configure the RemoteSe Plugin that manages PO Virtual Readers
       */
      logger.info("{} Create VirtualReaderService, start plugin", node.getNodeId());
      // Create masterAPI with a DtoSender
      // Dto Sender is required so masterAPI can send KeypleDTO to Slave
      // In this case, node is used as the dtosender (can be client or server)
      masterAPI =
          new MasterAPI(
              SeProxyService.getInstance(),
              node,
              RPC_TIMEOUT,
              MasterAPI.PLUGIN_TYPE_DEFAULT,
              REMOTESE_PLUGIN_NAME);

      // observe remote se plugin for events
      logger.info(
          "{} Observe SeRemotePlugin for Plugin Events and Reader Events", node.getNodeId());
      ReaderPlugin rsePlugin = masterAPI.getPlugin();

      // add a custom observer for the Remote SE plugin
      ((ObservablePlugin) rsePlugin)
          .addObserver(new RemoteSePluginObserver(masterAPI, samResourceManager, node.getNodeId()));

    } catch (KeypleReaderNotFoundException e) {
      e.printStackTrace();
    } catch (KeyplePluginInstantiationException e) {
      e.printStackTrace();
    } catch (KeypleReaderException e) {
      e.printStackTrace();
    }
  }
}
