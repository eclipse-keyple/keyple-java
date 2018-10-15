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

package org.eclipse.keyple.example.common.generic;

import org.eclipse.keyple.seproxy.SeProtocol;
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
