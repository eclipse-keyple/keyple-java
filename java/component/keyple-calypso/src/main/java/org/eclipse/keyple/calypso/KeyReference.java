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
package org.eclipse.keyple.calypso;

/**
 * The KeyReference class groups all information about a Calypso key
 */
public class KeyReference {
    /** key identifier */
    private final byte kif;
    /* key version */
    private final byte kvc;

    /* Constructor */
    public KeyReference(byte kif, byte kvc) {
        this.kif = kif;
        this.kvc = kvc;
    }

    /**
     * @return the key identifier
     */
    public byte getKif() {
        return kif;
    }

    /**
     * @return the key version
     */
    public byte getKvc() {
        return kvc;
    }
}
