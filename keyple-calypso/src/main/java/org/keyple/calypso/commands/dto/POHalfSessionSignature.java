/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.dto;

// TODO: Auto-generated Javadoc
/**
 * The Class POHalfSessionSignature. Half session signature return by a close secure session APDU
 * command
 */
public class POHalfSessionSignature {

    /** The value. */
    private byte[] value;

    /** The postponed data. */
    private byte[] postponedData;

    /**
     * Instantiates a new POHalfSessionSignature.
     *
     * @param value the value
     * @param postponedData the postponed data
     */
    public POHalfSessionSignature(byte[] value, byte[] postponedData) {
        super();
        this.value = (value == null) ? null : value.clone();
        this.postponedData = (postponedData == null ? null : postponedData.clone());
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public byte[] getValue() {
        if (value != null) {
            return value.clone();
        } else {
            return new byte[0];
        }
    }

    /**
     * Gets the postponed data.
     *
     * @return the postponed data
     */
    public byte[] getPostponedData() {
        if (postponedData != null) {
            return postponedData.clone();
        } else {
            return new byte[0];
        }
    }

}
