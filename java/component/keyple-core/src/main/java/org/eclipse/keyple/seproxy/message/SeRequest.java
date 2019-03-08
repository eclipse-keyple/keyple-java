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
package org.eclipse.keyple.seproxy.message;

import java.io.Serializable;
import java.util.List;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.protocol.Protocol;
import org.eclipse.keyple.seproxy.protocol.SeProtocol;

/**
 * List of APDU requests that will result in a {@link SeResponse}
 * 
 * @see SeResponse
 */
public final class SeRequest implements Serializable {

    static final long serialVersionUID = 6018469841127325812L;

    /**
     * SE seSelector is either an AID or an ATR regular expression
     */
    private final SeSelector seSelector;

    /**
     * contains a group of APDUCommand to operate on the selected SE application by the SE reader.
     */
    private List<ApduRequest> apduRequests;


    /**
     * the protocol flag is used to target specific SE technologies for a given request
     */
    private SeProtocol protocolFlag = Protocol.ANY;

    /**
     * the final logical channel status: the SE reader may kept active the logical channel of the SE
     * application after processing the group of APDU commands otherwise the SE reader will close
     * the logical channel of the SE application after processing the group of APDU commands (i.e.
     * after the receipt of the last APDU response).
     */
    private ChannelState channelState;


    /**
     * The constructor called by a ProxyReader in order to open a logical channel, to send a set of
     * APDU commands to a SE application, or both of them.
     *
     * @param seSelector the SeSelector containing the selection information to process the SE
     *        selection
     * @param apduRequests a optional list of {@link ApduRequest} to execute after a successful
     *        selection process
     * @param channelState the channel management parameter allowing to close or keep the channel
     *        open after the request execution
     * @param protocolFlag the expected protocol for the SE (may be set to Protocol.ANY if no check
     *        is needed)
     */
    public SeRequest(SeSelector seSelector, List<ApduRequest> apduRequests,
            ChannelState channelState, SeProtocol protocolFlag) {
        this.seSelector = seSelector;
        this.apduRequests = apduRequests;
        this.channelState = channelState;
        this.protocolFlag = protocolFlag;
    }

    /**
     * Constructor to be used when the SE is already selected (without {@link SeSelector})
     * 
     * @param apduRequests a list of ApudRequest
     * @param channelState a flag to tell if the channel has to be closed at the end
     */
    public SeRequest(List<ApduRequest> apduRequests, ChannelState channelState) {
        this.seSelector = null;
        this.apduRequests = apduRequests;
        this.channelState = channelState;
        this.protocolFlag = Protocol.ANY;
    }


    /**
     * Gets the SE seSelector.
     *
     * @return the current SE seSelector
     */
    public SeSelector getSeSelector() {
        return seSelector;
    }

    /**
     * Gets the apdu requests.
     *
     * @return the group of APDUs to be transmitted to the SE application for this instance of
     *         SERequest.
     */
    public List<ApduRequest> getApduRequests() {
        return apduRequests;
    }

    /**
     * Define if the channel should be kept open after the the {@link SeRequestSet} has been
     * executed.
     *
     * @return If the channel should be kept open
     */
    public boolean isKeepChannelOpen() {
        return channelState == ChannelState.KEEP_OPEN;
    }

    /**
     * Gets the protocol flag of the request
     * 
     * @return protocolFlag
     */
    public SeProtocol getProtocolFlag() {
        return protocolFlag;
    }

    @Override
    public String toString() {
        return String.format("SeRequest:{REQUESTS = %s, SELECTOR = %s, KEEPCHANNELOPEN = %s}",
                getApduRequests(), getSeSelector(), channelState);
    }
}
