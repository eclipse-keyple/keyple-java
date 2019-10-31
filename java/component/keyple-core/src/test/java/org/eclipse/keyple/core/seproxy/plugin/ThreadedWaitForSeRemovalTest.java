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

import static org.eclipse.keyple.core.seproxy.plugin.AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION;
import static org.eclipse.keyple.core.seproxy.plugin.AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.core.seproxy.plugin.mock.BlankSmartPresenceTheadedReader;
import org.eclipse.keyple.core.seproxy.plugin.monitor.CardAbsentPingMonitoringJob;
import org.eclipse.keyple.core.seproxy.plugin.monitor.SmartRemovalMonitoringJob;
import org.eclipse.keyple.core.seproxy.plugin.state.WaitForSeRemoval;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadedWaitForSeRemovalTest extends CoreBaseTest {


    private static final Logger logger =
            LoggerFactory.getLogger(ThreadedWaitForSeRemovalTest.class);

    final String PLUGIN_NAME = "ThreadedWaitForSeRemovalTestP";
    final String READER_NAME = "ThreadedWaitForSeRemovalTest";

    final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Before
    public void setUp() {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");
    }

    @Test
    public void waitForRemoval_STOP() throws Exception, NoStackTraceThrowable {
        /*
         * ------------ input polling mode is STOP SE has been removed within timeout
         */
        AbstractObservableLocalReader r =
                AbsSmartInsertionTheadedReaderTest.getSmartSpy(PLUGIN_NAME, READER_NAME, 0);
        WaitForSeRemoval waitForSeRemoval =
                new WaitForSeRemoval(r, new CardAbsentPingMonitoringJob(r), executorService);
        doReturn(ObservableReader.PollingMode.SINGLESHOT).when(r).getPollingMode();
        doReturn(false).when(r).isSePresentPing();

        /* test */
        waitForSeRemoval.onActivate();

        Thread.sleep(50l);

        /* Assert */
        // Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentState().getMonitoringState());
        verify(r, times(1)).switchState(WAIT_FOR_START_DETECTION);

        waitForSeRemoval.onDeactivate();

    }

    @Test
    public void waitForRemoval_CONTINUE() throws Exception, NoStackTraceThrowable {
        /*
         * ------------ input polling mode is CONTINUE SE has been removed within timeout
         */
        AbstractObservableLocalReader r =
                AbsSmartInsertionTheadedReaderTest.getSmartSpy(PLUGIN_NAME, READER_NAME, 0);
        WaitForSeRemoval waitForSeRemoval =
                new WaitForSeRemoval(r, new CardAbsentPingMonitoringJob(r), executorService);
        doReturn(ObservableReader.PollingMode.REPEATING).when(r).getPollingMode();
        doReturn(false).when(r).isSePresentPing();

        /* test */
        waitForSeRemoval.onActivate();

        Thread.sleep(50l);// wait for timeout

        /* Assert */
        // Assert.assertEquals(WAIT_FOR_SE_INSERTION, r.getCurrentState().getMonitoringState());
        verify(r, times(1)).switchState(WAIT_FOR_SE_INSERTION);
        waitForSeRemoval.onDeactivate();

    }

    // @Test
    // public void waitForRemoval_Timeout() throws Exception, NoStackTraceThrowable {
    // /*
    // * ------------ input polling mode is CONTINUE SE has NOT been removed within timeout
    // */
    // long timeout = 1000l;
    // AbstractObservableLocalReader r =
    // AbsSmartInsertionTheadedReaderTest.getSmartSpy(PLUGIN_NAME, READER_NAME, 0);
    // ThreadedWaitForSeRemoval waitForSeRemoval =
    // new ThreadedWaitForSeRemoval(r, executorService);
    // doReturn(true).when(r).isSePresentPing();
    //
    // /* test */
    // waitForSeRemoval.onActivate();
    //
    // Thread.sleep(2000l);// wait for timeout
    //
    // /* Assert */
    // // Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentState().getMonitoringState());
    // verify(r, times(1)).switchState(WAIT_FOR_START_DETECTION);
    // waitForSeRemoval.onDeactivate();
    //
    // }


    @Test
    public void smart_waitForRemoval_STOP() throws Exception, NoStackTraceThrowable {
        /*
         * ------------ input polling mode is STOP SE has been removed within timeout
         */
        BlankSmartPresenceTheadedReader r =
                AbsSmartPresenceTheadedReaderTest.getSmartSpy(PLUGIN_NAME, READER_NAME);
        WaitForSeRemoval waitForSeRemoval =
                new WaitForSeRemoval(r, new SmartRemovalMonitoringJob(r), executorService);
        doReturn(ObservableReader.PollingMode.SINGLESHOT).when(r).getPollingMode();
        doReturn(true).when(r).waitForCardAbsentNative();

        /* test */
        waitForSeRemoval.onActivate();

        Thread.sleep(50l);

        /* Assert */
        // Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentState().getMonitoringState());
        verify(r, times(1)).switchState(WAIT_FOR_START_DETECTION);
        waitForSeRemoval.onDeactivate();

    }

    @Test
    public void smart_waitForRemoval_CONTINUE() throws Exception, NoStackTraceThrowable {
        /*
         * ------------ input polling mode is CONTINUE SE has been removed within timeout
         */
        BlankSmartPresenceTheadedReader r =
                AbsSmartPresenceTheadedReaderTest.getSmartSpy(PLUGIN_NAME, READER_NAME);
        WaitForSeRemoval waitForSeRemoval =
                new WaitForSeRemoval(r, new SmartRemovalMonitoringJob(r), executorService);
        doReturn(ObservableReader.PollingMode.REPEATING).when(r).getPollingMode();
        doReturn(true).when(r).waitForCardAbsentNative();

        /* test */
        waitForSeRemoval.onActivate();

        Thread.sleep(50l);// wait

        /* Assert */
        // Assert.assertEquals(WAIT_FOR_SE_INSERTION, r.getCurrentState().getMonitoringState());
        verify(r, times(1)).switchState(WAIT_FOR_SE_INSERTION);
        waitForSeRemoval.onDeactivate();

    }

    // @Test
    // public void smart_waitForRemoval_Timeout() throws Exception, NoStackTraceThrowable {
    // /*
    // * ------------ input SE has NOT been removed within timeout
    // */
    // long timeout = 100l;
    // BlankSmartPresenceTheadedReader r =
    // AbsSmartPresenceTheadedReaderTest.getSmartSpy(PLUGIN_NAME, READER_NAME);
    // ThreadedWaitForSeRemoval waitForSeRemoval =
    // new ThreadedWaitForSeRemoval(r, executorService);
    // doReturn(false).when(r).waitForCardAbsentNative(timeout);
    //
    // /* test */
    // waitForSeRemoval.onActivate();
    //
    // Thread.sleep(50l);// wait for timeout
    //
    // /* Assert */
    // // Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentState().getMonitoringState());
    // verify(r, times(1)).switchState(WAIT_FOR_START_DETECTION);
    // waitForSeRemoval.onDeactivate();
    //
    // }

}
