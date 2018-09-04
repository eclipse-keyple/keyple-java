/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.server;

import com.sun.tools.doclint.Entity;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.UnexpectedPluginException;
import org.eclise.keyple.example.remote.server.transport.LocalTransport;
import org.eclise.keyple.example.remote.server.transport.Transport;
import org.eclise.keyple.example.remote.server.transport.TransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

public class Demo_RemoteSEServer {

    private static final Logger logger = LoggerFactory.getLogger(Demo_RemoteSEServer.class);

    public void boot(){

        logger.info("Boot Demo_RemoteSEServer");

        //configure Transport (ie Local, Websocket)
        LocalTransport localTransport = (LocalTransport) TransportFactory.getTransport(null);


        //register SeRemotePlugin with a Transport
        SeProxyService service = SeProxyService.getInstance();
        RemoteSePlugin remoteSePlugin = new RemoteSePlugin(localTransport);
        SortedSet<ReaderPlugin> plugins = new TreeSet<ReaderPlugin>();
        plugins.add(remoteSePlugin);
        service.setPlugins(plugins);

        //waits for SeClient to connect


    }

    public void status() throws UnexpectedPluginException, IOReaderException {
        //should show remote readers after a while
        SeProxyService service = SeProxyService.getInstance();
        logger.info("Remote readers connected {}" ,service.getPlugin("RemoteSePlugin").getReaders().size());
    }


}
