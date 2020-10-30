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

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.NfcBarcode
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import java.util.Arrays
import org.eclipse.keyple.core.service.exception.KeypleReaderException
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

// @RunWith(RobolectricTestRunner.class)
class TagProxyTest {

    @MockK
    lateinit var tagIso: Tag
    @MockK
    lateinit var tagMifare: Tag
    @MockK
    lateinit var tagMifareUL: Tag
    @MockK
    lateinit var tagNfcBarcode: Tag

    @MockK
    lateinit var isoDep: IsoDep
    @MockK
    lateinit var mifare: MifareClassic
    @MockK
    lateinit var mifareUL: MifareUltralight
    @MockK
    lateinit var nfcBarcode: NfcBarcode

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        mockkStatic(IsoDep::class)
        every { tagIso.techList } returns arrayOf("android.nfc.tech.IsoDep", "android.nfc.tech.NfcB")
        every { IsoDep.get(tagIso) } returns isoDep

        mockkStatic(MifareClassic::class)
        every { tagMifare.techList } returns arrayOf("android.nfc.tech.MifareClassic", "android.nfc.tech.NfcA")
        every { MifareClassic.get(tagMifare) } returns mifare

        mockkStatic(MifareUltralight::class)
        every { tagMifareUL.techList } returns arrayOf("android.nfc.tech.MifareUltralight", "android.nfc.tech.NfcA")
        every { MifareUltralight.get(tagMifareUL) } returns mifareUL

        mockkStatic(NfcBarcode::class)
        every { tagNfcBarcode.techList } returns arrayOf("android.nfc.tech.NfcBarcode")
        every { NfcBarcode.get(tagNfcBarcode) } returns nfcBarcode
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun getTagProxyIsoDep() {
        val tagProxy = TagProxy.getTagProxy(tagIso)
        Assert.assertEquals("android.nfc.tech.IsoDep", tagProxy.tech)
    }

    @Test
    fun getTagProxyMifareClassic() {
        val tagProxy = TagProxy.getTagProxy(tagMifare)
        Assert.assertEquals("android.nfc.tech.MifareClassic", tagProxy.tech)
    }

    @Test
    fun getTagProxyMifareUltralight() {
        val tagProxy = TagProxy.getTagProxy(tagMifareUL)
        Assert.assertEquals("android.nfc.tech.MifareUltralight", tagProxy.tech)
    }

    @Test(expected = KeypleReaderException::class)
    fun getTagProxyNull() {
        val tag = mockk<Tag>()
        every { tag.techList } returns arrayOf("unknown tag")
        TagProxy.getTagProxy(tag)
    }

    @Test
    fun getTag() {
        val tagProxy = TagProxy.getTagProxy(tagIso)
        every { isoDep.tag } returns tagIso
        Assert.assertEquals(tagIso, tagProxy.tag)
    }

    @Test
    fun connect() {
        val tagProxy = TagProxy.getTagProxy(tagIso)
        tagProxy.connect() // Should no throw errors
    }

    @Test
    fun close() {
        val tagProxy = TagProxy.getTagProxy(tagIso)
        tagProxy.close() // Should no throw errors
    }

    @Test
    fun isConnected() {
        val tagProxy = TagProxy.getTagProxy(tagIso)
        every { isoDep.isConnected } returns true
        Assert.assertTrue(tagProxy.isConnected) // Should no throw errors
    }

    @Test
    fun tranceive() {
        val tagProxy = TagProxy.getTagProxy(tagIso)
        every { isoDep.transceive(any()) } returns ByteArrayUtil.fromHex("9000")
        tagProxy.transceive("0000".toByteArray()) // Should no throw errors
    }

    @Test
    fun getATRMifare() {
        val tagProxy = TagProxy.getTagProxy(tagMifare)
        Assert.assertTrue(Arrays.equals(ByteArrayUtil.fromHex("3B8F8001804F0CA000000306030001000000006A"), tagProxy.atr))
    }

    @Test
    fun getATRIsodepHiLayerNotNull() {
        val tagProxy = TagProxy.getTagProxy(tagIso)
        every { isoDep.hiLayerResponse } returns ByteArrayUtil.fromHex("3B8F8001804F0CA0000003060300030000000068")
        Assert.assertTrue(Arrays.equals(ByteArrayUtil.fromHex("3B8F8001804F0CA0000003060300030000000068"), tagProxy.atr))
    }

    @Test
    fun getATRIsodepHiLayerNull() {
        val tagProxy = TagProxy.getTagProxy(tagIso)
        every { isoDep.tag } returns tagIso
        every { isoDep.hiLayerResponse } returns null
        every { isoDep.historicalBytes } returns ByteArrayUtil.fromHex("3B8F8001804F0CA0000003060300030000000068")
        Assert.assertTrue(Arrays.equals(ByteArrayUtil.fromHex("3B8F8001804F0CA0000003060300030000000068"), tagProxy.atr))
    }

    @Test
    fun getATRMifareUL() {
        val tagProxy = TagProxy.getTagProxy(tagMifareUL)
        every { isoDep.hiLayerResponse } returns ByteArrayUtil.fromHex("3B8F8001804F0CA0000003060300030000000068")
        Assert.assertTrue(Arrays.equals(ByteArrayUtil.fromHex("3B8F8001804F0CA0000003060300030000000068"), tagProxy.atr))
    }

    @Test(expected = KeypleReaderException::class)
    fun getTagProxyUnknownTagType() {
        TagProxy.getTagProxy(tagNfcBarcode) // KeypleReaderException
    }
}
