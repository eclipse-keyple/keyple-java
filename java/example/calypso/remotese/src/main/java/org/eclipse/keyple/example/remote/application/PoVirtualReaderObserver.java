/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
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

import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamCommandException;
import org.eclipse.keyple.calypso.exception.CalypsoNoSamResourceAvailableException;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.ElementaryFile;
import org.eclipse.keyple.calypso.transaction.PoResource;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.calypso.transaction.SamIdentifier;
import org.eclipse.keyple.calypso.transaction.SamResource;
import org.eclipse.keyple.calypso.transaction.SamResourceManager;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoTransactionException;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleAllocationReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.example.common.calypso.pc.transaction.CalypsoUtilities;
import org.eclipse.keyple.example.common.calypso.postructure.CalypsoClassicInfo;
import org.eclipse.keyple.plugin.remotese.pluginse.MasterAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Observes READER EVENT for the PO virtual readers
 */
public class PoVirtualReaderObserver implements ObservableReader.ReaderObserver {
    private static final Logger logger = LoggerFactory.getLogger(PoVirtualReaderObserver.class);

    private final MasterAPI masterAPI;
    private final String nodeId;// for logging purposes
    private final SeSelection seSelection;
    private final SamResourceManager samResourceManager;


    /**
     * Create a new Observer for a PO Virtual Reader
     * 
     * @param masterAPI : master API
     * @param seSelection : the default selection configured on the reader
     * @param nodeId : master node id, used for logging
     * @param samResourceManager : SAM Resource Manager required for transactions
     */
    PoVirtualReaderObserver(MasterAPI masterAPI, SamResourceManager samResourceManager,
            SeSelection seSelection, String nodeId) {
        this.masterAPI = masterAPI;
        this.nodeId = nodeId;
        this.seSelection = seSelection;
        this.samResourceManager = samResourceManager;
    }

    @Override
    public void update(ReaderEvent event) {
        logger.info("{} UPDATE {} {} {}", event.getEventType(), event.getPluginName(),
                event.getReaderName(), event.getDefaultSelectionsResponse());

        switch (event.getEventType()) {

            case SE_MATCHED:
                CalypsoPo calypsoPo = null;
                try {
                    calypsoPo = (CalypsoPo) seSelection
                            .processDefaultSelection(event.getDefaultSelectionsResponse())
                            .getActiveMatchingSe();
                } catch (KeypleException e) {
                    logger.error("Keyple Exception: {}", e.getMessage());
                }

                // retrieve PO virtual reader
                SeReader poReader = null;
                SamResource samResource = null;
                try {
                    poReader = masterAPI.getPlugin().getReader(event.getReaderName());

                    // create a Po Resource
                    PoResource poResource = new PoResource(poReader, calypsoPo);

                    // PO has matched
                    // executeReadEventLog(poResource);

                    /**
                     * Get a SamResource to perform authentication
                     */
                    samResource = samResourceManager.allocateSamResource(
                            SamResourceManager.AllocationMode.BLOCKING, SamIdentifier.builder()
                                    .samRevision(SamRevision.AUTO).serialNumber(".*").build());

                    if (samResource == null) {
                        throw new KeypleReaderIOException(
                                "No Sam resources available during the timeout");
                    }

                    executeCalypso4_PoAuthentication(poResource, samResource);

                } catch (KeypleReaderNotFoundException e) {
                    logger.error("Reader not found exception: {}", e.getMessage());
                } catch (KeypleReaderException e) {
                    logger.error("Reader exception: {}", e.getMessage());
                } catch (CalypsoNoSamResourceAvailableException e) {
                    logger.error("SAM resource not available {}", e.getMessage());
                } catch (KeypleAllocationReaderException e) {
                    logger.error("SAM resource allocation error exception {}", e.getMessage());
                } finally {
                    /**
                     * Release SamResource
                     */

                    if (samResource != null) {
                        logger.debug("Freeing Sam Resource at the end of processing {}",
                                samResource);
                        samResourceManager.freeSamResource(samResource);
                    }
                }
                break;
            case SE_INSERTED:
                logger.info("{} SE_INSERTED {} {}", nodeId, event.getPluginName(),
                        event.getReaderName());
                break;
            case SE_REMOVED:
                logger.info("{} SE_REMOVED {} {}", nodeId, event.getPluginName(),
                        event.getReaderName());
                break;

            case TIMEOUT_ERROR:
                logger.info("{} TIMEOUT_ERROR {} {}", nodeId, event.getPluginName(),
                        event.getReaderName());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + event.getEventType());
        }
    }


    /**
     * Execute non authenticated operations against a PO that has been already selected.
     * 
     * @param poResource : Reference to the matching PO embeeded in a PoResource
     */
    private void executeReadEventLog(PoResource poResource) {
        try {

            logger.info("{} Observer notification: the selection of the PO has succeeded.", nodeId);

            /* Go on with the reading of the first record of the EventLog file */
            logger.warn(
                    "==================================================================================");
            logger.warn(
                    "{} = 2nd PO exchange: reading transaction of the EventLog file.                     =",
                    nodeId);
            logger.warn(
                    "==================================================================================");

            PoTransaction poTransaction = new PoTransaction(poResource);

            /*
             * Prepare the reading order.
             */
            poTransaction.prepareReadRecordFile(CalypsoClassicInfo.SFI_EventLog,
                    CalypsoClassicInfo.RECORD_NUMBER_1);

            /*
             * Actual PO communication: send the prepared read order, then close the channel with
             * the PO
             */

            poTransaction.processPoCommands(ChannelControl.CLOSE_AFTER);

            logger.info("{} The reading of the EventLog has succeeded.", nodeId);

            /*
             * Retrieve the data read from the CalyspoPo updated during the transaction process
             */
            ElementaryFile efEventLog =
                    poResource.getMatchingSe().getFileBySfi(CalypsoClassicInfo.SFI_EventLog);
            String eventLog = ByteArrayUtil.toHex(efEventLog.getData().getContent());

            /* Log the result */
            logger.info("{} EventLog file data: {} ", nodeId, eventLog);

        } catch (CalypsoPoCommandException e) {
            logger.error("PO command {} failed with the status code 0x{}. {}", e.getCommand(),
                    Integer.toHexString(e.getStatusCode() & 0xFFFF).toUpperCase(), e.getMessage());
        } catch (CalypsoPoTransactionException e) {
            logger.error("CalypsoPoTransactionException: {}", e.getMessage());
        }
        logger.warn(
                "==================================================================================");
        logger.warn(
                "{} = End of the Calypso PO processing.                                              =",
                nodeId);
        logger.warn(
                "==================================================================================");
    }

    /**
     * Performs a PO Authenticated transaction with an explicit selection
     *
     * @param samResource : Required SAM Resource to execute this transaction
     * @param poResource : Reference to the matching PO embeeded in a PoResource
     */
    private void executeCalypso4_PoAuthentication(PoResource poResource, SamResource samResource) {
        try {

            /* Go on with the reading of the first record of the EventLog file */
            logger.warn(
                    "==================================================================================");
            logger.warn(
                    "= 2nd PO exchange: open and close a secure session to perform authentication.");
            logger.warn(
                    "==================================================================================");

            PoTransaction poTransaction = new PoTransaction(poResource,
                    CalypsoUtilities.getSecuritySettings(samResource));

            /*
             * Open Session for the debit key
             */
            poTransaction
                    .processOpening(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT);

            if (!poResource.getMatchingSe().isDfRatified()) {
                logger.warn(
                        "========= Previous Secure Session was not ratified. =====================");
            }
            /*
             * Prepare the reading order and keep the associated parser for later use once the
             * transaction has been processed.
             */
            poTransaction.processPoCommandsInSession();

            /*
             * Retrieve the data read from the CalyspoPo updated during the transaction process
             */
            ElementaryFile efEventLog =
                    poResource.getMatchingSe().getFileBySfi(CalypsoClassicInfo.SFI_EventLog);
            String eventLog = ByteArrayUtil.toHex(efEventLog.getData().getContent());

            /* Log the result */
            logger.info("EventLog file data: {}", eventLog);

            /*
             * Close the Secure Session.
             */
            if (logger.isInfoEnabled()) {
                logger.warn(
                        "================= PO Calypso session ======= Closing ============================");
            }

            /*
             * A ratification command will be sent (CONTACTLESS_MODE).
             */
            poTransaction.processClosing(ChannelControl.CLOSE_AFTER);

            logger.warn(
                    "==================================================================================");
            logger.warn("= End of the Calypso PO processing.");
            logger.warn(
                    "==================================================================================");

        } catch (CalypsoPoTransactionException e) {
            logger.error("CalypsoPoTransactionException: {}", e.getMessage());
        } catch (CalypsoSamCommandException e) {
            logger.error("SAM command {} failed with the status code 0x{}. {}", e.getCommand(),
                    Integer.toHexString(e.getStatusCode() & 0xFFFF), e.getMessage());
        } catch (CalypsoPoCommandException e) {
            logger.error("PO command {} failed with the status code 0x{}. {}", e.getCommand(),
                    Integer.toHexString(e.getStatusCode() & 0xFFFF), e.getMessage());
        }
    }
}
