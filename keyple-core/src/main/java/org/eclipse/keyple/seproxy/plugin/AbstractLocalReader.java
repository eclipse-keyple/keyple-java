/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.plugin;

import java.nio.ByteBuffer;
import java.util.*;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.exception.ChannelStateReaderException;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.SelectApplicationException;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manage the loop processing for SeRequest transmission in a set and for SeResponse reception in a
 * set
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.CyclomaticComplexity"})
public abstract class AbstractLocalReader extends AbstractObservableReader {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLocalReader.class);

    private boolean logicalChannelIsOpen = false;
    private ByteBuffer aidCurrentlySelected;
    private ApduResponse fciDataSelected;
    private ApduResponse atrData;

    public AbstractLocalReader(String pluginName, String readerName) {
        super(pluginName, readerName);
    }

    /**
     * Open (if needed) a physical channel (try to connect a card to the terminal, attempt to select
     * the application)
     *
     * @param selector the SE Selector: either the AID of the application to select or an ATR
     *        selection regular expression
     * @param successfulSelectionStatusCodes the list of successful status code for the select
     *        command
     * @return an array of 2 ByteBuffers: ByteBuffer[0] the SE ATR, ByteBuffer[1] the SE FCI
     * @throws IOReaderException if a reader error occurs
     * @throws SelectApplicationException if the application selection fails
     */
    protected abstract ByteBuffer[] openLogicalChannelAndSelect(SeRequest.Selector selector,
            Set<Short> successfulSelectionStatusCodes)
            throws IOReaderException, SelectApplicationException;

    /**
     * Closes the current physical channel.
     *
     * @throws IOReaderException if a reader error occurs
     */
    protected abstract void closePhysicalChannel() throws IOReaderException;

    /**
     * Transmits a single APDU and receives its response. The implementation of this abstract method
     * must handle the case where the SE response is 61xy and execute the appropriate get response
     * command
     *
     * @param apduIn byte buffer containing the ingoing data
     * @return apduResponse byte buffer containing the outgoing data.
     * @throws ChannelStateReaderException if the transmission fails
     */
    protected abstract ByteBuffer transmitApdu(ByteBuffer apduIn)
            throws ChannelStateReaderException;

    /**
     * Test if the current protocol matches the flag
     *
     * @param protocolFlag the protocol flag
     * @return true if the current protocol matches the provided protocol flag
     * @throws IOReaderException if a reader error occurs
     */
    protected abstract boolean protocolFlagMatches(SeProtocol protocolFlag)
            throws IOReaderException;

    /**
     * Transmits an ApduRequest and receives the ApduResponse with time measurement.
     *
     * @param apduRequest APDU request
     * @return APDU response
     * @throws ChannelStateReaderException Exception faced
     */
    protected final ApduResponse processApduRequest(ApduRequest apduRequest)
            throws ChannelStateReaderException {
        ApduResponse apduResponse;
        long before = 0;
        if (logger.isTraceEnabled()) {
            logger.trace("[{}] processApduRequest => {}.", this.getName(), apduRequest);
            before = System.nanoTime();
        }
        /*
         * Fix buffer position before sending data We shouldn't have to re-use the buffer that was
         * used to be sent but we have some code that does it.
         */
        ByteBuffer buffer = apduRequest.getBytes();
        final int posBeforeRead = buffer.position();
        apduResponse =
                new ApduResponse(transmitApdu(buffer), apduRequest.getSuccessfulStatusCodes());
        buffer.position(posBeforeRead);

        if (apduRequest.isCase4() && apduResponse.getDataOut().limit() == 0
                && apduResponse.isSuccessful()) {
            // do the get response command but keep the original status code
            apduResponse = case4HackGetResponse(apduResponse.getStatusCode());
        }

        if (logger.isTraceEnabled()) {
            double elapsedMs = (double) ((System.nanoTime() - before) / 100000) / 10;
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
     * @throws ChannelStateReaderException if the transmission fails.
     */
    private ApduResponse case4HackGetResponse(int originalStatusCode)
            throws ChannelStateReaderException {
        long before = 0;
        /*
         * build a get response command the actual length expected by the SE in the get response
         * command is handled in transmitApdu
         */
        ByteBuffer getResponseHackRequestBytes = ByteBufferUtils.fromHex("00C0000000");
        if (logger.isTraceEnabled()) {
            logger.trace(
                    "[{}] case4HackGetResponse => ApduRequest: NAME = \"Intrinsic Get Response\", RAWDATA = {}",
                    this.getName(), ByteBufferUtils.toHex(getResponseHackRequestBytes));
            before = System.nanoTime();
        }

        ByteBuffer getResponseHackResponseBytes = transmitApdu(getResponseHackRequestBytes);

        /* we expect here a 0x9000 status code */
        ApduResponse getResponseHackResponse = new ApduResponse(getResponseHackResponseBytes, null);

        if (logger.isTraceEnabled()) {
            double elapsedMs = (double) ((System.nanoTime() - before) / 100000) / 10;
            logger.trace("[{}] case4HackGetResponse => Intrinsic {}, elapsed {} ms.",
                    this.getName(), getResponseHackResponseBytes, elapsedMs);
        }

        if (getResponseHackResponse.isSuccessful()) {
            // replace the two last status word bytes by the original status word
            final int posBeforeChange = getResponseHackResponseBytes.position();
            int position = getResponseHackResponseBytes.limit();
            getResponseHackResponseBytes.position(position - 2);
            getResponseHackResponseBytes.put((byte) (originalStatusCode >> 8));
            getResponseHackResponseBytes.position(position - 1);
            getResponseHackResponseBytes.put((byte) (originalStatusCode & 0xFF));
            getResponseHackResponseBytes.position(posBeforeChange);
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
     * @throws IOReaderException if a reader error occurs
     */
    protected final SeResponseSet processSeRequestSet(SeRequestSet requestSet)
            throws IOReaderException {

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
         * The current requestSet is possibly made of several APDU command lists If the
         * requestMatchesProtocol is true we process the requestSet If the requestMatchesProtocol is
         * false we skip to the next requestSet If keepChannelOpen is false, we close the physical
         * channel for the last request.
         */
        List<SeResponse> responses = new ArrayList<SeResponse>();
        boolean stopProcess = false;
        for (SeRequest request : requestSet.getRequests()) {

            if (!stopProcess) {
                if (requestMatchesProtocol[requestIndex]) {
                    logger.debug("[{}] processSeRequestSet => transmit {}", this.getName(),
                            request);
                    SeResponse response = processSeRequest(request);
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
                    stopProcess = true;
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
     * @throws ChannelStateReaderException if a transmission fails
     */
    @SuppressWarnings({"PMD.ModifiedCyclomaticComplexity", "PMD.CyclomaticComplexity",
            "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity"})
    private SeResponse processSeRequest(SeRequest seRequest) throws IOReaderException {
        boolean previouslyOpen = true;

        List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();

        // unless the selector is null, we try to open a logical channel
        if (seRequest.getSelector() != null) {
            // check if AID changed if the channel is already open
            if (isLogicalChannelOpen() && seRequest.getSelector() instanceof SeRequest.AidSelector
                    && aidCurrentlySelected != ((SeRequest.AidSelector) seRequest.getSelector())
                            .getAidToSelect()) {
                // the AID changed, close the logical channel
                if (logger.isTraceEnabled()) {
                    logger.trace(
                            "[{}] processSeRequest => The AID changed, close the logical channel. AID = {}, EXPECTEDAID = {}",
                            this.getName(), ByteBufferUtils.toHex(aidCurrentlySelected),
                            seRequest.getSelector());
                }
                closeLogicalChannel();
            }

            if (!isLogicalChannelOpen()) {

                previouslyOpen = false;
                ByteBuffer atrAndFciDataBytes[];

                try {
                    atrAndFciDataBytes = openLogicalChannelAndSelect(seRequest.getSelector(),
                            seRequest.getSuccessfulSelectionStatusCodes());
                    logger.trace("[{}] processSeRequest => Logical channel opening success.",
                            this.getName());
                } catch (SelectApplicationException e) {
                    logger.trace("[{}] processSeRequest => Logical channel opening failure",
                            this.getName());
                    closeLogicalChannel();
                    // return a null SeResponse when the opening of the logical channel failed
                    return null;
                }

                if (atrAndFciDataBytes[0] != null) { // the SE Answer to reset
                    atrData = new ApduResponse(atrAndFciDataBytes[0], null);
                    if (seRequest.getSelector() instanceof SeRequest.AtrSelector) {
                        // channel is considered if the selection mode was ATR based
                        setLogicalChannelOpen();
                    }
                }

                if (atrAndFciDataBytes[1] != null) { // the logical channel opening is successful
                    aidCurrentlySelected =
                            ((SeRequest.AidSelector) seRequest.getSelector()).getAidToSelect();
                    fciDataSelected = new ApduResponse(atrAndFciDataBytes[1],
                            seRequest.getSuccessfulSelectionStatusCodes());
                    if (fciDataSelected.isSuccessful()) {
                        // the channel opening is successful
                        setLogicalChannelOpen();
                    } else {
                        closeLogicalChannel();
                    }
                }
            }
        } else {
            // selector is null, we expect that the logical channel was previously opened
            if (!isLogicalChannelOpen()) {
                throw new IllegalStateException(
                        "[" + this.getName() + "] processSeRequest => No logical channel opened!");
            }
        }

        // process request if not empty
        if (seRequest.getApduRequests() != null) {
            for (ApduRequest apduRequest : seRequest.getApduRequests()) {
                apduResponseList.add(processApduRequest(apduRequest));
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
