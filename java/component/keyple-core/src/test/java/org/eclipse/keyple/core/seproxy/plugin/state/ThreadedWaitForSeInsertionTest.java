package org.eclipse.keyple.core.seproxy.plugin.state;

import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.core.seproxy.plugin.AbsSmartInsertionTheadedReaderTest;
import org.eclipse.keyple.core.seproxy.plugin.AbstractThreadedObservableLocalReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.keyple.core.seproxy.plugin.state.AbstractObservableState.MonitoringState.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ThreadedWaitForSeInsertionTest extends CoreBaseTest {


    private static final Logger logger = LoggerFactory.getLogger(ThreadedWaitForSeInsertionTest.class);

    final String PLUGIN_NAME = "ThreadedWaitForSeInsertionTestP";
    final String READER_NAME = "ThreadedWaitForSeInsertionTest";

    AbstractObservableState waitForInsert;
    AbstractThreadedObservableLocalReader r;
    long timeout;

    @Before
    public void setUp() {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");

         timeout= 50l;

        r = AbsSmartInsertionTheadedReaderTest.getSmartSpy(PLUGIN_NAME,READER_NAME,1);
        waitForInsert = new ThreadedWaitForSeInsertion(r,timeout);
    }

    @Before
    public void tearDown() {
        logger.info("******************************");
        logger.info("End of Test {}", name.getMethodName() + "");
        logger.info("\"******************************");

        waitForInsert.deActivate();
    }


    @Test
    public void insertSe_matched() throws Exception, NoStackTraceThrowable {
        /* input
         *   SE inserted
         *   SE matched
         */
        //se matched
        doReturn(true).when(r).processSeInserted();

        ThreadedWaitForSeInsertion waitForInsert = new ThreadedWaitForSeInsertion(r, timeout);

        /* test */
        waitForInsert.activate();

        Thread.sleep(20l);

        /* Assert */
        //Assert.assertEquals(WAIT_FOR_SE_PROCESSING, r.getCurrentState().getMonitoringState());
        verify(r, times(1)).switchState(WAIT_FOR_SE_PROCESSING);

    }

    @Test
    public void testInsertSe_Notmatched() throws Exception, NoStackTraceThrowable {
        /* input
         *   SE inserted
         *   SE doesnt matched
         */
        //se not matched
        doReturn(false).when(r).processSeInserted();

        ThreadedWaitForSeInsertion waitForInsert = new ThreadedWaitForSeInsertion(r, timeout);

        /* test */
        waitForInsert.activate();

        Thread.sleep(20l);

        /* Assert */
        //Assert.assertEquals(WAIT_FOR_SE_REMOVAL, r.getCurrentState().getMonitoringState());
        verify(r, times(1)).switchState(WAIT_FOR_SE_REMOVAL);

    }

    @Test
    public void testTimeout() throws Exception, NoStackTraceThrowable {
        /* input
         *   no SE inserted within timeout
         */
        r = AbsSmartInsertionTheadedReaderTest.getSmartSpy(PLUGIN_NAME,READER_NAME,0);
        waitForInsert = new ThreadedWaitForSeInsertion(r,timeout);

        /* test */
        waitForInsert.activate();

        Thread.sleep(70l);//wait for timeout

        /* Assert */
        //Assert.assertEquals(WAIT_FOR_SE_INSERTION, r.getCurrentState().getMonitoringState());
        verify(r, times(1)).switchState(WAIT_FOR_SE_INSERTION);
    }

}
