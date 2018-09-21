/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.remote.server.transport.webservice;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import org.eclise.keyple.example.remote.webservice.old.rse.PluginEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class WebServiceServer {

    private static final Logger logger = LoggerFactory.getLogger(WebServiceServer.class);

    public static Integer port = 8000;
    public static String END_POINT = "/remote-se";
    private static Integer MAX_CONNECTION = 5;
    public static String URL = "0.0.0.0";

    public static void main(String[] args) throws IOException {
        InetSocketAddress inet = new InetSocketAddress(Inet4Address.getByName(URL), port);
        HttpServer server = HttpServer.create(inet, MAX_CONNECTION);
        server.createContext(END_POINT, new PluginEndpoint());
        server.setExecutor(null); // creates a default executor
        server.start();

        logger.info("Started Server on http://{}:{}{}", inet.getHostName(), inet.getPort(),
                END_POINT);
    }

}
