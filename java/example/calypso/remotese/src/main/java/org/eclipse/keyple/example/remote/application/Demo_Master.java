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
import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.selection.MatchingSelection;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.*;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.calypso.common.postructure.CalypsoClassicInfo;
import org.eclipse.keyple.example.calypso.common.stub.se.StubSamCalypsoClassic;
import org.eclipse.keyple.example.calypso.pc.transaction.CalypsoUtilities;
import org.eclipse.keyple.plugin.remotese.pluginse.MasterAPI;
import org.eclipse.keyple.plugin.remotese.pluginse.RemoteSePlugin;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReader;
import org.eclipse.keyple.plugin.remotese.transport.DtoNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.ClientNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.ServerNode;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;
import org.eclipse.keyple.plugin.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo_Master is the Master Device that controls remotely native readers. A Slave Terminal
 * delegates control of one of its native reader to the Master. In response a {@link VirtualReader}
 * is created and accessible via {@link RemoteSePlugin} like any local reader
 */
public class Demo_Master {

    private static final Logger logger = LoggerFactory.getLogger(Demo_Master.class);
    // private SeSelection seSelection;
    // private VirtualReader poReader;
    // private int readEnvironmentParserIndex;
    private SamResource samResource;
    private MasterAPI masterAPI;

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
             * @Override public void run() { // Master is client, connectPoReader to Slave Server }
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
        masterAPI = new MasterAPI(SeProxyService.getInstance(), node);

        // observe remote se plugin for events
        logger.info("{} Observe SeRemotePlugin for Plugin Events and Reader Events",
                node.getNodeId());
        ReaderPlugin rsePlugin = masterAPI.getPlugin();


        ((ObservablePlugin) rsePlugin).addObserver(new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {
                logger.info("{} UPDATE {} {} {}", node.getNodeId(), event.getEventType(),
                        event.getPluginName(), event.getReaderNames().first());
                switch (event.getEventType()) {
                    case READER_CONNECTED:

                        // a new virtual reader is connected, let's configure it
                        try {
                            ReaderPlugin remoteSEPlugin =
                                    SeProxyService.getInstance().getPlugin("RemoteSePlugin");

                            SeReader poReader =
                                    remoteSEPlugin.getReader(event.getReaderNames().first());

                            logger.info("{} Configure SeSelection", node.getNodeId());

                            /* set default selection request */
                            SeSelection seSelection = new SeSelection(
                                    MultiSeRequestProcessing.FIRST_MATCH, ChannelState.KEEP_OPEN);

                            /*
                             * Setting of an AID based selection of a Calypso REV3 PO
                             *
                             * Select the first application matching the selection AID whatever the
                             * SE communication protocol keep the logical channel open after the
                             * selection
                             */

                            /*
                             * Calypso selection: configures a PoSelectionRequest with all the
                             * desired attributes to make the selection and read additional
                             * information afterwards
                             */
                            PoSelectionRequest poSelectionRequest = new PoSelectionRequest(
                                    new PoSelector(SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                                            new PoSelector.PoAidSelector(
                                                    new SeSelector.AidSelector.IsoAid(
                                                            CalypsoClassicInfo.AID),
                                                    null),
                                            "AID: " + CalypsoClassicInfo.AID));

                            logger.info("{} Create a PoSelectionRequest", node.getNodeId());

                            /*
                             * Add the selection case to the current selection (we could have added
                             * other cases here)
                             */
                            seSelection.prepareSelection(poSelectionRequest);

                            logger.info("{} setDefaultSelectionRequest for PoReader {}",
                                    node.getNodeId(), poReader.getName());

                            /*
                             * Provide the SeReader with the selection operation to be processed
                             * when a PO is inserted.
                             */
                            ((ObservableReader) poReader).setDefaultSelectionRequest(
                                    seSelection.getSelectionOperation(),
                                    ObservableReader.NotificationMode.MATCHED_ONLY);


                            // observe reader events
                            logger.info("{} Add Master Thread as Observer of Virtual Reader {}",
                                    node.getNodeId(), poReader.getName());
                            ((ObservableReader) poReader)
                                    .addObserver(new ObservableReader.ReaderObserver() {
                                        @Override
                                        public void update(ReaderEvent event) {
                                            logger.info("{} UPDATE {} {} {} {}", node.getNodeId(),
                                                    event.getEventType(), event.getPluginName(),
                                                    event.getReaderName(),
                                                    event.getDefaultSelectionsResponse());

                                            switch (event.getEventType()) {

                                                case SE_MATCHED:

                                                    // executeReadEventLog(selectionsResult);
                                                    executeCalypso4_PoAuthentication(samResource,
                                                            event.getReaderName());

                                                    break;
                                                case SE_INSERTED:
                                                    logger.info("{} SE_INSERTED {} {}",
                                                            node.getNodeId(), event.getPluginName(),
                                                            event.getReaderName());

                                                    // Transmit a SeRequest to native reader
                                                    // CommandSample.transmit(logger,
                                                    // event.getReaderName());

                                                    break;
                                                case SE_REMOVED:
                                                    logger.info("{} SE_REMOVED {} {}",
                                                            node.getNodeId(), event.getPluginName(),
                                                            event.getReaderName());
                                                    break;

                                                case TIMEOUT_ERROR:
                                                    logger.info("{} TIMEOUT_ERROR {} {}",
                                                            node.getNodeId(), event.getPluginName(),
                                                            event.getReaderName());
                                                    break;
                                            }
                                        }
                                    });

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
        });

        /*
         * Plug a stub SAM Reader
         */

        SeProxyService.getInstance().registerPlugin(new StubPluginFactory());


        try {
            /* Get the instance of the Stub plugin */
            ReaderPlugin stubPlugin =
                    SeProxyService.getInstance().getPlugin(StubPlugin.PLUGIN_NAME);

            /* Plug the SAM stub reader. */
            ((StubPlugin) stubPlugin).plugStubReader("samReader", true);

            SeReader samReader = stubPlugin.getReader("samReader");

            samReader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO7816_3,
                    StubProtocolSetting.STUB_PROTOCOL_SETTING
                            .get(SeCommonProtocols.PROTOCOL_ISO7816_3));

            /* Create 'virtual' Calypso SAM */
            StubSecureElement calypsoSamStubSe = new StubSamCalypsoClassic();

            ((StubReader) samReader).insertSe(calypsoSamStubSe);
            logger.info("Stub SAM inserted");

            /*
             * Open logical channel for the SAM inserted in the reader
             *
             * (We expect the right is inserted)
             */
            samResource = CalypsoUtilities.checkSamAndOpenChannel(samReader);


        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
        } catch (KeyplePluginNotFoundException e) {
            e.printStackTrace();
        }


    }



    private void executeReadEventLog(SelectionsResult selectionsResult, String virtualReaderName) {


        if (selectionsResult.hasActiveSelection()) {
            try {
                SeReader poReader = masterAPI.getPlugin().getReader(virtualReaderName);

                AbstractMatchingSe selectedSe =
                        selectionsResult.getActiveSelection().getMatchingSe();

                logger.info("{} Observer notification: the selection of the PO has succeeded.",
                        node.getNodeId());

                /* Go on with the reading of the first record of the EventLog file */
                logger.info(
                        "==================================================================================");
                logger.info(
                        "{} = 2nd PO exchange: reading transaction of the EventLog file.                     =",
                        node.getNodeId());
                logger.info(
                        "==================================================================================");

                PoTransaction poTransaction =
                        new PoTransaction(new PoResource(poReader, (CalypsoPo) selectedSe));

                /*
                 * Prepare the reading order and keep the associated parser for later use once the
                 * transaction has been processed.
                 */
                int readEventLogParserIndex = poTransaction.prepareReadRecordsCmd(
                        CalypsoClassicInfo.SFI_EventLog, ReadDataStructure.SINGLE_RECORD_DATA,
                        CalypsoClassicInfo.RECORD_NUMBER_1,
                        String.format("EventLog (SFI=%02X, recnbr=%d))",
                                CalypsoClassicInfo.SFI_EventLog,
                                CalypsoClassicInfo.RECORD_NUMBER_1));

                /*
                 * Actual PO communication: send the prepared read order, then close the channel
                 * with the PO
                 */

                if (poTransaction.processPoCommands(ChannelState.CLOSE_AND_CONTINUE)) {
                    logger.info("{} The reading of the EventLog has succeeded.", node.getNodeId());

                    /*
                     * Retrieve the data read from the parser updated during the transaction process
                     */
                    ReadRecordsRespPars readEventLogParser = (ReadRecordsRespPars) poTransaction
                            .getResponseParser(readEventLogParserIndex);
                    byte eventLog[] = (readEventLogParser.getRecords())
                            .get((int) CalypsoClassicInfo.RECORD_NUMBER_1);

                    /* Log the result */
                    logger.info("{} EventLog file data: {} ", node.getNodeId(),
                            ByteArrayUtil.toHex(eventLog));
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
    }


    private void executeCalypso4_PoAuthentication(SamResource samResource,
            String virtualReaderName) {

        try {
            SeReader poReader = masterAPI.getPlugin().getReader(virtualReaderName);

            logger.info(
                    "==================================================================================");
            logger.info(
                    "= 1st PO exchange: AID based selection with reading of Environment file.         =");
            logger.info(
                    "==================================================================================");

            /*
             * Prepare a Calypso PO selection
             */
            SeSelection seSelection = new SeSelection();

            /*
             * Setting of an AID based selection of a Calypso REV3 PO
             *
             * Select the first application matching the selection AID whatever the SE communication
             * protocol keep the logical channel open after the selection
             */

            /*
             * Calypso selection: configures a PoSelectionRequest with all the desired attributes to
             * make the selection and read additional information afterwards
             */
            PoSelectionRequest poSelectionRequest = new PoSelectionRequest(new PoSelector(
                    SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                    new PoSelector.PoAidSelector(
                            new SeSelector.AidSelector.IsoAid(CalypsoClassicInfo.AID),
                            PoSelector.InvalidatedPo.REJECT),
                    "AID: " + CalypsoClassicInfo.AID));
            /*
             * Add the selection case to the current selection (we could have added other cases
             * here)
             */
            seSelection.prepareSelection(poSelectionRequest);

            /*
             * Actual PO communication: operate through a single request the Calypso PO selection
             * and the file read
             */
            SelectionsResult selectionsResult = null;

            /*
             * Process selection
             */
            selectionsResult = seSelection.processExplicitSelection(poReader);


            if (selectionsResult.hasActiveSelection()) {
                MatchingSelection matchingSelection = selectionsResult.getActiveSelection();

                CalypsoPo calypsoPo = (CalypsoPo) matchingSelection.getMatchingSe();
                logger.info("The selection of the PO has succeeded.");

                /* Go on with the reading of the first record of the EventLog file */
                logger.info(
                        "==================================================================================");
                logger.info(
                        "= 2nd PO exchange: open and close a secure session to perform authentication.    =");
                logger.info(
                        "==================================================================================");

                PoTransaction poTransaction = new PoTransaction(new PoResource(poReader, calypsoPo),
                        samResource, CalypsoUtilities.getSecuritySettings());

                /*
                 * Prepare the reading order and keep the associated parser for later use once the
                 * transaction has been processed.
                 */
                int readEventLogParserIndex = poTransaction.prepareReadRecordsCmd(
                        CalypsoClassicInfo.SFI_EventLog, ReadDataStructure.SINGLE_RECORD_DATA,
                        CalypsoClassicInfo.RECORD_NUMBER_1,
                        String.format("EventLog (SFI=%02X, recnbr=%d))",
                                CalypsoClassicInfo.SFI_EventLog,
                                CalypsoClassicInfo.RECORD_NUMBER_1));


                /*
                 * Open Session for the debit key
                 */
                boolean poProcessStatus = poTransaction.processOpening(
                        PoTransaction.ModificationMode.ATOMIC,
                        PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT, (byte) 0, (byte) 0);

                if (!poProcessStatus) {
                    throw new IllegalStateException("processingOpening failure.");
                }

                if (!poTransaction.wasRatified()) {
                    logger.info(
                            "========= Previous Secure Session was not ratified. =====================");
                }
                /*
                 * Prepare the reading order and keep the associated parser for later use once the
                 * transaction has been processed.
                 */
                int readEventLogParserIndexBis = poTransaction.prepareReadRecordsCmd(
                        CalypsoClassicInfo.SFI_EventLog, ReadDataStructure.SINGLE_RECORD_DATA,
                        CalypsoClassicInfo.RECORD_NUMBER_1,
                        String.format("EventLog (SFI=%02X, recnbr=%d))",
                                CalypsoClassicInfo.SFI_EventLog,
                                CalypsoClassicInfo.RECORD_NUMBER_1));

                poProcessStatus = poTransaction.processPoCommandsInSession();

                /*
                 * Retrieve the data read from the parser updated during the transaction process
                 */
                byte eventLog[] = (((ReadRecordsRespPars) poTransaction
                        .getResponseParser(readEventLogParserIndexBis)).getRecords())
                                .get((int) CalypsoClassicInfo.RECORD_NUMBER_1);

                /* Log the result */
                logger.info("EventLog file data: {}", ByteArrayUtil.toHex(eventLog));

                if (!poProcessStatus) {
                    throw new IllegalStateException("processPoCommandsInSession failure.");
                }

                /*
                 * Close the Secure Session.
                 */
                if (logger.isInfoEnabled()) {
                    logger.info(
                            "================= PO Calypso session ======= Closing ============================");
                }

                /*
                 * A ratification command will be sent (CONTACTLESS_MODE).
                 */
                poProcessStatus = poTransaction.processClosing(ChannelState.CLOSE_AND_CONTINUE);

                if (!poProcessStatus) {
                    throw new IllegalStateException("processClosing failure.");
                }

                logger.info(
                        "==================================================================================");
                logger.info(
                        "= End of the Calypso PO processing.                                              =");
                logger.info(
                        "==================================================================================");
            } else {
                logger.error("The selection of the PO has failed.");
            }
        } catch (KeypleReaderException e) {
            e.printStackTrace();
        }
    }

}
