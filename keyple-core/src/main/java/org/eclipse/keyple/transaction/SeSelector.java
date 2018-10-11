/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.transaction;

import java.util.*;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.SeRequest;

public class SeSelector {
    protected List<ApduRequest> seSelectionApduRequestList = new ArrayList<ApduRequest>();
    protected final static Set<Short> selectApplicationSuccessfulStatusCodes = new HashSet<Short>();
    protected final String atrRegex;
    private final boolean keepChannelOpen;
    private final byte[] seAid;
    private final boolean selectionByAid;
    private final SeProtocol protocolFlag;

    public SeSelector(String atrRegex, boolean keepChannelOpen, SeProtocol protocolFlag) {
        if (atrRegex == null || atrRegex.length() == 0) {
            throw new IllegalArgumentException("Selector ATR regex can't be null or empty");
        }
        this.atrRegex = atrRegex;
        this.keepChannelOpen = keepChannelOpen;
        seAid = null;
        selectionByAid = false;
        this.protocolFlag = protocolFlag;
    }

    public SeSelector(byte[] seAid, boolean keepChannelOpen, SeProtocol protocolFlag) {
        if (seAid == null) {
            throw new IllegalArgumentException("Selector AID can't be null");
        }
        atrRegex = null;
        this.keepChannelOpen = keepChannelOpen;
        this.seAid = seAid;
        selectionByAid = true;
        this.protocolFlag = protocolFlag;
        // TODO check if the following affirmation is true for rev2
        /**
         * with Rev2 and 3. SW=6283 in response to a selection (application invalidated) is
         * considered as successful
         */
        selectApplicationSuccessfulStatusCodes.add((short) 0x6283);
    }

    /**
     * @return the protocolFlag defined by the constructor
     */
    public SeProtocol getProtocolFlag() {
        return protocolFlag;
    }

    /**
     * Sets the list of ApduRequest to be executed following the selection operation
     * 
     * @param seSelectionApduRequestList
     */
    public void setSelectionApduRequestList(List<ApduRequest> seSelectionApduRequestList) {
        this.seSelectionApduRequestList = seSelectionApduRequestList;
    }

    /**
     * Returns a selection SeRequest built from the information provided in the constructor and
     * possibly completed with the seSelectionApduRequestList
     *
     * @return the selection SeRequest
     */
    protected SeRequest getSelectorRequest() {
        SeRequest seSelectionRequest;
        if (!selectionByAid) {
            seSelectionRequest = new SeRequest(new SeRequest.AtrSelector(atrRegex),
                    seSelectionApduRequestList, keepChannelOpen, protocolFlag);
        } else {
            seSelectionRequest = new SeRequest(new SeRequest.AidSelector(seAid),
                    seSelectionApduRequestList, keepChannelOpen, protocolFlag);
        }
        return seSelectionRequest;
    }
}
