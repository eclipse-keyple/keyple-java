/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.core.seproxy.plugin;

import static org.mockito.Mockito.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.seproxy.plugin.mock.BlankObservableLocalReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbsObservableLocalReaderTest extends CoreBaseTest {

    private static final Logger logger =
            LoggerFactory.getLogger(AbsObservableLocalReaderTest.class);


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
    public void isSePresent_false() throws Exception {
        AbstractObservableLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);

        final CountDownLatch lock = new CountDownLatch(1);
        r.addObserver(onRemovedCountDown(lock));

        when(r.checkSePresence()).thenReturn(false);
        when(r.isPhysicalChannelOpen()).thenReturn(true);

        // test
        Assert.assertFalse(r.isSePresent());

        // wait
        lock.await(100, TimeUnit.MILLISECONDS);
        verify(r, times(1)).closeLogicalAndPhysicalChannels();
        Assert.assertEquals(0, lock.getCount());

    }

    @Test
    public void isSePresent_true() throws Exception {
        AbstractObservableLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        when(r.checkSePresence()).thenReturn(true);

        // test
        Assert.assertTrue(r.isSePresent());
        verify(r, times(0)).processSeRemoved();
    }



    /** ==== Card event ====================================== */

    /*
     * no default selection
     */
    @Test
    public void seInserted() throws Exception {
        // empty reader
        AbstractObservableLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        final CountDownLatch lock = new CountDownLatch(1);

        r.addObserver(onInsertedCountDown(lock));

        // test
        r.processSeInserted();

        // wait
        lock.await(100, TimeUnit.MILLISECONDS);
        Assert.assertEquals(0, lock.getCount());
    }

    /*
     * selection is not successful
     */
    @Test
    public void seInserted_ALWAYS() throws Exception {
        AbstractObservableLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        final CountDownLatch lock = new CountDownLatch(1);

        // add a INSERTED observer
        r.addObserver(onInsertedCountDown(lock));

        // configure parameters
        Set<SeRequest> selections = new HashSet<SeRequest>();
        MultiSeRequestProcessing multi = MultiSeRequestProcessing.PROCESS_ALL;
        ChannelControl channel = ChannelControl.CLOSE_AFTER;
        ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.ALWAYS;

        // return matching selection
        doReturn(getNotMatchingResponses()).when(r).transmitSet(selections, multi, channel);

        r.setDefaultSelectionRequest(new DefaultSelectionsRequestImpl(selections, multi, channel),
                mode);

        // test
        r.processSeInserted();

        // wait
        lock.await(200, TimeUnit.MILLISECONDS);
        Assert.assertEquals(0, lock.getCount());
    }

    /*
     * selection is not successful
     */
    @Test
    public void noEvent_MATCHED_ONLY() throws Exception {
        AbstractObservableLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        final CountDownLatch lock = new CountDownLatch(1);

        // add a EVENT observer
        r.addObserver(onEventCountDown(lock));

        // configure parameters
        Set<SeRequest> selections = new HashSet<SeRequest>();
        MultiSeRequestProcessing multi = MultiSeRequestProcessing.PROCESS_ALL;
        ChannelControl channel = ChannelControl.CLOSE_AFTER;
        ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.MATCHED_ONLY;

        // return matching selection
        doReturn(getNotMatchingResponses()).when(r).transmitSet(selections, multi, channel);

        r.setDefaultSelectionRequest(new DefaultSelectionsRequestImpl(selections, multi, channel),
                mode);

        // test
        r.processSeInserted();

        // wait
        lock.await(200, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, lock.getCount());
        // no event is thrown
    }

    /*
     * selection is successful
     */
    @Test
    public void seMatched_MATCHED_ONLY() throws Exception {
        AbstractObservableLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        final CountDownLatch lock = new CountDownLatch(1);

        // add a MATCHED observer
        r.addObserver(onMatchedCountDown(lock));

        // configure parameters
        Set<SeRequest> selections = new HashSet<SeRequest>();
        MultiSeRequestProcessing multi = MultiSeRequestProcessing.PROCESS_ALL;
        ChannelControl channel = ChannelControl.CLOSE_AFTER;
        ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.MATCHED_ONLY;

        // return success selection
        doReturn(getMatchingResponses()).when(r).transmitSet(selections, multi, channel);

        r.setDefaultSelectionRequest(new DefaultSelectionsRequestImpl(selections, multi, channel),
                mode);

        // test
        r.processSeInserted();

        // wait
        lock.await(100, TimeUnit.MILLISECONDS);
        Assert.assertEquals(0, lock.getCount());
    }

    /*
     * selection is successful
     */
    @Test
    public void seMatched_ALWAYS() throws Exception {
        AbstractObservableLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        final CountDownLatch lock = new CountDownLatch(1);

        // add a MATCHED observer
        r.addObserver(onMatchedCountDown(lock));

        // configure parameters
        Set<SeRequest> selections = new HashSet<SeRequest>();
        MultiSeRequestProcessing multi = MultiSeRequestProcessing.PROCESS_ALL;
        ChannelControl channel = ChannelControl.CLOSE_AFTER;
        ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.ALWAYS;

        // return success selection
        doReturn(getMatchingResponses()).when(r).transmitSet(selections, multi, channel);

        r.setDefaultSelectionRequest(new DefaultSelectionsRequestImpl(selections, multi, channel),
                mode);


        // test
        r.processSeInserted();

        // wait
        lock.await(100, TimeUnit.MILLISECONDS);
        Assert.assertEquals(0, lock.getCount());
    }

    /*
     * Simulate an IOException while selecting Do not throw any event Nor an exception
     */
    @Test
    public void noEvent_IOError() throws Exception {
        AbstractObservableLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        final CountDownLatch lock = new CountDownLatch(1);

        // add a EVENT observer
        r.addObserver(onEventCountDown(lock));

        // configure parameters
        Set<SeRequest> selections = new HashSet<SeRequest>();
        MultiSeRequestProcessing multi = MultiSeRequestProcessing.PROCESS_ALL;
        ChannelControl channel = ChannelControl.CLOSE_AFTER;
        ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.ALWAYS;

        // throw IO
        doThrow(new KeypleIOReaderException("io error when selecting")).when(r)
                .transmitSet(selections, multi, channel);

        r.setDefaultSelectionRequest(new DefaultSelectionsRequestImpl(selections, multi, channel),
                mode);

        // test
        r.processSeInserted();

        // wait
        lock.await(100, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, lock.getCount());
        verify(r, times(1)).closeLogicalAndPhysicalChannels();
    }

    /**
     * State Machine
     */


    @Test
    public void switchState_sync() throws Exception {
        AbstractObservableLocalReader r = getBlank(PLUGIN_NAME, READER_NAME);

        // test method
        r.switchState(AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION);

        // assert result
        Assert.assertEquals(AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION,
                r.getCurrentMonitoringState());

    }

    @Test
    public void switchState_async_wait() throws Exception {
        final AbstractObservableLocalReader r = getBlank(PLUGIN_NAME, READER_NAME);

        FutureTask<Boolean> switchState = new FutureTask(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                logger.trace("Invoke waitForCardPresent asynchronously");
                try {
                    Thread.sleep(50);
                    r.switchState(AbstractObservableState.MonitoringState.WAIT_FOR_SE_PROCESSING);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        switchState.run();// run in the same thread

        Thread.sleep(100);

        // assert result
        Assert.assertEquals(AbstractObservableState.MonitoringState.WAIT_FOR_SE_PROCESSING,
                r.getCurrentMonitoringState());

    }

    @Test
    public void switchState_async_block() throws Exception {
        final AbstractObservableLocalReader r = getBlank(PLUGIN_NAME, READER_NAME);

        FutureTask<Boolean> switchState = new FutureTask(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                logger.trace("Invoke waitForCardPresent asynchronously");
                try {
                    Thread.sleep(500);
                    r.switchState(AbstractObservableState.MonitoringState.WAIT_FOR_SE_PROCESSING);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        Future stateSwitched = Executors.newSingleThreadExecutor().submit(switchState);// run in a
                                                                                       // different
                                                                                       // thread

        stateSwitched.get(2000, TimeUnit.MILLISECONDS);

        // assert result
        Assert.assertEquals(AbstractObservableState.MonitoringState.WAIT_FOR_SE_PROCESSING,
                r.getCurrentMonitoringState());

    }


    @Test
    public void states() throws Exception {
        AbstractObservableLocalReader r = getBlank(PLUGIN_NAME, READER_NAME);

        Assert.assertEquals(AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION,
                r.getCurrentMonitoringState());

        // start detection
        r.startSeDetection(ObservableReader.PollingMode.REPEATING);

        // assert currentState have changed
        Assert.assertEquals(AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION,
                r.getCurrentMonitoringState());

        // stop detection
        r.stopSeDetection();

        // assert currentState have changed
        Assert.assertEquals(AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION,
                r.getCurrentMonitoringState());

        // start detection
        r.startSeDetection(ObservableReader.PollingMode.REPEATING);

        // insert SE
        r.onEvent(AbstractObservableLocalReader.InternalEvent.SE_INSERTED);

        // assert currentState have changed
        Assert.assertEquals(AbstractObservableState.MonitoringState.WAIT_FOR_SE_PROCESSING,
                r.getCurrentMonitoringState());

        // SE has been processed
        r.startRemovalSequence();

        // assert currentState have changed
        Assert.assertEquals(AbstractObservableState.MonitoringState.WAIT_FOR_SE_REMOVAL,
                r.getCurrentMonitoringState());

        // remove SE
        r.onEvent(AbstractObservableLocalReader.InternalEvent.SE_REMOVED);

        // assert currentState have changed
        Assert.assertEquals(AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION,
                r.getCurrentMonitoringState());
    }



    /*
     * HELPERS
     */


    static public ObservableReader.ReaderObserver onEventCountDown(final CountDownLatch lock) {
        return new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                lock.countDown();
            }
        };
    }

    static public ObservableReader.ReaderObserver onRemovedCountDown(final CountDownLatch lock) {
        return new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                if (event.getEventType() == ReaderEvent.EventType.SE_REMOVED) {
                    lock.countDown();
                } ;

            }
        };
    }

    static public ObservableReader.ReaderObserver onInsertedCountDown(final CountDownLatch lock) {
        return new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                if (event.getEventType() == ReaderEvent.EventType.SE_INSERTED) {
                    lock.countDown();
                } ;

            }
        };
    }

    static public ObservableReader.ReaderObserver onMatchedCountDown(final CountDownLatch lock) {
        return new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                if (event.getEventType() == ReaderEvent.EventType.SE_MATCHED) {
                    lock.countDown();
                } ;

            }
        };
    }

    static public AbstractObservableLocalReader getBlank(String pluginName, String readerName) {
        AbstractObservableLocalReader r = new BlankObservableLocalReader(pluginName, readerName);
        return r;
    }



    static public AbstractObservableLocalReader getSpy(String pluginName, String readerName) {
        AbstractObservableLocalReader r =
                Mockito.spy(new BlankObservableLocalReader(pluginName, readerName));

        /*
         * doCallRealMethod().when(r).initStates(); doCallRealMethod().when(r).getCurrentState();
         * doCallRealMethod().when(r).setCurrentState(any(AbstractObservableState.class));
         * doCallRealMethod().when(r).switchState(any(AbstractObservableState.MonitoringState.class)
         * ); doCallRealMethod().when(r).startSeDetection(any(ObservableReader.PollingMode.class));
         */
        return r;
    }


    static public List<SeResponse> getMatchingResponses() {
        SelectionStatus selectionStatus =
                new SelectionStatus(null, new ApduResponse(AbsLocalReaderTransmitTest.RESP_SUCCESS,
                        AbsLocalReaderSelectionTest.STATUS_CODE), true);
        SeResponse seResponse = new SeResponse(true, false, selectionStatus, null);
        return Arrays.asList(seResponse);
    }

    static public List<SeResponse> getNotMatchingResponses() {
        SelectionStatus selectionStatus =
                new SelectionStatus(null, new ApduResponse(AbsLocalReaderTransmitTest.RESP_FAIL,
                        AbsLocalReaderSelectionTest.STATUS_CODE), false);
        SeResponse seResponse = new SeResponse(false, false, selectionStatus, null);
        return Arrays.asList(seResponse);
    }


}
