package org.eclipse.keyple.core.seproxy.plugin;

import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.eclipse.keyple.core.seproxy.plugin.AbstractObservableLocalReader.MonitoringState.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@RunWith(Parameterized.class)
public class AbsSmartInsertionTheadedReaderTest extends CoreBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(AbsSmartInsertionTheadedReaderTest.class);


    final String PLUGIN_NAME = "AbsSmartInsertionTheadedReaderTestP";
    final String READER_NAME = "AbsSmartInsertionTheadedReaderTest";

    BlankSmartInsertionTheadedReader r;

    //Execute tests 10 times
    @Parameterized.Parameters
    public static Object[][] data() {
        return new Object[10][0];
    }


    @Before
    public void setUp() throws KeypleReaderException {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");

       r = getSmartSpy(PLUGIN_NAME, READER_NAME);
    }

    /*
     */
    @After
    public void tearDown() throws Throwable {
            r.clearObservers();
            r.finalize();
            r = null;

    }

    /*
     * Observers management + Thread instanciation
     */
    @Test
    public void addObserver() throws Exception{
        //add observer
        r.addObserver(getObs());

        //should the thread start
        Assert.assertEquals(1, r.countObservers());
        Assert.assertEquals(WAIT_FOR_SE_INSERTION, r.getMonitoringState());
    }

    @Test
    public void removeObserver() throws Exception{
        ObservableReader.ReaderObserver obs = getObs();

        //add and remove observer
        r.addObserver(obs);
        r.removeObserver(obs);

        //should the thread start
        Assert.assertEquals(0, r.countObservers());
        Assert.assertEquals(null, r.getMonitoringState());
    }

    @Test
    public void clearObservers() throws Exception{
        //add and remove observer
        r.addObserver(getObs());
        r.clearObservers();

        //should the thread start and stop
        Assert.assertEquals(0, r.countObservers());
        Assert.assertEquals(null, r.getMonitoringState());
    }


    /*
     * SMART CARD DETECTION
     */

    @Test
    public void stopSeDetection() throws Exception{
        //do not present any card for this test
        doReturn(false).when(r).waitForCardPresent(any(long.class));

        r.addObserver(getObs());
        Thread.sleep(100);
        r.stopSeDetection();
        Thread.sleep(100);

        Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getMonitoringState());
    }

    @Test
    public void startSeDetection() throws Exception{
        //do not present any card for this test
        doReturn(false).when(r).waitForCardPresent(any(long.class));

        r.addObserver(getObs());
        Thread.sleep(100);
        r.stopSeDetection();
        Thread.sleep(100);
        r.startSeDetection(ObservableReader.PollingMode.STOP);
        Thread.sleep(100);

        Assert.assertEquals(WAIT_FOR_SE_INSERTION, r.getMonitoringState());

    }

    @Test
    public void seDetected_notMatched() throws Exception{

        doReturn(false).when(r).processSeInserted();
        doReturn(true).when(r).waitForCardPresent(any(long.class));

        r.addObserver(getObs());
        Thread.sleep(100);
        r.stopSeDetection();
        Thread.sleep(100);
        r.startSeDetection(ObservableReader.PollingMode.STOP);
        Thread.sleep(100);

        Assert.assertEquals(WAIT_FOR_SE_REMOVAL, r.getMonitoringState());

    }

    @Test
    public void seDetected_matched() throws Exception{

        doReturn(true).when(r).processSeInserted();
        doReturn(true).when(r).waitForCardPresent(any(long.class));

        r.addObserver(getObs());
        Thread.sleep(100);
        r.stopSeDetection();
        Thread.sleep(100);
        r.startSeDetection(ObservableReader.PollingMode.CONTINUE);

        Thread.sleep(100);

        Assert.assertEquals(WAIT_FOR_SE_PROCESSING, r.getMonitoringState());
    }

    @Test
    public void startRemovalSequence() throws Exception{

        doReturn(true).when(r).processSeInserted();
        doReturn(false).when(r).waitForCardPresent(any(long.class));

        r.addObserver(getObs());
        Thread.sleep(100);
        r.startRemovalSequence();
        Thread.sleep(100);

        //does nothing
        Assert.assertEquals(WAIT_FOR_SE_INSERTION, r.getMonitoringState());
    }

    @Test
    public void startRemovalSequence_CONTINUE() throws Exception{

        doReturn(true).when(r).processSeInserted();
        doReturn(true).when(r).waitForCardPresent(any(long.class));

        r.addObserver(getObs());
        Thread.sleep(100);
        r.startSeDetection(ObservableReader.PollingMode.CONTINUE);
        Thread.sleep(100);
        r.startRemovalSequence();
        Thread.sleep(100);

        Assert.assertEquals(WAIT_FOR_SE_REMOVAL, r.getMonitoringState());
    }

    @Test
    public void startRemovalSequence_STOP() throws Exception{
        doReturn(true).when(r).processSeInserted();
        doReturn(true).when(r).waitForCardPresent(any(long.class));

        r.addObserver(getObs());
        r.startSeDetection(ObservableReader.PollingMode.STOP);
        Thread.sleep(100);
        r.startRemovalSequence();
        Thread.sleep(100);

        Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getMonitoringState());
    }

    @Test
    public void seProcessing_timeout() throws Exception{

        CountDownLatch lock = new CountDownLatch(1);
        doReturn(true).when(r).processSeInserted();
        doReturn(true).when(r).waitForCardPresent(any(long.class));

        //configure reader to raise timeout if SeProcessing is too long
        r.setThreadWaitTimeout(100);
        //attach observer to detect TIMEOUT_EVENT
        r.addObserver(countDownOnTimeout(lock));

        Thread.sleep(100);
        r.startSeDetection(ObservableReader.PollingMode.CONTINUE);
        lock.await(5000, TimeUnit.MILLISECONDS);

        Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getMonitoringState());
        Assert.assertEquals(0 , lock.getCount());
    }

    @Test
    public void seRemoval_timeout() throws Exception{

        CountDownLatch lock = new CountDownLatch(1);
        doReturn(true).when(r).processSeInserted();
        doReturn(true).when(r).waitForCardPresent(any(long.class));

        //configure reader to raise timeout if SeProcessing is too long
        r.setThreadWaitTimeout(300);
        //attach observer to detect TIMEOUT_EVENT
        r.addObserver(countDownOnTimeout(lock));

        Thread.sleep(100);
        r.startSeDetection(ObservableReader.PollingMode.CONTINUE);
        Thread.sleep(100);
        r.startRemovalSequence();
        lock.await(5000, TimeUnit.MILLISECONDS);

        Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getMonitoringState());
        Assert.assertEquals(0 , lock.getCount());
    }

    @Test
    public void seRemoval_sePresence_CONTINUE() throws Exception{

        doReturn(true).when(r).processSeInserted();
        doReturn(true).when(r).waitForCardPresent(any(long.class));
        //Card removed
        doThrow(new KeypleIOReaderException("ping failed")).when(r).transmitApdu(any(byte[].class));

        r.addObserver(getObs());
        Thread.sleep(100);
        r.startSeDetection(ObservableReader.PollingMode.CONTINUE);
        Thread.sleep(100);

        doReturn(false).when(r).waitForCardPresent(any(long.class));

        r.startRemovalSequence();
        Thread.sleep(100);

        Assert.assertEquals(WAIT_FOR_SE_INSERTION, r.getMonitoringState());
    }

    @Test
    public void seRemoval_sePresence_STOP() throws Exception{

        doReturn(true).when(r).processSeInserted();
        doReturn(true).when(r).waitForCardPresent(any(long.class));
        //Card removed
        doThrow(new KeypleIOReaderException("ping failed")).when(r).transmitApdu(any(byte[].class));


        r.addObserver(getObs());
        Thread.sleep(100);
        r.startSeDetection(ObservableReader.PollingMode.STOP);

        doReturn(false).when(r).waitForCardPresent(any(long.class));
        Thread.sleep(100);

        r.startRemovalSequence();
        Thread.sleep(100);

        Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getMonitoringState());
    }

    @Test
    public void seRemoval_finalized() throws Throwable {

        doReturn(true).when(r).processSeInserted();
        doReturn(true).when(r).waitForCardPresent(any(long.class));
        //Card removed
        doThrow(new KeypleIOReaderException("ping failed")).when(r).transmitApdu(any(byte[].class));


        r.addObserver(getObs());
        Thread.sleep(100);
        r.startSeDetection(ObservableReader.PollingMode.STOP);
        Thread.sleep(100);
        r.startRemovalSequence();
        r.finalize();

        Assert.assertEquals(null, r.getMonitoringState());
    }

    /*
     * isSePresentPing
     */

    @Test
    public void isSePresentPing_true() throws Exception{
        doReturn(ByteArrayUtil.fromHex("00")).when(r).transmitApdu(any(byte[].class));

        Assert.assertEquals(true, r.isSePresentPing());
    }

    @Test
    public void isSePresentPing_false() throws Exception{
        doThrow(new KeypleIOReaderException("ping failed")).when(r).transmitApdu(any(byte[].class));

        Assert.assertEquals(false, r.isSePresentPing());
    }


    /*
     * Monitoring state
     */
    @Test
    public void noThread() throws Exception{
        Assert.assertEquals(null, r.getMonitoringState());
    }


    /*
     * Helpers
     */

    static public BlankSmartInsertionTheadedReader getSmartSpy(String pluginName, String readerName) throws KeypleReaderException {
        BlankSmartInsertionTheadedReader r =  Mockito.spy(new BlankSmartInsertionTheadedReader(pluginName,readerName));
        return  r;
    }


    static public ObservableReader.ReaderObserver getObs(){
        return  new ObservableReader.ReaderObserver() {@Override  public void update(ReaderEvent event) {}};
    }

    static public ObservableReader.ReaderObserver countDownOnTimeout(final CountDownLatch lock){
        return  new ObservableReader.ReaderObserver() {@Override  public void update(ReaderEvent event) {
            if(ReaderEvent.EventType.TIMEOUT_ERROR.equals(event.getEventType())){
                lock.countDown();
            }
        }};
    }

}
