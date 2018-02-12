/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.dto;

/**
 * The Class AID. AID: Application Identifier as defined in ISO/IEC 7816-4. Value unique in a
 * portable object, allowing to unambiguously identify an application.
 */
public class AID {

    /** The value. */
    private byte[] value;

    /**
     * Instantiates a new AID.
     *
     * @param value the byte value
     */
    public AID(byte[] value) {
        this.value = (value == null ? null : value.clone());
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public byte[] getValue() {
        return value.clone();
    }

}
