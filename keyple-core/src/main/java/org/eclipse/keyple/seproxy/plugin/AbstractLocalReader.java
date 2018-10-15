/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.seproxy.plugin;

import java.util.*;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.exception.KeypleApplicationSelectionException;
import org.eclipse.keyple.seproxy.exception.KeypleChannelStateException;
import org.eclipse.keyple.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manage the loop processing for SeRequest transmission in a set and for SeResponse reception in a
 * set
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.CyclomaticComplexity"})
public abstract class AbstractLocalReader extends AbstractObservableReader {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLocalReader.class);
    private static final byte[] getResponseHackRequestBytes = ByteArrayUtils.fromHex("00C0000000");
    private boolean logicalChannelIsOpen = false;
    private byte[] aidCurrentlySelected;
    private ApduResponse fciDataSelected;
    private ApduResponse atrData;
    private long before; // timestamp recorder

    public AbstractLocalReader(String pluginName, String readerName) {
        super(pluginName, readerName);
        this.before = System.nanoTime();
    }

    /**
     * Open (if needed) a physical channel (try to connect a card to the terminal, attempt to select
     * the application)
     *
     * @param selector the SE Selector: either the AID of the application to select or an ATR
     *        selection regular expression
     * @param successfulSelectionStatusCodes the list of successful status code for the select
     *        command
     * @return an array of 2 byte arrays: byte[0][] the SE ATR, byte[1][] the SE FCI
     * @throws KeypleReaderException if a reader error occurs
     * @throws KeypleApplicationSelectionException if the application selection fails
     */
    protected abstract byte[][] openLogicalChannelAndSelect(SeRequest.Selector selector,
            Set<Short> successfulSelectionStatusCodes)
            throws KeypleApplicationSelectionException, KeypleReaderException;

    /**
     * Closes the current physical channel.
     *
     * @throws KeypleChannelStateException if a reader error occurs
     */
    protected abstract void closePhysicalChannel() throws KeypleChannelStateException;

    /**
     * Transmits a single APDU and receives its response. The implementation of this abstract method
     * must handle the case where the SE response is 61xy and execute the appropriate get response
     * command
     *
     * @param apduIn byte buffer containing the ingoing data
     * @return apduResponse byte buffer containing the outgoing data.
     * @throws KeypleIOReaderException if the transmission fails
     */
    protected abstract byte[] transmitApdu(byte[] apduIn) throws KeypleIOReaderException;

    /**
     * Test if the current protocol matches the flag
     *
     * @param protocolFlag the protocol flag
     * @return true if the current protocol matches the provided protocol flag
     * @throws KeypleReaderException in case of a reader exception
     */
    protected abstract boolean protocolFlagMatches(SeProtocol protocolFlag)
            throws KeypleReaderException;

    /**
     * Transmits an ApduRequest and receives the ApduResponse with time measurement.
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
                    "[{}] case4HackGetResponse => ApduRequest: NAME = \"Intrinsic Get Response\", RAWDATA = {}, elapsed = {}",
                    this.getName(), ByteArrayUtils.toHex(getResponseHackRequestBytes), elapsedMs);
        }

        byte[] getResponseHackResponseBytes = transmitApdu(getResponseHackRequestBytes);

        /* we expect here a 0x9000 status code */
        ApduResponse getResponseHackResponse = new ApduResponse(getResponseHackResponseBytes, null);

        if (logger.isTraceEnabled()) {
            long timeStamp = System.nanoTime();
            double elapsedMs = (double) ((timeStamp - this.before) / 100000) / 10;
            this.before = timeStamp;
            logger.trace("[{}] case4HackGetResponse => Intrinsic {}, elapsed {} ms.",
                    this.getName(), getResponseHackResponseBytes, elapsedMs);
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
     * Do the transmission of all needed requestSet requests contained in the provided requestSet
     * according to the protocol flag selection logic. The responseSet responses are returned in the
     * responseSet object. The requestSet requests are ordered at application level and the
     * responses match this order. When a requestSet is not matching the current PO, the responseSet
     * responses pushed in the responseSet object is set to null.
     *
     * @param requestSet the request set
     * @return SeResponseSet the response set
     * @throws KeypleIOReaderException if a reader error occurs
     */
    protected final SeResponseSet processSeRequestSet(SeRequestSet requestSet)
            throws KeypleReaderException {

        boolean requestMatchesProtocol[] = new boolean[requestSet.getRequests().size()];
        int requestIndex = 0, lastRequestIndex;

        // Determine which requests are matching the current ATR
        for (SeRequest request : requestSet.getRequests()) {
            requestMatchesProtocol[requestIndex] = protocolFlagMatches(request.getProtocolFlag());
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
        for (SeRequest request : requestSet.getRequests()) {

            if (!stopProcess) {
                if (requestMatchesProtocol[requestIndex]) {
                    logger.debug("[{}] processSeRequestSet => transmit {}", this.getName(),
                            request);
                    SeResponse response = null;
                    try {
                        response = processSeRequest(request);
                    } catch (KeypleReaderException ex) {
                        /*
                         * The process has been interrupted. We are launching a
                         * KeypleReaderException with the responses collected so far.
                         */
                        /* Add the latest (and partial) SeResponse to the current list. */
                        responses.add(ex.getSeResponse());
                        /* Build a SeResponseSet with the available data. */
                        ex.setSeResponseSet(new SeResponseSet(responses));
                        throw ex;
                    }
                    responses.add(response);
                    logger.debug("[{}] processSeRequestSet => receive {}", this.getName(),
                            response);
                } else {
                    /*
                     * in case the protocolFlag of a SeRequest doesn't match the reader status, a
                     * null SeResponse is added to the SeResponseSet.
                     */
                    responses.add(null);
                }
                requestIndex++;
                if (!request.isKeepChannelOpen()) {
                    if (lastRequestIndex == requestIndex) {
                        // reset temporary properties
                        closeLogicalChannel();

                        /*
                         * For the processing of the last SeRequest with a protocolFlag matching the
                         * SE reader status, if the logical channel doesn't require to be kept open,
                         * then the physical channel is closed.
                         */
                        closePhysicalChannel();


                        logger.debug("[{}] processSeRequestSet => Closing of the physical channel.",
                                this.getName());
                    }
                } else {
                    if (isLogicalChannelOpen()) {
                        stopProcess = true;
                    }
                    /*
                     * When keepChannelOpen is true, we stop after the first matching request we
                     * exit the for loop here For the processing of a SeRequest with a protocolFlag
                     * which matches the current SE reader status, in case it's requested to keep
                     * the logical channel open, then the other remaining SeRequest are skipped, and
                     * null SeRequest are returned for them.
                     */
                }
            }
        }
        return new SeResponseSet(responses);
    }

    /**
     * Tells if a logical channel is open
     * 
     * @return true if the logical channel is open
     */
    protected final boolean isLogicalChannelOpen() {
        return logicalChannelIsOpen;
    }

    protected final void closeLogicalChannel() {
        logger.trace("[{}] closeLogicalChannel => Closing of the logical channel.", this.getName());
        logicalChannelIsOpen = false;
        fciDataSelected = null;
        atrData = null;
        aidCurrentlySelected = null;
    }

    private void setLogicalChannelOpen() {
        logicalChannelIsOpen = true;
    }

    /**
     * Executes a request made of one or more Apdus and receives their answers. The selection of the
     * application is handled. The methods allows decrease the cyclomatic complexity of
     * TransmitActual
     *
     * @param seRequest the SeRequest
     * @return the SeResponse to the SeRequest
     * @throws KeypleReaderException if a transmission fails
     */
    @SuppressWarnings({"PMD.ModifiedCyclomaticComplexity", "PMD.CyclomaticComplexity",
            "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity", "PMD.ExcessiveMethodLength"})
    protected final SeResponse processSeRequest(SeRequest seRequest)
            throws IllegalStateException, KeypleReaderException {
        boolean previouslyOpen = true;

        List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();

        /* unless the selector is null, we try to open a logical channel */
        if (seRequest.getSelector() != null) {
            /* check if AID changed if the channel is already open */
            // TODO implement "select next" to handle SE having several matching AIDs
            // for now we check only if the selection AID is included in the AID found in the FCI
            // data
            if (isLogicalChannelOpen()
                    && seRequest.getSelector() instanceof SeRequest.AidSelector) {
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
                if (((SeRequest.AidSelector) seRequest.getSelector())
                        .getAidToSelect().length >= aidCurrentlySelected.length
                        && aidCurrentlySelected.equals(Arrays.copyOfRange(
                                ((SeRequest.AidSelector) seRequest.getSelector()).getAidToSelect(),
                                0, aidCurrentlySelected.length))) {
                    // the AID changed, close the logical channel
                    if (logger.isTraceEnabled()) {
                        logger.trace(
                                "[{}] processSeRequest => The AID changed, close the logical channel. AID = {}, EXPECTEDAID = {}",
                                this.getName(), ByteArrayUtils.toHex(aidCurrentlySelected),
                                seRequest.getSelector());
                    }
                    closeLogicalChannel();
                }
            }

            if (!isLogicalChannelOpen()) {

                previouslyOpen = false;
                byte[] atrAndFciDataBytes[];

                try {
                    atrAndFciDataBytes = openLogicalChannelAndSelect(seRequest.getSelector(),
                            seRequest.getSuccessfulSelectionStatusCodes());
                    logger.trace("[{}] processSeRequest => Logical channel opening success.",
                            this.getName());
                } catch (KeypleApplicationSelectionException e) {
                    logger.trace("[{}] processSeRequest => Logical channel opening failure",
                            this.getName());
                    closeLogicalChannel();
                    /* return a null SeResponse when the opening of the logical channel failed */
                    return null;
                }

                if (atrAndFciDataBytes[0] != null) { // the SE Answer to reset
                    atrData = new ApduResponse(atrAndFciDataBytes[0], null);
                    if (seRequest.getSelector() instanceof SeRequest.AtrSelector) {
                        /* channel is considered if the selection mode was ATR based */
                        setLogicalChannelOpen();
                    }
                }

                if (atrAndFciDataBytes[1] != null) { // the logical channel opening is successful
                    aidCurrentlySelected =
                            ((SeRequest.AidSelector) seRequest.getSelector()).getAidToSelect();
                    fciDataSelected = new ApduResponse(atrAndFciDataBytes[1],
                            seRequest.getSuccessfulSelectionStatusCodes());
                    if (fciDataSelected.isSuccessful()) {
                        /* the channel opening is successful */
                        setLogicalChannelOpen();
                    } else {
                        closeLogicalChannel();
                    }
                }
            }
        } else {
            /* selector is null, we expect that the logical channel was previously opened */
            if (!isLogicalChannelOpen()) {
                throw new IllegalStateException(
                        "[" + this.getName() + "] processSeRequest => No logical channel opened!");
            }
        }

        /* process request if not empty */
        if (seRequest.getApduRequests() != null) {
            for (ApduRequest apduRequest : seRequest.getApduRequests()) {
                try {
                    apduResponseList.add(processApduRequest(apduRequest));
                } catch (KeypleIOReaderException ex) {
                    /*
                     * The process has been interrupted. We are launching a KeypleReaderException
                     * with the Apdu responses collected so far.
                     */
                    ex.setSeResponse(new SeResponse(previouslyOpen, atrData, fciDataSelected,
                            apduResponseList));
                    throw ex;
                }
            }
        }

        return new SeResponse(previouslyOpen, atrData, fciDataSelected, apduResponseList);
    }

    /**
     * PO selection map associating seProtocols and selection strings (e.g. ATR regex for Pcsc
     * plugins)
     */
    protected Map<SeProtocol, String> protocolsMap = new HashMap<SeProtocol, String>();

    public void addSeProtocolSetting(SeProtocolSetting seProtocolSetting) {
        this.protocolsMap.putAll(seProtocolSetting.getProtocolsMap());
    }
}
