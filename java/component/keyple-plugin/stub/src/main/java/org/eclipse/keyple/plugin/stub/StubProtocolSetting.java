/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.stub;

import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSettingList;

/**
 * These objects are used by the application to build the SeProtocolsMap
 */
public enum StubProtocolSetting implements SeProtocolSettingList {

    SETTING_PROTOCOL_ISO14443_4(ContactlessProtocols.PROTOCOL_ISO14443_4,
            ProtocolSetting.REGEX_PROTOCOL_ISO14443_4),

    SETTING_PROTOCOL_B_PRIME(ContactlessProtocols.PROTOCOL_B_PRIME,
            ProtocolSetting.REGEX_PROTOCOL_B_PRIME),

    SETTING_PROTOCOL_MIFARE_UL(ContactlessProtocols.PROTOCOL_MIFARE_UL,
            ProtocolSetting.REGEX_PROTOCOL_MIFARE_UL),

    SETTING_PROTOCOL_MIFARE_CLASSIC(ContactlessProtocols.PROTOCOL_MIFARE_CLASSIC,
            ProtocolSetting.REGEX_PROTOCOL_MIFARE_CLASSIC),

    SETTING_PROTOCOL_MIFARE_DESFIRE(ContactlessProtocols.PROTOCOL_MIFARE_DESFIRE,
            ProtocolSetting.REGEX_PROTOCOL_MIFARE_DESFIRE),

    SETTING_PROTOCOL_MEMORY_ST25(ContactlessProtocols.PROTOCOL_MEMORY_ST25,
            ProtocolSetting.REGEX_PROTOCOL_MEMORY_ST25);

    /**
     * Regular expressions to match ATRs produced by Stub readers
     * <p>
     * To be compared with the StubSE protocol
     */
    public interface ProtocolSetting {
        public static String REGEX_PROTOCOL_ISO14443_4 = "PROTOCOL_ISO14443_4";

        public static String REGEX_PROTOCOL_B_PRIME = "PROTOCOL_B_PRIME";

        public static String REGEX_PROTOCOL_MIFARE_UL = "PROTOCOL_MIFARE_UL";

        public static String REGEX_PROTOCOL_MIFARE_CLASSIC = "PROTOCOL_MIFARE_CLASSIC";

        public static String REGEX_PROTOCOL_MIFARE_DESFIRE = "PROTOCOL_MIFARE_DESFIRE";

        public static String REGEX_PROTOCOL_MEMORY_ST25 = "PROTOCOL_MEMORY_ST25";
    }

    /* the protocol flag */
    SeProtocol flag;

    /* the protocol setting value */
    String value;

    StubProtocolSetting(SeProtocol flag, String value) {
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
