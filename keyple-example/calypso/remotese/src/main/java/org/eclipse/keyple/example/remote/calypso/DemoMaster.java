/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.example.remote.calypso;

import java.io.IOException;
import org.eclipse.keyple.example.remote.transport.ClientNode;
import org.eclipse.keyple.example.remote.transport.ServerNode;
import org.eclipse.keyple.example.remote.transport.TransportFactory;
import org.eclipse.keyple.plugin.remotese.pluginse.RemoteSePlugin;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReader;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReaderService;
import org.eclipse.keyple.plugin.remotese.transport.TransportNode;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.util.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DemoMaster is the Master Device that controls remotely native readers. A Slave Terminal delegates
 * control of one of its native reader to the Master. In response a {@link VirtualReader} is created
 * and accessible via {@link RemoteSePlugin} like any local reader
 */
class DemoMaster implements org.eclipse.keyple.util.Observable.Observer {

    private static final Logger logger = LoggerFactory.getLogger(DemoMaster.class);

    // TransportNode used as to send and receive KeypleDto to Slaves
    private TransportNode node;

    /**
     * Constructor of the DemoMaster thread Starts a common node, can be server or client
     * 
     * @param transportFactory : type of transport used (websocket, webservice...)
     * @param isServer : is Master the server?
     */
    public DemoMaster(TransportFactory transportFactory, Boolean isServer) {

        logger.info("*******************");
        logger.info("Create DemoMaster  ");
        logger.info("*******************");

        if (isServer) {
            // Master is server, start Server and wait for Slave Clients
            try {
                node = transportFactory.getServer(true);
                // start server in a new thread
                new Thread() {
                    @Override
                    public void run() {
                        ((ServerNode) node).start();
                        logger.info("Waits for remote connections");
                    }

                }.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Master is client, connectAReader to Slave Server
            node = transportFactory.getClient(true);
            ((ClientNode) node).connect();
        }
    }

    /**
     * Initiate {@link VirtualReaderService} with both ingoing and outcoming {@link TransportNode}
     */
    public void boot() {


        logger.info("Create VirtualReaderService, start plugin");
        // Create virtualReaderService with a DtoSender
        // Dto Sender is required so virtualReaderService can send KeypleDTO to Slave
        // In this case, node is used as the dtosender (can be client or server)
        VirtualReaderService virtualReaderService =
                new VirtualReaderService(SeProxyService.getInstance(), node);

        // observe remote se plugin for events
        logger.info("Observe SeRemotePlugin for Plugin Events and Reader Events");
        ReaderPlugin rsePlugin = virtualReaderService.getPlugin();
        ((Observable) rsePlugin).addObserver(this);

        // Binds virtualReaderService to a TransportNode so virtualReaderService receives incoming
        // KeypleDto from Slaves
        // in this case we binds it to node (can be client or server)
        virtualReaderService.bindDtoEndpoint(node);


    }



    /**
     * Process Events from {@link RemoteSePlugin} and {@link VirtualReader}
     *
     * @param o : can be a ReaderEvent or PluginEvent
     */
    @Override
    public void update(final Object o) {
        logger.debug("UPDATE {}", o);
        final DemoMaster masterThread = this;

        // Receive a PluginEvent
        if (o instanceof PluginEvent) {
            PluginEvent event = (PluginEvent) o;
            switch (event.getEventType()) {
                case READER_CONNECTED:
                    // a new virtual reader is connected, let's observe it readers event
                    logger.info("READER_CONNECTED {} {}", event.getPluginName(),
                            event.getReaderName());
                    try {
                        RemoteSePlugin remoteSEPlugin = (RemoteSePlugin) SeProxyService
                                .getInstance().getPlugin("RemoteSePlugin");
                        VirtualReader virtualReader =
                                (VirtualReader) remoteSEPlugin.getReader(event.getReaderName());

                        // should parameter reader, addSeProtocolSetting, defaultCommand

                        // observe reader events
                        logger.info("Add ServerTicketingApp as a Observer of RSE reader");
                        virtualReader.addObserver(masterThread);

                    } catch (KeypleReaderNotFoundException e) {
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    } catch (KeyplePluginNotFoundException e) {
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }


                    break;
                case READER_DISCONNECTED:
                    logger.info("READER_DISCONNECTED {} {}", event.getPluginName(),
                            event.getReaderName());
                    break;
            }
        }
        // ReaderEvent
        else if (o instanceof ReaderEvent) {
            ReaderEvent event = (ReaderEvent) o;
            switch (event.getEventType()) {
                case SE_INSERTED:
                    logger.info("SE_INSERTED {} {}", event.getPluginName(), event.getReaderName());

                    // Transmit a SeRequestSet to native reader
                    CommandSample.transmit(logger, event.getReaderName());

                    break;
                case SE_REMOVAL:
                    logger.info("SE_REMOVAL {} {}", event.getPluginName(), event.getReaderName());
                    break;
                case IO_ERROR:
                    logger.info("IO_ERROR {} {}", event.getPluginName(), event.getReaderName());
                    break;
            }
        }
    }

}
