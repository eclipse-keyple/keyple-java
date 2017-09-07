package org.keyple.seproxy;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class SERequest. This class aggregates the elements of a request to a
 * local or remote SE Reader, sent through a ProxyReader, in order to open a
 * logical channel with a SE application to select, and to transfer a group of
 * APDU commands to run.
 *
 * @author Ixxi
 */
public class SERequest {

    /**
     * AID’s bytes of the SE application to select. In case the SE application
     * is currently not selected, a logical channel is established and the
     * corresponding SE application is selected by the SE reader, otherwise keep
     * the current channel.
     */
    private byte[] aidToSelect;

    /**
     * the final logical channel status: if true, the SE reader keep active the
     * logical channel of the SE application after processing the group of APDU
     * commands. If false, the SE reader will close the logical channel of the
     * SE application after processing the group of APDU commands (i.e. after
     * the receipt of the last APDU response).
     */
    private boolean keepChannelOpen;

    /**
     * contains a group of APDUCommand to operate on the selected SE application
     * by the SE reader.
     */
    private List<APDURequest> apduRequests;

    /**
     * the constructor called by a ProxyReader in order to open a logical
     * channel, to send a set of APDU commands to a SE application, or both of
     * them.
     *
     * @param aidToSelect
     *            the aid to select
     * @param keepChannelOpen
     *            the keep channel open
     * @param apduRequests
     *            the apdu requests
     */
    public SERequest(byte[] aidToSelect, boolean keepChannelOpen, List<APDURequest> apduRequests) {
        this.aidToSelect = aidToSelect;
        this.keepChannelOpen = keepChannelOpen;
        this.apduRequests = apduRequests;

    }

    public SERequest(List<APDURequest> apduRequests) {

    	this.keepChannelOpen = true;
        this.apduRequests = new ArrayList<>();

        if (apduRequests != null) {
            this.apduRequests.addAll(apduRequests);
        }

    }

    /**
     * Gets the aid to select.
     *
     * @return the current AID set to select
     */
    public byte[] getAidToSelect() {
        return aidToSelect;
    }

    /**
     * Gets the apdu requests.
     *
     * @return the group of APDUs to be transmitted to the SE application for
     *         this instance of SERequest.
     */
    public List<APDURequest> getApduRequests() {
        return apduRequests;
    }

    /**
     * Ask keep channel open.
     *
     * @return the boolean
     */
    public boolean askKeepChannelOpen() {
        return keepChannelOpen;
    }

}