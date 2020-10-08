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

import org.junit.Assert
import org.junit.Test

class AndroidNfcProtocolSettingsTest {

    @Test
    fun getSpecificSettings() {
        val specificSettings = AndroidNfcProtocolSettings.getSpecificSettings(setOf(AndroidNfcSupportedProtocols.ISO_14443_4.name))
        Assert.assertEquals(1, specificSettings.count())
        Assert.assertEquals("android.nfc.tech.IsoDep", specificSettings[AndroidNfcSupportedProtocols.ISO_14443_4.name])
//        Assert.assertEquals("android.nfc.tech.NfcA", specificSettings[AndroidNfcSupportedProtocols.NFC_A_ISO_14443_3A.name])
//        Assert.assertEquals("android.nfc.tech.NfcB", specificSettings[AndroidNfcSupportedProtocols.NFC_B_ISO_14443_3B.name])
    }

    @Test
    fun getSetting() {
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(AndroidNfcSupportedProtocols.ISO_14443_4.name), "android.nfc.tech.IsoDep")
//        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(AndroidNfcSupportedProtocols.NFC_A_ISO_14443_3A.name), "android.nfc.tech.NfcA")
//        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(AndroidNfcSupportedProtocols.NFC_B_ISO_14443_3B.name), "android.nfc.tech.NfcB")
//        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(AndroidNfcSupportedProtocols.NFC_F_JIS_6319_4.name), "android.nfc.tech.NfcF")
//        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(AndroidNfcSupportedProtocols.NFC_V_ISO_15693.name), "android.nfc.tech.NfcV")
//        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(AndroidNfcSupportedProtocols.NFC_NDEF_TAG.name), "android.nfc.tech.Ndef")
//        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(AndroidNfcSupportedProtocols.NFC_NDEF_FORMATABLE.name), "android.nfc.tech.NdefFormatable")
//        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(AndroidNfcSupportedProtocols.NFC_BARCODE.name), "android.nfc.tech.NfcBarcode")
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(AndroidNfcSupportedProtocols.MIFARE_ULTRA_LIGHT.name), "android.nfc.tech.MifareUltralight")
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(AndroidNfcSupportedProtocols.MIFARE_CLASSIC.name), "android.nfc.tech.MifareClassic")
    }
}
