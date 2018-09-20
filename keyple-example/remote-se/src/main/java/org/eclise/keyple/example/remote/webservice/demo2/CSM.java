/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.webservice.demo2;


import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclise.keyple.example.remote.webservice.webservice.common.HttpHelper;
import org.eclise.keyple.example.remote.webservice.webservice.nse.NseEndpoint;
import org.eclise.keyple.example.remote.webservice.webservice.nse.WsRseClient;
import org.eclise.keyple.example.stub.calypso.HoplinkStubSE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class CSM {

    private static final Logger logger = LoggerFactory.getLogger(CSM.class);

    // rse url
    private static String SERVER_URL = "http://localhost:8000/remote-se";


    // csm url
    public static String END_POINT = "/remote-se";
    public static Integer port = 8001;
    private static Integer MAX_CONNECTION = 5;
    public static String URL = "0.0.0.0";


    // physical reader
    StubReader localReader;

    void boot() throws IOException {
        logger.info("************************");
        logger.info("Init CSM Local Reader   ");
        logger.info("************************");

        // get seProxyService
        SeProxyService seProxyService = SeProxyService.getInstance();

        logger.info("Create Local StubPlugin");
        StubPlugin stubPlugin = StubPlugin.getInstance();
        SortedSet<ReaderPlugin> plugins = new TreeSet<ReaderPlugin>();
        plugins.add(stubPlugin);
        seProxyService.setPlugins(plugins);
        localReader = stubPlugin.plugStubReader("stubCSM");


        logger.info("*****************************");
        logger.info("Boot WebService in CSM Side  ");
        logger.info("*****************************");

        logger.info("Init Web Service Server");

        // Create Endpoints for plugin and reader API
        NseEndpoint nseEndpoint = new NseEndpoint(localReader);

        // deploy endpoint
        InetSocketAddress inet = new InetSocketAddress(Inet4Address.getByName(URL), port);
        HttpServer server = HttpServer.create(inet, MAX_CONNECTION);
        server.createContext(END_POINT + HttpHelper.TRANSMIT_ENDPOINT, nseEndpoint);

        // start rse
        server.setExecutor(null); // creates a default executor
        server.start();
        logger.info("Started Server on http://{}:{}{}", inet.getHostName(), inet.getPort(),
                END_POINT);



        try {
            // todo configure remote service with web service nse
            WsRseClient wsClientRSEClient = new WsRseClient(SERVER_URL);

            // logger.info(
            // "Register wsClientRSEClient as an observer of the local stubPlugin thus events will
            // be propagated");

            Map<String, Object> options = new HashMap<String, Object>();
            options.put("transmitUrl",
                    "http://localhost:" + port + END_POINT + HttpHelper.TRANSMIT_ENDPOINT);
            options.put("isAsync", true);

            // todo connect reader to remote service and specify that we can communicate with a rse
            // API /transmit
            String sessionId = wsClientRSEClient.connectReader(localReader, options);
            logger.info("Connect remotely the CSM Reader to rse with sessionId {}", sessionId);


            // IMPORTANT : only here for the demo, no need to listen to event in CSM
            localReader.addObserver(wsClientRSEClient);


        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    void demo() {
        logger.info("************************");
        logger.info("Start DEMO - insert SE  ");
        logger.info("************************");

        logger.info("Insert HoplinkStubSE into Local StubReader");
        // insert SE
        localReader.insertSe(new HoplinkStubSE());

        // todo Remove SE
        // logger.info("************************");
        // logger.info(" remove SE ");
        // logger.info("************************");
        //
        // localReader.removeSe();

    }



    public static void main(String[] args) throws Exception {


        CSM client = new CSM();
        client.boot();
        client.demo();


    }



}
