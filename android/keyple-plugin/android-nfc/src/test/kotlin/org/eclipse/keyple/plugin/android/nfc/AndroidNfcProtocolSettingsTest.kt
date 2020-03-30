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

import java.util.EnumSet
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols
import org.junit.Assert
import org.junit.Test

class AndroidNfcProtocolSettingsTest {

    @Test
    fun getSpecificSettings() {
        val specificSettings = AndroidNfcProtocolSettings.getSpecificSettings(EnumSet.of(SeCommonProtocols.PROTOCOL_ISO14443_4, SeCommonProtocols.PROTOCOL_ISO14443_3A, SeCommonProtocols.PROTOCOL_ISO14443_3B))
        Assert.assertEquals(3, specificSettings.count())
        Assert.assertEquals("android.nfc.tech.IsoDep", specificSettings[SeCommonProtocols.PROTOCOL_ISO14443_4])
        Assert.assertEquals("android.nfc.tech.NfcA", specificSettings[SeCommonProtocols.PROTOCOL_ISO14443_3A])
        Assert.assertEquals("android.nfc.tech.NfcB", specificSettings[SeCommonProtocols.PROTOCOL_ISO14443_3B])
    }

    @Test
    fun getSetting() {
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_ISO14443_4), "android.nfc.tech.IsoDep")
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_ISO14443_3A), "android.nfc.tech.NfcA")
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_ISO14443_3B), "android.nfc.tech.NfcB")
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_JIS_6319_4), "android.nfc.tech.NfcF")
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_ISO15693), "android.nfc.tech.NfcV")
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_NDEF), "android.nfc.tech.Ndef")
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_NDEF_FORMATABLE), "android.nfc.tech.NdefFormatable")
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_NFC_BARCODE), "android.nfc.tech.NfcBarcode")
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_MIFARE_UL), "android.nfc.tech.MifareUltralight")
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_MIFARE_CLASSIC), "android.nfc.tech.MifareClassic")
    }
}
