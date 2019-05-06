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

import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains all the parameters to identify the communication protocols supported by NFC
 * readers.
 * <p>
 * The application can choose to add all parameters or only a subset.
 */
public class AndroidNfcProtocolSettings {

    public static final Map<SeProtocol, String> NFC_PROTOCOL_SETTING;

    /**
     * Associates a protocol and a string defining how to identify it (here a regex to be applied on
     * the ATR)
     */
    static {

        Map<SeProtocol, String> map = new HashMap<SeProtocol, String>();

        map.put(SeCommonProtocols.PROTOCOL_ISO14443_4,
                "android.nfc.tech.IsoDep");

        map.put(SeCommonProtocols.PROTOCOL_ISO14443_3A, "android.nfc.tech.NfcA");

        map.put(SeCommonProtocols.PROTOCOL_ISO14443_3B, "android.nfc.tech.NfcB");

        map.put(SeCommonProtocols.PROTOCOL_JIS_6319_4, "android.nfc.tech.NfcF");

        map.put(SeCommonProtocols.PROTOCOL_ISO15693, "android.nfc.tech.NfcV");

        map.put(SeCommonProtocols.PROTOCOL_NDEF, "android.nfc.tech.Ndef");

        map.put(SeCommonProtocols.PROTOCOL_NDEF_FORMATABLE, "android.nfc.tech.NdefFormatable");

        map.put(SeCommonProtocols.PROTOCOL_NFC_BARCODE, "android.nfc.tech.NfcBarcode");

        map.put(SeCommonProtocols.PROTOCOL_MIFARE_UL, "android.nfc.tech.MifareUltralight");

        map.put(SeCommonProtocols.PROTOCOL_MIFARE_CLASSIC,
                "android.nfc.tech.MifareClassic");

        NFC_PROTOCOL_SETTING = Collections.unmodifiableMap(map);
    }

    /**
     * Return a subset of the settings map
     *
     * @param specificProtocols
     * @return a settings map
     */
    public static Map<SeProtocol, String> getSpecificSettings(
            EnumSet<SeCommonProtocols> specificProtocols) {
        Map<SeProtocol, String> map = new HashMap<SeProtocol, String>();
        for (SeCommonProtocols seCommonProtocols : specificProtocols) {
            map.put(seCommonProtocols, NFC_PROTOCOL_SETTING.get(seCommonProtocols));
        }
        return map;

    }

    /**
     * Return the whole settings map
     *
     * @return a settings map
     */
    public static Map<SeProtocol, String> getAllSettings() {
        return NFC_PROTOCOL_SETTING;
    }
}