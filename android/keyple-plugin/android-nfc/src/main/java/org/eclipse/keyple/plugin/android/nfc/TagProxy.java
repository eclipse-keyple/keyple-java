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

import java.io.IOException;
import java.util.Arrays;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.TagTechnology;

/**
 * Proxy Tag for {@link IsoDep}, {@link MifareClassic}, {@link MifareUltralight} Invoke
 * getTagTransceiver factory method to get a TagProxy object from a @{@link Tag} object
 */
class TagProxy implements TagTechnology {

    private static final Logger LOG = LoggerFactory.getLogger(TagProxy.class);


    private final TagTechnology tagTechnology;
    private final String tech;

    private TagProxy(TagTechnology tagTechnology, String tech) {
        this.tagTechnology = tagTechnology;
        this.tech = tech;
    }

    /*
     * Transceive
     */
    byte[] transceive(byte[] data) throws IOException {

        if (tech.equals(AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_MIFARE_CLASSIC)) {
            return ((MifareClassic) tagTechnology).transceive(data);
        } else if (tech.equals(AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_MIFARE_UL)) {
            return ((MifareUltralight) tagTechnology).transceive(data);
        } else if (tech.equals(AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_ISODEP)) {
            return ((IsoDep) tagTechnology).transceive(data);
        } else {
            return null;// can not happen
        }

    }

    String getTech() {
        return tech;
    }

    /**
     * Create a TagProxy based on a {@link Tag}
     *
     * @param tag : tag to be proxied
     * @return tagProxy
     * @throws KeypleReaderException
     */
    static TagProxy getTagProxy(Tag tag) throws KeypleReaderException {

        LOG.info("Matching Tag Type : " + tag.toString());

        if (Arrays.asList(tag.getTechList())
                .contains(AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_MIFARE_CLASSIC)) {
            LOG.debug("Tag embedded into MifareClassic");
            return new TagProxy(MifareClassic.get(tag),
                    AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_MIFARE_CLASSIC);
        }

        if (Arrays.asList(tag.getTechList())
                .contains(AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_MIFARE_UL)) {
            LOG.debug("Tag embedded into MifareUltralight");
            return new TagProxy(MifareUltralight.get(tag),
                    AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_MIFARE_UL);
        }

        if (Arrays.asList(tag.getTechList())
                .contains(AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_ISODEP)) {
            LOG.debug("Tag embedded into IsoDep");
            return new TagProxy(IsoDep.get(tag),
                    AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_ISODEP);
        }

        throw new KeypleReaderException("Keyple Android Reader supports only : "
                + AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_MIFARE_CLASSIC + ", "
                + AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_MIFARE_UL + ", "
                + AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_ISODEP

        );
    }

    /**
     * Retrieve Answer to reset from Tag. For Isodep, getHiLayerResponse and getHiLayerResponse are
     * used to retrieve ATR. For Mifare (Classic and UL) Smartcard, a virtual ATR is returned
     * inspired by PS/SC standard 3B8F8001804F0CA000000306030001000000006A for Mifare Classic
     * 3B8F8001804F0CA0000003060300030000000068 for Mifare Ultralight
     *
     * @return
     */
    byte[] getATR() {

        if (tech.equals(AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_MIFARE_CLASSIC)) {
            return ByteArrayUtils.fromHex("3B8F8001804F0CA000000306030001000000006A");
        } else if (tech.equals(AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_MIFARE_UL)) {
            return ByteArrayUtils.fromHex("3B8F8001804F0CA0000003060300030000000068");
        } else if (tech.equals(AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_ISODEP)) {
            return ((IsoDep) tagTechnology).getHiLayerResponse() != null
                    ? ((IsoDep) tagTechnology).getHiLayerResponse()
                    : ((IsoDep) tagTechnology).getHistoricalBytes();
        } else {
            return null;// can not happen
        }

    }

    @Override
    public Tag getTag() {
        return tagTechnology.getTag();
    }

    @Override
    public void connect() throws IOException {
        tagTechnology.connect();
    }

    @Override
    public void close() throws IOException {
        tagTechnology.close();
    }

    @Override
    public boolean isConnected() {
        return tagTechnology.isConnected();
    }
}
