package org.eclipse.keyple.plugin.androidnfc;

import android.nfc.Tag;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

import junit.framework.Assert;

import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.util.Observable;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TagProxy.class)
public class AndroidNfcReaderTest {

    private static final ILogger logger = SLoggerFactory.getLogger(AndroidNfcReaderTest.class);

    AndroidNfcReader reader;
    Tag tag;
    TagProxy tagProxy;

    @Before
    public void SetUp() throws IOReaderException {

        reader = Mockito.spy(AndroidNfcReader.class);
        tagProxy = Mockito.mock(TagProxy.class);
        PowerMockito.mockStatic(TagProxy.class);
        when(TagProxy.getTagProxy(tag)).thenReturn(tagProxy);

    }

    @Test(expected = IOException.class)
    public void setUnknownParameter() throws IOException {
        reader.setParameter("dsfsdf","sdfdsf");
    }

    @Test
    public void setCorrectParameter() throws IOException {
        reader.setParameter(AndroidNfcReader.FLAG_READER_NO_PLATFORM_SOUNDS,"1");
        reader.setParameter(AndroidNfcReader.FLAG_READER_NO_PLATFORM_SOUNDS,"0");
        reader.setParameter(AndroidNfcReader.FLAG_READER_PRESENCE_CHECK_DELAY,"10");
        reader.setParameter(AndroidNfcReader.FLAG_READER_SKIP_NDEF_CHECK,"0");
        reader.setParameter(AndroidNfcReader.FLAG_READER_SKIP_NDEF_CHECK,"1");
        Assert.assertTrue(true);
    }

    @Test(expected = IOException.class)
    public void setUnCorrectParameter() throws IOException {
        reader.setParameter(AndroidNfcReader.FLAG_READER_NO_PLATFORM_SOUNDS,"A");
    }

    @Test(expected = IOException.class)
    public void setUnCorrectParameter2() throws IOException {
        reader.setParameter(AndroidNfcReader.FLAG_READER_SKIP_NDEF_CHECK,"2");
    }

    @Test(expected = IOException.class)
    public void setUnCorrectParameter3() throws IOException {
        reader.setParameter(AndroidNfcReader.FLAG_READER_PRESENCE_CHECK_DELAY,"-1");
    }

    @Test
    public void insertedEvent(){

        when(tagProxy.isConnected()).thenReturn(true);

        reader.addObserver(new Observable.Observer<ReaderEvent>(){
            @Override
            public void update(Observable<ReaderEvent> observable, ReaderEvent event) {
                assertEquals(ReaderEvent.SE_INSERTED, event);
            }
        });

        reader.onTagDiscovered(tag);
        assertEquals(true, reader.isSePresent());
    }





}
