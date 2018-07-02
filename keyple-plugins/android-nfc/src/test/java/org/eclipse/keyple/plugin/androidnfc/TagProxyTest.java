package org.eclipse.keyple.plugin.androidnfc;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(PowerMockRunner.class)
public class TagProxyTest {

    private static final ILogger logger = SLoggerFactory.getLogger(TagProxyTest.class);

    Tag tag;

    @Before
    public void SetUp() throws IOReaderException {
        tag = Mockito.mock(Tag.class);
    }

    @Test
    public void getTagProxyIsoDep() throws IOReaderException {
        Mockito.when(tag.getTechList()).thenReturn(new String[]{"android.nfc.tech.IsoDep", "android.nfc.tech.NfcB"});
        TagProxy tagProxy= TagProxy.getTagProxy(tag);
        assertEquals("android.nfc.tech.IsoDep", tagProxy.getTech());

    }

    @Test
    public void getTagProxyMifareClassic() throws IOReaderException {
        Mockito.when(tag.getTechList()).thenReturn(new String[]{"android.nfc.tech.MifareClassic", "android.nfc.tech.NfcA"});
        TagProxy tagProxy= TagProxy.getTagProxy(tag);
        assertEquals("android.nfc.tech.MifareClassic", tagProxy.getTech());

    }

    @Test
    public void getTagProxyMifareUltralight() throws IOReaderException {
        Mockito.when(tag.getTechList()).thenReturn(new String[]{"android.nfc.tech.MifareUltralight", "android.nfc.tech.NfcA"});
        TagProxy tagProxy= TagProxy.getTagProxy(tag);
        assertEquals("android.nfc.tech.MifareUltralight", tagProxy.getTech());

    }

    @Test(expected = IOReaderException.class)
    public void getTagProxyNull()  throws IOReaderException {
        Tag tag = Mockito.mock(Tag.class);
        Mockito.when(tag.getTechList()).thenReturn(new String[]{"unknown tag"});
        TagProxy tagProxy= TagProxy.getTagProxy(tag);
    }


}
