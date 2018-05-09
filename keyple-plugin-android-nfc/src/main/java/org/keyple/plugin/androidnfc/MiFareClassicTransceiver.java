package org.keyple.plugin.androidnfc;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;

import java.io.IOException;

/**
 * Created by bonitasoft on 09/05/2018.
 */

public class MiFareClassicTransceiver extends TagTransceiver {


    private MifareClassic tag;

    @Override
    public int getMaxTransceiveLength() {
        return tag.getMaxTransceiveLength();
    }

    @Override
    public String getTech() {
        return "android.nfc.tech.MifareClassic";
    }

    public MiFareClassicTransceiver(Tag tag) {
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
        tag.connect();
    }

    @Override
    public boolean isConnected() {
        return tag.isConnected();
    }
}
