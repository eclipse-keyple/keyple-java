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
import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.core.seproxy.plugin.state.WaitForSeRemoval;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitForSeRemovalTest extends CoreBaseTest {


    private static final Logger logger =
            LoggerFactory.getLogger(ThreadedWaitForSeRemovalTest.class);

    final String PLUGIN_NAME = "ThreadedWaitForSeRemovalTestP";
    final String READER_NAME = "ThreadedWaitForSeRemovalTest";


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
                AbsSmartInsertionTheadedReaderTest.getMock(PLUGIN_NAME, READER_NAME, 0);
        WaitForSeRemoval waitForSeRemoval = new WaitForSeRemoval(r);

        /* test */
        waitForSeRemoval.onActivate();

        waitForSeRemoval.onEvent(AbstractObservableLocalReader.InternalEvent.SE_REMOVED);

        /* Assert */
        verify(r, times(1)).switchState(WAIT_FOR_START_DETECTION);
    }

    @Test
    public void waitForRemoval_CONTINUE() throws Exception, NoStackTraceThrowable {
        /*
         * ------------ input polling mode is CONTINUE SE has been removed within timeout
         */
        AbstractObservableLocalReader r =
                AbsSmartInsertionTheadedReaderTest.getMock(PLUGIN_NAME, READER_NAME, 0);
        WaitForSeRemoval waitForSeRemoval = new WaitForSeRemoval(r);
        doReturn(ObservableReader.PollingMode.REPEATING).when(r).getPollingMode();

        /* test */
        waitForSeRemoval.onActivate();
        waitForSeRemoval.onEvent(AbstractObservableLocalReader.InternalEvent.SE_REMOVED);


        /* Assert */
        verify(r, times(1)).switchState(WAIT_FOR_SE_INSERTION);
    }

    // @Test
    // public void waitForRemoval_Timeout() throws Exception, NoStackTraceThrowable {
    // /*
    // * ------------ input polling mode is CONTINUE SE has NOT been removed within timeout
    // */
    // AbstractObservableLocalReader r =
    // AbsSmartInsertionTheadedReaderTest.getMock(PLUGIN_NAME, READER_NAME, 0);
    // WaitForSeRemoval waitForSeRemoval = new WaitForSeRemoval(r);
    //
    // /* test */
    // waitForSeRemoval.onActivate();
    // waitForSeRemoval.onEvent(AbstractObservableLocalReader.InternalEvent.TIME_OUT);
    //
    // Thread.sleep(50l);// wait for timeout
    //
    // /* Assert */
    // verify(r, times(1)).switchState(WAIT_FOR_START_DETECTION);
    // }

}
