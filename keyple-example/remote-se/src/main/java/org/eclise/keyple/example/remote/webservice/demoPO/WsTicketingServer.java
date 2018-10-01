/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.webservice.demoPO;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.plugin.remote_se.rse.ISeResponseSetCallback;
import org.eclipse.keyple.plugin.remote_se.rse.RsePlugin;
import org.eclipse.keyple.plugin.remote_se.rse.RseReader;
import org.eclipse.keyple.plugin.remote_se.rse.VirtualSeRemoteService;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.eclise.keyple.example.remote.webservice.WsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class WsTicketingServer implements org.eclipse.keyple.util.Observable.Observer {

    private static final Logger logger = LoggerFactory.getLogger(WsTicketingServer.class);
    public static Integer PORT = 8007;
    public static String END_POINT = "/keypleDTO";
    public static String URL = "0.0.0.0";

    public static void main(String[] args) throws Exception {

        WsTicketingServer server = new WsTicketingServer();
        server.boot();
        // rse.status();

    }

    public void boot() throws IOException {

        logger.info("*****************************");
        logger.info("Boot Webservice server       ");
        logger.info("*****************************");

        logger.info("Init Web Service Server");

        WsServer server = new WsServer(URL, PORT, END_POINT);

        logger.info("******************************");
        logger.info("Create Remote PLugin Interface");
        logger.info("******************************");

        logger.info("Create SeRemotePLugin and register it to SeProxyService");
        RsePlugin rsePlugin = new RsePlugin();

        logger.info("Observe SeRemotePLugin for Plugin Events and Reader Events");
        VirtualSeRemoteService vse = new VirtualSeRemoteService();
        vse.bindTransportNode(server);
        vse.registerRsePlugin(rsePlugin);

        rsePlugin.addObserver(this);

        server.start();
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

                    } catch (KeypleReaderNotFoundException e) {
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
                    runAsyncCommandTest(event);
                    // runSyncCommandTest(event);
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


    private void runSyncCommandTest(ReaderEvent event) {
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
            SeRequest seRequest = new SeRequest(selector, poApduRequestList, true);

            // ASYNC transmit seRequestSet to Reader With Callback function
            logger.info("Execute sync transmit with seRequest {}", seRequest);
            SeResponseSet seResponseSet =
                    ((RseReader) reader).transmit(new SeRequestSet(seRequest));

            logger.info("Received synchronously a SeResponseSet with Webservice RemoteSE {}",
                    seResponseSet);



        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
        } catch (KeypleReaderException e) {
            e.printStackTrace();
        }

    }

    private void runAsyncCommandTest(ReaderEvent event) {
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
            final SeRequest seRequest = new SeRequest(selector, poApduRequestList, true);
            logger.info("1 - Execute async transmit with seRequest {}", seRequest);
            // ASYNC transmit seRequestSet to Reader With Callback function
            ((RseReader) reader).asyncTransmit(new SeRequestSet(seRequest),
                    new ISeResponseSetCallback() {
                        @Override
                        public void getResponseSet(SeResponseSet seResponseSet) {
                            logger.info(
                                    "Received asynchronously a SeResponseSet with Webservice RemoteSE {}",
                                    seResponseSet);

                            // ASYNC transmit seRequestSet to Reader With Callback function
                            logger.info("2 - Execute sync transmit with seRequest {}", seRequest);
                            try {
                                SeResponseSet seResponseSet2 =
                                        ((RseReader) reader).transmit(new SeRequestSet(seRequest));

                                logger.info(
                                        "Received synchronously a SeResponseSet with Webservice RemoteSE {}",
                                        seResponseSet2);


                            } catch (KeypleReaderException e) {
                                e.printStackTrace();
                            }


                        }
                    });



        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
        } catch (KeypleReaderException e) {
            e.printStackTrace();
        }

    }



}
