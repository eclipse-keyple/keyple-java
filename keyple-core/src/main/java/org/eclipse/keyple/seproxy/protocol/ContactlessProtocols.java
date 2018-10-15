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

package org.eclipse.keyple.seproxy.protocol;

import org.eclipse.keyple.seproxy.SeProtocol;

public enum ContactlessProtocols implements SeProtocol {

    /** Contactless standard */
    PROTOCOL_ISO14443_4("ISO 14443-4"), // equivalent IsoDep in Android NFC

    /** Contactless proprietary solutions */
    PROTOCOL_ISO14443_3A("ISO 14443-3 Type A"),

    PROTOCOL_ISO14443_3B("ISO 14443-3 Type B"),

    PROTOCOL_B_PRIME("Old Calypso B prime"),

    /** Mifare Ultralight and Ultralight C */
    PROTOCOL_MIFARE_UL("Mifare Ultra Light"),

    /** Mifare mini, 1K, 2K, 4K */
    PROTOCOL_MIFARE_CLASSIC("Mifare Classic"),

    PROTOCOL_MIFARE_DESFIRE("Mifare DESFire"),

    PROTOCOL_MEMORY_ST25("Memory ST25");

    /** The protocol name. */
    private String name;

    ContactlessProtocols(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
