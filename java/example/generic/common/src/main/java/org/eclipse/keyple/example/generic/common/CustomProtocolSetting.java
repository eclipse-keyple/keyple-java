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
package org.eclipse.keyple.example.generic.common;

import org.eclipse.keyple.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSettingList;

/**
 * Custom protocol setting definitions to illustrate the extension of the Keyple SDK definitions
 */
public enum CustomProtocolSetting implements SeProtocolSettingList {
    CUSTOM_SETTING_PROTOCOL_B_PRIME(CustomProtocols.CUSTOM_PROTOCOL_B_PRIME,
            "3B8F8001805A0A0103200311........829000.."),

    CUSTOM_SETTING_PROTOCOL_ISO14443_4(CustomProtocols.CUSTOM_PROTOCOL_MIFARE_DESFIRE,
            "3B8180018080");

    /* the protocol flag */
    SeProtocol flag;

    /* the protocol setting value */
    String value;

    CustomProtocolSetting(SeProtocol flag, String value) {
        this.flag = flag;
        this.value = value;
    }

    @Override
    public SeProtocol getFlag() {
        return flag;
    }

    @Override
    public String getValue() {
        return value;
    }
}
