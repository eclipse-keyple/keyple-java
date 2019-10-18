package org.eclipse.keyple.core.seproxy.plugin;

import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsResponse;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.eclipse.keyple.core.seproxy.plugin.AbsLocalReaderSelectionTest.AID;
import static org.eclipse.keyple.core.seproxy.plugin.AbsLocalReaderSelectionTest.STATUS_CODE;
import static org.eclipse.keyple.core.seproxy.plugin.AbsLocalReaderTransmitTest.APDU_IOEXC;
import static org.eclipse.keyple.core.seproxy.plugin.AbsLocalReaderTransmitTest.configure;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AbsObservableLocalReaderTest  extends CoreBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(AbsObservableLocalReaderTest.class);


    final String PLUGIN_NAME = "AbsObservableLocalReaderTestP";
    final String READER_NAME = "AbsObservableLocalReaderTest";

    @Before
    public void setUp() {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");
    }


    /** ==== Card presence management ====================================== */


    @Test
    public void isSePresent_false() throws Exception, NoStackTraceThrowable {
        AbstractObservableLocalReader r =  getSpy(PLUGIN_NAME,READER_NAME);

        final CountDownLatch lock = new CountDownLatch(1);
        r.addObserver(onRemovedCountDown(lock));

        when(r.checkSePresence()).thenReturn(false);
        when(r.isLogicalChannelOpen()).thenReturn(true);

        //test
        Assert.assertFalse(r.isSePresent());

        //wait
        lock.await(100, TimeUnit.MILLISECONDS);
        verify(r, times(1)).closeLogicalAndPhysicalChannels();
        Assert.assertEquals(0, lock.getCount());

    }

    @Test
    public void isSePresent_true() throws Exception, NoStackTraceThrowable {
        AbstractObservableLocalReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        when(r.checkSePresence()).thenReturn(true);

        //test
       Assert.assertTrue(r.isSePresent());
        verify(r, times(0)).processSeRemoved();
    }



    /** ==== Card event ====================================== */

    /*
     * no default selection
     */
    @Test
    public void seInserted() throws Exception{
        //empty reader
        AbstractObservableLocalReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        final CountDownLatch lock = new CountDownLatch(1);

        r.addObserver(onInsertedCountDown(lock));

        //test
        r.processSeInserted();

        //wait
        lock.await(100, TimeUnit.MILLISECONDS);
        Assert.assertEquals(0, lock.getCount());
    }

    /*
     * selection is not successful
     */
    @Test
    public void seInserted_ALWAYS() throws Exception{
        AbstractObservableLocalReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        final CountDownLatch lock = new CountDownLatch(1);

        //add a INSERTED observer
        r.addObserver(onInsertedCountDown(lock));

        //configure parameters
        Set<SeRequest> selections = new HashSet<SeRequest>();
        MultiSeRequestProcessing multi = MultiSeRequestProcessing.PROCESS_ALL;
        ChannelControl channel = ChannelControl.CLOSE_AFTER;
        ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.ALWAYS;

        //return matching selection
        doReturn(getNotMatchingResponses()).when(r).transmitSet(selections,multi,channel);

        r.setDefaultSelectionRequest(
                new DefaultSelectionsRequest(
                        selections,
                        multi,
                        channel),
                mode);

        //test
        r.processSeInserted();

        //wait
        lock.await(200, TimeUnit.MILLISECONDS);
        Assert.assertEquals(0, lock.getCount());
    }

    /*
     * selection is not successful
     */
    @Test
    public void noEvent_MATCHED_ONLY() throws Exception{
        AbstractObservableLocalReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        final CountDownLatch lock = new CountDownLatch(1);

        //add a EVENT observer
        r.addObserver(onEventCountDown(lock));

        //configure parameters
        Set<SeRequest> selections = new HashSet<SeRequest>();
        MultiSeRequestProcessing multi = MultiSeRequestProcessing.PROCESS_ALL;
        ChannelControl channel = ChannelControl.CLOSE_AFTER;
        ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.MATCHED_ONLY;

        //return matching selection
        doReturn(getNotMatchingResponses()).when(r).transmitSet(selections,multi,channel);

        r.setDefaultSelectionRequest(
                new DefaultSelectionsRequest(
                        selections,
                        multi,
                        channel),
                mode);

        //test
        r.processSeInserted();

        //wait
        lock.await(200, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, lock.getCount());
        //no event is thrown
    }

    /*
     * selection is successful
     */
    @Test
    public void seMatched_MATCHED_ONLY() throws Exception{
        AbstractObservableLocalReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        final CountDownLatch lock = new CountDownLatch(1);

        //add a MATCHED observer
        r.addObserver(onMatchedCountDown(lock));

        //configure parameters
        Set<SeRequest> selections = new HashSet<SeRequest>();
        MultiSeRequestProcessing multi = MultiSeRequestProcessing.PROCESS_ALL;
        ChannelControl channel = ChannelControl.CLOSE_AFTER;
        ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.MATCHED_ONLY;

        //return success selection
       doReturn(getMatchingResponses()).when(r).transmitSet(selections,multi,channel);

        r.setDefaultSelectionRequest(
                new DefaultSelectionsRequest(
                        selections,
                        multi,
                    channel),
                mode);


        //test
        r.processSeInserted();

        //wait
        lock.await(100, TimeUnit.MILLISECONDS);
        Assert.assertEquals(0, lock.getCount());
    }

    /*
     * selection is successful
     */
    @Test
    public void seMatched_ALWAYS() throws Exception{
        AbstractObservableLocalReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        final CountDownLatch lock = new CountDownLatch(1);

        //add a MATCHED observer
        r.addObserver(onMatchedCountDown(lock));

        //configure parameters
        Set<SeRequest> selections = new HashSet<SeRequest>();
        MultiSeRequestProcessing multi = MultiSeRequestProcessing.PROCESS_ALL;
        ChannelControl channel = ChannelControl.CLOSE_AFTER;
        ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.ALWAYS;

        //return success selection
        doReturn(getMatchingResponses()).when(r).transmitSet(selections,multi,channel);

        r.setDefaultSelectionRequest(
                new DefaultSelectionsRequest(
                        selections,
                        multi,
                        channel),
                mode);


        //test
        r.processSeInserted();

        //wait
        lock.await(100, TimeUnit.MILLISECONDS);
        Assert.assertEquals(0, lock.getCount());
    }

    /*
     * Simulate an IOException while selecting
     * Do not throw any event
     * Nor an exception
     */
    @Test
    public void noEvent_IOError() throws Exception{
        AbstractObservableLocalReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        final CountDownLatch lock = new CountDownLatch(1);

        //add a EVENT observer
        r.addObserver(onEventCountDown(lock));

        //configure parameters
        Set<SeRequest> selections = new HashSet<SeRequest>();
        MultiSeRequestProcessing multi = MultiSeRequestProcessing.PROCESS_ALL;
        ChannelControl channel = ChannelControl.CLOSE_AFTER;
        ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.ALWAYS;

        //throw IO
        doThrow(new KeypleIOReaderException("io error when selecting")).when(r).transmitSet(selections,multi,channel);

        r.setDefaultSelectionRequest(
                new DefaultSelectionsRequest(
                        selections,
                        multi,
                        channel),
                mode);

        //test
        r.processSeInserted();

        //wait
        lock.await(100, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, lock.getCount());
        verify(r, times(1)).closeLogicalAndPhysicalChannels();
    }






















    /*
     *  HELPERS
     */


    static public ObservableReader.ReaderObserver onEventCountDown(final CountDownLatch lock){
        return new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                lock.countDown();
            }
        };
    }

    static public ObservableReader.ReaderObserver onRemovedCountDown(final CountDownLatch lock){
        return new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                if(event.getEventType()== ReaderEvent.EventType.SE_REMOVED){
                    lock.countDown();
                };

            }
        };
    }

    static public ObservableReader.ReaderObserver onInsertedCountDown(final CountDownLatch lock){
        return new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                if(event.getEventType()== ReaderEvent.EventType.SE_INSERTED){
                    lock.countDown();
                };

            }
        };
    }

    static public ObservableReader.ReaderObserver onMatchedCountDown(final CountDownLatch lock){
        return new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                if(event.getEventType()== ReaderEvent.EventType.SE_MATCHED){
                    lock.countDown();
                };

            }
        };
    }

    static public AbstractObservableLocalReader getSpy(String pluginName, String readerName) throws KeypleReaderException {
        AbstractObservableLocalReader r =  Mockito.spy(new BlankObservableLocalReader(pluginName,readerName));
        return  r;
    }


    static public List<SeResponse> getMatchingResponses(){
        SelectionStatus selectionStatus = new SelectionStatus(null,
                new ApduResponse(AbsLocalReaderTransmitTest.RESP_SUCCESS, AbsLocalReaderSelectionTest.STATUS_CODE),
                true);
        SeResponse seResponse =
                new SeResponse(true,false,selectionStatus,null);
        return Arrays.asList(seResponse);
    }

    static public List<SeResponse> getNotMatchingResponses(){
        SelectionStatus selectionStatus = new SelectionStatus(null,
                new ApduResponse(AbsLocalReaderTransmitTest.RESP_FAIL, AbsLocalReaderSelectionTest.STATUS_CODE)
                , false);
        SeResponse seResponse =
                new SeResponse(false,false,selectionStatus,null);
        return Arrays.asList(seResponse);
    }


}
