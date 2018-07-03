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
import org.eclipse.keyple.seproxy.protocol.SeProtocolSettings;
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
public class AndroidNfcReaderTest {

    private static final ILogger logger = SLoggerFactory.getLogger(AndroidNfcReaderTest.class);

    AndroidNfcReader reader;
    Tag tag;
    TagProxy tagProxy;
    Intent intent;

    @Before
    public void SetUp() throws IOReaderException {

        reader = Mockito.spy(AndroidNfcReader.class);
        tagProxy = Mockito.mock(TagProxy.class);
        intent = Mockito.mock(Intent.class);
        PowerMockito.mockStatic(TagProxy.class);
        when(TagProxy.getTagProxy(tag)).thenReturn(tagProxy);

    }



    /*
     * Test Parameters
     *
     */

    @Test(expected = IOException.class)
    public void setUnknownParameter() throws IOException {
        reader.setParameter("dsfsdf", "sdfdsf");
    }



    @Test
    public void setCorrectParameter() throws IOException {
        reader.setParameter(AndroidNfcReader.FLAG_READER_NO_PLATFORM_SOUNDS, "1");
        Assert.assertEquals(0 | NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS, reader.getFlags());
        reader.setParameter(AndroidNfcReader.FLAG_READER_NO_PLATFORM_SOUNDS, "0");
        Assert.assertEquals(0, reader.getFlags());
        reader.setParameter(AndroidNfcReader.FLAG_READER_SKIP_NDEF_CHECK, "1");
        Assert.assertEquals(0 | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, reader.getFlags());
        reader.setParameter(AndroidNfcReader.FLAG_READER_SKIP_NDEF_CHECK, "0");
        Assert.assertEquals(0, reader.getFlags());
        reader.setParameter(AndroidNfcReader.FLAG_READER_PRESENCE_CHECK_DELAY, "10");
        Assert.assertEquals(10, reader.getOptions().get(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY));
        Assert.assertEquals(3,reader.getParameters().size());

    }

    @Test(expected = IOException.class)
    public void setUnCorrectParameter() throws IOException {
        reader.setParameter(AndroidNfcReader.FLAG_READER_NO_PLATFORM_SOUNDS, "A");
    }

    @Test(expected = IOException.class)
    public void setUnCorrectParameter2() throws IOException {
        reader.setParameter(AndroidNfcReader.FLAG_READER_SKIP_NDEF_CHECK, "2");
    }

    @Test(expected = IOException.class)
    public void setUnCorrectParameter3() throws IOException {
        reader.setParameter(AndroidNfcReader.FLAG_READER_PRESENCE_CHECK_DELAY, "-1");
    }

    @Test
    public void setIsoDepProtocol() {
        reader.addSeProtocolSetting(AndroidNfcProtocolSettings.SETTING_PROTOCOL_ISO14443_4);
        assertEquals(0 | NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_NFC_A, reader.getFlags());
    }

    @Test
    public void setMifareProtocol() {
        reader.addSeProtocolSetting(AndroidNfcProtocolSettings.SETTING_PROTOCOL_MIFARE_CLASSIC);
        assertEquals(0 | NfcAdapter.FLAG_READER_NFC_A, reader.getFlags());
    }

    @Test
    public void setMifareULProtocol() {
        reader.addSeProtocolSetting(AndroidNfcProtocolSettings.SETTING_PROTOCOL_MIFARE_UL);
        assertEquals(0 | NfcAdapter.FLAG_READER_NFC_A, reader.getFlags());
    }

    /*
     * Test link between Android NFC Reader and Tag Proxy

     */


    @Test
    public void insertEvent() {

        //when(tagProxy.isConnected()).thenReturn(true);
        reader.addObserver(new Observable.Observer<ReaderEvent>() {
            @Override
            public void update(Observable<ReaderEvent> observable, ReaderEvent event) {
                assertEquals(ReaderEvent.SE_INSERTED, event);
            }
        });

        insertSe();
    }

    @Test
    public void isConnected() {
        insertSe();
        when(tagProxy.isConnected()).thenReturn(true);
        assertEquals(true, reader.isSePresent());
    }


    @Test
    public void getATR() {
        insertSe();
        byte[] atr = new byte[]{(byte) 0x90, 0x00};
        when(tagProxy.getATR()).thenReturn(atr);
        assertEquals(atr, reader.getATR().array());
    }

    @Test
    public void isPhysicalChannelOpen() {
        insertSe();
        when(tagProxy.isConnected()).thenReturn(true);
        assertEquals(true, reader.isPhysicalChannelOpen());
    }

    @Test
    public void openPhysicalChannelSuccess() throws IOReaderException {
        insertSe();
        when(tagProxy.isConnected()).thenReturn(false);
        reader.openPhysicalChannel();
    }

    @Test(expected = ChannelStateReaderException.class)
    public void openPhysicalChannelError() throws IOException {
        //init
        insertSe();
        when(tagProxy.isConnected()).thenReturn(false);
        doThrow(new IOException()).when(tagProxy).connect();

        //test
        reader.openPhysicalChannel();
    }

    @Test
    public void closePhysicalChannelSuccess() throws IOReaderException {
        //init
        insertSe();
        when(tagProxy.isConnected()).thenReturn(true);

        //test
        reader.closePhysicalChannel();

        //no exception

    }

    @Test(expected = ChannelStateReaderException.class)
    public void closePhysicalChannelError() throws IOException {
        //init
        insertSe();
        when(tagProxy.isConnected()).thenReturn(true);
        doThrow(new IOException()).when(tagProxy).close();

        //test
        reader.closePhysicalChannel();

        //throw exception

    }

    @Test
    public void transmitAPDUSuccess() throws IOException {
        //init
        insertSe();
        byte[] in = new byte[]{(byte) 0x90, 0x00};
        byte[] out = new byte[]{(byte) 0x90, 0x00};
        when(tagProxy.transceive(in)).thenReturn(out);

        //test
        ByteBuffer outBB = reader.transmitApdu(ByteBuffer.wrap(in));

        //assert
        Assert.assertEquals(ByteBuffer.wrap(out), outBB);

    }

    @Test(expected = ChannelStateReaderException.class)
    public void transmitAPDUError() throws IOException{
        //init
        insertSe();
        byte[] in = new byte[]{(byte) 0x90, 0x00};
        byte[] out = new byte[]{(byte) 0x90, 0x00};
        when(tagProxy.transceive(in)).thenThrow(new IOException(""));

        //test
        ByteBuffer outBB = reader.transmitApdu(ByteBuffer.wrap(in));

        //throw exception
    }

    @Test
    public void protocolFlagMatchesTrue() throws IOException{
        //init
        insertSe();
        reader.addSeProtocolSetting(AndroidNfcProtocolSettings.SETTING_PROTOCOL_ISO14443_4);
        when(tagProxy.getTech()).thenReturn(AndroidNfcProtocolSettings.SETTING_PROTOCOL_ISO14443_4.getValue());

        //test
        Assert.assertTrue(reader.protocolFlagMatches(ContactlessProtocols.PROTOCOL_ISO14443_4));
    }

    @Test
    public void processIntent(){
        reader.processIntent(intent);
        Assert.assertTrue(true);//no test
    }

    @Test
    public void printTagId(){
        reader.printTagId();
        Assert.assertTrue(true);//no test
    }


    /*
    * Helper method
     */

    private void insertSe(){
        reader.onTagDiscovered(tag);

    }
}
