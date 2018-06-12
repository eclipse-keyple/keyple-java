/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.pcsc;

import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSettings;

/**
 * These objects are used by the application to build the SeProtocolsMap
 */
public enum PcscProtocolSetting implements SeProtocolSettings {

    SETTING_PROTOCOL_ISO14443_4(ContactlessProtocols.PROTOCOL_ISO14443_4,
            ProtocolSetting.REGEX_PROTOCOL_ISO14443_4),

    SETTING_PROTOCOL_B_PRIME(ContactlessProtocols.PROTOCOL_B_PRIME,
            ProtocolSetting.REGEX_PROTOCOL_B_PRIME),

    SETTING_PROTOCOL_MIFARE_UL(ContactlessProtocols.PROTOCOL_MIFARE_UL,
            ProtocolSetting.REGEX_PROTOCOL_MIFARE_UL),

    SETTING_PROTOCOL_MIFARE_CLASSIC(ContactlessProtocols.PROTOCOL_MIFARE_CLASSIC,
            ProtocolSetting.REGEX_PROTOCOL_MIFARE_CLASSIC),

    SETTING_PROTOCOL_MIFARE_DESFIRE(ContactlessProtocols.PROTOCOL_MIFARE_DESFIRE,
            ProtocolSetting.REGEX_PROTOCOL_DESFIRE),

    SETTING_PROTOCOL_MEMORY_ST25(ContactlessProtocols.PROTOCOL_MEMORY_ST25,
            ProtocolSetting.REGEX_PROTOCOL_MEMORY_ST25);

    /**
     * Regular expressions to match ATRs produced by PcSc readers
     */
    public interface ProtocolSetting {
        public static String REGEX_PROTOCOL_ISO14443_4 =
                "3B8880010000000000718100F9|3B8C800150........00000000007181..";

        public static String REGEX_PROTOCOL_B_PRIME = "3B8F8001805A0A0103200311........829000..";

        public static String REGEX_PROTOCOL_MIFARE_UL = "3B8F8001804F0CA0000003060300030000000068";

        public static String REGEX_PROTOCOL_MIFARE_CLASSIC =
                "3B8F8001804F0CA000000306030001000000006A";

        public static String REGEX_PROTOCOL_DESFIRE = "3B8180018080";

        public static String REGEX_PROTOCOL_MEMORY_ST25 =
                "3B8F8001804F0CA000000306070007D0020C00B6";
    }

    /* the protocol flag */
    SeProtocol flag;

    /* the protocol setting value */
    String value;

    PcscProtocolSetting(SeProtocol flag, String value) {
        this.flag = flag;
        this.value = value;
    }

    @Override
    public SeProtocol getFlag() {
        return this.flag;
    }

    @Override
    public String getValue() {
        return this.value;
    }
}
