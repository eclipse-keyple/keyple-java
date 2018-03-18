/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class SERequest. This class aggregates the elements of a request to a local or remote SE
 * Reader, sent through a ProxyReader, in order to open a logical channel with a SE application to
 * select, and to transfer a group of APDU commands to run.
 *
 * @author Ixxi
 */
public class SeRequest {

    /**
     * - AID’s bytes of the SE application to select. In case the SE application is currently not
     * selected, a logical channel is established and the corresponding SE application is selected
     * by the SE reader, otherwise keep the current channel.
     *
     * - Could be missing when operating SE which don’t support the Select Application command (as
     * it is the case for CSM).
     */
    private ByteBuffer aidToSelect;

    /**
     * the final logical channel status: if true, the SE reader keep active the logical channel of
     * the SE application after processing the group of APDU commands. If false, the SE reader will
     * close the logical channel of the SE application after processing the group of APDU commands
     * (i.e. after the receipt of the last APDU response).
     */
    private boolean keepChannelOpen;

    /**
     * contains a group of APDUCommand to operate on the selected SE application by the SE reader.
     */
    private List<ApduRequest> apduRequests;

    /**
     * the constructor called by a ProxyReader in order to open a logical channel, to send a set of
     * APDU commands to a SE application, or both of them.
     *
     * @param aidToSelect the aid to select
     * @param apduRequests the apdu requests
     * @param keepChannelOpen the keep channel open
     */
    public SeRequest(ByteBuffer aidToSelect, List<ApduRequest> apduRequests,
            boolean keepChannelOpen) {
        this.aidToSelect = aidToSelect;
        this.keepChannelOpen = keepChannelOpen;
        this.apduRequests = apduRequests;

    }

    /**
     * @param apduRequests list of APDU requests
     */
    public SeRequest(List<ApduRequest> apduRequests) {

        this.keepChannelOpen = true;
        this.apduRequests = new ArrayList<ApduRequest>();

        if (apduRequests != null) {
            this.apduRequests.addAll(apduRequests);
        }

    }

    /**
     * Gets the aid to select.
     *
     * @return the current AID set to select
     */
    public ByteBuffer getAidToSelect() {
        return aidToSelect;
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
     * Define if the channel should be kept open after the the {@link SeRequest} has been executed.
     *
     * @return If the channel should be kept open
     */
    public boolean keepChannelOpen() {
        return keepChannelOpen;
    }

}
