package org.eclipse.keyple.core.seproxy.plugin.state;

import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.core.seproxy.plugin.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.keyple.core.seproxy.plugin.state.AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION;
import static org.eclipse.keyple.core.seproxy.plugin.state.AbstractObservableState.MonitoringState.WAIT_FOR_SE_PROCESSING;
import static org.eclipse.keyple.core.seproxy.plugin.state.AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ThreadedWaitForSeRemovalTest extends CoreBaseTest {


    private static final Logger logger = LoggerFactory.getLogger(ThreadedWaitForSeRemovalTest.class);

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
            ------------ input
            polling mode is STOP
            SE has been removed within timeout
         */
        long timeout = 100l;
        AbstractThreadedObservableLocalReader r = AbsSmartInsertionTheadedReaderTest.getSmartSpy(PLUGIN_NAME,READER_NAME,0);
        ThreadedWaitForSeRemoval waitForSeRemoval = new ThreadedWaitForSeRemoval(r, timeout);
        doReturn(ObservableReader.PollingMode.STOP).when(r).getCurrentPollingMode();
        doReturn(false).when(r).isSePresentPing();

        /* test */
        waitForSeRemoval.activate();

        Thread.sleep(50l);

        /* Assert */
        //Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentState().getMonitoringState());
        verify(r, times(1)).switchState(WAIT_FOR_START_DETECTION);

        waitForSeRemoval.deActivate();

    }

    @Test
    public void waitForRemoval_CONTINUE() throws Exception, NoStackTraceThrowable {
         /*
            ------------ input
            polling mode is CONTINUE
            SE has been removed within timeout
         */
        long timeout = 100l;
        AbstractThreadedObservableLocalReader r = AbsSmartInsertionTheadedReaderTest.getSmartSpy(PLUGIN_NAME,READER_NAME,0);
        ThreadedWaitForSeRemoval waitForSeRemoval = new ThreadedWaitForSeRemoval(r, timeout);
        doReturn(ObservableReader.PollingMode.CONTINUE).when(r).getCurrentPollingMode();
        doReturn(false).when(r).isSePresentPing();

        /* test */
        waitForSeRemoval.activate();

        Thread.sleep(50l);//wait for timeout

        /* Assert */
        //Assert.assertEquals(WAIT_FOR_SE_INSERTION, r.getCurrentState().getMonitoringState());
        verify(r, times(1)).switchState(WAIT_FOR_SE_INSERTION);
        waitForSeRemoval.deActivate();

    }

    @Test
    public void waitForRemoval_Timeout() throws Exception, NoStackTraceThrowable {
         /*
            ------------ input
            polling mode is CONTINUE
            SE has NOT been removed within timeout
         */
        long timeout = 100l;
        AbstractThreadedObservableLocalReader r = AbsSmartInsertionTheadedReaderTest.getSmartSpy(PLUGIN_NAME,READER_NAME,0);
        ThreadedWaitForSeRemoval waitForSeRemoval = new ThreadedWaitForSeRemoval(r, timeout);
        doReturn(true).when(r).isSePresentPing();

        /* test */
        waitForSeRemoval.activate();

        Thread.sleep(50l);//wait for timeout

        /* Assert */
        //Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentState().getMonitoringState());
        verify(r, times(1)).switchState(WAIT_FOR_START_DETECTION);
        waitForSeRemoval.deActivate();

    }


    @Test
    public void smart_waitForRemoval_STOP() throws Exception, NoStackTraceThrowable {
        /*
            ------------ input
            polling mode is STOP
            SE has been removed within timeout
         */
        long timeout = 100l;
        BlankSmartPresenceTheadedReader r = AbsSmartPresenceTheadedReaderTest.getSmartSpy(PLUGIN_NAME,READER_NAME);
        ThreadedWaitForSeRemoval waitForSeRemoval = new ThreadedWaitForSeRemoval(r, timeout);
        doReturn(ObservableReader.PollingMode.STOP).when(r).getCurrentPollingMode();
        doReturn(true).when(r).waitForCardAbsentNative(timeout);

        /* test */
        waitForSeRemoval.activate();

        Thread.sleep(50l);

        /* Assert */
        //Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentState().getMonitoringState());
        verify(r, times(1)).switchState(WAIT_FOR_START_DETECTION);
        waitForSeRemoval.deActivate();

    }

    @Test
    public void smart_waitForRemoval_CONTINUE() throws Exception, NoStackTraceThrowable {
         /*
            ------------ input
            polling mode is CONTINUE
            SE has been removed within timeout
         */
        long timeout = 100l;
        BlankSmartPresenceTheadedReader r = AbsSmartPresenceTheadedReaderTest.getSmartSpy(PLUGIN_NAME,READER_NAME);
        ThreadedWaitForSeRemoval waitForSeRemoval = new ThreadedWaitForSeRemoval(r, timeout);
        doReturn(ObservableReader.PollingMode.CONTINUE).when(r).getCurrentPollingMode();
        doReturn(true).when(r).waitForCardAbsentNative(timeout);

        /* test */
        waitForSeRemoval.activate();

        Thread.sleep(50l);//wait

        /* Assert */
        //Assert.assertEquals(WAIT_FOR_SE_INSERTION, r.getCurrentState().getMonitoringState());
        verify(r, times(1)).switchState(WAIT_FOR_SE_INSERTION);
        waitForSeRemoval.deActivate();

    }

    @Test
    public void smart_waitForRemoval_Timeout() throws Exception, NoStackTraceThrowable {
         /*
            ------------ input
            SE has NOT been removed within timeout
         */
        long timeout = 100l;
        BlankSmartPresenceTheadedReader r = AbsSmartPresenceTheadedReaderTest.getSmartSpy(PLUGIN_NAME,READER_NAME);
        ThreadedWaitForSeRemoval waitForSeRemoval = new ThreadedWaitForSeRemoval(r, timeout);
        doReturn(false).when(r).waitForCardAbsentNative(timeout);

        /* test */
        waitForSeRemoval.activate();

        Thread.sleep(50l);//wait for timeout

        /* Assert */
        //Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentState().getMonitoringState());
        verify(r, times(1)).switchState(WAIT_FOR_START_DETECTION);
        waitForSeRemoval.deActivate();

    }

}
