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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.message.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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


    @Test
    public void notifySeProcessed() throws Exception {
        AbstractObservableLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        // set the starting state
        r.switchState(AbstractObservableState.MonitoringState.WAIT_FOR_SE_PROCESSING);

        // keep open
        r.transmitSeRequest(SeRequestTest.getSeRequestSample(), ChannelControl.KEEP_OPEN);

        // initiate the SE removal sequence
        r.notifySeProcessed();

        // assert currentState have changed
        Assert.assertEquals(AbstractObservableState.MonitoringState.WAIT_FOR_SE_REMOVAL,
                r.getCurrentMonitoringState());
    }

    @Ignore
    @Test
    public void transmitSeRequest_withCloseAfter() throws Exception {
        AbstractObservableLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        // set the starting state
        r.switchState(AbstractObservableState.MonitoringState.WAIT_FOR_SE_PROCESSING);

        // close after
        r.transmitSeRequest(SeRequestTest.getSeRequestSample(), ChannelControl.CLOSE_AFTER);

        // assert currentState have changed
        Assert.assertEquals(AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION,
                r.getCurrentMonitoringState());
    }


    /*
     * Observers
     */

    @Test
    public void addObserver() throws Exception {
        AbstractObservableLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        ObservableReader.ReaderObserver obs = getReaderObserver();
        r.addObserver(obs);
        Assert.assertEquals(1, r.countObservers());
    }

    @Test
    public void removeObserver() throws Exception {
        AbstractObservableLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
        ObservableReader.ReaderObserver obs = getReaderObserver();
        r.addObserver(obs);
        r.removeObserver(obs);
        Assert.assertEquals(0, r.countObservers());
    }

    /*
     * HELPERS
     */


    public static ObservableReader.ReaderObserver onEventCountDown(final CountDownLatch lock) {
        return new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                lock.countDown();
            }
        };
    }

    public static ObservableReader.ReaderObserver onRemovedCountDown(final CountDownLatch lock) {
        return new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                if (event.getEventType() == ReaderEvent.EventType.SE_REMOVED) {
                    lock.countDown();
                } ;

            }
        };
    }

    public static ObservableReader.ReaderObserver onInsertedCountDown(final CountDownLatch lock) {
        return new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                if (event.getEventType() == ReaderEvent.EventType.SE_INSERTED) {
                    lock.countDown();
                } ;

            }
        };
    }

    public static ObservableReader.ReaderObserver onMatchedCountDown(final CountDownLatch lock) {
        return new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                if (event.getEventType() == ReaderEvent.EventType.SE_MATCHED) {
                    lock.countDown();
                } ;

            }
        };
    }

    public static AbstractObservableLocalReader getBlank(String pluginName, String readerName) {
        AbstractObservableLocalReader r = new BlankObservableLocalReader(pluginName, readerName);
        return r;
    }



    public static AbstractObservableLocalReader getSpy(String pluginName, String readerName) {
        BlankObservableLocalReader blankObservableLocalReader =
                new BlankObservableLocalReader(pluginName, readerName);
        blankObservableLocalReader.startSeDetection(ObservableReader.PollingMode.REPEATING);
        AbstractObservableLocalReader r = Mockito.spy(blankObservableLocalReader);
        doReturn(SeResponseTest.getASeResponse()).when(r).processSeRequest(any(SeRequest.class),
                any(ChannelControl.class));
        doReturn(getSeResponses()).when(r).processSeRequests(any(List.class),
                any(MultiSeRequestProcessing.class), any(ChannelControl.class));
        return r;
    }


    public static List<SeResponse> getMatchingResponses() {
        SelectionStatus selectionStatus =
                new SelectionStatus(null, new ApduResponse(AbsLocalReaderTransmitTest.RESP_SUCCESS,
                        AbsLocalReaderSelectionTest.STATUS_CODE_LIST), true);
        SeResponse seResponse = new SeResponse(true, false, selectionStatus, null);
        return Arrays.asList(seResponse);
    }

    public static List<SeResponse> getNotMatchingResponses() {
        SelectionStatus selectionStatus =
                new SelectionStatus(null, new ApduResponse(AbsLocalReaderTransmitTest.RESP_FAIL,
                        AbsLocalReaderSelectionTest.STATUS_CODE_LIST), false);
        SeResponse seResponse = new SeResponse(false, false, selectionStatus, null);
        return Arrays.asList(seResponse);
    }

    public static ObservableReader.ReaderObserver getReaderObserver() {
        return new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent readerEvent) {}
        };
    }

    public static List<SeResponse> getSeResponses() {
        List<SeResponse> responses = new ArrayList<SeResponse>();
        responses.add(SeResponseTest.getASeResponse());
        return responses;
    }
}
