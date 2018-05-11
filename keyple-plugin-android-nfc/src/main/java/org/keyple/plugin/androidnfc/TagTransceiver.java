/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.androidnfc;

import java.io.IOException;
import java.util.Arrays;
import org.keyple.seproxy.exceptions.IOReaderException;
import android.nfc.Tag;
import android.nfc.tech.TagTechnology;

/**
 * Enhance Abstract Class for Tag with transceive method. Invoke getTagTransceiver factory method to
 * get a TagTransceiver object
 */
public abstract class TagTransceiver implements TagTechnology {


    /*
     * Transceive
     */
    abstract public byte[] transceive(byte[] data) throws IOException;

    abstract public int getMaxTransceiveLength();

    abstract public String getTech();

    static public TagTransceiver getTagTransceiver(Tag tag) throws IOReaderException {



        if (Arrays.asList(tag.getTechList()).contains("android.nfc.tech.IsoDep")) {

            return new IsoDepTransceiver(tag);
        }

        if (Arrays.asList(tag.getTechList()).contains("android.nfc.tech.MifareClassic")) {
            return new MiFareClassicTransceiver(tag);
        }

        throw new IOReaderException("Unknown tag");
    }

}
