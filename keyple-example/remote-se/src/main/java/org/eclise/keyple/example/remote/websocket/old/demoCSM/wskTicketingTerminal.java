/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.websocket.old.demoCSM;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.keyple.plugin.remote_se.rse.RsePlugin;
import org.eclipse.keyple.plugin.remote_se.rse.VirtualSeRemoteService;
import org.eclipse.keyple.plugin.remote_se.transport.TransportNode;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclise.keyple.example.remote.common.CommandSample;
import org.eclise.keyple.example.remote.websocket.WskClient;
import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class wskTicketingTerminal implements org.eclipse.keyple.util.Observable.Observer {

    private static final Logger logger = LoggerFactory.getLogger(wskTicketingTerminal.class);


    public static void main(String[] args) throws Exception {

        wskTicketingTerminal server = new wskTicketingTerminal();
        server.boot();


        // rse.status();

    }

    public void boot() throws IOException, URISyntaxException {

        logger.info("************************");
        logger.info("Boot Slave Network     ");
        logger.info("************************");

        String ENDPOINT_URL = "http://localhost:8002/remote-se";
        WebSocketClient wskClient = new WskClient(new URI(ENDPOINT_URL));
        wskClient.connect();

        logger.info("**********************************");
        logger.info("Boot Remote SE Plugin Network     ");
        logger.info("**********************************");

        logger.info("Create SeRemotePLugin");
        RsePlugin rsePlugin = new RsePlugin();

        logger.info("Observe SeRemotePLugin for Plugin Events and Reader Events");
        rsePlugin.addObserver(this);

        SortedSet<ReaderPlugin> plugins = new TreeSet<ReaderPlugin>();
        plugins.add(rsePlugin);
        SeProxyService.getInstance().setPlugins(plugins);

        VirtualSeRemoteService remoteService = new VirtualSeRemoteService();
        remoteService.bindTransportNode((TransportNode) wskClient);
        remoteService.registerRsePlugin(rsePlugin);

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
    public void update(Object o) {

        logger.debug("UPDATE {}", o);

        // PluginEvent
        if (o instanceof PluginEvent) {
            PluginEvent event = (PluginEvent) o;
            switch (event.getEventType()) {
                case READER_CONNECTED:
                    logger.info("READER_CONNECTED {} {}", event.getPluginName(),
                            event.getReaderName());

                    CommandSample.transmitSyncCommand(logger, event.getReaderName());
                    // CommandSample.asyncTransmit(logger, event.getReaderName());



                    break;
                case READER_DISCONNECTED:
                    logger.info("READER_DISCONNECTED {} {}", event.getPluginName(),
                            event.getReaderName());
                    break;
            }
        }
    }



}
