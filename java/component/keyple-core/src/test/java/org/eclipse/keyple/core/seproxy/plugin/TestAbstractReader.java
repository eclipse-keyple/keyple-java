package org.eclipse.keyple.core.seproxy.plugin;

import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeRequestTest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.message.SeResponseTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.eclipse.keyple.core.seproxy.ChannelControl.CLOSE_AFTER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestAbstractReader extends CoreBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractReaderTestOld.class);


    final String PLUGIN_NAME = "abstractPluginTest";
    final String READER_NAME = "AbstractReaderTest";


    @Before
    public void setUp() {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");
    }

    @Test
    public void testConstructor()throws Exception {
        AbstractReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        Assert.assertEquals(PLUGIN_NAME, r.getPluginName());
        Assert.assertEquals(READER_NAME, r.getName());
    }

    @Test
    public void testCompareTo()throws Exception {
        AbstractReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        AbstractReader r2 =  getSpy(PLUGIN_NAME,READER_NAME);
        Assert.assertEquals(0, r.compareTo(r2));
    }

    /*
     * TransmitSet "ts_"
     */

    @Test(expected = IllegalArgumentException.class)
    public void ts_transmit_null() throws Exception {
        AbstractReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        r.transmitSet(null, MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.CLOSE_AFTER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ts_transmit2_null() throws Exception {
        AbstractReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        r.transmitSet(null);
    }

    @Test
    public void ts_transmit() throws Exception {
        AbstractReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        Set<SeRequest> set = getSeRequestSet();
        List<SeResponse> responses = r.transmitSet(set, MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.CLOSE_AFTER);
        verify(r, times(1)).processSeRequestSet(set, MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.CLOSE_AFTER);
        Assert.assertNotNull(responses);
    }

    @Test
    public void ts_notifySeProcessed_withForceClosing() throws Exception {
        AbstractReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        Set<SeRequest> set = getSeRequestSet();
        //keep open
        r.transmitSet(set, MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.KEEP_OPEN);
        //force closing
        r.notifySeProcessed();
        verify(r, times(1)).processSeRequest(null, CLOSE_AFTER);
    }

    @Test
    public void ts_notifySeProcessed_withoutForceClosing() throws Exception {
        AbstractReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        Set<SeRequest> set = getSeRequestSet();
        //close after
        r.transmitSet(set, MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.CLOSE_AFTER);
        r.notifySeProcessed();

        //force closing is not called (only the transmit)
        verify(r, times(0)).processSeRequest(null, CLOSE_AFTER);
    }

    /*
     * Transmit
     */

    @Test(expected = IllegalArgumentException.class)
    public void transmit_null() throws Exception {
        AbstractReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        r.transmit(null, ChannelControl.CLOSE_AFTER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transmit2_null() throws Exception {
        AbstractReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        r.transmit(null);
    }

    @Test
    public void transmit() throws Exception {
        AbstractReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        SeRequest request = SeRequestTest.getSeRequestSample();
        SeResponse response = r.transmit(request, ChannelControl.CLOSE_AFTER);
        verify(r, times(1)).processSeRequest(request, ChannelControl.CLOSE_AFTER);
        Assert.assertNotNull(response);
    }


    @Test
    public void notifySeProcessed_withForceClosing() throws Exception {
        AbstractReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        //keep open
        r.transmit(SeRequestTest.getSeRequestSample(), ChannelControl.KEEP_OPEN);
        //force closing
        r.notifySeProcessed();
        verify(r, times(1)).processSeRequest(null, CLOSE_AFTER);
    }

    @Test
    public void notifySeProcessed_withoutForceClosing() throws Exception {
        AbstractReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        SeRequest request = SeRequestTest.getSeRequestSample();
        //close after
        r.transmit(request, ChannelControl.CLOSE_AFTER);
        r.notifySeProcessed();

        //force closing is not called (only the transmit)
        verify(r, times(0)).processSeRequest(null, CLOSE_AFTER);
    }


    /*
     * Observers
     */

    @Test
    public void addObserver() throws Exception {
        AbstractReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        ObservableReader.ReaderObserver obs = getReaderObserver();
        r.addObserver(obs);
        Assert.assertEquals(1, r.countObservers());
    }

    @Test
    public void removeObserver() throws Exception {
        AbstractReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        ObservableReader.ReaderObserver obs = getReaderObserver();
        r.addObserver(obs);
        r.removeObserver(obs);
        Assert.assertEquals(0, r.countObservers());
    }




    /*
     * Helpers
     */

    /**
     * Return a basic spy reader
     * @param pluginName
     * @param readerName
     * @return  basic spy reader
     * @throws KeypleReaderException
     */
    static public AbstractReader getSpy(String pluginName, String readerName) throws KeypleReaderException {
        AbstractReader r =  Mockito.spy(new BlankAbstractReader(pluginName,readerName));
        when(r.processSeRequest(any(SeRequest.class),any(ChannelControl.class))).thenReturn(SeResponseTest.getASeResponse());
        when(r.processSeRequestSet(any(Set.class),any(MultiSeRequestProcessing.class),any(ChannelControl.class))).thenReturn(getSeResponses());
        return  r;
    }

    static public Set<SeRequest> getSeRequestSet(){
        Set<SeRequest> set = new HashSet<SeRequest>();
        set.add(SeRequestTest.getSeRequestSample());
        return set;
    }

    static public List<SeResponse> getSeResponses(){
        List<SeResponse> responses = new ArrayList<SeResponse>();
        responses.add(SeResponseTest.getASeResponse());
        return responses;
    }

    static public ObservableReader.ReaderObserver getReaderObserver() {
        return new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent readerEvent) {}
        };
    }

}
