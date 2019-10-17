package org.eclipse.keyple.core.seproxy.plugin;

import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsResponse;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    public void seMatched() throws Exception{
        //empty reader
        AbstractObservableLocalReader r =  getSpy(PLUGIN_NAME,READER_NAME);
        AbsLocalReaderTransmitTest.configure(r);

        r.setDefaultSelectionRequest(
                new DefaultSelectionsRequest(
                    AbsLocalReaderTransmitTest.getPartialRequestSet(r, 4),
                    MultiSeRequestProcessing.PROCESS_ALL,
                    ChannelControl.CLOSE_AFTER),
                ObservableReader.NotificationMode.MATCHED_ONLY);

        final CountDownLatch lock = new CountDownLatch(1);
        r.addObserver(onMatchedCountDown(lock));

        //test
        r.processSeInserted();

        //wait
        lock.await(100, TimeUnit.MILLISECONDS);
        Assert.assertEquals(0, lock.getCount());
    }
























    /*
     *  HELPERS
     */


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


}
