/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.websocket;

import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.UnexpectedReaderException;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.eclise.keyple.example.remote.server.RsePlugin;
import org.eclise.keyple.example.remote.server.RseReader;
import org.eclise.keyple.example.remote.server.transport.RseAPI;
import org.eclise.keyple.example.remote.server.transport.RseClient;
import org.eclise.keyple.example.remote.server.transport.webservice.rse.SeResponseSetCallback;
import org.eclise.keyple.example.remote.server.transport.websocket.common.WskServer;
import org.eclise.keyple.example.remote.server.transport.websocket.rse.WskRseAPI;
import org.eclise.keyple.example.remote.server.transport.websocket.rse.WskRseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class wskTicketingServer implements org.eclipse.keyple.util.Observable.Observer {

    private static final Logger logger = LoggerFactory.getLogger(wskTicketingServer.class);
    public static Integer port = 8000;
    public static String END_POINT = "/remote-se";
    private static Integer MAX_CONNECTION = 5;
    public static String URL = "0.0.0.0";

    public static void main(String[] args) throws Exception {

        wskTicketingServer server = new wskTicketingServer();
        server.boot();
        // rse.status();

    }

    public void boot() throws IOException {

        logger.info("*****************************");
        logger.info("Boot Serverside Ticketing App");
        logger.info("*****************************");

        logger.info("Init Web Socket Server");

        InetSocketAddress inet = new InetSocketAddress(Inet4Address.getByName(URL), port);
        logger.info("Started Server on http://{}:{}{}", inet.getHostName(), inet.getPort(),
                END_POINT);

        RseAPI rseAPI = new WskRseAPI();
        RseClient wskRseClient = new WskRseClient();
        WskServer wskServer = new WskServer(inet, rseAPI, wskRseClient);


        logger.info("Create SeRemotePLugin and register it to SeProxyService");
        RsePlugin rsePlugin = new RsePlugin();

        logger.info("Observe SeRemotePLugin for Plugin Events and Reader Events");
        rsePlugin.addObserver(this);

        SortedSet<ReaderPlugin> plugins = new TreeSet<ReaderPlugin>();
        plugins.add(rsePlugin);
        SeProxyService.getInstance().setPlugins(plugins);

        wskServer.run();

        logger.info("Waits for remote connections");


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
                    try {
                        RsePlugin rsePlugin =
                                (RsePlugin) SeProxyService.getInstance().getPlugins().first();
                        RseReader rseReader =
                                (RseReader) rsePlugin.getReader(event.getReaderName());

                        logger.info("Add ServerTicketingApp as a Observer of RSE reader");
                        rseReader.addObserver(this);

                    } catch (UnexpectedReaderException e) {
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
                    runCommandTest(event);
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



    private void runCommandTest(ReaderEvent event) {
        try {

            // get the reader by its name
            final RseReader reader =
                    (RseReader) ((RsePlugin) SeProxyService.getInstance().getPlugins().first())
                            .getReaderByRemoteName(event.getReaderName());

            String poAid = "A000000291A000000191";

            // build 1st seRequestSet with keep channel open to true
            final ReadRecordsCmdBuild poReadRecordCmd_T2Env = new ReadRecordsCmdBuild(
                    PoRevision.REV3_1, (byte) 0x14, (byte) 0x01, true, (byte) 0x20);



            List<ApduRequest> poApduRequestList;
            poApduRequestList = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest());
            final SeRequest.Selector selector =
                    new SeRequest.AidSelector(ByteBufferUtils.fromHex(poAid));
            SeRequest seRequest = new SeRequest(selector, poApduRequestList, true,
                    ContactlessProtocols.PROTOCOL_ISO14443_4);

            // ASYNC transmit seRequestSet to Reader With Callback function
            ((RseReader) reader).asyncTransmit(new SeRequestSet(seRequest),
                    new SeResponseSetCallback() {
                        @Override
                        public void getResponseSet(SeResponseSet seResponseSet) {
                            logger.info(
                                    "Received asynchronously a SeResponseSet with Webservice RemoteSE {}",
                                    seResponseSet);

                            List<ApduRequest> poApduRequestList2;

                            final ReadRecordsCmdBuild poReadRecordCmd_T2Usage =
                                    new ReadRecordsCmdBuild(PoRevision.REV3_1, (byte) 0x1A,
                                            (byte) 0x01, true, (byte) 0x30);
                            poApduRequestList2 =
                                    Arrays.asList(poReadRecordCmd_T2Usage.getApduRequest(),
                                            poReadRecordCmd_T2Usage.getApduRequest());

                            SeRequest seRequest2 = new SeRequest(selector, poApduRequestList2,
                                    false, ContactlessProtocols.PROTOCOL_ISO14443_4);

                            // ASYNC transmit seRequestSet to Reader
                            try {
                                ((RseReader) reader).asyncTransmit(new SeRequestSet(seRequest2),
                                        new SeResponseSetCallback() {
                                            @Override
                                            public void getResponseSet(
                                                    SeResponseSet seResponseSet) {
                                                logger.info(
                                                        "Received asynchronously a SeResponseSet with Webservice RemoteSE : {}",
                                                        seResponseSet);
                                            }
                                        });
                            } catch (IOReaderException e) {
                                e.printStackTrace();
                            }
                        }
                    });



        } catch (UnexpectedReaderException e) {
            e.printStackTrace();
        } catch (IOReaderException e) {
            e.printStackTrace();
        }

    }



}
