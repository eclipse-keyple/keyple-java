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
import org.eclise.keyple.example.remote.server.Demo_RemoteSEServer;
import org.eclise.keyple.example.remote.server.transport.LocalTransport;
import org.eclise.keyple.example.remote.server.transport.TransportFactory;
import org.eclise.keyple.example.stub.calypso.HoplinkStubSE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class Demo_RemoteSEClient {

    private static final Logger logger = LoggerFactory.getLogger(Demo_RemoteSEClient.class);

    StubReader localReader ;


    void boot(){

        //register StubPlugin with StubReader
        StubPlugin plugin = StubPlugin.getInstance();
        localReader  = plugin.plugStubReader("stubPhysicalReader");

        //configure Transport (ie Local, Websocket) with a configuration Bundle
        LocalTransport localTransport = (LocalTransport) TransportFactory.getTransport(null);

        //connect StubReader to Remote Server via Transport
        localTransport.clientConnect(localReader);
    }

    void demo(){

        //insert SE
        localReader.insertSe(new HoplinkStubSE());

    }

    public static void main(String[] args) throws Exception{


        Demo_RemoteSEServer server = new Demo_RemoteSEServer();
        server.boot();

        Demo_RemoteSEClient client = new Demo_RemoteSEClient();
        client.boot();
        client.demo();

        server.status();


    }




}
