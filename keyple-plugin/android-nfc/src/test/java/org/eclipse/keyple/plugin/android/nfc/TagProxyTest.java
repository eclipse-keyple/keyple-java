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

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.Arrays;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.util.ByteArrayUtils;
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
    public void SetUp() throws KeypleReaderException {
        initIsoDep();
        initMifare();
        initMifareUL();
    }



    /*
     * PUBLIC METHODS
     */
    @Test
    public void getTagProxyIsoDep() throws KeypleReaderException {
        TagProxy tagProxy = TagProxy.getTagProxy(tagIso);
        assertEquals("android.nfc.tech.IsoDep", tagProxy.getTech());
    }

    @Test
    public void getTagProxyMifareClassic() throws KeypleReaderException {
        TagProxy tagProxy = TagProxy.getTagProxy(tagMifare);
        assertEquals("android.nfc.tech.MifareClassic", tagProxy.getTech());

    }

    @Test
    public void getTagProxyMifareUltralight() throws KeypleReaderException {
        TagProxy tagProxy = TagProxy.getTagProxy(tagMifareUL);
        assertEquals("android.nfc.tech.MifareUltralight", tagProxy.getTech());

    }

    @Test(expected = KeypleReaderException.class)
    public void getTagProxyNull() throws KeypleReaderException {
        Tag tag = Mockito.mock(Tag.class);
        when(tag.getTechList()).thenReturn(new String[] {"unknown tag"});
        TagProxy tagProxy = TagProxy.getTagProxy(tag);
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void getTag() throws KeypleReaderException, IOException {

        // test
        TagProxy tagProxy = TagProxy.getTagProxy(tagIso);

    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void connect() throws KeypleReaderException, IOException {
        // test
        TagProxy tagProxy = TagProxy.getTagProxy(tagIso);

        tagProxy.connect();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void close() throws KeypleReaderException, IOException {

        // test
        TagProxy tagProxy = TagProxy.getTagProxy(tagIso);

        tagProxy.close();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void isConnected() throws KeypleReaderException, IOException {

        // test
        TagProxy tagProxy = TagProxy.getTagProxy(tagIso);

        tagProxy.isConnected();
    }


    @Test(expected = Test.None.class /* no exception expected */)
    public void transceive() throws KeypleReaderException, IOException {

        TagProxy tagProxy = TagProxy.getTagProxy(tagIso);

        tagProxy.transceive("0000".getBytes());
    }


    @Test(expected = Test.None.class /* no exception expected */)
    public void getATRMifare() throws KeypleReaderException, IOException {

        TagProxy tagProxy = TagProxy.getTagProxy(tagMifare);

        Assert.assertTrue(Arrays.equals(tagProxy.getATR(),
                ByteArrayUtils.fromHex("3B8F8001804F0CA000000306030001000000006A")));
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void getATRIsodep() throws KeypleReaderException, IOException {
        // test
        TagProxy tagProxy = TagProxy.getTagProxy(tagIso);

        Assert.assertNull(tagProxy.getATR());
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void getATRMifareUL() throws KeypleReaderException, IOException {

        TagProxy tagProxy = TagProxy.getTagProxy(tagMifareUL);
        Assert.assertTrue(Arrays.equals(tagProxy.getATR(),
                ByteArrayUtils.fromHex("3B8F8001804F0CA0000003060300030000000068")));
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
