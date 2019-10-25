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
import static org.eclipse.keyple.core.seproxy.plugin.state.AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DefaultWaitForSeRemovalTest extends CoreBaseTest {


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
        AbstractThreadedObservableLocalReader r = AbsSmartInsertionTheadedReaderTest.getSmartSpy(PLUGIN_NAME,READER_NAME,0);
        DefaultWaitForSeRemoval waitForSeRemoval = new DefaultWaitForSeRemoval(r);

        /* test */
        waitForSeRemoval.activate();

        waitForSeRemoval.onEvent(AbstractObservableLocalReader.InternalEvent.SE_REMOVED);

        /* Assert */
        verify(r, times(1)).switchState(WAIT_FOR_START_DETECTION);
    }

    @Test
    public void waitForRemoval_CONTINUE() throws Exception, NoStackTraceThrowable {
         /*
            ------------ input
            polling mode is CONTINUE
            SE has been removed within timeout
         */
        AbstractThreadedObservableLocalReader r = AbsSmartInsertionTheadedReaderTest.getSmartSpy(PLUGIN_NAME,READER_NAME,0);
        DefaultWaitForSeRemoval waitForSeRemoval = new DefaultWaitForSeRemoval(r);
        doReturn(ObservableReader.PollingMode.CONTINUE).when(r).getCurrentPollingMode();

        /* test */
        waitForSeRemoval.activate();
        waitForSeRemoval.onEvent(AbstractObservableLocalReader.InternalEvent.SE_REMOVED);


        /* Assert */
        verify(r, times(1)).switchState(WAIT_FOR_SE_INSERTION);
    }

    @Test
    public void waitForRemoval_Timeout() throws Exception, NoStackTraceThrowable {
         /*
            ------------ input
            polling mode is CONTINUE
            SE has NOT been removed within timeout
         */
        AbstractThreadedObservableLocalReader r = AbsSmartInsertionTheadedReaderTest.getSmartSpy(PLUGIN_NAME,READER_NAME,0);
        DefaultWaitForSeRemoval waitForSeRemoval = new DefaultWaitForSeRemoval(r);

        /* test */
        waitForSeRemoval.activate();
        waitForSeRemoval.onEvent(AbstractObservableLocalReader.InternalEvent.TIME_OUT);

        Thread.sleep(50l);//wait for timeout

        /* Assert */
        verify(r, times(1)).switchState(WAIT_FOR_START_DETECTION);    }

}
