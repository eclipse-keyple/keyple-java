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
package org.eclipse.keyple.core.seproxy.plugin.local;

import static org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION;
import static org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION;
import static org.mockito.Mockito.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.plugin.local.monitoring.CardAbsentPingMonitoringJob;
import org.eclipse.keyple.core.seproxy.plugin.local.monitoring.SmartRemovalMonitoringJob;
import org.eclipse.keyple.core.seproxy.plugin.local.state.WaitForSeRemoval;
import org.eclipse.keyple.core.seproxy.plugin.mock.BlankSmartPresenceTheadedReader;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitForSeRemovalJobExecutorTest extends CoreBaseTest {


    private static final Logger logger =
            LoggerFactory.getLogger(WaitForSeRemovalJobExecutorTest.class);

    final String PLUGIN_NAME = "WaitForSeRemovalJobExecutorTestP";
    final String READER_NAME = "WaitForSeRemovalJobExecutorTest";

    final ExecutorService executorService = Executors.newSingleThreadExecutor();

    final Long WAIT = 200l;

    @Before
    public void setUp() {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");
    }

    @Test
    public void waitForRemoval_SINGLESHOT() throws Exception {
        /*
         * ------------ input
         *
         * polling mode is SINGLESHOT
         *
         * SE has been removed
         */
        AbstractObservableLocalReader r = AbsSmartInsertionTheadedReaderTest.getMock(READER_NAME);
        WaitForSeRemoval waitForSeRemoval =
                new WaitForSeRemoval(r, new CardAbsentPingMonitoringJob(r), executorService);
        doReturn(ObservableReader.PollingMode.SINGLESHOT).when(r).getPollingMode();
        doReturn(false).when(r).isSePresentPing();
        doNothing().when(r).processSeRemoved();
        /* test */
        waitForSeRemoval.onActivate();

        Thread.sleep(WAIT);// wait for the monitoring to act

        /* Assert */
        // Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentState().getMonitoringState());
        verify(r, times(1)).switchState(WAIT_FOR_START_DETECTION);

        waitForSeRemoval.onDeactivate();

    }

    @Test
    public void waitForRemoval_REPEATING() throws Exception {
        /*
         * ------------ input polling mode is CONTINUE SE has been removed within timeout
         */
        AbstractObservableLocalReader r = AbsSmartInsertionTheadedReaderTest.getMock(READER_NAME);
        WaitForSeRemoval waitForSeRemoval =
                new WaitForSeRemoval(r, new CardAbsentPingMonitoringJob(r), executorService);
        doReturn(ObservableReader.PollingMode.REPEATING).when(r).getPollingMode();
        doReturn(false).when(r).isSePresentPing();
        doNothing().when(r).processSeRemoved();

        /* test */
        waitForSeRemoval.onActivate();

        Thread.sleep(WAIT);// wait for the monitoring to act

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
    // WaitForSeRemoval waitForSeRemoval =
    // new WaitForSeRemoval(r, executorService);
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
    public void smart_waitForRemoval_SINGLESHOT() throws Exception {
        /*
         * ------------ input polling mode is STOP SE has been removed within timeout
         */
        BlankSmartPresenceTheadedReader r =
                AbsSmartPresenceTheadedReaderTest.getSmartSpy(PLUGIN_NAME, READER_NAME);
        WaitForSeRemoval waitForSeRemoval =
                new WaitForSeRemoval(r, new SmartRemovalMonitoringJob(r), executorService);
        doReturn(ObservableReader.PollingMode.SINGLESHOT).when(r).getPollingMode();
        doReturn(true).when(r).waitForCardAbsentNative();
        doNothing().when(r).processSeRemoved();
        /* test */
        waitForSeRemoval.onActivate();

        Thread.sleep(WAIT);// wait for the monitoring to act

        /* Assert */
        // Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentState().getMonitoringState());
        verify(r, times(1)).switchState(WAIT_FOR_START_DETECTION);
        waitForSeRemoval.onDeactivate();

    }

    @Test
    public void smart_waitForRemoval_REPEATING() throws Exception {
        /*
         * ------------ input polling mode is CONTINUE SE has been removed within timeout
         */
        BlankSmartPresenceTheadedReader r =
                AbsSmartPresenceTheadedReaderTest.getSmartSpy(PLUGIN_NAME, READER_NAME);
        WaitForSeRemoval waitForSeRemoval =
                new WaitForSeRemoval(r, new SmartRemovalMonitoringJob(r), executorService);
        doReturn(ObservableReader.PollingMode.REPEATING).when(r).getPollingMode();
        doReturn(true).when(r).waitForCardAbsentNative();
        doNothing().when(r).processSeRemoved();
        /* test */
        waitForSeRemoval.onActivate();

        Thread.sleep(WAIT);// wait for the monitoring to act

        /* Assert */
        // Assert.assertEquals(WAIT_FOR_SE_INSERTION, r.getCurrentState().getMonitoringState());
        verify(r, times(1)).switchState(WAIT_FOR_SE_INSERTION);
        waitForSeRemoval.onDeactivate();

    }

    // @Test
    // public void smart_waitForRemoval_Timeout() throws Exception {
    // /*
    // * ------------ input SE has NOT been removed within timeout
    // */
    // long timeout = 100l;
    // BlankSmartPresenceTheadedReader r =
    // AbsSmartPresenceTheadedReaderTest.getSmartSpy(PLUGIN_NAME, READER_NAME);
    // WaitForSeRemoval waitForSeRemoval =
    // new WaitForSeRemoval(r, executorService);
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
