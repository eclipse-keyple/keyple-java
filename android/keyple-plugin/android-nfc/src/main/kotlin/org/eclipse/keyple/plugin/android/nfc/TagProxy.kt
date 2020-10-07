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
import android.nfc.tech.TagTechnology
import java.io.IOException
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.core.util.ContactlessCardCommonProtocols
import org.slf4j.LoggerFactory

/**
 * Proxy Tag for [IsoDep], [MifareClassic], [MifareUltralight] Invoke
 * getTagTransceiver factory method to get a TagProxy object from a @[Tag] object
 */
internal class TagProxy private constructor(private val tagTechnology: TagTechnology, val tech: String) : TagTechnology {

    /**
     * Retrieve Answer to reset from Tag. For Isodep, getHiLayerResponse and getHiLayerResponse are
     * used to retrieve ATR. For Mifare (Classic and UL) Smartcard, a virtual ATR is returned
     * inspired by PS/SC standard 3B8F8001804F0CA000000306030001000000006A for Mifare Classic
     * 3B8F8001804F0CA0000003060300030000000068 for Mifare Ultralight
     *
     * @return
     */
    // can not happen
    val atr: ByteArray?
        @Throws(IOException::class, NoSuchElementException::class)
        get() = when (tech) {
            AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.MIFARE_CLASSIC.name) -> ByteArrayUtil.fromHex("3B8F8001804F0CA000000306030001000000006A")
            AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.MIFARE_ULTRA_LIGHT.name) -> ByteArrayUtil.fromHex("3B8F8001804F0CA0000003060300030000000068")
            AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.ISO_14443_4.name) ->
                if ((tagTechnology as IsoDep).hiLayerResponse != null)
                    tagTechnology.hiLayerResponse
                else
                    tagTechnology.historicalBytes
            else -> throw NoSuchElementException("Protocol $tech not found in plugin's settings.")
        }

    /*
     * Transceive
     */
    @Throws(IOException::class, NoSuchElementException::class)
    fun transceive(data: ByteArray): ByteArray {
        return when (tech) {
            AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.MIFARE_CLASSIC.name) -> (tagTechnology as MifareClassic).transceive(data)
            AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.MIFARE_ULTRA_LIGHT.name) -> (tagTechnology as MifareUltralight).transceive(data)
            AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.ISO_14443_4.name) -> (tagTechnology as IsoDep).transceive(data)
            else -> throw NoSuchElementException("Protocol $tech not found in plugin's settings.")
        }
    }

    override fun getTag(): Tag {
        return tagTechnology.tag
    }

    @Throws(IOException::class)
    override fun connect() {
        tagTechnology.connect()
    }

    @Throws(IOException::class)
    override fun close() {
        tagTechnology.close()
    }

    override fun isConnected(): Boolean {
        return tagTechnology.isConnected
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(TagProxy::class.java)

        /**
         * Create a TagProxy based on a [Tag]
         *
         * @param tag : tag to be proxied
         * @return tagProxy
         * @throws KeypleReaderIOException
         */
        @Throws(KeypleReaderIOException::class)
        fun getTagProxy(tag: Tag): TagProxy {

            LOG.info("Matching Tag Type : $tag")

            return tag.techList.firstOrNull {
                it == AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.MIFARE_CLASSIC.name) ||
                        it == AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.MIFARE_ULTRA_LIGHT.name) ||
                        it == AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.ISO_14443_4.name)
            }.let {
                when (it) {
                    AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.MIFARE_CLASSIC.name) -> {
                        LOG.debug("Tag embedded into MifareClassic")
                        TagProxy(MifareClassic.get(tag),
                                AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.MIFARE_CLASSIC.name))
                    }
                    AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.MIFARE_ULTRA_LIGHT.name) -> {
                        LOG.debug("Tag embedded into MifareUltralight")
                        TagProxy(MifareUltralight.get(tag),
                                AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.MIFARE_ULTRA_LIGHT.name))
                    }
                    AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.ISO_14443_4.name) -> {
                        LOG.debug("Tag embedded into IsoDep")
                        TagProxy(IsoDep.get(tag),
                                AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.ISO_14443_4.name))
                    }
                    else -> {
                        throw KeypleReaderIOException("Keyple Android Reader supports only : " +
                                AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.MIFARE_CLASSIC.name) + ", " +
                                AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.MIFARE_ULTRA_LIGHT.name) + ", " +
                                AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.ISO_14443_4.name))
                    }
                }
            }
        }
    }
}
