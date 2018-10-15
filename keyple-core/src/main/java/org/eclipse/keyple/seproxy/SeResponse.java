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

package org.eclipse.keyple.seproxy;

import java.io.Serializable;
import java.util.List;
import org.eclipse.keyple.util.ByteArrayUtils;

/**
 * Group of SE responses received in response to a {@link SeRequest}.
 * 
 * @see SeRequest
 */
@SuppressWarnings("PMD.NPathComplexity")
public final class SeResponse implements Serializable {

    static final long serialVersionUID = 265369841119873812L;


    /**
     * is defined as true by the SE reader in case a logical channel was already open with the
     * target SE application.
     */
    private boolean channelPreviouslyOpen;

    /**
     * The SE answer to reset data
     */
    private final ApduResponse atr;

    /**
     * present if channelPreviouslyOpen is false, contains the FCI response of the channel opening
     * (either the response of a SelectApplication command, or the response of a GetData(‘FCI’)
     * command).
     */
    private ApduResponse fci;

    /**
     * could contain a group of APDUResponse returned by the selected SE application on the SE
     * reader.
     */
    private List<ApduResponse> apduResponses;

    /**
     * the constructor called by a ProxyReader during the processing of the ‘transmit’ method.
     *
     * @param channelPreviouslyOpen the channel previously open
     * @param atr the SE atr (may be null)
     * @param fci the fci data
     * @param apduResponses the apdu responses
     * @throws IllegalArgumentException if both atr and fci are null
     */
    public SeResponse(boolean channelPreviouslyOpen, ApduResponse atr, ApduResponse fci,
            List<ApduResponse> apduResponses) throws IllegalArgumentException {
        if (atr == null && fci == null) {
            throw new IllegalArgumentException("Atr and Fci can't be null at the same time.", null);
        }
        this.channelPreviouslyOpen = channelPreviouslyOpen;
        this.atr = atr;
        this.fci = fci;
        this.apduResponses = apduResponses;
    }

    /**
     * Was channel previously open.
     *
     * @return the previous state of the logical channel.
     */
    public boolean wasChannelPreviouslyOpen() {
        return channelPreviouslyOpen;
    }

    /**
     * Gets the atr data.
     *
     * @return null or the answer to reset.
     */
    public ApduResponse getAtr() {
        return this.atr;
    }

    /**
     * Gets the fci data.
     *
     * @return null or the FCI response if a channel was opened.
     */
    public ApduResponse getFci() {
        return this.fci;
    }

    /**
     * Gets the apdu responses.
     *
     * @return the group of APDUs responses returned by the SE application for this instance of
     *         SEResponse.
     */
    public List<ApduResponse> getApduResponses() {
        return apduResponses;
    }

    @Override
    public String toString() {
        return String.format("SeResponse:{RESPONSES = %s, ATR = %s, FCI =%s, CHANNELWASOPEN = %s}",
                getApduResponses(), ByteArrayUtils.toHex(getAtr().getBytes()), getFci(),
                wasChannelPreviouslyOpen());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SeResponse)) {
            return false;
        }

        SeResponse seResponse = (SeResponse) o;
        return (seResponse.getAtr() == null ? atr == null : seResponse.getAtr().equals(atr))
                && (seResponse.getFci() == null ? fci == null : seResponse.getFci().equals(fci))
                && (seResponse.getApduResponses() == null ? apduResponses == null
                        : seResponse.getApduResponses().equals(apduResponses))
                && seResponse.wasChannelPreviouslyOpen() == channelPreviouslyOpen;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + (atr == null ? 0 : atr.hashCode());
        hash = 7 * hash + (fci == null ? 0 : fci.hashCode());
        hash = 29 * hash + (apduResponses == null ? 0 : this.apduResponses.hashCode());
        hash = 13 * hash + (this.channelPreviouslyOpen ? 1 : 0);
        return hash;
    }
}
