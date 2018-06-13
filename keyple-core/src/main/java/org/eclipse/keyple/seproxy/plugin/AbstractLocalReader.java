/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.plugin;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.smartcardio.CardException;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.AbstractObservableReader;
import org.eclipse.keyple.seproxy.exception.ChannelStateReaderException;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.InvalidMessageException;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSettings;
import org.eclipse.keyple.util.ByteBufferUtils;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

// TODO remove after refactoring this class to reduce the number of method
@SuppressWarnings("PMD.TooManyMethods")
/**
 * Manage the loop processing for SeRequest transmission in a set and for SeResponse reception in a
 * set
 */
public abstract class AbstractLocalReader extends AbstractObservableReader {

    private static final ILogger logger = SLoggerFactory.getLogger(AbstractLocalReader.class);

    private ByteBuffer aidCurrentlySelected;
    private ApduResponse fciDataSelected; // if fciDataSelected is NULL, it means that no
                                          // application is selected

    private boolean logging;
    private static final String ACTION_STR = "action"; // PMD rule AvoidDuplicateLiterals


    /**
     * Checks the presence of a physical channel. Creates one if needed, generates an exception in
     * case of failure.
     *
     * @throws IOReaderException
     */
    public abstract ByteBuffer openLogicalChannelAndSelect(ByteBuffer aid) throws IOReaderException;


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
     * Test if the current protocol matches the flag
     *
     * @param protocolFlag
     * @return true if the current protocol matches the provided protocol flag
     * @throws InvalidMessageException
     */
    public abstract boolean protocolFlagMatches(SeProtocol protocolFlag) throws IOReaderException;

    /**
     * Transmits a SeRequestSet and receives the SeResponseSet with time measurement.
     *
     * @param requestSet
     * @return responseSet
     * @throws IOReaderException
     */

    /**
     * Transmission of each APDU request
     *
     * @param apduRequest APDU request
     * @return APDU response
     * @throws ChannelStateReaderException Exception faced
     */
    public final ApduResponse processApduRequest(ApduRequest apduRequest)
            throws ChannelStateReaderException {
        ByteBuffer apduResponse;
        long before = logging ? System.nanoTime() : 0;
        try {
            ByteBuffer buffer = apduRequest.getBuffer();
            { // Sending data
              // We shouldn't have to re-use the buffer that was used to be sent but we have
              // some code that does it.
                final int posBeforeRead = buffer.position();
                apduResponse = transmitApdu(buffer);
                buffer.position(posBeforeRead);
            }

            // duplicated from org.eclipse.keyple.seproxyApduResponse.getStatusCode()
            int statusCode = apduResponse.getShort(apduResponse.limit() - 2);
            // java is signed only
            if (statusCode < 0) {
                statusCode += -2 * Short.MIN_VALUE;
            }
            boolean successfulStatus = false;
            if (statusCode == 0x9000) // TODO manage other successfull status
            {
                successfulStatus = true;
            }

            if (apduRequest.isCase4() && apduResponse.limit() == 0 && successfulStatus) {
                // a get response command is requested by the application for this Apdu
                apduResponse = case4HackGetResponse();

                // TODO - to check if the status code of the Hack GetResponse command should be
                // replaced by the status code of the original command.
            }

            if (logging) {
                double elapsedMs = (double) ((System.nanoTime() - before) / 100000) / 10;
                logger.info("LocalReader: Transmission", ACTION_STR,
                        "local_reader.processApduRequest", "apduRequest", apduRequest,
                        "apduResponse", apduResponse, "elapsedMs", elapsedMs, "apduName",
                        apduRequest.getName());
            }

            return new ApduResponse(apduResponse, successfulStatus);
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
    private ByteBuffer case4HackGetResponse() throws CardException, ChannelStateReaderException {
        // build a get response command
        // the actual length expected by the SE in the get response command is handled in
        // transmitApdu
        ByteBuffer command = ByteBufferUtils.fromHex("00C0000000");

        ByteBuffer response = transmitApdu(command);
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
    public final SeResponseSet processSeRequestSet(SeRequestSet requestSet)
            throws IOReaderException {

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
        boolean stopProcess = false;
        for (SeRequest request : requestSet.getRequests()) {

            if (!stopProcess) {
                if (requestMatchesProtocol[requestIndex] == true) {
                    responses.add(processSeRequest(request));
                } else {
                    // in case the protocolFlag of a SeRequest doesn't match the reader status, a
                    // null SeResponse is added to the SeResponseSet.
                    responses.add(null);
                }
                requestIndex++;
                if (!request.isKeepChannelOpen()) {
                    if (lastRequestIndex == requestIndex) {
                        // For the processing of the last SeRequest with a protocolFlag matching
                        // the SE reader status, if the logical channel doesn't require to be kept
                        // open,
                        // then the physical channel is closed.
                        closePhysicalChannel();

                        // reset temporary properties
                        aidCurrentlySelected = null;
                        fciDataSelected = null;

                        logger.info("Closing of the physical SE channel.", ACTION_STR,
                                "local_reader.transmit_actual");
                    }
                } else {
                    stopProcess = true;
                    // When keepChannelOpen is true, we stop after the first matching request
                    // we exit the for loop here
                    // For the processing of a SeRequest with a protocolFlag which matches the
                    // current SE reader status, in case it's requested to keep the logical channel
                    // open, then the other remaining SeRequest are skipped, and null
                    // SeRequest are returned for them.
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
    public final boolean isLogicalChannelOpen() {
        return fciDataSelected != null;
    }

    /**
     * Executes a request made of one or more Apdus and receives their answers. The selection of the
     * application is handled. The methods allows decrease the cyclomatic complexity of
     * TransmitActual
     *
     * @param seRequest
     * @return the SeResponse to the requestS
     * @throws ChannelStateReaderException
     */
    private SeResponse processSeRequest(SeRequest seRequest) throws IOReaderException {
        boolean previouslyOpen = true;
        List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();

        if (fciDataSelected == null // if no SE application is explicitely (through an AID) or
                                    // implicitely (without AID) selected
                || aidCurrentlySelected != seRequest.getAidToSelect()) { // or if selected AID is
                                                                         // different than requested
                                                                         // AID (does the
                                                                         // comparaison works if
                                                                         // both AID are null ?)
            previouslyOpen = false;

            ByteBuffer fciDataBytes = openLogicalChannelAndSelect(seRequest.getAidToSelect());

            if (fciDataBytes != null) { // the logical channel opening is successful
                if (seRequest.getAidToSelect() != null) {
                    aidCurrentlySelected = seRequest.getAidToSelect();
                }
                fciDataSelected = new ApduResponse(fciDataBytes, true);
            } else {
                logger.info("Application selection failed!", ACTION_STR,
                        "local_reader.transmit_actual");
                return null;
            }
        }

        // process ApduRequest
        for (ApduRequest apduRequest : seRequest.getApduRequests()) {
            apduResponseList.add(processApduRequest(apduRequest));
        }

        return new SeResponse(previouslyOpen, fciDataSelected, apduResponseList);
    }

    /**
     * PO selection map associating seProtocols and selection strings (e.g. ATR regex for Pcsc
     * plugins)
     */
    public Map<SeProtocol, String> protocolsMap;

    public final void addSeProtocolSetting(Map<SeProtocol, String> seProtocolSettings)
            throws IOReaderException {
        this.protocolsMap.putAll(seProtocolSettings);
    }

    public final void addSeProtocolSetting(SeProtocolSettings seProtocolSetting) {
        this.protocolsMap.put(seProtocolSetting.getFlag(), seProtocolSetting.getValue());
    }

    // TODO How to force class T to be an enum implementing SeProtocolSettings?
    public final <T extends Enum<T>> void addSeProtocolSetting(Class<T> settings) {
        for (Enum<T> setting : settings.getEnumConstants()) {
            addSeProtocolSetting((SeProtocolSettings) setting);
        }
    }
}
