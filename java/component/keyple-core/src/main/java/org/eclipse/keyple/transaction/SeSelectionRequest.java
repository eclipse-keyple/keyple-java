/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.transaction;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.message.ApduRequest;
import org.eclipse.keyple.seproxy.message.SeRequest;
import org.eclipse.keyple.seproxy.message.SeResponse;
import org.eclipse.keyple.seproxy.protocol.SeProtocol;

/**
 * The SeSelectionRequest class combines a SeSelector with additional helper methods useful to the
 * selection process done in {@link SeSelection}.
 * <p>
 * This class may also be extended to add particular features specific to a SE family.
 */
public class SeSelectionRequest {
    protected SeSelector seSelector;

    /** optional apdu requests list to be executed following the selection process */
    protected final List<ApduRequest> seSelectionApduRequestList = new ArrayList<ApduRequest>();

    /**
     * the channelState and protocolFlag may be accessed from derived classes. Let them with the
     * protected access level.
     */
    protected final ChannelState channelState;
    protected final SeProtocol protocolFlag;

    public SeSelectionRequest(SeSelector seSelector, ChannelState channelState,
            SeProtocol protocolFlag) {
        this.seSelector = seSelector;
        this.channelState = channelState;
        this.protocolFlag = protocolFlag;
    }

    /**
     * Returns a selection SeRequest built from the information provided in the constructor and
     * possibly completed with the seSelectionApduRequestList
     *
     * @return the selection SeRequest
     */
    protected final SeRequest getSelectionRequest() {
        SeRequest seSelectionRequest = null;
        seSelectionRequest =
                new SeRequest(seSelector, seSelectionApduRequestList, channelState, protocolFlag);
        return seSelectionRequest;
    }

    public SeSelector getSeSelector() {
        return seSelector;
    }

    /**
     * Add an additional {@link ApduRequest} to be executed after the selection process if it
     * succeeds.
     * <p>
     * If more than one {@link ApduRequest} is added, all will be executed in the order in which
     * they were added.
     *
     * @param apduRequest an {@link ApduRequest}
     */
    protected final void addApduRequest(ApduRequest apduRequest) {
        seSelectionApduRequestList.add(apduRequest);
    }

    /**
     * Return the parser corresponding to the command whose index is provided.
     *
     * @param seResponse the received SeResponse containing the commands raw responses
     * @param commandIndex the command index
     * @return a parser of the type matching the command
     */
    public AbstractApduResponseParser getCommandParser(SeResponse seResponse, int commandIndex) {
        /* not yet implemented in keyple-core */
        // TODO add a generic command parser
        throw new IllegalStateException("No parsers available for this request.");
    }

    /**
     * Create a MatchingSe object containing the selection data received from the plugin
     * 
     * @param seResponse the SE response received
     * @return a {@link MatchingSe}
     */
    protected MatchingSe parse(SeResponse seResponse) {
        return new MatchingSe(seResponse, seSelector.getExtraInfo());
    }

    @Override
    public String toString() {
        // TODO
        return "";
    }
}
