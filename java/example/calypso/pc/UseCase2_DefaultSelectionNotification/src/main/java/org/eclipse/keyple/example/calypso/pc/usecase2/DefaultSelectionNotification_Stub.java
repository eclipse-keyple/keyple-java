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
package org.eclipse.keyple.example.calypso.pc.usecase2;


import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars;
import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoDesynchronisedExchangesException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoSecureSessionException;
import org.eclipse.keyple.core.selection.MatchingSelection;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.seproxy.*;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ObservableReader.ReaderObserver;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.common.calypso.postructure.CalypsoClassicInfo;
import org.eclipse.keyple.example.common.calypso.stub.StubCalypsoClassic;
import org.eclipse.keyple.plugin.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>Use Case ‘Calypso 2’ – Default Selection Notification (Stub)</h1>
 * <ul>
 * <li>
 * <h2>Scenario:</h2>
 * <ul>
 * <li>Define a default selection of ISO 14443-4 Calypso PO and set it to an observable reader, on
 * SE detection in case the Calypso selection is successful, notify the terminal application with
 * the PO information, then the terminal follows by operating a simple Calypso PO transaction.</li>
 * <li><code>
 Default Selection Notification
 </code> means that the SE processing is automatically started when detected.</li>
 * <li>PO messages:
 * <ul>
 * <li>A first SE message to notify about the selected Calypso PO</li>
 * <li>A second SE message to operate the simple Calypso transaction</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 */
public class DefaultSelectionNotification_Stub implements ReaderObserver {
    protected static final Logger logger =
            LoggerFactory.getLogger(DefaultSelectionNotification_Stub.class);
    private SeSelection seSelection;
    private int readEnvironmentParserIndex;
    /**
     * This object is used to freeze the main thread while card operations are handle through the
     * observers callbacks. A call to the notify() method would end the program (not demonstrated
     * here).
     */
    private static final Object waitForEnd = new Object();

    public DefaultSelectionNotification_Stub() throws KeypleException, InterruptedException {

        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        final String STUB_PLUGIN_NAME = "stub1";

        /* Register Stub plugin in the platform */
        seProxyService.registerPlugin(new StubPluginFactory(STUB_PLUGIN_NAME));
        ReaderPlugin stubPlugin = seProxyService.getPlugin(STUB_PLUGIN_NAME);

        /* Plug the PO stub reader. */
        ((StubPlugin) stubPlugin).plugStubReader("poReader", true);


        /*
         * Get a PO reader ready to work with Calypso PO.
         */
        StubReader poReader = (StubReader) (stubPlugin.getReader("poReader"));

        /* Check if the reader exists */
        if (poReader == null) {
            throw new IllegalStateException("Bad PO reader setup");
        }

        poReader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                StubProtocolSetting.STUB_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO14443_4));

        logger.info(
                "=============== UseCase Calypso #2: AID based default selection ===================");
        logger.info("= PO Reader  NAME = {}", poReader.getName());

        /*
         * Prepare a Calypso PO selection
         */
        seSelection = new SeSelection();

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
        PoSelectionRequest poSelectionRequest =
                new PoSelectionRequest(new PoSelector(SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                        new PoSelector.PoAidSelector(
                                new SeSelector.AidSelector.IsoAid(CalypsoClassicInfo.AID),
                                PoSelector.InvalidatedPo.REJECT),
                        "AID: " + CalypsoClassicInfo.AID));

        /*
         * Prepare the reading order and keep the associated parser for later use once the selection
         * has been made.
         */
        readEnvironmentParserIndex =
                poSelectionRequest.prepareReadRecords(CalypsoClassicInfo.SFI_EnvironmentAndHolder,
                        ReadDataStructure.SINGLE_RECORD_DATA, CalypsoClassicInfo.RECORD_NUMBER_1);

        /*
         * Add the selection case to the current selection (we could have added other cases here)
         */
        seSelection.prepareSelection(poSelectionRequest);

        /*
         * Provide the SeReader with the selection operation to be processed when a PO is inserted.
         */
        ((ObservableReader) poReader).setDefaultSelectionRequest(
                seSelection.getSelectionOperation(), ObservableReader.NotificationMode.MATCHED_ONLY,
                ObservableReader.PollingMode.REPEATING);

        /* Set the current class as Observer of the first reader */
        ((ObservableReader) poReader).addObserver(this);

        logger.info(
                "==================================================================================");
        logger.info(
                "= Wait for a PO. The default AID based selection with reading of Environment     =");
        logger.info(
                "= file is ready to be processed as soon as the PO is detected.                   =");
        logger.info(
                "==================================================================================");

        /* Create 'virtual' Calypso PO */
        StubSecureElement calypsoStubSe = new StubCalypsoClassic();

        /* Wait a while. */
        Thread.sleep(100);

        logger.info("Insert stub PO.");
        poReader.insertSe(calypsoStubSe);

        /* Wait a while. */
        Thread.sleep(1000);

        logger.info("Remove stub PO.");
        poReader.removeSe();

        System.exit(0);
    }

    /**
     * Method invoked in the case of a reader event
     * 
     * @param event the reader event
     */
    @Override
    public void update(ReaderEvent event) {
        switch (event.getEventType()) {
            case SE_MATCHED:
                SeReader poReader = null;
                MatchingSelection matchingSelection =
                        seSelection.processDefaultSelection(event.getDefaultSelectionsResponse())
                                .getActiveSelection();

                CalypsoPo calypsoPo = (CalypsoPo) matchingSelection.getMatchingSe();

                if (calypsoPo.isSelected()) {
                    try {
                        poReader = SeProxyService.getInstance().getPlugin(event.getPluginName())
                                .getReader(event.getReaderName());
                    } catch (KeyplePluginNotFoundException e) {
                        e.printStackTrace();
                    } catch (KeypleReaderNotFoundException e) {
                        e.printStackTrace();
                    }

                    logger.info("Observer notification: the selection of the PO has succeeded.");

                    /*
                     * Retrieve the data read from the parser updated during the selection process
                     */
                    ReadRecordsRespPars readEnvironmentParser =
                            (ReadRecordsRespPars) matchingSelection
                                    .getResponseParser(readEnvironmentParserIndex);

                    byte environmentAndHolder[] = (readEnvironmentParser.getRecords())
                            .get((int) CalypsoClassicInfo.RECORD_NUMBER_1);

                    /* Log the result */
                    logger.info("Environment file data: {}",
                            ByteArrayUtil.toHex(environmentAndHolder));

                    /* Go on with the reading of the first record of the EventLog file */
                    logger.info(
                            "==================================================================================");
                    logger.info(
                            "= 2nd PO exchange: reading transaction of the EventLog file.                     =");
                    logger.info(
                            "==================================================================================");

                    PoTransaction poTransaction =
                            new PoTransaction(new PoResource(poReader, calypsoPo));

                    /*
                     * Prepare the reading order and keep the associated parser for later use once
                     * the transaction has been processed.
                     */
                    int readEventLogParserIndex = poTransaction.prepareReadRecords(
                            CalypsoClassicInfo.SFI_EventLog, ReadDataStructure.SINGLE_RECORD_DATA,
                            CalypsoClassicInfo.RECORD_NUMBER_1);

                    /*
                     * Actual PO communication: send the prepared read order, then close the channel
                     * with the PO
                     */
                    try {
                        if (poTransaction.processPoCommands(ChannelControl.CLOSE_AFTER)) {
                            logger.info("The reading of the EventLog has succeeded.");

                            /*
                             * Retrieve the data read from the parser updated during the transaction
                             * process
                             */
                            byte eventLog[] = (((ReadRecordsRespPars) poTransaction
                                    .getResponseParser(readEventLogParserIndex)).getRecords())
                                            .get((int) CalypsoClassicInfo.RECORD_NUMBER_1);

                            /* Log the result */
                            logger.info("EventLog file data: {}", ByteArrayUtil.toHex(eventLog));
                        }
                    } catch (KeypleReaderException e) {
                        e.printStackTrace();
                    } catch (CalypsoSecureSessionException e) {
                        e.printStackTrace();
                    } catch (CalypsoDesynchronisedExchangesException e) {
                        e.printStackTrace();
                    }
                    logger.info(
                            "==================================================================================");
                    logger.info(
                            "= End of the Calypso PO processing.                                              =");
                    logger.info(
                            "==================================================================================");
                } else {
                    logger.error(
                            "The selection of the PO has failed. Should not have occurred due to the MATCHED_ONLY selection mode.");
                }
                break;
            case SE_INSERTED:
                logger.error(
                        "SE_INSERTED event: should not have occurred due to the MATCHED_ONLY selection mode.");
                break;
            case SE_REMOVED:
                logger.info("There is no PO inserted anymore. Return to the waiting state...");
                break;
            default:
                break;
        }


        if (event.getEventType() == ReaderEvent.EventType.SE_INSERTED
                || event.getEventType() == ReaderEvent.EventType.SE_MATCHED) {
            /*
             * Informs the underlying layer of the end of the SE processing, in order to manage the
             * removal sequence. <p>If closing has already been requested, this method will do
             * nothing.
             */
            try {
                ((ObservableReader) SeProxyService.getInstance().getPlugin(event.getPluginName())
                        .getReader(event.getReaderName())).notifySeProcessed();
            } catch (KeypleReaderNotFoundException e) {
                e.printStackTrace();
            } catch (KeyplePluginNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * main program entry
     */
    public static void main(String[] args) throws InterruptedException, KeypleException {
        /* Create the observable object to handle the PO processing */
        DefaultSelectionNotification_Stub m = new DefaultSelectionNotification_Stub();
    }
}
