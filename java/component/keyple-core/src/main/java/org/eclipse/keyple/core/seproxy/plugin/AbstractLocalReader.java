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
package org.eclipse.keyple.core.seproxy.plugin;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.keyple.core.seproxy.ChannelState;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.*;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manage the loop processing for SeRequest transmission in a set and for SeResponse reception in a
 * set
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.CyclomaticComplexity"})
public abstract class AbstractLocalReader extends AbstractObservableReader {

    /** logger */
    private static final Logger logger = LoggerFactory.getLogger(AbstractLocalReader.class);

    /** predefined "get response" byte array */
    private static final byte[] getResponseHackRequestBytes = ByteArrayUtil.fromHex("00C0000000");

    /** logical channel status flag */
    private boolean logicalChannelIsOpen = false;

    /** current AID if any */
    private SeSelector.AidSelector.IsoAid aidCurrentlySelected;

    /** current selection status */
    private SelectionStatus currentSelectionStatus;

    /** notification status flag used to avoid redundant notifications */
    private boolean presenceNotified = false;

    /** Timestamp recorder */
    private long before;

    /** ==== Constructor =================================================== */

    /**
     * Reader constructor
     * <p>
     * Force the definition of a name through the use of super method.
     * <p>
     * Initialize the time measurement
     *
     * @param pluginName the name of the plugin that instantiated the reader
     * @param readerName the name of the reader
     */
    public AbstractLocalReader(String pluginName, String readerName) {
        super(pluginName, readerName);
        this.before = System.nanoTime(); /*
                                          * provides an initial value for measuring the
                                          * inter-exchange time. The first measurement gives the
                                          * time elapsed since the plugin was loaded.
                                          */
    }

    /** ==== Card presence management ====================================== */

    /**
     * Check the presence of a SE
     * <p>
     * This method is recommended for non-observable readers.
     * <p>
     * When the card is not present the logical and physical channels status may be refreshed
     * through a call to the cardRemoved method.
     *
     * @return true if the SE is present
     */
    public final boolean isSePresent() throws NoStackTraceThrowable {
        if (checkSePresence()) {
            return true;
        } else {
            if (isLogicalChannelOpen() || isPhysicalChannelOpen()) {
                cardRemoved();
            }
            return false;
        }
    }

    /**
     * Wrapper for the native method of the plugin specific local reader to verify the presence of
     * the SE.
     * <p>
     * This method must be implemented by the ProxyReader plugin (e.g. Pcsc reader plugin).
     * <p>
     * This method is invoked by isSePresent.
     *
     * @return true if the SE is present
     * @throws NoStackTraceThrowable exception without stack trace
     */
    protected abstract boolean checkSePresence() throws NoStackTraceThrowable;

    /**
     * This method is invoked when a SE is inserted in the case of an observable reader.
     * <p>
     * e.g. from the monitoring thread in the case of a Pcsc plugin or from the NfcAdapter callback
     * method onTagDiscovered in the case of a Android NFC plugin.
     * <p>
     * It will fire an ReaderEvent in the following cases:
     * <ul>
     * <li>SE_INSERTED: if no default selection request was defined</li>
     * <li>SE_MATCHED: if a default selection request was defined in any mode and a SE matched the
     * selection</li>
     * <li>SE_INSERTED: if a default selection request was defined in ALWAYS mode but no SE matched
     * the selection (the DefaultSelectionsResponse is however transmitted)</li>
     * </ul>
     * <p>
     * It will do nothing if a default selection is defined in MATCHED_ONLY mode but no SE matched
     * the selection.
     */
    protected final void cardInserted() {
        if (defaultSelectionsRequest == null) {
            /* no default request is defined, just notify the SE insertion */
            notifyObservers(new ReaderEvent(this.pluginName, this.name,
                    ReaderEvent.EventType.SE_INSERTED, null));
            presenceNotified = true;
        } else {
            /*
             * a default request is defined, send it a notify according to the notification mode and
             * the selection status
             */
            boolean aSeMatched = false;
            try {
                List<SeResponse> seResponseList =
                        processSeRequestSet(defaultSelectionsRequest.getSelectionSeRequestSet(),
                                defaultSelectionsRequest.getMultiSeRequestProcessing(),
                                defaultSelectionsRequest.getChannelState());

                for (SeResponse seResponse : seResponseList) {
                    if (seResponse != null && seResponse.getSelectionStatus().hasMatched()) {
                        aSeMatched = true;
                        break;
                    }
                }
                if (notificationMode == ObservableReader.NotificationMode.MATCHED_ONLY) {
                    /* notify only if a SE matched the selection, just ignore if not */
                    if (aSeMatched) {
                        notifyObservers(new ReaderEvent(this.pluginName, this.name,
                                ReaderEvent.EventType.SE_MATCHED,
                                new DefaultSelectionsResponse(seResponseList)));
                        presenceNotified = true;
                    } else {
                        /* request the removal sequence if enabled */
                        doRemovalSequence = true;
                    }
                } else {
                    if (aSeMatched) {
                        /* The SE matched, notify an SE_MATCHED event with the received response */
                        notifyObservers(new ReaderEvent(this.pluginName, this.name,
                                ReaderEvent.EventType.SE_MATCHED,
                                new DefaultSelectionsResponse(seResponseList)));
                    } else {
                        /*
                         * The SE didn't match, notify an SE_INSERTED event with the received
                         * response
                         */
                        notifyObservers(new ReaderEvent(this.pluginName, this.name,
                                ReaderEvent.EventType.SE_INSERTED,
                                new DefaultSelectionsResponse(seResponseList)));
                    }
                    presenceNotified = true;
                }
            } catch (KeypleReaderException e) {
                /* the last transmission failed, close the logical and physical channels */
                closeLogicalAndPhysicalChannels();
                e.printStackTrace();
                // in this case the card has been removed or not read correctly, do not throw event
            }
        }
    }

    /**
     * This method is invoked when a SE is removed in the case of an observable reader.
     * <p>
     * It will also be invoked if isSePresent is called and at least one of the physical or logical
     * channels is still open (case of a non-observable reader)
     * <p>
     * The SE will be notified removed only if it has been previously notified present (observable
     * reader only)
     * 
     * @throws NoStackTraceThrowable a exception without stack trace in order to be catched and
     *         processed silently
     */
    protected final void cardRemoved() throws NoStackTraceThrowable {
        if (presenceNotified) {
            notifyObservers(new ReaderEvent(this.pluginName, this.name,
                    ReaderEvent.EventType.SE_REMOVAL, null));
            presenceNotified = false;
        }
        closeLogicalAndPhysicalChannels();
    }

    /** ==== Physical and logical channels management ====================== */

    /**
     * Close both logical and physical channels
     */
    private void closeLogicalAndPhysicalChannels() {
        closeLogicalChannel();
        try {
            closePhysicalChannel();
        } catch (KeypleChannelStateException e) {
            logger.trace("[{}] Exception occured in closeLogicalAndPhysicalChannels. Message: {}",
                    this.getName(), e.getMessage());
        }
    }

    /**
     * This abstract method must be implemented by the derived class in order to provide the SE ATR
     * when available.
     * <p>
     * Gets the SE Answer to reset
     *
     * @return ATR returned by the SE or reconstructed by the reader (contactless)
     */
    protected abstract byte[] getATR();


    /**
     * This method is dedicated to the case where no FCI data is available in return for the select
     * command.
     * <p>
     *
     * @param aidSelector used to retrieve the successful status codes from the main AidSelector
     * @return a ApduResponse containing the FCI
     */
    private ApduResponse selectionGetData(SeSelector.AidSelector aidSelector)
            throws KeypleIOReaderException {
        ApduResponse fciResponse;
        byte[] getDataCommand = new byte[4];
        getDataCommand[0] = (byte) 0x00; // CLA
        getDataCommand[1] = (byte) 0xCA; // INS
        getDataCommand[2] = (byte) 0x00; // P1: always 0
        getDataCommand[3] = (byte) 0x6F; // P2: 0x6F FCI for the current DF

        /*
         * The successful status codes list for this command is provided.
         */
        fciResponse = processApduRequest(new ApduRequest("Internal Get Data", getDataCommand, false,
                aidSelector.getSuccessfulSelectionStatusCodes()));

        if (!fciResponse.isSuccessful()) {
            logger.trace("[{}] selectionGetData => Get data failed. SELECTOR = {}", this.getName(),
                    aidSelector);
        }
        return fciResponse;
    }

    /**
     * This abstract method must be implemented by the derived class in order to provide a selection
     * and ATR filtering mechanism.
     * <p>
     * The Selector provided in argument holds all the needed data to handle the Application
     * Selection and ATR matching process and build the resulting SelectionStatus.
     *
     * @param seSelector the SE selector
     * @return the SelectionStatus
     */
    /** ==== ATR filtering and application selection by AID ================ */

    /**
     * Build a select application command, transmit it to the SE and deduct the SelectionStatus.
     *
     * @param seSelector the targeted application SE selector
     * @return the SelectionStatus containing the actual selection result (ATR and/or FCI and the
     *         matching status flag).
     * @throws KeypleIOReaderException if a reader error occurs
     * @throws KeypleChannelStateException if a channel state exception occurs
     * @throws KeypleApplicationSelectionException if a selection exception occurs
     */
    protected SelectionStatus openLogicalChannel(SeSelector seSelector)
            throws KeypleIOReaderException, KeypleChannelStateException,
            KeypleApplicationSelectionException {
        byte[] atr = getATR();
        boolean selectionHasMatched = true;
        SelectionStatus selectionStatus;

        /** Perform ATR filtering if requested */
        if (seSelector.getAtrFilter() != null) {
            if (atr == null) {
                throw new KeypleIOReaderException("Didn't get an ATR from the SE.");
            }

            if (logger.isTraceEnabled()) {
                logger.trace("[{}] openLogicalChannel => ATR = {}", this.getName(),
                        ByteArrayUtil.toHex(atr));
            }
            if (!seSelector.getAtrFilter().atrMatches(atr)) {
                logger.info("[{}] openLogicalChannel => ATR didn't match. SELECTOR = {}, ATR = {}",
                        this.getName(), seSelector, ByteArrayUtil.toHex(atr));
                selectionHasMatched = false;
            }
        }

        /**
         * Perform application selection if requested and if ATR filtering matched or was not
         * requested
         */
        if (selectionHasMatched && seSelector.getAidSelector() != null) {
            ApduResponse fciResponse;
            if (this instanceof SmartSelectionReader) {
                fciResponse = ((SmartSelectionReader) this)
                        .openChannelForAid(seSelector.getAidSelector());
            } else {
                fciResponse = processExplicitAidSelection(seSelector.getAidSelector());
            }

            if (fciResponse.isSuccessful() && fciResponse.getDataOut().length == 0) {
                /**
                 * The selection didn't provide data (e.g. OMAPI), we get the FCI using a Get Data
                 * command.
                 * <p>
                 * The AID selector is provided to handle successful status word in the Get Data
                 * command.
                 */
                fciResponse = selectionGetData(seSelector.getAidSelector());
            }

            /*
             * The ATR filtering matched or was not requested. The selection status is determined by
             * the answer to the select application command.
             */
            selectionStatus = new SelectionStatus(new AnswerToReset(atr), fciResponse,
                    fciResponse.isSuccessful());
        } else {
            /*
             * The ATR filtering didn't match or no AidSelector was provided. The selection status
             * is determined by the ATR filtering.
             */
            selectionStatus = new SelectionStatus(new AnswerToReset(atr),
                    new ApduResponse(null, null), selectionHasMatched);
        }
        return selectionStatus;
    }


    /**
     * Open (if needed) a physical channel and try to establish a logical channel.
     * <p>
     * The logical opening is done either by sending a Select Application command (AID based
     * selection) or by checking the current ATR received from the SE (ATR based selection).
     * <p>
     * If the selection is successful, the logical channel is considered open. On the contrary, if
     * the selection fails, the logical channel remains closed.
     * <p>
     *
     * @param seSelector the SE Selector: either the AID of the application to select or an ATR
     *        selection regular expression
     * @return a {@link SelectionStatus} object containing the SE ATR, the SE FCI and a flag giving
     *         the selection process result. When ATR or FCI are not available, they are set to null
     *         but they can't be both null at the same time.
     * @throws KeypleIOReaderException if a reader error occurs
     * @throws KeypleChannelStateException if a channel state exception occurs
     * @throws KeypleApplicationSelectionException if a selection exception occurs
     */
    protected final SelectionStatus openLogicalChannelAndSelect(SeSelector seSelector)
            throws KeypleChannelStateException, KeypleIOReaderException,
            KeypleApplicationSelectionException {

        SelectionStatus selectionStatus;

        if (seSelector == null) {
            throw new KeypleChannelStateException("Try to open logical channel without selector.");
        }

        if (!isLogicalChannelOpen()) {
            /*
             * init of the physical SE channel: if not yet established, opening of a new physical
             * channel
             */
            if (!isPhysicalChannelOpen()) {
                openPhysicalChannel();
            }
            if (!isPhysicalChannelOpen()) {
                throw new KeypleChannelStateException("Fail to open physical channel.");
            }
        }

        selectionStatus = openLogicalChannel(seSelector);

        return selectionStatus;
    }

    /**
     * Attempts to open the physical channel
     *
     * @throws KeypleChannelStateException if the channel opening fails
     */
    protected abstract void openPhysicalChannel() throws KeypleChannelStateException;

    /**
     * Closes the current physical channel.
     * <p>
     * This method must be implemented by the ProxyReader plugin (e.g. Pcsc/Nfc/Omapi Reader).
     *
     * @throws KeypleChannelStateException if a reader error occurs
     */
    protected abstract void closePhysicalChannel() throws KeypleChannelStateException;

    /**
     * Tells if the physical channel is open or not
     * <p>
     * This method must be implemented by the ProxyReader plugin (e.g. Pcsc/Nfc/Omapi Reader).
     *
     * @return true is the channel is open
     */
    protected abstract boolean isPhysicalChannelOpen();

    /**
     * Tells if a logical channel is open
     *
     * @return true if the logical channel is open
     */
    final boolean isLogicalChannelOpen() {
        return logicalChannelIsOpen;
    }

    /**
     * Close the logical channel.
     */
    private void closeLogicalChannel() {
        logger.trace("[{}] closeLogicalChannel => Closing of the logical channel.", this.getName());
        logicalChannelIsOpen = false;
        aidCurrentlySelected = null;
        currentSelectionStatus = null;
    }

    /** ==== Protocol management =========================================== */

    /**
     * PO selection map associating seProtocols and selection strings.
     * <p>
     * The String associated with a particular protocol can be anything that is relevant to be
     * interpreted by reader plugins implementing protocolFlagMatches (e.g. ATR regex for Pcsc
     * plugins, technology name for Nfc plugins, etc).
     */
    protected Map<SeProtocol, String> protocolsMap = new HashMap<SeProtocol, String>();

    /**
     * Defines the protocol setting Map to allow SE to be differentiated according to their
     * communication protocol.
     *
     * @param seProtocol the protocol key identifier to be added to the plugin internal list
     * @param protocolRule a string use to define how to identify the protocol
     */
    @Override
    public void addSeProtocolSetting(SeProtocol seProtocol, String protocolRule) {
        this.protocolsMap.put(seProtocol, protocolRule);
    }

    /**
     * Complete the current setting map with the provided map
     * 
     * @param protocolSetting the protocol setting map
     */
    public void setSeProtocolSetting(Map<SeProtocol, String> protocolSetting) {
        this.protocolsMap.putAll(protocolSetting);
    }

    /**
     * Test if the current protocol matches the provided protocol flag.
     * <p>
     * The method must be implemented by the ProxyReader plugin.
     * <p>
     * The protocol flag is used to retrieve from the protocolsMap the String used to differentiate
     * this particular protocol. (e.g. in PC/SC the only way to identify the SE protocol is to
     * analyse the ATR returned by the reader [ISO SE and memory card SE have specific ATR], in
     * Android Nfc the SE protocol can be deduced with the TagTechnology interface).
     * 
     * @param protocolFlag the protocol flag
     * @return true if the current protocol matches the provided protocol flag
     * @throws KeypleReaderException in case of a reader exception
     */
    protected abstract boolean protocolFlagMatches(SeProtocol protocolFlag)
            throws KeypleReaderException;

    /** ==== SeRequestSe and SeRequest transmission management ============= */

    /**
     * Do the transmission of all needed requestSet requests contained in the provided requestSet
     * according to the protocol flag selection logic. The responseSet responses are returned in the
     * responseSet object. The requestSet requests are ordered at application level and the
     * responses match this order. When a requestSet is not matching the current PO, the responseSet
     * responses pushed in the responseSet object is set to null.
     *
     * @param requestSet the request set
     * @param multiSeRequestProcessing the multi se processing mode
     * @param channelState indicates if the channel has to be closed at the end of the processing
     * @return the response list
     * @throws KeypleIOReaderException if a reader error occurs
     */
    protected final List<SeResponse> processSeRequestSet(Set<SeRequest> requestSet,
            MultiSeRequestProcessing multiSeRequestProcessing, ChannelState channelState)
            throws KeypleReaderException {

        boolean requestMatchesProtocol[] = new boolean[requestSet.size()];
        int requestIndex = 0, lastRequestIndex;

        // Determine which requests are matching the current ATR
        // All requests without selector are considered matching
        for (SeRequest request : requestSet) {
            SeSelector seSelector = request.getSeSelector();
            if (seSelector != null) {
                requestMatchesProtocol[requestIndex] =
                        protocolFlagMatches(request.getSeSelector().getSeProtocol());
            } else {
                requestMatchesProtocol[requestIndex] = true;
            }
            requestIndex++;
        }

        /*
         * we have now an array of booleans saying whether the corresponding request and the current
         * SE match or not
         */

        lastRequestIndex = requestIndex;
        requestIndex = 0;

        /*
         * The current requestSet is possibly made of several APDU command lists.
         *
         * If the requestMatchesProtocol is true we process the requestSet.
         *
         * If the requestMatchesProtocol is false we skip to the next requestSet.
         *
         * If keepChannelOpen is false, we close the physical channel for the last request.
         */
        List<SeResponse> responses = new ArrayList<SeResponse>();
        boolean stopProcess = false;
        for (SeRequest request : requestSet) {

            if (!stopProcess) {
                if (requestMatchesProtocol[requestIndex]) {
                    logger.debug("[{}] processSeRequestSet => transmit {}", this.getName(),
                            request);
                    SeResponse response = null;
                    try {
                        response = processSeRequestLogical(request);
                    } catch (KeypleReaderException ex) {
                        /*
                         * The process has been interrupted. We launch a KeypleReaderException with
                         * the responses collected so far.
                         */
                        /* Add the latest (and partial) SeResponse to the current list. */
                        responses.add(ex.getSeResponse());
                        /* Build a List of SeResponse with the available data. */
                        ex.setSeResponseSet(responses);
                        logger.debug(
                                "[{}] processSeRequestSet => transmit : process interrupted, collect previous responses {}",
                                this.getName(), responses);
                        throw ex;
                    }
                    responses.add(response);
                    logger.debug("[{}] processSeRequestSet => receive {}", this.getName(),
                            response);
                } else {
                    /*
                     * in case the protocolFlag of a SeRequest doesn't match the reader status, a
                     * null SeResponse is added to the SeResponse List.
                     */
                    responses.add(null);
                }
                if (multiSeRequestProcessing == MultiSeRequestProcessing.PROCESS_ALL) {
                    // multi SeRequest case: just close the logical channel and go on with the next
                    // selection.
                    closeLogicalChannel();
                } else {
                    // the current PO matches the first selection case, we stop here.
                    stopProcess = true;
                }
                requestIndex++;
                if (lastRequestIndex == requestIndex) {
                    if (channelState == ChannelState.CLOSE_AFTER) {
                        if (!(this instanceof SmartRemovalReader)
                                && (((ObservableReader) this).countObservers() > 0)
                                && waitForRemovalModeEnabled) {
                            /* observed reader */
                            doRemovalSequence = true;
                        } else {
                            /* close the physical channel if requested */
                            closePhysicalChannel();
                        }
                    }
                }
            }
        }
        return responses;
    }

    /**
     * Executes a request made of one or more Apdus and receives their answers. The selection of the
     * application is handled.
     * <p>
     * The physical channel is closed if requested.
     *
     * @param seRequest the SeRequest
     * @param channelState indicates if the channel has to be closed at the end of the processing
     * @return the SeResponse to the SeRequest
     * @throws KeypleReaderException if a transmission fails
     */
    @SuppressWarnings({"PMD.ModifiedCyclomaticComplexity", "PMD.CyclomaticComplexity",
            "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity", "PMD.ExcessiveMethodLength"})
    protected final SeResponse processSeRequest(SeRequest seRequest, ChannelState channelState)
            throws IllegalStateException, KeypleReaderException {

        SeResponse seResponse = processSeRequestLogical(seRequest);

        if (channelState == ChannelState.CLOSE_AFTER) {
            if (!(this instanceof SmartRemovalReader)
                    && (((ObservableReader) this).countObservers() > 0)
                    && waitForRemovalModeEnabled) {
                doRemovalSequence = true;
            } else {
                /* close the physical channel if requested */
                closePhysicalChannel();
            }
        }

        return seResponse;
    }

    /**
     * Implements the logical processSeRequest.
     * <p>
     * This method is called by processSeRequestSet and processSeRequest.
     * <p>
     * It opens both physical and logical channels if needed.
     * <p>
     * The logical channel is closed when requested.
     *
     * @param seRequest the {@link SeRequest} to be sent
     * @return seResponse
     * @throws IllegalStateException
     * @throws KeypleReaderException
     */
    private SeResponse processSeRequestLogical(SeRequest seRequest)
            throws IllegalStateException, KeypleReaderException {
        boolean previouslyOpen = true;
        SelectionStatus selectionStatus = null;

        List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();

        logger.trace("[{}] processSeRequest => Logical channel open = {}", this.getName(),
                isLogicalChannelOpen());
        /*
         * unless the selector is null, we try to open a logical channel; if the channel was open
         * and the PO is still matching we won't redo the selection and just use the current
         * selection status
         */
        if (seRequest.getSeSelector() != null) {
            /* check if AID changed if the channel is already open */
            if (isLogicalChannelOpen() && seRequest.getSeSelector().getAidSelector() != null) {
                /*
                 * AID comparison hack: we check here if the initial selection AID matches the
                 * beginning of the AID provided in the SeRequest (coming from FCI data and supposed
                 * to be longer than the selection AID).
                 *
                 * The current AID (selector) length must be at least equal or greater than the
                 * selection AID. All bytes of the selection AID must match the beginning of the
                 * current AID.
                 */
                if (aidCurrentlySelected == null) {
                    throw new IllegalStateException("AID currently selected shouldn't be null.");
                }
                if (seRequest.getSeSelector().getAidSelector()
                        .getFileOccurrence() == SeSelector.AidSelector.FileOccurrence.NEXT) {
                    if (logger.isTraceEnabled()) {
                        logger.trace(
                                "[{}] processSeRequest => The current selection is a next selection, close the "
                                        + "logical channel.",
                                this.getName());
                    }
                    /* close the channel (will reset the current selection status) */
                    closeLogicalChannel();
                } else if (!aidCurrentlySelected
                        .startsWith(seRequest.getSeSelector().getAidSelector().getAidToSelect())) {
                    // the AID changed (longer or different), close the logical channel
                    if (logger.isTraceEnabled()) {
                        logger.trace(
                                "[{}] processSeRequest => The AID changed, close the logical channel. AID = {}, EXPECTEDAID = {}",
                                this.getName(),
                                ByteArrayUtil.toHex(aidCurrentlySelected.getValue()),
                                seRequest.getSeSelector());
                    }
                    /* close the channel (will reset the current selection status) */
                    closeLogicalChannel();
                }
                /* keep the current selection status (may be null if the current PO didn't match) */
                selectionStatus = currentSelectionStatus;
            }

            /* open the channel and do the selection if needed */
            if (!isLogicalChannelOpen()) {
                previouslyOpen = false;

                try {
                    selectionStatus = openLogicalChannelAndSelect(seRequest.getSeSelector());
                    logger.trace("[{}] processSeRequest => Logical channel opening success.",
                            this.getName());
                } catch (KeypleApplicationSelectionException e) {
                    logger.trace("[{}] processSeRequest => Logical channel opening failure",
                            this.getName());
                    closeLogicalChannel();
                    /* return a null SeResponse when the opening of the logical channel failed */
                    return null;
                }

                if (selectionStatus.hasMatched()) {
                    /* The selection process succeeded, the logical channel is open */
                    logicalChannelIsOpen = true;

                    if (selectionStatus.getFci().isSuccessful()) {
                        /* the selection AID based was successful, keep the aid */
                        aidCurrentlySelected =
                                seRequest.getSeSelector().getAidSelector().getAidToSelect();
                    }
                    currentSelectionStatus = selectionStatus;
                } else {
                    /* The selection process failed, close the logical channel */
                    closeLogicalChannel();
                }
            }
        } else {
            /* selector is null, we expect that the logical channel was previously opened */
            if (!isLogicalChannelOpen()) {
                throw new IllegalStateException(
                        "[" + this.getName() + "] processSeRequest => No logical channel opened!");
            } else {
                selectionStatus = null;
            }
        }

        /* process request if not empty */
        if (seRequest.getApduRequests() != null) {
            for (ApduRequest apduRequest : seRequest.getApduRequests()) {
                try {
                    apduResponseList.add(processApduRequest(apduRequest));
                } catch (KeypleIOReaderException ex) {
                    /*
                     * The process has been interrupted. We close the logical channel and launch a
                     * KeypleReaderException with the Apdu responses collected so far.
                     */
                    logger.debug(
                            "The process has been interrupted, collect Apdu responses collected so far");
                    closeLogicalAndPhysicalChannels();
                    ex.setSeResponse(new SeResponse(false, previouslyOpen, selectionStatus,
                            apduResponseList));
                    throw ex;
                }
            }
        }

        return new SeResponse(isLogicalChannelOpen(), previouslyOpen, selectionStatus,
                apduResponseList);
    }

    /** ==== APDU transmission management ================================== */

    /**
     * Transmits an ApduRequest and receives the ApduResponse
     * <p>
     * The time measurement is carried out and logged with the detailed information of the exchanges
     * (TRACE level).
     *
     * @param apduRequest APDU request
     * @return APDU response
     * @throws KeypleIOReaderException Exception faced
     */
    protected final ApduResponse processApduRequest(ApduRequest apduRequest)
            throws KeypleIOReaderException {
        ApduResponse apduResponse;
        if (logger.isTraceEnabled()) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - before) / 100000) / 10;
            this.before = timeStamp;
            logger.trace("[{}] processApduRequest => {}, elapsed {} ms.", this.getName(),
                    apduRequest, elapsedMs);
        }

        byte[] buffer = apduRequest.getBytes();
        apduResponse =
                new ApduResponse(transmitApdu(buffer), apduRequest.getSuccessfulStatusCodes());

        if (apduRequest.isCase4() && apduResponse.getDataOut().length == 0
                && apduResponse.isSuccessful()) {
            // do the get response command but keep the original status code
            apduResponse = case4HackGetResponse(apduResponse.getStatusCode());
        }

        if (logger.isTraceEnabled()) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - before) / 100000) / 10;
            this.before = timeStamp;
            logger.trace("[{}] processApduRequest => {}, elapsed {} ms.", this.getName(),
                    apduResponse, elapsedMs);
        }
        return apduResponse;
    }

    /**
     * Execute a get response command in order to get outgoing data from specific cards answering
     * 9000 with no data although the command has outgoing data. Note that this method relies on the
     * right get response management by transmitApdu
     *
     * @param originalStatusCode the status code of the command that didn't returned data
     * @return ApduResponse the response to the get response command
     * @throws KeypleIOReaderException if the transmission fails.
     */
    private ApduResponse case4HackGetResponse(int originalStatusCode)
            throws KeypleIOReaderException {
        /*
         * build a get response command the actual length expected by the SE in the get response
         * command is handled in transmitApdu
         */
        if (logger.isTraceEnabled()) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.trace(
                    "[{}] case4HackGetResponse => ApduRequest: NAME = \"Internal Get Response\", RAWDATA = {}, elapsed = {}",
                    this.getName(), ByteArrayUtil.toHex(getResponseHackRequestBytes), elapsedMs);
        }

        byte[] getResponseHackResponseBytes = transmitApdu(getResponseHackRequestBytes);

        /* we expect here a 0x9000 status code */
        ApduResponse getResponseHackResponse = new ApduResponse(getResponseHackResponseBytes, null);

        if (logger.isTraceEnabled()) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.trace("[{}] case4HackGetResponse => Internal {}, elapsed {} ms.", this.getName(),
                    getResponseHackResponseBytes, elapsedMs);
        }

        if (getResponseHackResponse.isSuccessful()) {
            // replace the two last status word bytes by the original status word
            getResponseHackResponseBytes[getResponseHackResponseBytes.length - 2] =
                    (byte) (originalStatusCode >> 8);
            getResponseHackResponseBytes[getResponseHackResponseBytes.length - 1] =
                    (byte) (originalStatusCode & 0xFF);
        }
        return getResponseHackResponse;
    }

    /**
     * Transmits a single APDU and receives its response.
     * <p>
     * This abstract method must be implemented by the ProxyReader plugin (e.g. Pcsc, Nfc). The
     * implementation must handle the case where the SE response is 61xy and execute the appropriate
     * get response command.
     *
     * @param apduIn byte buffer containing the ingoing data
     * @return apduResponse byte buffer containing the outgoing data.
     * @throws KeypleIOReaderException if the transmission fails
     */
    protected abstract byte[] transmitApdu(byte[] apduIn) throws KeypleIOReaderException;

    /** ==== Default selection assignment ================================== */

    /**
     * If defined, the prepared setDefaultSelectionRequest will be processed as soon as a SE is
     * inserted. The result of this request set will be added to the reader event.
     * <p>
     * Depending on the notification mode, the observer will be notified whenever an SE is inserted,
     * regardless of the selection status, or only if the current SE matches the selection criteria.
     *
     * @param defaultSelectionsRequest the {@link AbstractDefaultSelectionsRequest} to be executed
     *        when a SE is inserted
     * @param notificationMode the notification mode enum (ALWAYS or MATCHED_ONLY)
     */
    public void setDefaultSelectionRequest(
            AbstractDefaultSelectionsRequest defaultSelectionsRequest,
            ObservableReader.NotificationMode notificationMode) {
        this.defaultSelectionsRequest = (DefaultSelectionsRequest) defaultSelectionsRequest;
        this.notificationMode = notificationMode;
    };

    /* Monitoring thread management methods */
    private EventThread thread;
    private static final AtomicInteger threadCount = new AtomicInteger();
    protected boolean waitForRemovalModeEnabled = false;
    protected boolean doRemovalSequence = false;

    /**
     * Thread wait timeout in ms
     */
    protected long threadWaitTimeout;

    /**
     * Start the monitoring thread.
     * <p>
     * The thread is created if it does not already exist
     */
    @Override
    protected void startObservation() {
        thread = new EventThread(this.getPluginName(), this.getName());
        thread.start();
    }

    /**
     * Terminate the monitoring thread
     */
    @Override
    protected void stopObservation() {
        if (thread != null) {
            thread.end();
        }
    }

    /**
     * setter to fix the wait timeout in ms.
     *
     * @param timeout Timeout to use
     */
    protected final void setThreadWaitTimeout(long timeout) {
        this.threadWaitTimeout = timeout;
    }

    /**
     * Thread in charge of reporting live events
     */
    private class EventThread extends Thread {
        /**
         * Plugin name
         */
        private final String pluginName;

        /**
         * Reader that we'll report about
         */
        private final String readerName;


        /**
         * If the thread should be kept a alive
         */
        private volatile boolean running = true;

        /**
         * Constructor
         *
         * @param pluginName name of the plugin that instantiated the reader
         * @param readerName name of the reader who owns this thread
         */
        EventThread(String pluginName, String readerName) {
            super("observable-reader-events-" + threadCount.addAndGet(1));
            setDaemon(true);
            this.pluginName = pluginName;
            this.readerName = readerName;
        }

        /**
         * Marks the thread as one that should end when the last cardWaitTimeout occurs
         */
        void end() {
            running = false;
            this.interrupt(); // exit io wait if needed
        }

        public void run() {
            try {
                if (!(AbstractLocalReader.this instanceof SmartInsertionReader)) {
                    throw new IllegalStateException(
                            "An observed reader must implement the SmartInsertionReader "
                                    + "interface.");
                }
                // First thing we'll do is to notify that a card was inserted if one is already
                // present.
                if (isSePresent()) {
                    logger.trace("[{}] Card is already present in reader", readerName);
                    cardInserted();
                    if (waitForRemovalModeEnabled) {
                        // wait as long as the PO responds (timeout is useless)
                        logger.trace("[{}] Observe card removal", readerName);
                        if (AbstractLocalReader.this instanceof SmartRemovalReader) {
                            ((SmartRemovalReader) this).waitForCardAbsentNative(0);
                        } else {
                            waitForCardAbsentPing(0);
                        }
                    }
                    // notify removal
                    cardRemoved();
                }

                while (running) {
                    logger.trace("[{}] observe card insertion", readerName);
                    // we will wait for it to appear
                    if (((SmartInsertionReader) AbstractLocalReader.this).waitForCardPresent(0)) {
                        // notify insertion
                        logger.debug("Card inserted.");
                        cardInserted();
                        if (waitForRemovalModeEnabled && doRemovalSequence) {
                            doRemovalSequence = false;
                            // wait as long as the PO responds (timeout is useless)
                            logger.trace("[{}] Observe card removal", readerName);
                            if (AbstractLocalReader.this instanceof SmartRemovalReader) {
                                ((SmartRemovalReader) AbstractLocalReader.this)
                                        .waitForCardAbsentNative(0);
                            } else {
                                waitForCardAbsentPing(0);
                            }
                        }
                        // handle removal (notify if needed)
                        cardRemoved();
                    }
                }
            } catch (NoStackTraceThrowable e) {
                logger.trace("[{}] Exception occurred in monitoring thread: {}", readerName,
                        e.getMessage());
            }
        }
    }

    /**
     * Wait for the card to disappear.
     * <p>
     * The method used to do this is to replay, while the physical channel is still open, the
     * request that made the current selection until the PO no longer responds.
     *
     * @param timeout the delay in millisecond we wait for a card insertion, a value of zero means
     *        wait for ever.
     */
    protected void waitForCardAbsentPing(int timeout) {
        // APDU sent to check the communication with the PO
        byte[] apdu = new byte[] {(byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        // loop for ever until the PO stop responding
        try {
            while (true) {
                byte[] rapdu = new byte[0];
                rapdu = transmitApdu(apdu);
                // sleep a little to reduce the cpu consumption of the current thread
                Thread.sleep(50);
            }
        } catch (KeypleIOReaderException e) {
            // log only unexpected exceptions, else exit silently
            logger.trace("[{}] Exception occured in waitForCardAbsentPing. Message: {}",
                    this.getName(), e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.debug("Card removed.");
    }

    public void setWaitForRemovalMode(boolean waitForRemovalModeEnabled) {
        this.waitForRemovalModeEnabled = waitForRemovalModeEnabled;
    }


    /**
     * Called when the class is unloaded. Attempt to do a clean exit.
     *
     * @throws Throwable a generic exception
     */
    @Override
    protected void finalize() throws Throwable {
        thread.end();
        thread = null;
        logger.trace("[{}] Observable Reader thread ended.", this.getName());
        super.finalize();
    }

    /* Selection management */

    /**
     * Executes the selection application command and returns the requested data according to
     * AidSelector attributes.
     *
     * @param aidSelector the selection parameters
     * @return the response to the select application command
     * @throws KeypleIOReaderException if a reader error occurs
     */
    protected ApduResponse processExplicitAidSelection(SeSelector.AidSelector aidSelector)
            throws KeypleIOReaderException {
        ApduResponse fciResponse;
        final byte aid[] = aidSelector.getAidToSelect().getValue();
        if (aid == null) {
            throw new IllegalArgumentException("AID must not be null for an AidSelector.");
        }
        if (logger.isTraceEnabled()) {
            logger.trace("[{}] openLogicalChannel => Select Application with AID = {}",
                    this.getName(), ByteArrayUtil.toHex(aid));
        }
        /*
         * build a get response command the actual length expected by the SE in the get response
         * command is handled in transmitApdu
         */
        byte[] selectApplicationCommand = new byte[6 + aid.length];
        selectApplicationCommand[0] = (byte) 0x00; // CLA
        selectApplicationCommand[1] = (byte) 0xA4; // INS
        selectApplicationCommand[2] = (byte) 0x04; // P1: select by name
        // P2: b0,b1 define the File occurrence, b2,b3 define the File control information
        // we use the bitmask defined in the respective enums
        selectApplicationCommand[3] = (byte) (aidSelector.getFileOccurrence().getIsoBitMask()
                | aidSelector.getFileControlInformation().getIsoBitMask());
        selectApplicationCommand[4] = (byte) (aid.length); // Lc
        System.arraycopy(aid, 0, selectApplicationCommand, 5, aid.length); // data
        selectApplicationCommand[5 + aid.length] = (byte) 0x00; // Le

        /*
         * we use here processApduRequest to manage case 4 hack. The successful status codes list
         * for this command is provided.
         */
        fciResponse = processApduRequest(new ApduRequest("Internal Select Application",
                selectApplicationCommand, true, aidSelector.getSuccessfulSelectionStatusCodes()));

        if (!fciResponse.isSuccessful()) {
            logger.trace("[{}] openLogicalChannel => Application Selection failed. SELECTOR = {}",
                    this.getName(), aidSelector);
        }
        return fciResponse;
    }
}
