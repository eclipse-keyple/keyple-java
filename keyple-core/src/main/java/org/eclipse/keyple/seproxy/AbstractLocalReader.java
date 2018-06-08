/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import javax.smartcardio.CardException;
import org.eclipse.keyple.seproxy.exceptions.ChannelStateReaderException;
import org.eclipse.keyple.seproxy.exceptions.IOReaderException;
import org.eclipse.keyple.seproxy.exceptions.InvalidMessageException;
import org.eclipse.keyple.util.ByteBufferUtils;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

/**
 * Manage the loop processing for SeRequest transmission in a set and for SeResponse reception in a
 * set
 */
public abstract class AbstractLocalReader extends AbstractObservableReader {

    private static final ILogger logger = SLoggerFactory.getLogger(AbstractLocalReader.class);
    private ByteBuffer aidCurrentlySelected;
    private ApduResponse fciDataSelected;
    private boolean logging;
    private static final String ACTION_STR = "action"; // PMD rule AvoidDuplicateLiterals

    /**
     * Checks the presence of a physical channel. Creates one if needed, generates an exception in
     * case of failure.
     *
     * @throws IOReaderException
     */
    public abstract void checkOrOpenPhysicalChannel() throws IOReaderException;

    /**
     * Closes the current physical channel.
     *
     * @throws IOReaderException
     */
    public abstract void closePhysicalChannel() throws IOReaderException;

    /**
     * Transmits a single APDU and receives its response. The implementation of this abstract method
     * must handle the case where the SE response is 61xy and execute the appropriate get response
     * command
     *
     * @param apduIn byte buffer containing the ingoing data
     * @return apduResponse byte buffer containing the outgoing data.
     * @throws ChannelStateReaderException
     */
    public abstract ByteBuffer transmitApdu(ByteBuffer apduIn) throws ChannelStateReaderException;

    /**
     * Return a pseudo FCI when no application selection is possible (e.g. ATR from CSM) The FCI
     * data buffer ends with SW1Sw2=9000
     *
     * @return FCI data
     */
    public abstract ByteBuffer getAlternateFci();

    /**
     * Test if the current protocol matches the flag
     *
     * @param protocolFlag
     * @return true if the current protocol matches the provided protocol flag
     * @throws InvalidMessageException
     */
    public abstract boolean protocolFlagMatches(SeProtocol protocolFlag)
            throws InvalidMessageException;

    /**
     * Transmits a SeRequestSet and receives the SeResponseSet with time measurement.
     *
     * @param requestSet
     * @return responseSet
     * @throws IOReaderException
     */
    public final SeResponseSet transmit(SeRequestSet requestSet) throws IOReaderException {
        long before = System.nanoTime();
        try {
            SeResponseSet responseSet = transmitActual(requestSet);
            // Switching to the 10th of milliseconds and dividing by 10 to get the ms
            double elapsedMs = (double) ((System.nanoTime() - before) / 100000) / 10;
            logger.info("LocalReader: Data exchange", ACTION_STR, "local_reader.transmit",
                    "requestSet", requestSet, "responseSet", responseSet, "elapsedMs", elapsedMs);
            return responseSet;
        } catch (IOReaderException ex) {
            // Switching to the 10th of milliseconds and dividing by 10 to get the ms
            double elapsedMs = (double) ((System.nanoTime() - before) / 100000) / 10;
            logger.info("LocalReader: Data exchange", ACTION_STR, "local_reader.transmit_failure",
                    "requestSet", requestSet, "elapsedMs", elapsedMs);
            throw ex;
        }
    }

    /**
     * Connects to the card's application (do the select application command)
     *
     * @throws ChannelStateReaderException
     */
    private ApduResponse connect(ByteBuffer aid) throws ChannelStateReaderException {
        logger.info("Connecting to card", ACTION_STR, "local_reader.connect", "aid",
                ByteBufferUtils.toHex(aid), "readerName", getName());
        try {
            ByteBuffer command = ByteBuffer.allocate(aid.limit() + 6);
            command.put((byte) 0x00);
            command.put((byte) 0xA4);
            command.put((byte) 0x04);
            command.put((byte) 0x00);
            command.put((byte) aid.limit());
            command.put(aid);
            command.put((byte) 0x00);
            command.position(0);
            ApduResponse fciResponse = new ApduResponse(transmitApdu(command), true);

            if (fciResponse.getDataOut().limit() == 0 && fciResponse.getStatusCode() == 0x9000) {
                // the select command always returns data
                // this SE is probably expecting a get response command (e.g. old Calypso cards)
                fciResponse = case4HackGetResponse();
            }

            aidCurrentlySelected = aid;
            return fciResponse;
        } catch (CardException e1) {
            throw new ChannelStateReaderException(e1);
        }
    }

    /**
     * Transmission of each APDU request
     *
     * @param apduRequest APDU request
     * @return APDU response
     * @throws ChannelStateReaderException Exception faced
     */
    private ApduResponse transmit(ApduRequest apduRequest) throws ChannelStateReaderException {
        ApduResponse apduResponse;
        long before = logging ? System.nanoTime() : 0;
        try {
            ByteBuffer buffer = apduRequest.getBuffer();
            { // Sending data
              // We shouldn't have to re-use the buffer that was used to be sent but we have
              // some code that does it.
                final int posBeforeRead = buffer.position();
                apduResponse = new ApduResponse(transmitApdu(buffer), true);
                buffer.position(posBeforeRead);
            }

            if (apduRequest.isCase4() && apduResponse.getDataOut().limit() == 0
                    && apduResponse.getStatusCode() == 0x9000) {
                // a get response command is requested by the application for this Apdu
                apduResponse = case4HackGetResponse();
            }

            if (logging) {
                double elapsedMs = (double) ((System.nanoTime() - before) / 100000) / 10;
                logger.info("LocalReader: Transmission", ACTION_STR, "local_reader.transmit",
                        "apduRequest", apduRequest, "apduResponse", apduResponse, "elapsedMs",
                        elapsedMs, "apduName", apduRequest.getName());
            }

            return apduResponse;
        } catch (CardException e) {
            throw new ChannelStateReaderException(e);
        }
    }

    /**
     * Execute a get response command in order to get outgoing data from specific cards answering
     * 9000 with no data although the command has outgoing data. Note that this method relies on the
     * right get response management by transmitApdu
     *
     * @return response the response to the get response command
     * @throws CardException
     * @throws ChannelStateReaderException
     */
    private ApduResponse case4HackGetResponse() throws CardException, ChannelStateReaderException {
        ByteBuffer command = ByteBuffer.allocate(5);
        // build a get response command
        // the actual length expected by the SE in the get response command is handled in
        // transmitApdu
        command.put((byte) 0x00);
        command.put((byte) 0xC0);
        command.put((byte) 0x00);
        command.put((byte) 0x00);
        command.put((byte) 0x00);

        ApduResponse response = new ApduResponse(transmitApdu(command), true);
        logger.info("Case4 hack", ACTION_STR, "local_reader.case4_hack");

        return response;
    }



    /**
     * Do the transmission of all needed requestSet requests contained in the provided requestSet
     * according to the protocol flag selection logic. The responseSet responses are returned in the
     * responseSet object. The requestSet requests are ordered at application level and the
     * responses match this order. When a requestSet is not matching the current PO, the responseSet
     * responses pushed in the responseSet object is set to null.
     *
     * @param requestSet
     * @return responseSet
     * @throws IOReaderException
     */
    private SeResponseSet transmitActual(SeRequestSet requestSet) throws IOReaderException {

        checkOrOpenPhysicalChannel();

        boolean previouslyOpen = false;
        boolean requestMatchesProtocol[] = new boolean[requestSet.getRequests().size()];
        int requestIndex = 0, lastRequestIndex;

        // Determine which requests are matching the current ATR
        for (SeRequest request : requestSet.getRequests()) {
            requestMatchesProtocol[requestIndex] = protocolFlagMatches(request.getProtocolFlag());
            requestIndex++;
        }

        // we have now an array of booleans saying whether the corresponding request and the
        // current SE match or not

        lastRequestIndex = requestIndex;
        requestIndex = 0;

        // The current requestSet is possibly made of several APDU command lists
        // If the requestMatchesProtocol is true we process the requestSet
        // If the requestMatchesProtocol is false we skip to the next requestSet
        // If keepChannelOpen is false, we close the physical channel for the last request.
        List<SeResponse> responses = new ArrayList<SeResponse>();
        for (SeRequest request : requestSet.getRequests()) {
            if (requestMatchesProtocol[requestIndex] == true) {
                responses.add(executeRequest(request, previouslyOpen));
            } else {
                // in case the protocolFlag of a SeRequest doesn't match the reader status, a
                // null SeResponse is added to the SeResponseSet.
                responses.add(null);
            }
            requestIndex++;
            if (!request.isKeepChannelOpen()) {
                if (lastRequestIndex == requestIndex) {
                    // For the processing of the last SeRequest with a protocolFlag matching
                    // the SE reader status, if the logical channel doesn't require to be kept open,
                    // then the physical channel is closed.
                    closePhysicalChannel();

                    // reset temporary properties
                    aidCurrentlySelected = null;
                    fciDataSelected = null;

                    logger.info("Closing of the physical SE channel.", ACTION_STR,
                            "local_reader.transmit_actual");
                }
            } else {
                previouslyOpen = true;
                // When keepChannelOpen is true, we stop after the first matching request
                // we exit the for loop here
                // For the processing of a SeRequest with a protocolFlag which matches the
                // current SE reader status, in case it's requested to keep the logical channel
                // open, then the other remaining SeRequest are skipped, and null
                // SeRequest are returned for them.
                break;
            }
        }
        return new SeResponseSet(responses);
    }

    /**
     * Executes a request made of one or more Apdus and receives their answers. The selection of the
     * application is handled. The methods allows decrease the cyclomatic complexity of
     * TransmitActual
     * 
     * @param request
     * @param previouslyOpen
     * @return the SeResponse to the requestS
     * @throws ChannelStateReaderException
     */
    private SeResponse executeRequest(SeRequest request, boolean previouslyOpen)
            throws ChannelStateReaderException {
        boolean executeRequest = true;
        List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();
        if (request.getAidToSelect() != null && aidCurrentlySelected == null) {
            // Opening of a logical channel with a SE application
            fciDataSelected = connect(request.getAidToSelect());
            if (fciDataSelected.getStatusCode() != 0x9000) {
                // TODO: Remark, for a Calypso PO, the status 6283h (DF invalidated) is
                // considered as successful for the Select Application command.
                logger.info("Application selection failed!", ACTION_STR,
                        "local_reader.transmit_actual");
                executeRequest = false;
            }
        } else {
            // In this case, the SE application is implicitly selected (and only one logical
            // channel is managed by the SE).
            fciDataSelected = new ApduResponse(getAlternateFci(), true);
        }

        if (executeRequest) {
            for (ApduRequest apduRequest : request.getApduRequests()) {
                apduResponseList.add(transmit(apduRequest));
            }
        }
        return new SeResponse(previouslyOpen, fciDataSelected, apduResponseList);
    }
}
