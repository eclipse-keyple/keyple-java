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
import org.eclipse.keyple.core.seproxy.Plugin;
import org.eclipse.keyple.core.seproxy.SmartCardService;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginInstantiationException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.nativese.SlaveAPI;
import org.eclipse.keyple.plugin.remotese.transport.DtoNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.ClientNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.ServerNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubPluginFactory;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.plugin.stub.StubSecureElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SlaveNodeController is where slave readers are physically located, it connects one native reader
 * to the master to delegate control of it. In this example, Slave nodes listen for Calypso Portable
 * Object (PO) while the SAM resource is connected to the Master Node.
 */
public class SlaveNodeController {

  private static final Logger logger = LoggerFactory.getLogger(SlaveNodeController.class);

  // physical reader, in this case a StubReader
  private StubReader localReader;

  // DtoNode used as to send and receive KeypleDto to Master
  private DtoNode node;

  // SlaveAPI, used to connect PoReader and disconnect readers
  private SlaveAPI slaveAPI;

  private String nativeReaderName;

  public static String STUB_SLAVE = "stubSlave";

  public static long RPC_TIMEOUT = 20000;

  /**
   * Create a new SlaveNodeController
   *
   * <p>Starts a new thread that can be server or client
   *
   * <p>At startup, create the {@link DtoNode} object, either a {@link ClientNode} or a {@link
   * ServerNode}
   *
   * @param transportFactory : factory to get the type of transport needed (websocket,
   *     webservice...)
   * @param isServer : true if a Server is wanted
   */
  public SlaveNodeController(
      final TransportFactory transportFactory,
      Boolean isServer,
      final String slaveNodeId,
      String masterNodeId) {

    nativeReaderName = "STUB_READER" + slaveNodeId;

    logger.info("|{}| Create DemoSlave    ", slaveNodeId);

    if (isServer) {
      // Slave is a server, start Server and wait for Master clients
      try {
        node = transportFactory.getServer();

        // slave server needs to know to which master client it should connects
        // slaveNodeId = transportFactory.getClient(slaveNodeId).getNodeId();

        // start server in a new thread
        new Thread() {
          @Override
          public void run() {
            ((ServerNode) node).start();
            logger.info("|{}| Waits for remote connections", slaveNodeId);
          }
        }.start();

        // if slave is server, must specify which master to connect to
        slaveAPI = new SlaveAPI(SmartCardService.getInstance(), node, masterNodeId);

        initPoReader();

      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {

      // Slave is client, connectPoReader to Master Server
      node = transportFactory.getClient(slaveNodeId);

      // slave client uses its clientid to connect to server
      // slaveNodeId = node.getNodeId();

      ((ClientNode) node)
          .connect(
              new ClientNode.ConnectCallback() {
                @Override
                public void onConnectSuccess() {
                  logger.trace("|{}| onConnectSuccess ", slaveNodeId);
                }

                @Override
                public void onConnectFailure() {}
              });
      // if slave is client, master is the configured server
      slaveAPI =
          new SlaveAPI(SmartCardService.getInstance(), node, ((ClientNode) node).getServerNodeId());

      initPoReader();
    }
  }

  /**
   * Creates and configures a {@link StubReader} for the PO
   *
   * @throws KeypleReaderException
   * @throws InterruptedException
   */
  private final void initPoReader() {

    try {
      logger.info("||{}|| Boot DemoSlave LocalReader ", node.getNodeId());

      logger.info("|{}| Create Local StubPlugin", node.getNodeId());

      /* Get the instance of the SmartCardService (Singleton pattern) */
      SmartCardService smartCardService = SmartCardService.getInstance();

      /* Assign PcscPlugin to the SmartCardService */
      Plugin stubPlugin = smartCardService.registerPlugin(new StubPluginFactory(STUB_SLAVE));

      ObservablePlugin.PluginObserver observer =
          new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {
              logger.info(
                  "|{}| Update - pluginEvent from inline observer {}", node.getNodeId(), event);
            }
          };

      // add observer to have the reader management done by the monitoring thread
      ((ObservablePlugin) stubPlugin).addObserver(observer);
      Thread.sleep(100);

      ((StubPlugin) stubPlugin).plugStubReader(nativeReaderName, true);

      Thread.sleep(1000);

      // get the created proxy reader
      localReader = (StubReader) stubPlugin.getReader(nativeReaderName);

      // start card detectino in REPEATING MODE
      localReader.startCardDetection(ObservableReader.PollingMode.REPEATING);

    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (KeypleReaderNotFoundException e) {
      e.printStackTrace();
    } catch (KeyplePluginInstantiationException e) {
      e.printStackTrace();
    }
  }

  public void insertStubSe(StubSecureElement card) {
    logger.info("|{}| insert card  ", node.getNodeId());
    localReader.insertSe(card);
  }

  public void removeSe() {
    logger.info("|{}| remove card ", node.getNodeId());
    localReader.removeSe();
  }

  public void disconnect(String sessionId, String nativeReaderName)
      throws KeypleReaderException, KeypleRemoteException {
    logger.info("|{}| Disconnect native reader ", node.getNodeId());
    slaveAPI.disconnectReader(sessionId, localReader.getName());
  }

  public void executeScenario(final StubSecureElement card, final Boolean killAtEnd)
      throws KeypleReaderNotFoundException, InterruptedException, KeypleReaderException,
          KeypleRemoteException {
    // logger.info("------------------------");
    logger.info("|{}| **Start Scenario** - Connect Reader to Master", node.getNodeId());
    // logger.info("------------------------");

    // Thread.sleep(1000);
    logger.info("|{}| Connect remotely the Native Reader ", node.getNodeId());
    String sessionId = slaveAPI.connectReader(localReader);

    // logger.info("--------------------------------------------------");
    logger.info("|{}| Session created on server {}", node.getNodeId(), sessionId);
    // logger.info("Wait 2 seconds, then insert card");
    // logger.info("--------------------------------------------------");
    this.insertStubSe(card);
    logger.info("|{}| Wait 2 seconds for PO read/session, then remove the card", node.getNodeId());
    Thread.sleep(2000);
    this.removeSe();
    Thread.sleep(2000);
    logger.info("|{}| Disconnect reader", node.getNodeId());
    this.disconnect(sessionId, null);

    if (killAtEnd) {
      logger.info("|{}| Shutdown jvm", node.getNodeId());
      Thread.sleep(500);

      Runtime.getRuntime().exit(0);
    }
  }
}
