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

import static org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableState.MonitoringState.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.plugin.local.monitoring.SmartInsertionMonitoringJob;
import org.eclipse.keyple.core.seproxy.plugin.local.state.WaitForSeInsertion;
import org.eclipse.keyple.core.seproxy.plugin.mock.BlankSmartInsertionTheadedReader;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitForSeInsertionJobExecutorTest extends CoreBaseTest {


    private static final Logger logger =
            LoggerFactory.getLogger(WaitForSeInsertionJobExecutorTest.class);

    final String READER_NAME = "WaitForSeInsertionJobExecutorTest";

    AbstractObservableState waitForInsert;
    BlankSmartInsertionTheadedReader r;
    long timeout;
    ExecutorService executorService = Executors.newSingleThreadExecutor();;


    @Before
    public void setUp() {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");

        /*
         * Setup new parameters for each tests
         */
        timeout = 100l;
        // executorService = Executors.newSingleThreadExecutor();
        r = AbsSmartInsertionTheadedReaderTest.getMock(READER_NAME);
        waitForInsert =
                new WaitForSeInsertion(r, new SmartInsertionMonitoringJob(r), executorService);
    }

    @Before
    public void tearDown() {
        logger.info("******************************");
        logger.info("End of Test {}", name.getMethodName() + "");
        logger.info("\"******************************");

        waitForInsert.onDeactivate();

        // shutdown executorService, no tasks should be left actived
        // List<Runnable> activeTask = executorService.shutdownNow();
        // Assert.assertTrue(activeTask.isEmpty());
    }


    @Test
    public void insertSe_matched() throws Exception {
        /*
         * input SE inserted SE matched
         */
        // se matched
        doReturn(true).when(r).processSeInserted();
        doReturn(true).when(r).waitForCardPresent();

        /* test */
        waitForInsert.onActivate();

        Thread.sleep(20l);

        /* Assert */
        // Assert.assertEquals(WAIT_FOR_SE_PROCESSING, r.getCurrentState().getMonitoringState());
        verify(r, times(1)).switchState(WAIT_FOR_SE_PROCESSING);

    }

    @Test
    public void testInsertSe_Notmatched() throws Exception {
        /*
         * input SE inserted SE doesnt matched
         */
        // se not matched
        doReturn(false).when(r).processSeInserted();
        doReturn(true).when(r).waitForCardPresent();

        /* test */
        waitForInsert.onActivate();

        Thread.sleep(20l);

        /* Assert */
        // switched to the same state to relaunch the monitoring job
        verify(r, times(1)).switchState(WAIT_FOR_SE_INSERTION);
    }

    // @Test
    // public void testTimeout() throws Exception, NoStackTraceThrowable {
    // /*
    // * input no SE inserted within timeout
    // */
    // r = AbsSmartInsertionTheadedReaderTest.getMock(PLUGIN_NAME, READER_NAME, 0);
    // waitForInsert = new WaitForSeInsertion(r, executorService);
    //
    // /* test */
    // waitForInsert.onActivate();
    //
    // Thread.sleep(70l);// wait for timeout
    //
    // /* Assert */
    // // Assert.assertEquals(WAIT_FOR_SE_INSERTION, r.getCurrentState().getMonitoringState());
    // verify(r, times(1)).switchState(WAIT_FOR_SE_INSERTION);
    // }

}
