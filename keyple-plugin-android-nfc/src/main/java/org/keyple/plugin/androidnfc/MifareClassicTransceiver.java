/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.androidnfc;

import java.io.IOException;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;


public class MifareClassicTransceiver extends TagTransceiver {


    private final MifareClassic tag;

    @Override
    public int getMaxTransceiveLength() {
        return tag.getMaxTransceiveLength();
    }

    @Override
    public String getTech() {
        return "android.nfc.tech.MifareClassic";
    }

    MifareClassicTransceiver(Tag tag) {
        this.tag = MifareClassic.get(tag);
    }

    @Override
    public byte[] transceive(byte[] data) throws IOException {
        return tag.transceive(data);
    }

    @Override
    public Tag getTag() {
        return tag.getTag();
    }

    @Override
    public void connect() throws IOException {
        tag.connect();
    }

    @Override
    public void close() throws IOException {
        tag.close();
    }

    @Override
    public boolean isConnected() {

        return tag.isConnected();
    }
}
