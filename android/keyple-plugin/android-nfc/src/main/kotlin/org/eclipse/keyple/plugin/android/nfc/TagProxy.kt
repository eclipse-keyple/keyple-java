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
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols
import org.eclipse.keyple.core.util.ByteArrayUtil
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
            AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_MIFARE_CLASSIC) -> ByteArrayUtil.fromHex("3B8F8001804F0CA000000306030001000000006A")
            AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_MIFARE_UL) -> ByteArrayUtil.fromHex("3B8F8001804F0CA0000003060300030000000068")
            AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_ISO14443_4) ->
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
            AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_MIFARE_CLASSIC) -> (tagTechnology as MifareClassic).transceive(data)
            AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_MIFARE_UL) -> (tagTechnology as MifareUltralight).transceive(data)
            AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_ISO14443_4) -> (tagTechnology as IsoDep).transceive(data)
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
         * @throws KeypleReaderException
         */
        @Throws(KeypleReaderException::class)
        fun getTagProxy(tag: Tag): TagProxy {

            LOG.info("Matching Tag Type : $tag")

            return tag.techList.firstOrNull {
                it == AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_MIFARE_CLASSIC) ||
                        it == AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_MIFARE_UL) ||
                        it == AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_ISO14443_4)
            }.let {
                when (it) {
                    AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_MIFARE_CLASSIC) -> {
                        LOG.debug("Tag embedded into MifareClassic")
                        TagProxy(MifareClassic.get(tag),
                                AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_MIFARE_CLASSIC))
                    }
                    AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_MIFARE_UL) -> {
                        LOG.debug("Tag embedded into MifareUltralight")
                        TagProxy(MifareUltralight.get(tag),
                                AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_MIFARE_UL))
                    }
                    AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_ISO14443_4) -> {
                        LOG.debug("Tag embedded into IsoDep")
                        TagProxy(IsoDep.get(tag),
                                AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_ISO14443_4))
                    }
                    else -> {
                        throw KeypleReaderException("Keyple Android Reader supports only : " +
                                AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_MIFARE_CLASSIC) + ", " +
                                AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_MIFARE_UL) + ", " +
                                AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_ISO14443_4))
                    }
                }
            }
        }
    }
}
