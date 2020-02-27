package org.eclipse.keyple.plugin.android.nfc

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.util.Arrays

//@RunWith(RobolectricTestRunner.class)
class TagProxyTest {

    @MockK
    lateinit var tagIso: Tag
    @MockK
    lateinit var tagMifare: Tag
    @MockK
    lateinit var tagMifareUL: Tag

    @MockK
    lateinit var isoDep: IsoDep
    @MockK
    lateinit var mifare: MifareClassic
    @MockK
    lateinit var mifareUL: MifareUltralight

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        mockkStatic(IsoDep::class)
        every { tagIso.techList } returns arrayOf("android.nfc.tech.IsoDep", "android.nfc.tech.NfcB")
        every { IsoDep.get(tagIso)  } returns isoDep

        mockkStatic(MifareClassic::class)
        every { tagMifare.techList } returns arrayOf("android.nfc.tech.MifareClassic", "android.nfc.tech.NfcA")
        every { MifareClassic.get(tagMifare) } returns mifare

        mockkStatic(MifareUltralight::class)
        every { tagMifareUL.techList } returns arrayOf("android.nfc.tech.MifareUltralight", "android.nfc.tech.NfcA")
        every { MifareUltralight.get(tagMifareUL) } returns mifareUL
    }

    @Test
    fun getTagProxyIsoDep(){
        val tagProxy = TagProxy.getTagProxy(tagIso)
        Assert.assertEquals("android.nfc.tech.IsoDep", tagProxy.tech)
    }

    @Test
    fun getTagProxyMifareClassic(){
        val tagProxy = TagProxy.getTagProxy(tagMifare)
        Assert.assertEquals("android.nfc.tech.MifareClassic", tagProxy.tech)
    }

    @Test
    fun getTagProxyMifareUltralight(){
        val tagProxy = TagProxy.getTagProxy(tagMifareUL)
        Assert.assertEquals("android.nfc.tech.MifareUltralight", tagProxy.tech)
    }

    @Test(expected = KeypleReaderException::class)
    fun getTagProxyNull(){
        val tag = mockk<Tag>()
        every { tag.techList } returns arrayOf("unknown tag")
        TagProxy.getTagProxy(tag)
    }

    @Test
    fun getTag(){
        TagProxy.getTagProxy(tagIso)
    }

    @Test
    fun connect(){
        val tagProxy = TagProxy.getTagProxy(tagIso)
        tagProxy.connect() //Should no throw errors
    }

    @Test
    fun close(){
        val tagProxy = TagProxy.getTagProxy(tagIso)
        tagProxy.close() //Should no throw errors
    }

    @Test
    @Ignore
    fun isConnected(){
        val tagProxy = TagProxy.getTagProxy(tagIso)
        tagProxy.isConnected //Should no throw errors
    }

    @Test
    @Ignore
    fun tranceive(){
        val tagProxy = TagProxy.getTagProxy(tagIso)
        tagProxy.transceive("0000".toByteArray()) //Should no throw errors
    }

    @Test
    fun getATRMifare(){
        val tagProxy = TagProxy.getTagProxy(tagMifare)
        Assert.assertTrue(Arrays.equals(ByteArrayUtil.fromHex("3B8F8001804F0CA000000306030001000000006A"),tagProxy.atr))
    }

    @Test
    @Ignore
    fun getATRIsodep(){
        val tagProxy = TagProxy.getTagProxy(tagIso)
        Assert.assertNull(tagProxy.atr)
    }

    @Test
    fun getATRMifareUL(){
        val tagProxy = TagProxy.getTagProxy(tagMifareUL)
        Assert.assertTrue(Arrays.equals(ByteArrayUtil.fromHex("3B8F8001804F0CA0000003060300030000000068"),tagProxy.atr))
    }
}