package org.keyple.plugin.androidnfc;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.TagTechnology;
import android.util.Log;

import org.keyple.seproxy.exceptions.IOReaderException;

import java.io.IOException;
import java.util.Arrays;

/**
* Enhance Abstract Class for Tag with transceive method. Invoke getTagTransceiver factory method to
 * get a TagTransceiver object
 */
public abstract class TagTransceiver implements TagTechnology {


    /*
    Transceive
     */
    abstract public byte[] transceive(byte[] data) throws IOException;

    abstract public int getMaxTransceiveLength();

    abstract public String getTech();

    static public TagTransceiver getTagTransceiver(Tag tag) throws IOReaderException{

        if(Arrays.asList(tag.getTechList()).contains("android.nfc.tech.IsoDep")) {

            return new IsoDepTransceiver(tag);
        }

        if(Arrays.asList(tag.getTechList()).contains("android.nfc.tech.MifareClassic")) {
            return new MiFareClassicTransceiver(tag);
        }

        throw  new IOReaderException("Unknown tag");
    }

}
