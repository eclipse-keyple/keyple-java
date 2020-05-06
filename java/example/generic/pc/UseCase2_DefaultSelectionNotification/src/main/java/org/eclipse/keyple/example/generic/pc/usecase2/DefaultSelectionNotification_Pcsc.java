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
package org.eclipse.keyple.example.generic.pc.usecase2;


import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ObservableReader.ReaderObserver;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.common.ReaderUtilities;
import org.eclipse.keyple.example.common.generic.GenericSeSelectionRequest;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>Use Case ‘generic 2’ – Default Selection Notification (PC/SC)</h1>
 * <ul>
 * <li>
 * <h2>Scenario:</h2>
 * <ul>
 * <li>Define a default selection of ISO 14443-4 (here a Calypso PO) and set it to an observable
 * reader, on SE detection in case the selection is successful, notify the terminal application with
 * the SE information.</li>
 * <li><code>
 Default Selection Notification
 </code> means that the SE processing is automatically started when detected.</li>
 * <li>PO messages:
 * <ul>
 * <li>A single SE message handled at SeReader level</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 */
public class DefaultSelectionNotification_Pcsc implements ReaderObserver {
    private static final Logger logger =
            LoggerFactory.getLogger(DefaultSelectionNotification_Pcsc.class);
    private String seAid = "A0000004040125090101";
    private SeSelection seSelection;
    /**
     * This object is used to freeze the main thread while card operations are handle through the
     * observers callbacks. A call to the notify() method would end the program (not demonstrated
     * here).
     */
    private static final Object waitForEnd = new Object();

    public DefaultSelectionNotification_Pcsc() throws KeypleException, InterruptedException {
        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.registerPlugin(new PcscPluginFactory());

        /*
         * Get a SE reader ready to work with contactless SE. Use the getReader helper method from
         * the ReaderUtilities class.
         */
        SeReader seReader = ReaderUtilities.getDefaultContactLessSeReader();

        /* Check if the reader exists */
        if (seReader == null) {
            throw new IllegalStateException("Bad SE reader setup");
        }

        logger.info(
                "=============== UseCase Generic #2: AID based default selection ===================");
        logger.info("= SE Reader  NAME = {}", seReader.getName());

        /*
         * Prepare a SE selection
         */
        seSelection = new SeSelection();

        /*
         * Setting of an AID based selection
         *
         * Select the first application matching the selection AID whatever the SE communication
         * protocol keep the logical channel open after the selection
         */

        /*
         * Generic selection: configures a SeSelector with all the desired attributes to make the
         * selection
         */
        GenericSeSelectionRequest seSelector =
                new GenericSeSelectionRequest(new SeSelector(SeCommonProtocols.PROTOCOL_ISO14443_4,
                        null, new SeSelector.AidSelector(
                                new SeSelector.AidSelector.IsoAid(ByteArrayUtil.fromHex(seAid)))));

        /*
         * Add the selection case to the current selection (we could have added other cases here)
         */
        seSelection.prepareSelection(seSelector);

        /*
         * Provide the SeReader with the selection operation to be processed when a SE is inserted.
         */
        ((ObservableReader) seReader).setDefaultSelectionRequest(
                seSelection.getSelectionOperation(), ObservableReader.NotificationMode.MATCHED_ONLY,
                ObservableReader.PollingMode.REPEATING);

        /* Set the current class as Observer of the first reader */
        ((ObservableReader) seReader).addObserver(this);

        logger.info(
                "==================================================================================");
        logger.info(
                "= Wait for a SE. The default AID based selection to be processed as soon as the  =");
        logger.info(
                "= SE is detected.                                                                =");
        logger.info(
                "==================================================================================");

        /* Wait for ever (exit with CTRL-C) */
        synchronized (waitForEnd) {
            waitForEnd.wait();
        }
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
                /* the selection has one target, get the result at index 0 */
                AbstractMatchingSe selectedSe = null;
                try {
                    selectedSe = seSelection
                            .processDefaultSelection(event.getDefaultSelectionsResponse())
                            .getActiveMatchingSe();
                } catch (KeypleException e) {
                    // TODO Rework error management
                    e.printStackTrace();
                }

                if (selectedSe != null) {
                    logger.info("Observer notification: the selection of the SE has succeeded.");

                    logger.info(
                            "==================================================================================");
                    logger.info(
                            "= End of the SE processing.                                                      =");
                    logger.info(
                            "==================================================================================");
                } else {
                    logger.error(
                            "The selection of the SE has failed. Should not have occurred due to the MATCHED_ONLY selection mode.");
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
        /* Create the observable object to handle the SE processing */
        DefaultSelectionNotification_Pcsc m = new DefaultSelectionNotification_Pcsc();
    }
}
