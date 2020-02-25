/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.android.nfc

import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol
import java.util.EnumSet

/**
 * This class contains all the parameters to identify the communication protocols supported by NFC
 * readers.
 *
 *
 * The application can choose to add all parameters or only a subset.
 */
object AndroidNfcProtocolSettings {

    /**
     * Associates a protocol and a string defining how to identify it (here a regex to be applied on
     * the ATR)
     */
    private val allSettings: Map<SeProtocol, String> = mapOf(
        SeCommonProtocols.PROTOCOL_ISO14443_4 to "android.nfc.tech.IsoDep",
        SeCommonProtocols.PROTOCOL_ISO14443_3A to "android.nfc.tech.NfcA",
        SeCommonProtocols.PROTOCOL_ISO14443_3B to "android.nfc.tech.NfcB",
        SeCommonProtocols.PROTOCOL_JIS_6319_4 to "android.nfc.tech.NfcF",
        SeCommonProtocols.PROTOCOL_ISO15693 to "android.nfc.tech.NfcV",
        SeCommonProtocols.PROTOCOL_NDEF to "android.nfc.tech.Ndef",
        SeCommonProtocols.PROTOCOL_NDEF_FORMATABLE to "android.nfc.tech.NdefFormatable",
        SeCommonProtocols.PROTOCOL_NFC_BARCODE to "android.nfc.tech.NfcBarcode",
        SeCommonProtocols.PROTOCOL_MIFARE_UL to "android.nfc.tech.MifareUltralight",
        SeCommonProtocols.PROTOCOL_MIFARE_CLASSIC to "android.nfc.tech.MifareClassic")

    /**
     * Return a subset of the settings map
     *
     * @param specificProtocols
     * @return a settings map
     */
    @Throws(NoSuchElementException::class)
    fun getSpecificSettings(specificProtocols: EnumSet<SeCommonProtocols>):
            Map<SeProtocol, String> {
        return specificProtocols.filter { allSettings[it] != null }.associateBy({ it }, { getSetting(it) })
    }

    /**
     * Associates a protocol and a string defining how to identify it (here a regex to be applied on
     * the ATR)
     *
     * @return a settings map
     * @throws No such Element Exception if protocol not found in settings
     */
    @Throws(NoSuchElementException::class)
    fun getSetting(seProtocol: SeProtocol): String {
        return allSettings.getValue(seProtocol)
    }
}
