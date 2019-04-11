/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.example.remote.application;

import java.io.IOException;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.example.calypso.common.postructure.CalypsoClassicInfo;
import org.eclipse.keyple.plugin.remotese.pluginse.MasterAPI;
import org.eclipse.keyple.plugin.remotese.pluginse.RemoteSePlugin;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReader;
import org.eclipse.keyple.plugin.remotese.transport.DtoNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.ClientNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.ServerNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.transaction.MatchingSe;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.transaction.SelectionsResult;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.eclipse.keyple.util.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo_Master is the Master Device that controls remotely native readers. A Slave Terminal
 * delegates control of one of its native reader to the Master. In response a {@link VirtualReader}
 * is created and accessible via {@link RemoteSePlugin} like any local reader
 */
public class Demo_Master implements org.eclipse.keyple.util.Observable.Observer {

    private static final Logger logger = LoggerFactory.getLogger(Demo_Master.class);
    private SeSelection seSelection;
    private VirtualReader poReader;
    private int readEnvironmentParserIndex;


    // DtoNode used as to send and receive KeypleDto to Slaves
    private DtoNode node;

    /**
     * Constructor of the DemoMaster thread Starts a common node, can be server or client
     * 
     * @param transportFactory : type of transport used (websocket, webservice...)
     * @param isServer : is Master the server?
     */
    public Demo_Master(final TransportFactory transportFactory, Boolean isServer,
            final String clientNodeId) {


        logger.info(
                "*****************************************************************************");
        logger.info("Create DemoMaster  ");
        logger.info(
                "*****************************************************************************");
        if (isServer) {
            // Master is server, start Server and wait for Slave Clients
            try {
                node = transportFactory.getServer();

                // start server in a new thread
                new Thread() {
                    @Override
                    public void run() {
                        ((ServerNode) node).start();
                        logger.info("{} Waits for remote connections", node.getNodeId());
                    }

                }.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            node = transportFactory.getClient(clientNodeId);

            ((ClientNode) node).connect(null);

            /*
             * // start client in a new thread new Thread() {
             * 
             * @Override public void run() { // Master is client, connectAReader to Slave Server }
             * }.start();
             */
        }
    }

    /**
     * Initiate {@link MasterAPI} with both ingoing and outcoming {@link DtoNode}
     */
    public void boot() {


        logger.info("{} Create VirtualReaderService, start plugin", node.getNodeId());
        // Create masterAPI with a DtoSender
        // Dto Sender is required so masterAPI can send KeypleDTO to Slave
        // In this case, node is used as the dtosender (can be client or server)
        MasterAPI masterAPI = new MasterAPI(SeProxyService.getInstance(), node);

        // observe remote se plugin for events
        logger.info("{} Observe SeRemotePlugin for Plugin Events and Reader Events",
                node.getNodeId());
        ReaderPlugin rsePlugin = masterAPI.getPlugin();
        ((Observable) rsePlugin).addObserver(this);

    }



    /**
     * Process Events from {@link RemoteSePlugin} and {@link VirtualReader}
     *
     * @param o : can be a ReaderEvent or PluginEvent
     */
    @Override
    public void update(final Object o) {
        final Demo_Master masterThread = this;

        // Receive a PluginEvent
        if (o instanceof PluginEvent) {
            PluginEvent event = (PluginEvent) o;
            logger.info("{} UPDATE {} {} {}", node.getNodeId(), event.getEventType(),
                    event.getPluginName(), event.getReaderNames().first());
            switch (event.getEventType()) {
                case READER_CONNECTED:
                    // a new virtual reader is connected, let's configure it
                    try {
                        RemoteSePlugin remoteSEPlugin = (RemoteSePlugin) SeProxyService
                                .getInstance().getPlugin("RemoteSePlugin");
                        poReader = (VirtualReader) remoteSEPlugin
                                .getReader(event.getReaderNames().first());

                        logger.info("{} Configure SeSelection", node.getNodeId());

                        /* set default selection request */
                        seSelection = new SeSelection();

                        /*
                         * Setting of an AID based selection of a Calypso REV3 PO
                         *
                         * Select the first application matching the selection AID whatever the SE
                         * communication protocol keep the logical channel open after the selection
                         */

                        /*
                         * Calypso selection: configures a PoSelectionRequest with all the desired
                         * attributes to make the selection and read additional information
                         * afterwards
                         */
                        PoSelectionRequest poSelectionRequest =
                                new PoSelectionRequest(
                                        new SeSelector(
                                                new SeSelector.AidSelector(ByteArrayUtils
                                                        .fromHex(CalypsoClassicInfo.AID), null),
                                                null, "AID: " + CalypsoClassicInfo.AID),
                                        ChannelState.KEEP_OPEN,
                                        ContactlessProtocols.PROTOCOL_ISO14443_4);

                        logger.info("{} Create a PoSelectionRequest", node.getNodeId());

                        /*
                         * Prepare the reading order and keep the associated parser for later use
                         * once the selection has been made.
                         */
                        readEnvironmentParserIndex = poSelectionRequest.prepareReadRecordsCmd(
                                CalypsoClassicInfo.SFI_EnvironmentAndHolder,
                                ReadDataStructure.SINGLE_RECORD_DATA,
                                CalypsoClassicInfo.RECORD_NUMBER_1,
                                String.format("EnvironmentAndHolder (SFI=%02X))",
                                        CalypsoClassicInfo.SFI_EnvironmentAndHolder));

                        /*
                         * Add the selection case to the current selection (we could have added
                         * other cases here)
                         */
                        seSelection.prepareSelection(poSelectionRequest);

                        logger.info("{} setDefaultSelectionRequest for PoReader {}",
                                node.getNodeId(), poReader.getName());

                        /*
                         * Provide the SeReader with the selection operation to be processed when a
                         * PO is inserted.
                         */
                        ((ObservableReader) poReader).setDefaultSelectionRequest(
                                seSelection.getSelectionOperation(),
                                ObservableReader.NotificationMode.MATCHED_ONLY);


                        // observe reader events
                        logger.info("{} Add Master Thread as a Observer of virtual reader {}",
                                node.getNodeId(), poReader.getName());
                        poReader.addObserver(masterThread);

                    } catch (KeypleReaderNotFoundException e) {
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    } catch (KeyplePluginNotFoundException e) {
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }


                    break;
                case READER_DISCONNECTED:
                    logger.info("{} READER_DISCONNECTED {} {}", node.getNodeId(),
                            event.getPluginName(), event.getReaderNames().first());
                    break;
            }
        }
        // ReaderEvent
        else if (o instanceof ReaderEvent) {
            ReaderEvent event = (ReaderEvent) o;
            logger.debug("{} UPDATE {} {} {} {}", node.getNodeId(), event.getEventType(),
                    event.getPluginName(), event.getReaderName(),
                    event.getDefaultSelectionResponse());
            switch (event.getEventType()) {

                case SE_MATCHED:
                    SelectionsResult selectionsResult = seSelection
                            .processDefaultSelection(event.getDefaultSelectionResponse());
                    if (selectionsResult.hasActiveSelection()) {
                        MatchingSe selectedSe =
                                selectionsResult.getActiveSelection().getMatchingSe();

                        logger.info(
                                "{} Observer notification: the selection of the PO has succeeded.",
                                node.getNodeId());

                        /*
                         * Retrieve the data read from the parser updated during the selection
                         * process
                         */
                        ReadRecordsRespPars readEnvironmentParser =
                                (ReadRecordsRespPars) selectionsResult.getActiveSelection()
                                        .getResponseParser(readEnvironmentParserIndex);

                        byte environmentAndHolder[] = (readEnvironmentParser.getRecords())
                                .get((int) CalypsoClassicInfo.RECORD_NUMBER_1);

                        /* Log the result */
                        logger.info("{} Environment file data: {}", node.getNodeId(),
                                ByteArrayUtils.toHex(environmentAndHolder));

                        /* Go on with the reading of the first record of the EventLog file */
                        logger.info(
                                "==================================================================================");
                        logger.info(
                                "{} = 2nd PO exchange: reading transaction of the EventLog file.                     =",
                                node.getNodeId());
                        logger.info(
                                "==================================================================================");

                        PoTransaction poTransaction =
                                new PoTransaction(poReader, (CalypsoPo) selectedSe);

                        /*
                         * Prepare the reading order and keep the associated parser for later use
                         * once the transaction has been processed.
                         */
                        int readEventLogParserIndex =
                                poTransaction.prepareReadRecordsCmd(CalypsoClassicInfo.SFI_EventLog,
                                        ReadDataStructure.SINGLE_RECORD_DATA,
                                        CalypsoClassicInfo.RECORD_NUMBER_1,
                                        String.format("EventLog (SFI=%02X, recnbr=%d))",
                                                CalypsoClassicInfo.SFI_EventLog,
                                                CalypsoClassicInfo.RECORD_NUMBER_1));

                        /*
                         * Actual PO communication: send the prepared read order, then close the
                         * channel with the PO
                         */
                        try {
                            if (poTransaction.processPoCommands(ChannelState.CLOSE_AFTER)) {
                                logger.info("{} The reading of the EventLog has succeeded.",
                                        node.getNodeId());

                                /*
                                 * Retrieve the data read from the parser updated during the
                                 * transaction process
                                 */
                                ReadRecordsRespPars readEventLogParser =
                                        (ReadRecordsRespPars) poTransaction
                                                .getResponseParser(readEventLogParserIndex);
                                byte eventLog[] = (readEventLogParser.getRecords())
                                        .get((int) CalypsoClassicInfo.RECORD_NUMBER_1);

                                /* Log the result */
                                logger.info("{} EventLog file data: {} ", node.getNodeId(),
                                        ByteArrayUtils.toHex(eventLog));
                            }
                        } catch (KeypleReaderException e) {
                            e.printStackTrace();
                        }
                        logger.info(
                                "==================================================================================");
                        logger.info(
                                "{} = End of the Calypso PO processing.                                              =",
                                node.getNodeId());
                        logger.info(
                                "==================================================================================");
                    } else {
                        logger.error(
                                "{} The selection of the PO has failed. Should not have occurred due to the MATCHED_ONLY selection mode.",
                                node.getNodeId());
                    }
                    break;
                case SE_INSERTED:
                    logger.info("{} SE_INSERTED {} {}", node.getNodeId(), event.getPluginName(),
                            event.getReaderName());

                    // Transmit a SeRequestSet to native reader
                    // CommandSample.transmit(logger, event.getReaderName());

                    break;
                case SE_REMOVAL:
                    logger.info("{} SE_REMOVAL {} {}", node.getNodeId(), event.getPluginName(),
                            event.getReaderName());
                    break;
                case IO_ERROR:
                    logger.info("{} IO_ERROR {} {}", node.getNodeId(), event.getPluginName(),
                            event.getReaderName());
                    break;
            }
        }
    }

}
