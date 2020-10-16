/* **************************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.seproxy.plugin.reader;

import static org.eclipse.keyple.core.seproxy.plugin.reader.AbstractObservableState.MonitoringState.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.concurrent.CountDownLatch;
import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
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

@RunWith(Parameterized.class)
public class AbsSmartInsertionTheadedReaderTest extends CoreBaseTest {

  private static final Logger logger =
      LoggerFactory.getLogger(AbsSmartInsertionTheadedReaderTest.class);

  final String PLUGIN_NAME = "AbsSmartInsertionTheadedReaderTestP";
  final String READER_NAME = "AbsSmartInsertionTheadedReaderTest";

  BlankSmartInsertionTheadedReader r;

  // Execute tests X times
  @Parameterized.Parameters
  public static Object[][] data() {
    int x = 0;
    return new Object[x][0];
  }

  @Before
  public void setUp() {
    logger.info("------------------------------");
    logger.info("Test {}", name.getMethodName() + "");
    logger.info("------------------------------");
  }

  /*
   */
  @After
  public void tearDown() throws Throwable {
    r.clearObservers();
    r = null;
  }

  /*
   * Observers management + Thread instantiation
   */
  @Test
  public void addObserver() throws Exception {
    r = getBlank(PLUGIN_NAME, READER_NAME, 0);

    // add observer
    r.addObserver(getObs());

    // should the thread start
    Assert.assertEquals(1, r.countObservers());
    Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentMonitoringState());
  }

  @Test
  public void removeObserver() throws Exception {
    r = getBlank(PLUGIN_NAME, READER_NAME, 0);
    ObservableReader.ReaderObserver obs = getObs();

    // add and remove observer
    r.addObserver(obs);
    r.removeObserver(obs);

    // should the thread start
    Assert.assertEquals(0, r.countObservers());
    Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentMonitoringState());
  }

  @Test
  public void clearObservers() throws Exception {
    r = getBlank(PLUGIN_NAME, READER_NAME, 0);

    // add and remove observer
    r.addObserver(getObs());
    r.clearObservers();

    // should the thread start and stop
    Assert.assertEquals(0, r.countObservers());
    Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentMonitoringState());
  }

  /*
   * SMART CARD DETECTION
   */

  @Test
  public void startSeDetection() throws Exception {
    // do not present any card for this test
    r = getBlank(PLUGIN_NAME, READER_NAME, 0);

    r.addObserver(getObs());
    r.startSeDetection(ObservableReader.PollingMode.SINGLESHOT);

    Thread.sleep(500);
    Assert.assertEquals(WAIT_FOR_SE_INSERTION, r.getCurrentMonitoringState());

    r.stopSeDetection();
  }

  @Test
  public void stopSeDetection() throws Exception {
    // do not present any card for this test
    r = getBlank(PLUGIN_NAME, READER_NAME, 0);

    r.addObserver(getObs());
    r.startSeDetection(ObservableReader.PollingMode.SINGLESHOT);
    r.stopSeDetection();

    Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentMonitoringState());
  }

  /*
   * isCardPresentPing
   */

  @Test
  public void isCardPresentPing_true() throws Exception {
    r = getSmartSpy(PLUGIN_NAME, READER_NAME, 0);
    doReturn(ByteArrayUtil.fromHex("00")).when(r).transmitApdu(any(byte[].class));
    Assert.assertEquals(true, r.isCardPresentPing());
  }

  @Test
  public void isCardPresentPing_false() throws Exception {
    r = getSmartSpy(PLUGIN_NAME, READER_NAME, 0);
    doThrow(new KeypleReaderIOException("ping failed")).when(r).transmitApdu(any(byte[].class));

    Assert.assertEquals(false, r.isCardPresentPing());
  }

  /*
   * @Test public void cardDetected_notMatched() throws Exception {
   *
   * r = getSmartSpy(PLUGIN_NAME, READER_NAME, 1);// present one card once for this test
   * doReturn(false).when(r).processCardInserted();
   *
   * r.addObserver(getObs()); Thread.sleep(100);
   * r.startSeDetection(ObservableReader.PollingMode.SINGLESHOT); Thread.sleep(100);
   *
   * Assert.assertEquals(WAIT_FOR_SE_REMOVAL, r.getCurrentMonitoringState());
   *
   * }
   *
   * @Test public void cardDetected_matched() throws Exception { r = getSmartSpy(PLUGIN_NAME,
   * READER_NAME, 1);// present one card once for this test
   *
   * doReturn(true).when(r).processCardInserted(); r.addObserver(getObs()); Thread.sleep(100);
   * r.startSeDetection(ObservableReader.PollingMode.REPEATING);
   *
   * Thread.sleep(100);
   *
   * Assert.assertEquals(WAIT_FOR_SE_PROCESSING, r.getCurrentMonitoringState()); }
   *
   *
   *
   * @Test public void startRemovalSequence_CONTINUE() throws Exception { r =
   * getSmartSpy(PLUGIN_NAME, READER_NAME, 1);// present one card once for this test
   *
   * doReturn(true).when(r).processCardInserted();
   *
   * r.addObserver(getObs()); Thread.sleep(100);
   * r.startSeDetection(ObservableReader.PollingMode.REPEATING); Thread.sleep(100);
   * r.startRemovalSequence(); Thread.sleep(100);
   *
   * Assert.assertEquals(WAIT_FOR_SE_REMOVAL, r.getCurrentMonitoringState()); }
   *
   * @Test public void startRemovalSequence_STOP() throws Exception { r = getSmartSpy(PLUGIN_NAME,
   * READER_NAME, 1);// present one card once for this test
   * doReturn(true).when(r).processCardInserted();
   *
   * r.addObserver(getObs()); r.startSeDetection(ObservableReader.PollingMode.SINGLESHOT);
   * //Thread.sleep(100); r.startRemovalSequence(); //Thread.sleep(100);
   *
   * Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentMonitoringState()); }
   *
   * @Test public void cardProcessing_timeout() throws Exception { r = getSmartSpy(PLUGIN_NAME,
   * READER_NAME, 1);// present one card once for this test
   * doReturn(true).when(r).processCardInserted();
   *
   * CountDownLatch lock = new CountDownLatch(1);
   *
   * // configure reader to raise timeout if SeProcessing is too long r.setThreadWaitTimeout(100);
   * // attach observer to detect TIMEOUT_EVENT r.addObserver(countDownOnTimeout(lock));
   *
   * // Thread.sleep(100); r.startSeDetection(ObservableReader.PollingMode.SINGLESHOT);
   * lock.await(5000, TimeUnit.MILLISECONDS);
   *
   * Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentMonitoringState());
   * Assert.assertEquals(0, lock.getCount()); }
   *
   * @Test public void cardRemoval_timeout() throws Exception { r = getSmartSpy(PLUGIN_NAME,
   * READER_NAME, 1);// present one card once for this test
   *
   * CountDownLatch lock = new CountDownLatch(1); doReturn(true).when(r).processCardInserted();
   *
   * // configure reader to raise timeout if SeProcessing is too long r.setThreadWaitTimeout(300);
   * // attach observer to detect TIMEOUT_EVENT r.addObserver(countDownOnTimeout(lock));
   *
   * Thread.sleep(100); r.startSeDetection(ObservableReader.PollingMode.REPEATING);
   * Thread.sleep(100); r.startRemovalSequence(); lock.await(5000, TimeUnit.MILLISECONDS);
   *
   * Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentMonitoringState());
   * Assert.assertEquals(0, lock.getCount()); }
   *
   * @Test public void cardRemoval_sePresence_CONTINUE() throws Exception {
   *
   * r = getSmartSpy(PLUGIN_NAME, READER_NAME, 1);// present one card once for this test
   * doReturn(true).when(r).processCardInserted();
   *
   * // Card removed doThrow(new
   * KeypleReaderIOException("ping failed")).when(r).transmitApdu(any(byte[].class));
   *
   * r.addObserver(getObs()); //Thread.sleep(100);
   * r.startSeDetection(ObservableReader.PollingMode.REPEATING); //Thread.sleep(500);
   * r.startRemovalSequence(); //Thread.sleep(100);
   *
   * Assert.assertEquals(WAIT_FOR_SE_INSERTION, r.getCurrentMonitoringState()); }
   *
   * @Test public void cardRemoval_sePresence_STOP() throws Exception { r = getSmartSpy(PLUGIN_NAME,
   * READER_NAME, 1);// present one card once for this test
   *
   * doReturn(true).when(r).processCardInserted(); // Card removed doThrow(new
   * KeypleReaderIOException("ping failed")).when(r).transmitApdu(any(byte[].class));
   *
   *
   * r.addObserver(getObs()); Thread.sleep(100);
   * r.startSeDetection(ObservableReader.PollingMode.SINGLESHOT);
   *
   * Thread.sleep(100);
   *
   * r.startRemovalSequence(); Thread.sleep(100);
   *
   * Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentMonitoringState()); }
   *
   * @Test public void cardRemoval_finalized() throws Throwable { r = getSmartSpy(PLUGIN_NAME,
   * READER_NAME, 1);// present one card once for this test
   *
   * doReturn(true).when(r).processCardInserted(); // Card removed doThrow(new
   * KeypleReaderIOException("ping failed")).when(r).transmitApdu(any(byte[].class));
   *
   *
   * r.addObserver(getObs()); Thread.sleep(100);
   * r.startSeDetection(ObservableReader.PollingMode.SINGLESHOT); Thread.sleep(100);
   * r.startRemovalSequence();
   *
   * Assert.assertEquals(null, r.getCurrentMonitoringState()); }
   *
   */

  /*
   * Helpers
   */

  public static BlankSmartInsertionTheadedReader getSmartSpy(
      String pluginName, String readerName, Integer mockDetect) {
    BlankSmartInsertionTheadedReader r =
        Mockito.spy(new BlankSmartInsertionTheadedReader(pluginName, readerName, mockDetect));
    return r;
  }

  public static BlankSmartInsertionTheadedReader getBlank(
      String pluginName, String readerName, Integer mockDetect) {
    BlankSmartInsertionTheadedReader r =
        new BlankSmartInsertionTheadedReader(pluginName, readerName, mockDetect);
    return r;
  }

  public static BlankSmartInsertionTheadedReader getMock(String readerName) {
    BlankSmartInsertionTheadedReader r = Mockito.mock(BlankSmartInsertionTheadedReader.class);
    doReturn(readerName).when(r).getName();
    return r;
  }

  public static ObservableReader.ReaderObserver getObs() {
    return new ObservableReader.ReaderObserver() {
      @Override
      public void update(ReaderEvent event) {}
    };
  }

  public static ObservableReader.ReaderObserver countDownOnTimeout(final CountDownLatch lock) {
    return new ObservableReader.ReaderObserver() {
      @Override
      public void update(ReaderEvent event) {
        if (ReaderEvent.EventType.TIMEOUT_ERROR.equals(event.getEventType())) {
          lock.countDown();
        }
      }
    };
  }
}
