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
package org.eclipse.keyple.integration.experimental.samresourcemanager;



import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.seproxy.ChannelState;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin.PluginObserver;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.transaction.SeSelection;
import org.eclipse.keyple.core.transaction.SelectionsResult;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.calypso.common.postructure.CalypsoClassicInfo;
import org.eclipse.keyple.plugin.pcsc.PcscPlugin;
import org.eclipse.keyple.plugin.pcsc.PcscProtocolSetting;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>Use Case ‘Calypso 5’ – SAM Resource Manager (PC/SC)</h1>
 */
public class UseCase_Calypso6_SamResourceManager_Pcsc implements PluginObserver {
    protected static final Logger logger =
            LoggerFactory.getLogger(UseCase_Calypso6_SamResourceManager_Pcsc.class);
    PoReaderObserver poReaderObserver;

    private SamResourceManager samResourceManager;
    private int readEnvironmentParser;
    /**
     * This object is used to freeze the main thread while card operations are handle through the
     * observers callbacks. A call to the notify() method would end the program (not demonstrated
     * here).
     */
    private static final Object waitForEnd = new Object();

    public UseCase_Calypso6_SamResourceManager_Pcsc()
            throws KeypleBaseException, InterruptedException {
        /* Get the instance of the SeProxyService (Singleton pattern) */
        SeProxyService seProxyService = SeProxyService.getInstance();

        /* Get the instance of the PC/SC plugin */
        PcscPlugin pcscPlugin = PcscPlugin.getInstance();

        /* Assign PcscPlugin to the SeProxyService */
        seProxyService.addPlugin(pcscPlugin);

        /* create a PoReaderObserver ready for the SE selection */
        poReaderObserver = new PoReaderObserver(getSeSelection());

        /* Add an observer to the plugin to handle reader connections/disconnections */
        ((ObservablePlugin) pcscPlugin).addObserver(this);

        samResourceManager = new SamResourceManager(pcscPlugin,
                ".*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*");

        logger.info(
                "=============== UseCase Calypso #6: SAM resource manager =========================");
        logger.info(
                "= Wait for a PO reader.                                                          =");
        logger.info(
                "==================================================================================");

        /* Wait for ever (exit with CTRL-C) */
        synchronized (waitForEnd) {
            waitForEnd.wait();
        }
    }


    public final SeSelection getSeSelection() {
        SeSelection seSelection;
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
        PoSelectionRequest poSelectionRequest = new PoSelectionRequest(
                new PoSelector(
                        new PoSelector.PoAidSelector(ByteArrayUtil.fromHex(CalypsoClassicInfo.AID),
                                PoSelector.InvalidatedPo.REJECT),
                        null, "AID: " + CalypsoClassicInfo.AID),
                ChannelState.KEEP_OPEN, ContactlessProtocols.PROTOCOL_ISO14443_4);

        /*
         * Prepare the reading order and keep the associated parser for later use once the selection
         * has been made.
         */
        readEnvironmentParser = poSelectionRequest.prepareReadRecordsCmd(
                CalypsoClassicInfo.SFI_EnvironmentAndHolder, ReadDataStructure.SINGLE_RECORD_DATA,
                CalypsoClassicInfo.RECORD_NUMBER_1,
                String.format("EnvironmentAndHolder (SFI=%02X))",
                        CalypsoClassicInfo.SFI_EnvironmentAndHolder));

        /*
         * Add the selection case to the current selection (we could have added other cases here)
         */
        seSelection.prepareSelection(poSelectionRequest);

        return seSelection;
    }

    @Override
    public void update(PluginEvent event) {
        for (String readerName : event.getReaderNames()) {
            SeReader reader = null;
            logger.info("PluginEvent: PLUGINNAME = {}, READERNAME = {}, EVENTTYPE = {}",
                    event.getPluginName(), readerName, event.getEventType());

            /* ignore non contactless readers */
            if (ReaderUtilities
                    .getReaderType(readerName) != ReaderUtilities.ReaderType.CONTACTLESS_READER) {
                return;
            }

            /* We retrieve the reader object from its name. */
            try {
                reader = SeProxyService.getInstance().getPlugin(event.getPluginName())
                        .getReader(readerName);
            } catch (KeyplePluginNotFoundException e) {
                e.printStackTrace();
            } catch (KeypleReaderNotFoundException e) {
                e.printStackTrace();
            }
            switch (event.getEventType()) {
                case READER_CONNECTED:
                    logger.info("New reader! READERNAME = {}", reader.getName());

                    /*
                     * We are informed here of a disconnection of a reader.
                     *
                     * We add an observer to this reader if this is possible.
                     */
                    if (reader instanceof ObservableReader) {
                        if (poReaderObserver != null) {
                            logger.info("Add observer READERNAME = {}", reader.getName());

                            ((ObservableReader) reader).addObserver(poReaderObserver);

                            try {
                                /* Enable logging */
                                reader.setParameter(PcscReader.SETTING_KEY_LOGGING, "true");

                                /* Contactless SE works with T1 protocol */
                                reader.setParameter(PcscReader.SETTING_KEY_PROTOCOL,
                                        PcscReader.SETTING_PROTOCOL_T1);

                                /*
                                 * PC/SC card access mode:
                                 *
                                 * The SAM is left in the SHARED mode (by default) to avoid
                                 * automatic resets due to the limited time between two consecutive
                                 * exchanges granted by Windows.
                                 *
                                 * The PO reader is set to EXCLUSIVE mode to avoid side effects
                                 * during the selection step that may result in session failures.
                                 *
                                 * These two points will be addressed in a coming release of the
                                 * Keyple PcSc reader plugin.
                                 */
                                reader.setParameter(PcscReader.SETTING_KEY_MODE,
                                        PcscReader.SETTING_MODE_SHARED);

                                /* Set the PO reader protocol flag */
                                reader.addSeProtocolSetting(new SeProtocolSetting(
                                        PcscProtocolSetting.SETTING_PROTOCOL_ISO14443_4));
                            } catch (KeypleBaseException e) {
                                e.printStackTrace();
                            }

                            ((ObservableReader) reader).setDefaultSelectionRequest(
                                    getSeSelection().getSelectionOperation(),
                                    ObservableReader.NotificationMode.MATCHED_ONLY);
                        } else {
                            logger.info("No observer to add READERNAME = {}", reader.getName());
                        }
                    }
                    break;
                case READER_DISCONNECTED:
                    /*
                     * We are informed here of a disconnection of a reader.
                     *
                     * The reader object still exists but will be removed from the reader list right
                     * after. Thus, we can properly remove the observer attached to this reader
                     * before the list update.
                     */
                    logger.info("Reader removed. READERNAME = {}", readerName);
                    if (reader instanceof ObservableReader) {
                        if (poReaderObserver != null) {
                            logger.info("Remove observer READERNAME = {}", readerName);
                            ((ObservableReader) reader).removeObserver(poReaderObserver);
                        } else {
                            logger.info("Unplugged reader READERNAME = {} wasn't observed.",
                                    readerName);
                        }
                    }
                    break;
                default:
                    logger.info("Unexpected reader event. EVENT = {}",
                            event.getEventType().getName());
                    break;
            }
        }
    }


    /**
     * This method is called whenever a Reader event occurs (SE insertion/removal)
     */
    public class PoReaderObserver implements ObservableReader.ReaderObserver {
        private SeSelection seSelection;

        PoReaderObserver(SeSelection seSelection) {
            super();
            this.seSelection = seSelection;
        }

        /**
         * Method invoked in the case of a reader event
         *
         * @param event the reader event
         */
        @Override
        public void update(ReaderEvent event) {
            SeReader poReader = null;
            SamResource samResource = null;
            try {
                poReader = ReaderUtilities.getReaderByName(SeProxyService.getInstance(),
                        event.getReaderName());
            } catch (KeypleReaderException e) {
                e.printStackTrace();
            }
            switch (event.getEventType()) {
                case SE_MATCHED:
                    SelectionsResult selectionsResult = seSelection
                            .processDefaultSelection(event.getDefaultSelectionResponse());
                    if (selectionsResult.hasActiveSelection()) {
                        CalypsoPo calypsoPo =
                                (CalypsoPo) selectionsResult.getActiveSelection().getMatchingSe();

                        logger.info(
                                "Observer notification: the selection of the PO has succeeded.");

                        try {
                            samResource = samResourceManager.allocateSamResource(
                                    SamResourceManager.AllocationMode.BLOCKING,
                                    new SamIdentifier(SamRevision.C1, "", null));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                        PoTransaction poTransaction =
                                new PoTransaction(new PoResource(poReader, calypsoPo), samResource,
                                        new SecuritySettings());

                        try {

                            /*
                             * Open Session for the debit key
                             */
                            boolean poProcessStatus = false;
                            poProcessStatus = poTransaction.processOpening(
                                    PoTransaction.ModificationMode.ATOMIC,
                                    PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT, (byte) 0,
                                    (byte) 0);

                            if (!poProcessStatus) {
                                throw new IllegalStateException("processingOpening failure.");
                            }

                            int readEventLogParserIndex = poTransaction.prepareReadRecordsCmd(
                                    CalypsoClassicInfo.SFI_EventLog,
                                    ReadDataStructure.SINGLE_RECORD_DATA,
                                    CalypsoClassicInfo.RECORD_NUMBER_1,
                                    String.format("EventLog (SFI=%02X, recnbr=%d))",
                                            CalypsoClassicInfo.SFI_EventLog,
                                            CalypsoClassicInfo.RECORD_NUMBER_1));

                            if (poTransaction.processPoCommandsInSession()) {
                                logger.info("The reading of the EventLog has succeeded.");

                                byte eventLog[] = ((ReadRecordsRespPars) (poTransaction
                                        .getResponseParser(readEventLogParserIndex))).getRecords()
                                                .get((int) CalypsoClassicInfo.RECORD_NUMBER_1);

                                logger.info("EventLog file data: {}",
                                        ByteArrayUtil.toHex(eventLog));
                            }

                            /*
                             * Close the Secure Session.
                             */
                            if (logger.isInfoEnabled()) {
                                logger.info(
                                        "========= PO Calypso session ======= Closing ============================");
                            }

                            /*
                             * A ratification command will be sent (CONTACTLESS_MODE).
                             */
                            poProcessStatus = poTransaction.processClosing(
                                    TransmissionMode.CONTACTLESS, ChannelState.CLOSE_AFTER);

                            if (!poProcessStatus) {
                                throw new IllegalStateException("processClosing failure.");
                            }
                        } catch (KeypleReaderException e) {
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
                    samResourceManager.freeSamResource(samResource);
                    break;
                case SE_INSERTED:
                    logger.error(
                            "SE_INSERTED event: should not have occurred due to the MATCHED_ONLY selection mode.");
                    break;
                case SE_REMOVAL:
                    logger.info("The PO has been removed.");
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * main program entry
     */
    public static void main(String[] args) throws InterruptedException, KeypleBaseException {
        /* Create the observable object to handle the PO processing */
        UseCase_Calypso6_SamResourceManager_Pcsc m = new UseCase_Calypso6_SamResourceManager_Pcsc();
    }

}
