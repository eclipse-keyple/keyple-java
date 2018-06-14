/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.androidnfc;

import java.io.IOException;
import java.util.Arrays;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.TagTechnology;
import android.util.Log;

/**
 * Decorate {@link Tag} with transceive method. Invoke getTagTransceiver factory method to get a
 * TagProxy object from a @{@link Tag} object
 */
class TagProxy implements TagTechnology {

    static private final String TAG = TagProxy.class.getSimpleName();

    private TagTechnology tagTechnology;
    private String tech;

    TagProxy(TagTechnology tagTechnology, String tech){
        this.tagTechnology = tagTechnology;
        this.tech = tech;
    }
    /*
     * Transceive
     */
    protected byte[] transceive(byte[] data) throws IOException{

        if(tech.equals(AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_MIFARE_CLASSIC)){
            return ((MifareClassic) tagTechnology).transceive(data);
        } else
        if(tech.equals(AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_MIFARE_UL)){
            return ((MifareUltralight) tagTechnology).transceive(data);
        } else
        if(tech.equals(AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_ISODEP)){
            return ((IsoDep) tagTechnology).transceive(data);
        }else{
            return null;//can not happen
        }

    };

    protected String getTech(){
        return tech;
    };

    static TagProxy getTagProxy(Tag tag) throws IOReaderException {

        Log.i(TAG, "Matching Tag Type : " + tag.toString());

        if (Arrays.asList(tag.getTechList())
                .contains(AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_MIFARE_CLASSIC)) {
            Log.d(TAG, "Tag embedded into MifareClassic");
            return new TagProxy(MifareClassic.get(tag), AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_MIFARE_CLASSIC);
        }

        if (Arrays.asList(tag.getTechList())
                .contains(AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_MIFARE_UL)) {
            Log.d(TAG, "Tag embedded into MifareUltralight");
            return new TagProxy(MifareUltralight.get(tag), AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_MIFARE_UL);
        }

        if (Arrays.asList(tag.getTechList())
                .contains(AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_ISODEP)) {
            Log.d(TAG, "Tag embedded into IsoDep");
            return new TagProxy(IsoDep.get(tag), AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_ISODEP);
        }

        throw new IOReaderException("Keyple Android Reader supports only : "
                + AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_MIFARE_CLASSIC + ", "
                + AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_MIFARE_UL + ", "
                + AndroidNfcProtocolSettings.ProtocolSetting.NFC_TAG_TYPE_ISODEP

        );
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
