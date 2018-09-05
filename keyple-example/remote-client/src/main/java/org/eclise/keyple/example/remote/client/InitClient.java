/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.client;

import org.eclipse.keyple.plugin.stub.StubPlugin;

import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclise.keyple.example.remote.server.transport.ClientListener;
import org.eclise.keyple.example.remote.server.transport.ClientTransport;
import org.eclise.keyple.example.remote.server.transport.TransportFactory;
import org.eclise.keyple.example.remote.server.transport.local.LocalClientListener;
import org.eclise.keyple.example.remote.server.transport.local.LocalTransportFactory;
import org.eclise.keyple.example.stub.calypso.HoplinkStubSE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

public class InitClient {

    private static final Logger logger = LoggerFactory.getLogger(InitClient.class);

    StubReader localReader ;


    void boot(){

        //register StubPlugin with StubReader
        StubPlugin plugin = StubPlugin.getInstance();
        localReader  = plugin.plugStubReader("stubPhysicalReader");

        ClientListener clientListener = new LocalClientListener(localReader);

        //configure ServerTransport (ie Local, Websocket) with a configuration Bundle
        TransportFactory transportFactory = LocalTransportFactory.getInstance();

        try {
            ClientTransport clientTransport = transportFactory.getClientTransport(clientListener);

            localReader.addObserver(clientTransport);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        //connect StubReader to Remote Server via ServerTransport

    }

    void demo(){

        //insert SE
        localReader.insertSe(new HoplinkStubSE());

    }






}
