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
package org.eclipse.keyple.core.seproxy.protocol;

import static org.eclipse.keyple.core.seproxy.protocol.TransmissionMode.*;

public enum SeCommonProtocols implements SeProtocol {

    /* ---- contactless standard / NFC compliant ------------- */
    PROTOCOL_ISO14443_4("ISO 14443-4", CONTACTLESS),

    PROTOCOL_ISO15693("ISO 15693 Type V", CONTACTLESS),

    /* ---- contactless proprietary NFC compliant ------------ */
    PROTOCOL_ISO14443_3A("ISO 14443-3 Type A", CONTACTLESS),

    PROTOCOL_ISO14443_3B("ISO 14443-3 Type B", CONTACTLESS),

    PROTOCOL_JIS_6319_4("JIS 6319-4 Felica", CONTACTLESS),

    PROTOCOL_NDEF("NFC NDEF TAG", CONTACTLESS),

    PROTOCOL_NDEF_FORMATABLE("NFC NDEF FORMATABLE", CONTACTLESS),

    PROTOCOL_NFC_BARCODE("NFC BARCODE", CONTACTLESS),

    PROTOCOL_MIFARE_UL("Mifare Ultra Light", CONTACTLESS),

    PROTOCOL_MIFARE_CLASSIC("Mifare Classic", CONTACTLESS),

    PROTOCOL_MIFARE_DESFIRE("Mifare Desfire", CONTACTLESS),

    /* ---- contactless proprietary not NFC compliant -------- */
    PROTOCOL_B_PRIME("Old Calypso B Prime", CONTACTLESS),

    PROTOCOL_MEMORY_ST25("Memory ST25", CONTACTLESS),

    /* ---- contacts ISO standard ---------------------------- */
    PROTOCOL_ISO7816_3("ISO 7816-3", CONTACTS),

    /* ---- contacts proprietary ---------------- */
    PROTOCOL_HSP("Old Calypso SAM HSP", CONTACTS);

    private final String name;
    private final TransmissionMode transmissionMode;

    /**
     * Constructor
     */
    SeCommonProtocols(String name, TransmissionMode transmissionMode) {
        this.name = name;
        this.transmissionMode = transmissionMode;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TransmissionMode getTransmissionMode() {
        return transmissionMode;
    }
}
