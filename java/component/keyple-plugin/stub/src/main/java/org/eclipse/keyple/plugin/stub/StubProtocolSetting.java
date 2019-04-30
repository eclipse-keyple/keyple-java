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

import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocol;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocolSettingList;

/**
 * These objects are used by the application to build the SeProtocolsMap
 */
public enum StubProtocolSetting implements SeProtocolSettingList {
    /**
     * Associates protocol names and regular expressions to match ATRs produced by Stub readers
     * <p>
     * To be compared with the StubSE protocol
     */
    SETTING_PROTOCOL_ISO14443_4(SeCommonProtocol.PROTOCOL_ISO14443_4, "PROTOCOL_ISO14443_4"),

    SETTING_PROTOCOL_B_PRIME(SeCommonProtocol.PROTOCOL_B_PRIME, "PROTOCOL_B_PRIME"),

    SETTING_PROTOCOL_MIFARE_UL(SeCommonProtocol.PROTOCOL_MIFARE_UL, "PROTOCOL_MIFARE_UL"),

    SETTING_PROTOCOL_MIFARE_CLASSIC(SeCommonProtocol.PROTOCOL_MIFARE_CLASSIC,
            "PROTOCOL_MIFARE_CLASSIC"),

    SETTING_PROTOCOL_MIFARE_DESFIRE(SeCommonProtocol.PROTOCOL_MIFARE_DESFIRE,
            "PROTOCOL_MIFARE_DESFIRE"),

    SETTING_PROTOCOL_MEMORY_ST25(SeCommonProtocol.PROTOCOL_MEMORY_ST25, "PROTOCOL_MEMORY_ST25"),

    SETTING_PROTOCOL_ISO7816_3(SeCommonProtocol.PROTOCOL_ISO7816_3, "PROTOCOL_ISO7816_3");

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
