/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy;

public enum ContactlessProtocols implements SeProtocol {

    // Contactless standard
    PROTOCOL_ISO14443_4("ISO 14443-4"), // equivalent IsoDep in Android NFC

    PROTOCOL_ISO14443_4A("ISO 14443-4 Type A"), // ISOA in Android NFC, included in
                                                // PROTOCOL_ISO14443_4

    PROTOCOL_ISO14443_4B("ISO 14443-4 Type B"), // ISOB in Android NFC, included in
                                                // PROTOCOL_ISO14443_4

    // Contactless proprietary solutions
    PROTOCOL_B_PRIME("Old Calypso B prime"),

    PROTOCOL_MIFARE_UL("Mifare Ultra Light"),

    PROTOCOL_MIFARE_1K("Mifare 1k"),

    PROTOCOL_MIFARE_CLASSIC("Mifare classic"),

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
