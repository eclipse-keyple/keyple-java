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
package org.eclipse.keyple.plugin.android.nfc;

import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocol;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocolPluginSetting;

public enum AndroidNfcProtocolSettings implements SeProtocolPluginSetting {

    SETTING_PROTOCOL_ISO14443_4(SeCommonProtocol.PROTOCOL_ISO14443_4,
            "android.nfc.tech.IsoDep"),

    SETTING_PROTOCOL_MIFARE_UL(SeCommonProtocol.PROTOCOL_MIFARE_UL,
            "android.nfc.tech.MifareUltralight"),

    SETTING_PROTOCOL_MIFARE_CLASSIC(SeCommonProtocol.PROTOCOL_MIFARE_CLASSIC,
            "android.nfc.tech.MifareClassic");

    private final SeProtocol flag;
    private final String value;

    AndroidNfcProtocolSettings(SeProtocol flag, String value) {
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
