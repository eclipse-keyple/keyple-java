/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.androidnfc;

import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSettings;

public enum AndroidNfcProtocolSettings implements SeProtocolSettings {

    SETTING_PROTOCOL_ISO14443_4(ContactlessProtocols.PROTOCOL_ISO14443_4,
            ProtocolSetting.NFC_TAG_TYPE_ISODEP), SETTING_PROTOCOL_MIFARE_UL(
                    ContactlessProtocols.PROTOCOL_MIFARE_UL,
                    ProtocolSetting.NFC_TAG_TYPE_MIFARE_UL), SETTING_PROTOCOL_MIFARE_CLASSIC(
                            ContactlessProtocols.PROTOCOL_MIFARE_CLASSIC,
                            ProtocolSetting.NFC_TAG_TYPE_MIFARE_CLASSIC);

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

    public interface ProtocolSetting {

        String NFC_TAG_TYPE_ISODEP = "android.nfc.tech.IsoDep";

        String NFC_TAG_TYPE_MIFARE_UL = "android.nfc.tech.MifareUltralight";

        String NFC_TAG_TYPE_MIFARE_CLASSIC = "android.nfc.tech.MifareClassic";



    }
}
