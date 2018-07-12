/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.android.nfc;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;
import java.io.IOException;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import junit.framework.Assert;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IsoDep.class, MifareUltralight.class, MifareClassic.class})
public class TagProxyTest {


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    Tag tagIso;
    Tag tagMifare;
    Tag tagMifareUL;


    IsoDep isoDep;
    MifareClassic mifare;
    MifareUltralight mifareUL;

    // init before each test
    @Before
    public void SetUp() throws IOReaderException {
        initIsoDep();
        initMifare();
        initMifareUL();
    }



    /*
     * PUBLIC METHODS
     */
    @Test
    public void getTagProxyIsoDep() throws IOReaderException {
        TagProxy tagProxy = TagProxy.getTagProxy(tagIso);
        assertEquals("android.nfc.tech.IsoDep", tagProxy.getTech());
    }

    @Test
    public void getTagProxyMifareClassic() throws IOReaderException {
        TagProxy tagProxy = TagProxy.getTagProxy(tagMifare);
        assertEquals("android.nfc.tech.MifareClassic", tagProxy.getTech());

    }

    @Test
    public void getTagProxyMifareUltralight() throws IOReaderException {
        TagProxy tagProxy = TagProxy.getTagProxy(tagMifareUL);
        assertEquals("android.nfc.tech.MifareUltralight", tagProxy.getTech());

    }

    @Test(expected = IOReaderException.class)
    public void getTagProxyNull() throws IOReaderException {
        Tag tag = Mockito.mock(Tag.class);
        when(tag.getTechList()).thenReturn(new String[] {"unknown tag"});
        TagProxy tagProxy = TagProxy.getTagProxy(tag);
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void getTag() throws IOReaderException {

        // test
        TagProxy tagProxy = TagProxy.getTagProxy(tagIso);

    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void connect() throws IOException {
        // test
        TagProxy tagProxy = TagProxy.getTagProxy(tagIso);

        tagProxy.connect();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void close() throws IOException {

        // test
        TagProxy tagProxy = TagProxy.getTagProxy(tagIso);

        tagProxy.close();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void isConnected() throws IOException {

        // test
        TagProxy tagProxy = TagProxy.getTagProxy(tagIso);

        tagProxy.isConnected();
    }


    @Test(expected = Test.None.class /* no exception expected */)
    public void transceive() throws IOException {

        TagProxy tagProxy = TagProxy.getTagProxy(tagIso);

        tagProxy.transceive("0000".getBytes());
    }


    @Test(expected = Test.None.class /* no exception expected */)
    public void getATRMifare() throws IOException {

        TagProxy tagProxy = TagProxy.getTagProxy(tagMifare);

        Assert.assertNull(tagProxy.getATR());
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void getATRIsodep() throws IOException {
        // test
        TagProxy tagProxy = TagProxy.getTagProxy(tagIso);

        Assert.assertNull(tagProxy.getATR());
    }


    /*
     * HELPERS
     */

    void initIsoDep() {
        PowerMockito.mockStatic(IsoDep.class);
        isoDep = Mockito.mock(IsoDep.class);
        tagIso = Mockito.mock(Tag.class);

        when(tagIso.getTechList())
                .thenReturn(new String[] {"android.nfc.tech.IsoDep", "android.nfc.tech.NfcB"});
        when(IsoDep.get(tagIso)).thenReturn(isoDep);


    }

    void initMifare() {
        PowerMockito.mockStatic(MifareClassic.class);
        mifare = Mockito.mock(MifareClassic.class);
        tagMifare = Mockito.mock(Tag.class);


        when(MifareClassic.get(tagMifare)).thenReturn(mifare);
        when(tagMifare.getTechList()).thenReturn(
                new String[] {"android.nfc.tech.MifareClassic", "android.nfc.tech.NfcA"});


    }

    void initMifareUL() {
        PowerMockito.mockStatic(MifareUltralight.class);
        tagMifareUL = Mockito.mock(Tag.class);
        mifareUL = Mockito.mock(MifareUltralight.class);

        when(tagMifareUL.getTechList()).thenReturn(
                new String[] {"android.nfc.tech.MifareUltralight", "android.nfc.tech.NfcA"});
        when(MifareUltralight.get(tagMifareUL)).thenReturn(mifareUL);


    }



}
