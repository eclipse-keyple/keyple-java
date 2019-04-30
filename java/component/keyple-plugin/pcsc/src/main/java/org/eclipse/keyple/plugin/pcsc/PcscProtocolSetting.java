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
package org.eclipse.keyple.plugin.pcsc;

import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocol;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocolSettingList;

/**
 * These objects are used by the application to build the SeProtocolsMap
 */
public enum PcscProtocolSetting implements SeProtocolSettingList {
    /**
     * Associates protocol names and regular expressions to match ATRs produced by PC/SC readers
     * <p>
     * To be compared with the PC/SC protocol
     */
    SETTING_PROTOCOL_ISO14443_4(SeCommonProtocol.PROTOCOL_ISO14443_4,
            "3B8880....................|3B8C800150.*|.*4F4D4141544C4153.*"),

    SETTING_PROTOCOL_B_PRIME(SeCommonProtocol.PROTOCOL_B_PRIME,
            "3B8F8001805A0A0103200311........829000.."),

    SETTING_PROTOCOL_MIFARE_UL(SeCommonProtocol.PROTOCOL_MIFARE_UL,
            "3B8F8001804F0CA0000003060300030000000068"),

    SETTING_PROTOCOL_MIFARE_CLASSIC(SeCommonProtocol.PROTOCOL_MIFARE_CLASSIC,
            "3B8F8001804F0CA000000306030001000000006A"),

    SETTING_PROTOCOL_MIFARE_DESFIRE(SeCommonProtocol.PROTOCOL_MIFARE_DESFIRE, "3B8180018080"),

    SETTING_PROTOCOL_MEMORY_ST25(SeCommonProtocol.PROTOCOL_MEMORY_ST25,
            "3B8F8001804F0CA000000306070007D0020C00B6"),

    SETTING_PROTOCOL_ISO7816_3(SeCommonProtocol.PROTOCOL_ISO7816_3, "3.*");

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
