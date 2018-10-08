/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.remote;

import java.io.IOException;
import org.eclipse.keyple.example.remote.common.ClientNode;
import org.eclipse.keyple.example.remote.common.CommandSample;
import org.eclipse.keyple.example.remote.common.ServerNode;
import org.eclipse.keyple.example.remote.common.TransportFactory;
import org.eclipse.keyple.example.remote.common.TransportNode;
import org.eclipse.keyple.plugin.remote_se.rse.RsePlugin;
import org.eclipse.keyple.plugin.remote_se.rse.RseReader;
import org.eclipse.keyple.plugin.remote_se.rse.VirtualSeRemoteService;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Master implements org.eclipse.keyple.util.Observable.Observer {

    private static final Logger logger = LoggerFactory.getLogger(Master.class);

    private TransportNode node;
    private Boolean transmitSync;


    public Master(TransportFactory transportFactory, Boolean isServer, Boolean transmitSync) {

        this.transmitSync = transmitSync;

        logger.info("*******************");
        logger.info("Create Master    ");
        logger.info("*******************");

        if (isServer) {
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
            node = transportFactory.getClient(true);
            ((ClientNode) node).connect();
        }
    }

    public void boot() throws IOException {


        logger.info("Create Remote PLugin Interface");


        logger.info("Create SeRemotePLugin and register it to SeProxyService");
        RsePlugin rsePlugin = new RsePlugin();

        logger.info("Observe SeRemotePLugin for Plugin Events and Reader Events");
        VirtualSeRemoteService vse = new VirtualSeRemoteService();
        vse.setDtoSender(node);
        vse.registerRsePlugin(rsePlugin);
        this.node.setDtoDispatcher(rsePlugin);

        rsePlugin.addObserver(this);



    }



    /*
     * public void status() throws UnexpectedPluginException, IOReaderException { // should show
     * remote readers after a while SeProxyService service = SeProxyService.getInstance();
     * logger.info("Remote readers connected {}",
     * service.getPlugin("RemoteSePlugin").getReaders().size()); }
     */

    /**
     * Receives Event from RSE Plugin
     *
     * @param o : can be a ReaderEvent or PluginEvent
     */
    @Override
    public void update(final Object o) {

        logger.debug("UPDATE {}", o);
        logger.debug("CREATING A NEW THREAD TO PROCESS THE EVENT");

        final Master master = this;

        new Thread() {

            public void run() {
                // PluginEvent
                if (o instanceof PluginEvent) {
                    PluginEvent event = (PluginEvent) o;
                    switch (event.getEventType()) {
                        case READER_CONNECTED:
                            logger.info("READER_CONNECTED {} {}", event.getPluginName(),
                                    event.getReaderName());
                            try {
                                RsePlugin rsePlugin = (RsePlugin) SeProxyService.getInstance()
                                        .getPlugin("RemoteSePlugin");
                                RseReader rseReader =
                                        (RseReader) rsePlugin.getReader(event.getReaderName());

                                logger.info("Add ServerTicketingApp as a Observer of RSE reader");
                                rseReader.addObserver(master);

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
                            logger.info("SE_INSERTED {} {}", event.getPluginName(),
                                    event.getReaderName());
                            // CommandSample.asyncTransmit(logger, event.getReaderName());
                            // CommandSample.transmitSyncCommand(logger, event.getReaderName());
                            if (transmitSync) {
                                CommandSample.transmit(logger, event.getReaderName());
                            } else {
                                CommandSample.asyncTransmit(logger, event.getReaderName());
                            }
                            break;
                        case SE_REMOVAL:
                            logger.info("SE_REMOVAL {} {}", event.getPluginName(),
                                    event.getReaderName());
                            break;
                        case IO_ERROR:
                            logger.info("IO_ERROR {} {}", event.getPluginName(),
                                    event.getReaderName());
                            break;

                    }
                }

            }


        }.start();


    }

}
