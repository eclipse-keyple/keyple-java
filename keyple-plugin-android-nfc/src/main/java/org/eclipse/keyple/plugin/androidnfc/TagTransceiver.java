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
import org.eclipse.keyple.seproxy.exceptions.IOReaderException;
import android.nfc.Tag;
import android.nfc.tech.TagTechnology;
import android.util.Log;

/**
 * Decorate @{@link Tag} with transceive method. Invoke getTagTransceiver factory method to get a
 * TagTransceiver object from a @{@link Tag} object
 */
abstract class TagTransceiver implements TagTechnology {

    static private final String TAG = TagTransceiver.class.getSimpleName();

    /*
     * Transceive
     */
    abstract byte[] transceive(byte[] data) throws IOException;

    abstract int getMaxTransceiveLength();

    abstract String getTech();

    static TagTransceiver getTagTransceiver(Tag tag) throws IOReaderException {


        if (Arrays.asList(tag.getTechList()).contains("android.nfc.tech.MifareClassic")) {
            Log.d(TAG, "Tag embedded into MifareClassic Transceiver");
            return new MifareClassicTransceiver(tag);
        }

        if (Arrays.asList(tag.getTechList()).contains("android.nfc.tech.MifareUltralight")) {
            Log.d(TAG, "Tag embedded into MifareUltralight Transceiver");
            return new MifareUltralightTransceiver(tag);
        }

        if (Arrays.asList(tag.getTechList()).contains("android.nfc.tech.IsoDep")) {
            Log.d(TAG, "Tag embedded into IsoDep Transceiver");
            return new IsoDepTransceiver(tag);
        }

        if (Arrays.asList(tag.getTechList()).contains("android.nfc.tech.NfcA")) {
            Log.d(TAG, "Tag embedded into NfcA Transceiver");
            return new NfcATransceiver(tag);
        }

        if (Arrays.asList(tag.getTechList()).contains("android.nfc.tech.NfcB")) {
            Log.d(TAG, "Tag embedded into NfcB Transceiver");
            return new NfcBTransceiver(tag);
        }

        if (Arrays.asList(tag.getTechList()).contains("android.nfc.tech.NfcF")) {
            Log.d(TAG, "Tag embedded into NfcF Transceiver");
            return new NfcFTransceiver(tag);
        }

        if (Arrays.asList(tag.getTechList()).contains("android.nfc.tech.NfcV")) {
            Log.d(TAG, "Tag embedded into NfcV Transceiver");
            return new NfcVTransceiver(tag);
        }

        throw new IOReaderException("Unknown tag");
    }

}
