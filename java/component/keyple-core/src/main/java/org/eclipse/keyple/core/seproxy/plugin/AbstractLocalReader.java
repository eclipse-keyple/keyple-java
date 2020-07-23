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
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
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
public abstract class AbstractLocalReader extends AbstractReader {

    /** logger */
    private static final Logger logger = LoggerFactory.getLogger(AbstractLocalReader.class);

    /** predefined "get response" byte array */
    private static final byte[] getResponseHackRequestBytes = ByteArrayUtil.fromHex("00C0000000");

    /** logical channel status flag */
    private boolean logicalChannelIsOpen = false;

    /** current AID if any */
    private byte[] aidCurrentlySelected;

    /** current selection status */
    private SelectionStatus currentSelectionStatus;

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
     * through a call to the processSeRemoved method.
     *
     * @return true if the SE is present
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    @Override
    public boolean isSePresent() {
        return checkSePresence();
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
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    protected abstract boolean checkSePresence();


    /* ==== Physical and logical channels management ====================== */

    /**
     * Close both logical and physical channels
     */
    void closeLogicalAndPhysicalChannels() {
        closeLogicalChannel();
        try {
            closePhysicalChannel();
        } catch (KeypleReaderIOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "[{}] Exception occurred in closeLogicalAndPhysicalChannels. Message: {}",
                        this.getName(), e.getMessage());
            }
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

    /* ==== Physical and logical channels management ====================== */
    /* Selection management */

    /**
     * This method is dedicated to the case where no FCI data is available in return for the select
     * command.
     * <p>
     *
     * @param aidSelector used to retrieve the successful status codes from the main AidSelector
     * @return a ApduResponse containing the FCI
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    private ApduResponse recoverSelectionFciData(SeSelector.AidSelector aidSelector) {
        ApduResponse fciResponse;
        // Get Data APDU: CLA, INS, P1: always 0, P2: 0x6F FCI for the current DF, LC: 0
        byte[] getDataCommand = {(byte) 0x00, (byte) 0xCA, (byte) 0x00, (byte) 0x6F, (byte) 0x00};

        /*
         * The successful status codes list for this command is provided.
         */
        fciResponse = processApduRequest(new ApduRequest("Internal Get Data", getDataCommand, false,
                aidSelector.getSuccessfulSelectionStatusCodes()));

        if (!fciResponse.isSuccessful()) {
            if (logger.isDebugEnabled()) {
                logger.debug("[{}] selectionGetData => Get data failed. SELECTOR = {}",
                        this.getName(), aidSelector);
            }
        }
        return fciResponse;
    }

    /**
     * Executes the selection application command and returns the requested data according to
     * AidSelector attributes.
     *
     * @param aidSelector the selection parameters
     * @return the response to the select application command
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    private ApduResponse processExplicitAidSelection(SeSelector.AidSelector aidSelector) {
        ApduResponse fciResponse;
        final byte[] aid = aidSelector.getAidToSelect();
        if (aid == null) {
            throw new IllegalArgumentException("AID must not be null for an AidSelector.");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[{}] openLogicalChannel => Select Application with AID = {}",
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
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "[{}] openLogicalChannel => Application Selection failed. SELECTOR = {}",
                        this.getName(), aidSelector);
            }
        }
        return fciResponse;
    }

    /*
     * This abstract method must be implemented by the derived class in order to provide a selection
     * and ATR filtering mechanism. <p> The Selector provided in argument holds all the needed data
     * to handle the Application Selection and ATR matching process and build the resulting
     * SelectionStatus.
     * 
     * @param seSelector the SE selector
     * 
     * @return the SelectionStatus
     */

    /* ==== ATR filtering and application selection by AID ================ */

    /**
     * Build a select application command, transmit it to the SE and deduct the SelectionStatus.
     *
     * @param seSelector the targeted application SE selector
     * @return the SelectionStatus containing the actual selection result (ATR and/or FCI and the
     *         matching status flag).
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    SelectionStatus openLogicalChannel(SeSelector seSelector) {
        byte[] atr = getATR();
        boolean selectionHasMatched = true;
        SelectionStatus selectionStatus;

        /* Perform ATR filtering if requested */
        if (seSelector.getAtrFilter() != null) {
            if (atr == null) {
                throw new KeypleReaderIOException("Didn't get an ATR from the SE.");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("[{}] openLogicalChannel => ATR = {}", this.getName(),
                        ByteArrayUtil.toHex(atr));
            }
            if (!seSelector.getAtrFilter().atrMatches(atr)) {
                if (logger.isInfoEnabled()) {
                    logger.info(
                            "[{}] openLogicalChannel => ATR didn't match. SELECTOR = {}, ATR = {}",
                            this.getName(), seSelector, ByteArrayUtil.toHex(atr));
                }
                selectionHasMatched = false;
            }
        }

        /*
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
                /*
                 * The selection didn't provide data (e.g. OMAPI), we get the FCI using a Get Data
                 * command.
                 *
                 * The AID selector is provided to handle successful status word in the Get Data
                 * command.
                 */
                fciResponse = recoverSelectionFciData(seSelector.getAidSelector());
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
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    final SelectionStatus openLogicalChannelAndSelect(SeSelector seSelector) {

        SelectionStatus selectionStatus;

        if (seSelector == null) {
            throw new IllegalArgumentException("Try to open logical channel without selector.");
        }

        if (!logicalChannelIsOpen) {
            /*
             * init of the physical SE channel: if not yet established, opening of a new physical
             * channel
             */
            if (!isPhysicalChannelOpen()) {
                openPhysicalChannel();
            }
            if (!isPhysicalChannelOpen()) {
                throw new KeypleReaderIOException("Fail to open physical channel.");
            }
        }

        selectionStatus = openLogicalChannel(seSelector);

        return selectionStatus;
    }

    /**
     * Attempts to open the physical channel
     *
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    protected abstract void openPhysicalChannel();

    /**
     * Closes the current physical channel.
     * <p>
     * This method must be implemented by the ProxyReader plugin (e.g. Pcsc/Nfc/Omapi Reader).
     *
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    protected abstract void closePhysicalChannel();

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
        if (logger.isTraceEnabled()) {
            logger.trace("[{}] closeLogicalChannel => Closing of the logical channel.",
                    this.getName());
        }
        logicalChannelIsOpen = false;
        aidCurrentlySelected = null;
        currentSelectionStatus = null;
    }

    /* ==== Protocol management =========================================== */

    /**
     * PO selection map associating seProtocols and selection strings.
     * <p>
     * The String associated with a particular protocol can be anything that is relevant to be
     * interpreted by reader plugins implementing protocolFlagMatches (e.g. ATR regex for Pcsc
     * plugins, technology name for Nfc plugins, etc).
     */
    private final Map<SeProtocol, String> protocolsMap = new HashMap<SeProtocol, String>();

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
    @Override
    public void setSeProtocolSetting(Map<SeProtocol, String> protocolSetting) {
        this.protocolsMap.putAll(protocolSetting);
    }

    /**
     * @return the Map containing the protocol definitions set by addSeProtocolSetting and
     *         setSeProtocolSetting
     */
    protected final Map<SeProtocol, String> getProtocolsMap() {
        return protocolsMap;
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
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    protected abstract boolean protocolFlagMatches(SeProtocol protocolFlag);

    /* ==== SeRequestSe and SeRequest transmission management ============= */

    /**
     * Do the transmission of all requests according to the protocol flag selection logic.<br>
     * <br>
     * The received responses are returned as {@link List} of {@link SeResponse} The requests are
     * ordered at application level and the responses match this order.<br>
     * When a request is not matching the current PO, the response responses pushed in the response
     * List object is set to null.
     *
     * @param seRequests the request list
     * @param multiSeRequestProcessing the multi se processing mode
     * @param channelControl indicates if the channel has to be closed at the end of the processing
     * @return the response list
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    @Override
    protected final List<SeResponse> processSeRequests(List<SeRequest> seRequests,
            MultiSeRequestProcessing multiSeRequestProcessing, ChannelControl channelControl) {

        boolean[] requestMatchesProtocol = new boolean[seRequests.size()];
        int requestIndex = 0;
        int lastRequestIndex;

        // Determine which requests are matching the current ATR
        // All requests without selector are considered matching
        for (SeRequest request : seRequests) {
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
         * The current request list is possibly made of several APDU command lists.
         *
         * If the requestMatchesProtocol is true we process the SeRequest.
         *
         * If the requestMatchesProtocol is false we skip to the next SeRequest.
         *
         * If keepChannelOpen is false, we close the physical channel for the last request.
         */
        List<SeResponse> responses = new ArrayList<SeResponse>();
        boolean stopProcess = false;
        for (SeRequest request : seRequests) {

            if (!stopProcess) {
                if (requestMatchesProtocol[requestIndex]) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[{}] processSeRequests => transmit {}", this.getName(),
                                request);
                    }
                    SeResponse response;
                    try {
                        response = processSeRequestLogical(request);
                    } catch (KeypleReaderIOException ex) {
                        /*
                         * The process has been interrupted. We launch a KeypleReaderException with
                         * the responses collected so far.
                         */
                        /* Add the latest (and partial) SeResponse to the current list. */
                        responses.add(ex.getSeResponse());
                        /* Build a List of SeResponse with the available data. */
                        ex.setSeResponses(responses);
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "[{}] processSeRequests => transmit : process interrupted, collect previous responses {}",
                                    this.getName(), responses);
                        }
                        throw ex;
                    }
                    responses.add(response);
                    if (logger.isDebugEnabled()) {
                        logger.debug("[{}] processSeRequests => receive {}", this.getName(),
                                response);
                    }
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
                    if (logicalChannelIsOpen) {
                        // the current PO matches the selection case, we stop here.
                        stopProcess = true;
                    }
                }
                requestIndex++;
                if (lastRequestIndex == requestIndex
                        && channelControl != ChannelControl.KEEP_OPEN) {

                    // close logical channel unconditionally
                    closeLogicalChannel();

                    // not observable or no observers ?
                    if (!(this instanceof ObservableReader)
                            || (((ObservableReader) this).countObservers() == 0)) {
                        // close immediately the physical channel
                        closePhysicalChannel();
                    } else {
                        // manage the end of communication in observed mode
                        terminateSeCommunication();
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
     * @param seRequest the SeRequest (null if only the closing of the physical channel is
     *        requested)
     * @param channelControl indicates if the channel has to be closed at the end of the processing
     * @return the SeResponse to the SeRequest
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    @SuppressWarnings({"PMD.ModifiedCyclomaticComplexity", "PMD.CyclomaticComplexity",
            "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity", "PMD.ExcessiveMethodLength"})
    @Override
    protected final SeResponse processSeRequest(SeRequest seRequest,
            ChannelControl channelControl) {

        SeResponse seResponse = null;

        /* The SeRequest may be null when we just need to close the physical channel */
        if (seRequest != null) {
            seResponse = processSeRequestLogical(seRequest);
        }

        if (channelControl != ChannelControl.KEEP_OPEN) {
            // close logical channel unconditionally
            closeLogicalChannel();

            // OD : couldn't we move this to AbstractObservableLocalReader?
            if (!(this instanceof ObservableReader)
                    || (((ObservableReader) this).countObservers() == 0)) {
                /* Not observable/observed: close immediately the physical channel if requested */
                closePhysicalChannel();
            }

            if (this instanceof AbstractObservableLocalReader) {
                /*
                 * request the removal sequence when the reader is monitored by a thread
                 */
                ((AbstractObservableLocalReader) this).terminateSeCommunication();
            }
        }

        return seResponse;
    }

    /**
     * Indicates whether this array of bytes starts with this other one
     *
     * @param source the byte array to examine
     * @param match the byte array to compare in <code>source</code>
     * @return true if the starting bytes of <code>source</code> equal <code>match</code>
     */
    private static boolean startsWith(byte[] source, byte[] match) {

        if (match.length > source.length) {
            return false;
        }

        for (int i = 0; i < match.length; i++) {
            if (source[i] != match[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Implements the logical processSeRequest.
     * <p>
     * This method is called by processSeRequests and processSeRequest.
     * <p>
     * It opens both physical and logical channels if needed.
     * <p>
     * The logical channel is closed when requested.
     *
     * @param seRequest the {@link SeRequest} to be sent
     * @return seResponse
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    private SeResponse processSeRequestLogical(SeRequest seRequest) {
        boolean previouslyOpen = true;
        SelectionStatus selectionStatus = null;

        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();

        if (logger.isDebugEnabled()) {
            logger.debug("[{}] processSeRequest => Logical channel open = {}", this.getName(),
                    logicalChannelIsOpen);
        }
        /*
         * unless the selector is null, we try to open a logical channel; if the channel was open
         * and the PO is still matching we won't redo the selection and just use the current
         * selection status
         */
        if (seRequest.getSeSelector() != null) {
            /* check if AID changed if the channel is already open */
            if (logicalChannelIsOpen && seRequest.getSeSelector().getAidSelector() != null) {
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
                } else if (!startsWith(aidCurrentlySelected,
                        seRequest.getSeSelector().getAidSelector().getAidToSelect())) {
                    // the AID changed (longer or different), close the logical channel
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "[{}] processSeRequest => The AID changed, close the logical channel. AID = {}, EXPECTEDAID = {}",
                                this.getName(), ByteArrayUtil.toHex(aidCurrentlySelected),
                                seRequest.getSeSelector());
                    }
                    /* close the channel (will reset the current selection status) */
                    closeLogicalChannel();
                }
                /* keep the current selection status (may be null if the current PO didn't match) */
                selectionStatus = currentSelectionStatus;
            }

            /* open the channel and do the selection if needed */
            if (!logicalChannelIsOpen) {
                previouslyOpen = false;

                try {
                    selectionStatus = openLogicalChannelAndSelect(seRequest.getSeSelector());
                    if (logger.isTraceEnabled()) {
                        logger.trace("[{}] processSeRequest => Logical channel opening success.",
                                this.getName());
                    }
                } catch (IllegalArgumentException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[{}] processSeRequest => Logical channel opening failure",
                                this.getName());
                    }
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
            if (!logicalChannelIsOpen) {
                throw new IllegalStateException(
                        "[" + this.getName() + "] processSeRequest => No logical channel opened!");
            }
        }

        /* process request if not empty */
        if (seRequest.getApduRequests() != null) {
            for (ApduRequest apduRequest : seRequest.getApduRequests()) {
                try {
                    apduResponses.add(processApduRequest(apduRequest));
                } catch (KeypleReaderIOException ex) {
                    /*
                     * The process has been interrupted. We close the logical channel and launch a
                     * KeypleReaderException with the Apdu responses collected so far.
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "The process has been interrupted, collect Apdu responses collected so far");
                    }
                    closeLogicalAndPhysicalChannels();
                    ex.setSeResponse(
                            new SeResponse(false, previouslyOpen, selectionStatus, apduResponses));
                    throw ex;
                }
            }
        }

        return new SeResponse(logicalChannelIsOpen, previouslyOpen, selectionStatus, apduResponses);
    }

    /* ==== APDU transmission management ================================== */

    /**
     * Transmits an ApduRequest and receives the ApduResponse
     * <p>
     * The time measurement is carried out and logged with the detailed information of the exchanges
     * (TRACE level).
     *
     * @param apduRequest APDU request
     * @return APDU response
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    private ApduResponse processApduRequest(ApduRequest apduRequest) {
        ApduResponse apduResponse;
        if (logger.isDebugEnabled()) {
            long timeStamp = System.nanoTime();
            long elapsed10ms = (timeStamp - before) / 100000;
            this.before = timeStamp;
            logger.debug("[{}] processApduRequest => {}, elapsed {} ms.", this.getName(),
                    apduRequest, elapsed10ms / 10.0);
        }

        byte[] buffer = apduRequest.getBytes();
        apduResponse =
                new ApduResponse(transmitApdu(buffer), apduRequest.getSuccessfulStatusCodes());

        if (apduRequest.isCase4() && apduResponse.getDataOut().length == 0
                && apduResponse.isSuccessful()) {
            // do the get response command but keep the original status code
            apduResponse = case4HackGetResponse(apduResponse.getStatusCode());
        }

        if (logger.isDebugEnabled()) {
            long timeStamp = System.nanoTime();
            long elapsed10ms = (timeStamp - before) / 100000;
            this.before = timeStamp;
            logger.debug("[{}] processApduRequest => {}, elapsed {} ms.", this.getName(),
                    apduResponse, elapsed10ms / 10.0);
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
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    private ApduResponse case4HackGetResponse(int originalStatusCode) {
        /*
         * build a get response command the actual length expected by the SE in the get response
         * command is handled in transmitApdu
         */
        if (logger.isDebugEnabled()) {
            long timeStamp = System.nanoTime();
            long elapsed10ms = (timeStamp - before) / 100000;
            this.before = timeStamp;
            logger.debug(
                    "[{}] case4HackGetResponse => ApduRequest: NAME = \"Internal Get Response\", RAWDATA = {}, elapsed = {}",
                    this.getName(), ByteArrayUtil.toHex(getResponseHackRequestBytes),
                    elapsed10ms / 10.0);
        }

        byte[] getResponseHackResponseBytes = transmitApdu(getResponseHackRequestBytes);

        /* we expect here a 0x9000 status code */
        ApduResponse getResponseHackResponse = new ApduResponse(getResponseHackResponseBytes, null);

        if (logger.isDebugEnabled()) {
            long timeStamp = System.nanoTime();
            long elapsed10ms = (timeStamp - before) / 100000;
            this.before = timeStamp;
            logger.debug("[{}] case4HackGetResponse => Internal {}, elapsed {} ms.", this.getName(),
                    getResponseHackResponseBytes, elapsed10ms / 10.0);
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
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    protected abstract byte[] transmitApdu(byte[] apduIn);

    /**
     * Method to be implemented by child classes in order to handle the needed actions when
     * terminating the communication with a SE (closing of the physical channel, initiating a
     * removal sequence, etc.)
     */
    abstract void terminateSeCommunication();
}
