package org.eclipse.keyple.core.seproxy.plugin.state;

import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.core.seproxy.plugin.AbsSmartInsertionTheadedReaderTest;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableLocalReader;
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

public class DefaultWaitForSeInsertionTest extends CoreBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(ThreadedWaitForSeRemovalTest.class);

    final String PLUGIN_NAME = "DefaultWaitForSeInsertionTestP";
    final String READER_NAME = "DefaultWaitForSeInsertionTest";

    AbstractObservableState waitForInsert;
    AbstractThreadedObservableLocalReader r;

    @Before
    public void setUp() {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");

        r = AbsSmartInsertionTheadedReaderTest.getSmartSpy(PLUGIN_NAME,READER_NAME,1);
        waitForInsert = new DefaultWaitForSeInsertion(r);

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
        doReturn(true).when(r).processSeInserted();

        /* test */
        waitForInsert.activate();
        waitForInsert.onEvent(AbstractObservableLocalReader.InternalEvent.SE_INSERTED);

        /* Assert */
        verify(r, times(1)).switchState(WAIT_FOR_SE_PROCESSING);

        //Assert.assertEquals(WAIT_FOR_SE_PROCESSING, r.getCurrentState().getMonitoringState());

    }

    @Test
    public void testInsertSe_Notmatched() throws Exception {
        /* input
         *   SE inserted
         *   SE doesnt matched
         */
        doReturn(false).when(r).processSeInserted();

        /* test */
        waitForInsert.activate();
        waitForInsert.onEvent(AbstractObservableLocalReader.InternalEvent.SE_INSERTED);
        /* Assert */
        verify(r, times(1)).switchState(WAIT_FOR_SE_REMOVAL);
    }

    @Test
    public void testTimeout() throws Exception {
        /* input
         *   no SE inserted within timeout
         */

        /* test */
        waitForInsert.activate();
        waitForInsert.onEvent(AbstractObservableLocalReader.InternalEvent.TIME_OUT);

        /* Assert */
        verify(r, times(1)).switchState(WAIT_FOR_SE_INSERTION);
    }

}
