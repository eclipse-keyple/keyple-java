/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.local;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.plugin.remote_se.rse.RsePlugin;
import org.eclipse.keyple.plugin.remote_se.rse.RseReader;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.UnexpectedPluginException;
import org.eclipse.keyple.seproxy.exception.UnexpectedReaderException;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.eclise.keyple.example.remote.local.local.LocalServer;
import org.eclise.keyple.example.remote.local.local.RseAPI;
import org.eclise.keyple.example.remote.local.local.rse.LocalRseAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalServerTicketingApp implements org.eclipse.keyple.util.Observable.Observer {

    private static final Logger logger = LoggerFactory.getLogger(LocalServerTicketingApp.class);

    public void boot() {

        logger.info("*****************************");
        logger.info("Boot Serverside Ticketing App");
        logger.info("*****************************");

        logger.info("Init Server");
        // configure ServerConnection (ie Local, Websocket)
        LocalServer localServer = LocalServer.getInstance();
        RseAPI rseAPI = localServer.initServerListener();

        logger.info("Create SeRemotePLugin and register it to SeProxyService");
        // register SeRemotePlugin with a ServerConnection
        SeProxyService service = SeProxyService.getInstance();
        RsePlugin RsePlugin = new RsePlugin();

        logger.info("Observe SeRemotePLugin for Events");
        RsePlugin.addObserver(this);

        SortedSet<ReaderPlugin> plugins = new TreeSet<ReaderPlugin>();
        plugins.add(RsePlugin);
        service.setPlugins(plugins);

        logger.info("Register Plugin to listen to remote connections");
        ((LocalRseAPI) rseAPI).setPlugin(RsePlugin);

        logger.info("Waits for remote connections");


    }

    public void status() throws UnexpectedPluginException, IOReaderException {
        // should show remote readers after a while
        SeProxyService service = SeProxyService.getInstance();
        logger.info("Remote readers connected {}",
                service.getPlugin("RemoteSePlugin").getReaders().size());
    }


    @Override
    public void update(Object o) {

        logger.debug("UPDATE {}", o);


        if (o instanceof ReaderEvent) {
            ReaderEvent event = (ReaderEvent) o;
            switch (event.getEventType()) {
                case SE_INSERTED:
                    logger.info("SE_INSERTED {} {}", event.getPluginName(), event.getReaderName());

                    runTest(event);



                    break;
                case SE_REMOVAL:
                    logger.info("SE_REMOVAL {} {}", event.getPluginName(), event.getReaderName());
                    break;
                case IO_ERROR:
                    logger.info("IO_ERROR {} {}", event.getPluginName(), event.getReaderName());
                    break;

            }
        } else if (o instanceof PluginEvent) {
            PluginEvent event = (PluginEvent) o;
            switch (event.getEventType()) {
                case READER_CONNECTED:
                    logger.info("READER_CONNECTED {} {}", event.getPluginName(),
                            event.getReaderName());
                    logger.info("Observe SeRemoteReader for Events");
                    try {
                        ((RseReader) SeProxyService.getInstance().getPlugins().first().getReaders()
                                .first()).addObserver(this);

                    } catch (IOReaderException e) {
                        e.printStackTrace();
                    }

                    break;
                case READER_DISCONNECTED:
                    logger.info("READER_DISCONNECTED {} {}", event.getPluginName(),
                            event.getReaderName());
                    break;
            }
        }
    }



    private void runTest(ReaderEvent event) {
        ProxyReader reader = null;
        try {
            reader = ((RsePlugin) SeProxyService.getInstance().getPlugins().first())
                    .getReaderByRemoteName(event.getReaderName());
            String poAid = "A000000291A000000191";

            ReadRecordsCmdBuild poReadRecordCmd_T2Env = new ReadRecordsCmdBuild(PoRevision.REV3_1,
                    (byte) 0x14, (byte) 0x01, true, (byte) 0x20);

            List<ApduRequest> poApduRequestList;

            poApduRequestList = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest());

            SeRequest.Selector selector = new SeRequest.AidSelector(ByteBufferUtils.fromHex(poAid));

            SeRequest seRequest = new SeRequest(selector, poApduRequestList, true,
                    ContactlessProtocols.PROTOCOL_ISO14443_4);

            // transmit seRequestSet to Reader
            final SeResponseSet seResponseSet = reader.transmit(new SeRequestSet(seRequest));


            List<ApduRequest> poApduRequestList2;

            poApduRequestList2 = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest());

            SeRequest seRequest2 = new SeRequest(selector, poApduRequestList2, false,
                    ContactlessProtocols.PROTOCOL_ISO14443_4);

            // transmit seRequestSet to Reader
            final SeResponseSet seResponseSet2 = reader.transmit(new SeRequestSet(seRequest2));


        } catch (UnexpectedReaderException e) {
            e.printStackTrace();
        } catch (IOReaderException e) {
            e.printStackTrace();
        }


    }

}
