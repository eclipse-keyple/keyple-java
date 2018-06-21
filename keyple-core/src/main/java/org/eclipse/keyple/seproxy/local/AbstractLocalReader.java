/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.local;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.smartcardio.CardException;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.AbstractObservableReader;
import org.eclipse.keyple.seproxy.exception.ChannelStateReaderException;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.InvalidMessageException;
import org.eclipse.keyple.seproxy.exception.SelectApplicationException;
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
    private ApduResponse atrData;
    private boolean logging = true; // TODO make this changeable

    // TODO change the way to do the logging
    private static final String ACTION_STR = "action"; // PMD rule AvoidDuplicateLiterals
    private static final String ADPU_NAME_STR = "apdu.name";


    // protected abstract ByteBuffer[] openLogicalChannelAndSelect(ByteBuffer aid)
    // throws IOReaderException, SelectApplicationException;

    /**
     * Gets the SE Answer to reset
     * 
     * @return ATR returned by the SE or reconstructed by the reader (contactless)
     */
    protected abstract ByteBuffer getATR();

    /**
     * Tells if the physical channel is open or not
     * 
     * @return true is the channel is open
     */
    protected abstract boolean isPhysicalChannelOpen();

    /**
     * Attempts to open the physical channel
     * 
     * @throws IOReaderException
     * @throws ChannelStateReaderException
     */
    protected abstract void openPhysicalChannel()
            throws IOReaderException, ChannelStateReaderException;

    /**
     * Open (if needed) a physical channel (try to connect a card to the terminal)
     *
     * @param seRequest the current SeRequest (possibly containing the app AID and the successful
     *        status codes list)
     * @return ByteBuffer[0] the SE ATR ByteBuffer[1] the SE FCI
     * @throws IOReaderException
     */
    protected final ApduResponse[] openLogicalChannelAndSelect(SeRequest seRequest)
            throws IOReaderException, SelectApplicationException {
        ApduResponse[] atrAndFci = new ApduResponse[2];

        if (!isLogicalChannelOpen()) {
            // init of the physical SE channel: if not yet established, opening of a new physical
            // channel
            if (!isPhysicalChannelOpen()) {
                openPhysicalChannel();
            }
            if (!isPhysicalChannelOpen()) {
                throw new ChannelStateReaderException("Fail to open physical channel.");
            }
        }

        // add ATR
        atrAndFci[0] = new ApduResponse(getATR(), true);
        ByteBuffer aid = seRequest.getAidToSelect();
        if (aid != null) {
            logger.info("Connecting to card", "action", "local_reader.openLogicalChannel", "aid",
                    ByteBufferUtils.toHex(aid), "readerName", getName());
            try {
                // build a get response command
                // the actual length expected by the SE in the get response command is handled in
                // transmitApdu
                ByteBuffer selectApplicationCommand = ByteBufferUtils
                        .fromHex("00A40400" + String.format("%02X", (byte) aid.limit())
                                + ByteBufferUtils.toHex(aid) + "00");

                // we use here processApduRequest to manage case 4 hack
                // the successful status codes list for this command is provided
                atrAndFci[1] = processApduRequest(new ApduRequest(selectApplicationCommand, true,
                        seRequest.getSuccessfulSelectionStatusCodes()));

                if (!atrAndFci[1].isSuccessful()) {
                    logger.info("Application selection failed", "action",
                            "pcsc_reader.openLogicalChannel", "aid", ByteBufferUtils.toHex(aid),
                            "fci", ByteBufferUtils.toHex(atrAndFci[1].getBytes()));
                    throw new SelectApplicationException("Application selection failed");
                }
            } catch (ChannelStateReaderException e1) {
                throw new ChannelStateReaderException(e1);
            }
        }
        return atrAndFci;
    }

    /**
     * Closes the current physical channel.
     *
     * @throws IOReaderException
     */
    protected abstract void closePhysicalChannel() throws IOReaderException;

    /**
     * Transmits a single APDU and receives its response. The implementation of this abstract method
     * must handle the case where the SE response is 61xy and execute the appropriate get response
     * command
     *
     * @param apduIn byte buffer containing the ingoing data
     * @return apduResponse byte buffer containing the outgoing data.
     * @throws ChannelStateReaderException
     */
    protected abstract ByteBuffer transmitApdu(ByteBuffer apduIn)
            throws ChannelStateReaderException;

    /**
     * Test if the current protocol matches the flag
     *
     * @param protocolFlag
     * @return true if the current protocol matches the provided protocol flag
     * @throws InvalidMessageException
     */
    protected abstract boolean protocolFlagMatches(SeProtocol protocolFlag)
            throws IOReaderException;

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
    protected final ApduResponse processApduRequest(ApduRequest apduRequest)
            throws ChannelStateReaderException {
        ApduResponse apduResponse;
        long before = 0;
        if (logging) {
            logger.info("processApduRequest: request", ADPU_NAME_STR, apduRequest.getName(),
                    "command.data", ByteBufferUtils.toHex(apduRequest.getBytes()));
            before = logging ? System.nanoTime() : 0;
        }
        try {
            // TODO understand why this code and this comment???
            // Sending data
            // We shouldn't have to re-use the buffer that was used to be sent but we have
            // some code that does it.
            ByteBuffer buffer = apduRequest.getBytes();
            final int posBeforeRead = buffer.position();
            apduResponse =
                    new ApduResponse(transmitApdu(buffer), apduRequest.getSuccessfulStatusCodes());
            buffer.position(posBeforeRead);

            if (apduRequest.isCase4() && apduResponse.getDataOut().limit() == 0
                    && apduResponse.isSuccessful()) {
                apduResponse = new ApduResponse(case4HackGetResponse(), null);
                // TODO - to check if the status code of the Hack GetResponse command should be
                // replaced by the status code of the original command.
            }

            if (logging) {
                double elapsedMs = (double) ((System.nanoTime() - before) / 100000) / 10;
                logger.info("processApduRequest: response", ADPU_NAME_STR, apduRequest.getName(),
                        "response.data", ByteBufferUtils.toHex(apduResponse.getDataOut()),
                        "elapsedMs", elapsedMs);
            }
            return apduResponse;
        } catch (CardException e) {
            logger.info("processApduRequest: exception", ADPU_NAME_STR, apduRequest.getName(),
                    "response.data", "none");
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
        long before = 0;
        if (logging) {
            logger.info("case4HackGetResponse: request", ADPU_NAME_STR, "Get Response",
                    "command.data", ByteBufferUtils.toHex(command));
            before = logging ? System.nanoTime() : 0;
        }

        ByteBuffer response = transmitApdu(command);

        if (logging) {
            double elapsedMs = (double) ((System.nanoTime() - before) / 100000) / 10;
            logger.info("processApduRequest: response", ADPU_NAME_STR, "Get Response",
                    "response.data", ByteBufferUtils.toHex(response), "elapsedMs", elapsedMs);
        }
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
    protected final SeResponseSet processSeRequestSet(SeRequestSet requestSet)
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
                        // reset temporary properties
                        closeLogicalChannel();

                        // For the processing of the last SeRequest with a protocolFlag matching
                        // the SE reader status, if the logical channel doesn't require to be kept
                        // open,
                        // then the physical channel is closed.
                        closePhysicalChannel();


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
    protected final boolean isLogicalChannelOpen() {
        return fciDataSelected != null || atrData != null;
    }

    protected final void closeLogicalChannel() {
        fciDataSelected = null;
        atrData = null;
        aidCurrentlySelected = null;
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

            ApduResponse atrAndFciDataBytes[] = new ApduResponse[2];
            try {
                atrAndFciDataBytes = openLogicalChannelAndSelect(seRequest);
            } catch (SelectApplicationException e) {
                // return a null SeReponse when the opening of the logical channel failed
                return null;
            }

            if (atrAndFciDataBytes[0] != null) { // the SE Answer to reset
                atrData = atrAndFciDataBytes[0];
            }
            if (atrAndFciDataBytes[1] != null) { // the logical channel opening is successful
                aidCurrentlySelected = seRequest.getAidToSelect();
                fciDataSelected = atrAndFciDataBytes[1];
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
