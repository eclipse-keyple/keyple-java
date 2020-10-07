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

import org.eclipse.keyple.core.util.ContactlessCardCommonProtocols
import org.junit.Assert
import org.junit.Test

class AndroidNfcProtocolSettingsTest {

    @Test
    fun getSpecificSettings() {
        val specificSettings = AndroidNfcProtocolSettings.getSpecificSettings(setOf(ContactlessCardCommonProtocols.ISO_14443_4.name, ContactlessCardCommonProtocols.NFC_A_ISO_14443_3A.name, ContactlessCardCommonProtocols.NFC_B_ISO_14443_3B.name))
        Assert.assertEquals(3, specificSettings.count())
        Assert.assertEquals("android.nfc.tech.IsoDep", specificSettings[ContactlessCardCommonProtocols.ISO_14443_4.name])
        Assert.assertEquals("android.nfc.tech.NfcA", specificSettings[ContactlessCardCommonProtocols.NFC_A_ISO_14443_3A.name])
        Assert.assertEquals("android.nfc.tech.NfcB", specificSettings[ContactlessCardCommonProtocols.NFC_B_ISO_14443_3B.name])
    }

    @Test
    fun getSetting() {
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.ISO_14443_4.name), "android.nfc.tech.IsoDep")
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.NFC_A_ISO_14443_3A.name), "android.nfc.tech.NfcA")
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.NFC_B_ISO_14443_3B.name), "android.nfc.tech.NfcB")
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.NFC_F_JIS_6319_4.name), "android.nfc.tech.NfcF")
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.NFC_V_ISO_15693.name), "android.nfc.tech.NfcV")
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.NFC_NDEF_TAG.name), "android.nfc.tech.Ndef")
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.NFC_NDEF_FORMATABLE.name), "android.nfc.tech.NdefFormatable")
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.NFC_BARCODE.name), "android.nfc.tech.NfcBarcode")
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.MIFARE_ULTRA_LIGHT.name), "android.nfc.tech.MifareUltralight")
        Assert.assertEquals(AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.MIFARE_CLASSIC.name), "android.nfc.tech.MifareClassic")
    }
}
