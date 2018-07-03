package org.eclipse.keyple.plugin.androidnfc;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

import junit.framework.Assert;

import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.ChannelStateReaderException;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.util.Observable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.nio.ByteBuffer;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TagProxy.class)
public class AndroidNfcPluginTest {

    private static final ILogger logger = SLoggerFactory.getLogger(AndroidNfcPluginTest.class);

    AndroidNfcPlugin plugin;


    @Before
    public void SetUp() throws IOReaderException {

        plugin = Mockito.spy(AndroidNfcPlugin.class);

    }



    /*
     * Test Parameters
     *
     */

    @Test(expected = IOException.class)
    public void setUnknownParameter() throws IOException {
    }


}
