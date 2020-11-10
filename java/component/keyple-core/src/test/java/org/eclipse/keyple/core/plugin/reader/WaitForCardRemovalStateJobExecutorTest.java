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
package org.eclipse.keyple.core.plugin.reader;

import static org.eclipse.keyple.core.plugin.reader.AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION;
import static org.eclipse.keyple.core.plugin.reader.AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION;
import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class WaitForCardRemovalStateJobExecutorTest extends CoreBaseTest {

  private static final Logger logger =
      LoggerFactory.getLogger(WaitForCardRemovalStateJobExecutorTest.class);

  final String PLUGIN_NAME = "WaitForCardRemovalJobExecutorTestP";
  final String READER_NAME = "WaitForCardRemovalStateJobExecutorTest";

  final ExecutorService executorService = Executors.newSingleThreadExecutor();

  final Long WAIT = 500l;

  static final Integer X_TIMES = 5; // run tests multiple times to reproduce flaky

  @Parameterized.Parameters
  public static Object[][] data() {
    return new Object[X_TIMES][0];
  }

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
     * card has been removed
     */
    AbstractObservableLocalReader r = AbsSmartInsertionTheadedReaderTest.getMock(READER_NAME);
    WaitForCardRemovalState waitForCardRemovalState =
        new WaitForCardRemovalState(r, new CardAbsentPingMonitoringJob(r), executorService);
    doReturn(ObservableReader.PollingMode.SINGLESHOT).when(r).getPollingMode();
    doReturn(false).when(r).isCardPresentPing();
    doNothing().when(r).processCardRemoved();
    /* test */
    waitForCardRemovalState.onActivate();

    Thread.sleep(WAIT); // wait for the monitoring to act

    /* Assert */
    // Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentState().getMonitoringState());
    verify(r, times(1)).switchState(WAIT_FOR_START_DETECTION);

    waitForCardRemovalState.onDeactivate();
  }

  @Test
  public void waitForRemoval_REPEATING() throws Exception {
    /*
     * ------------ input polling mode is CONTINUE card has been removed within timeout
     */
    AbstractObservableLocalReader r = AbsSmartInsertionTheadedReaderTest.getMock(READER_NAME);
    WaitForCardRemovalState waitForCardRemovalState =
        new WaitForCardRemovalState(r, new CardAbsentPingMonitoringJob(r), executorService);
    doReturn(ObservableReader.PollingMode.REPEATING).when(r).getPollingMode();
    doReturn(false).when(r).isCardPresentPing();
    doNothing().when(r).processCardRemoved();

    /* test */
    waitForCardRemovalState.onActivate();

    Thread.sleep(WAIT); // wait for the monitoring to act

    /* Assert */
    // Assert.assertEquals(WAIT_FOR_SE_INSERTION, r.getCurrentState().getMonitoringState());
    verify(r, times(1)).switchState(WAIT_FOR_SE_INSERTION);
    waitForCardRemovalState.onDeactivate();
  }

  @Test
  public void waitForRemoval_STOP() throws Exception {
    /*
     * ------------ input
     *
     * polling mode is SINGLESHOT
     *
     * card has been removed
     */
    AbstractObservableLocalReader r = AbsSmartInsertionTheadedReaderTest.getMock(READER_NAME);
    AbstractObservableState stateMock = Mockito.mock(AbstractObservableState.class);
    CardAbsentPingMonitoringJob jobControl = new CardAbsentPingMonitoringJob(r);
    doReturn(ObservableReader.PollingMode.SINGLESHOT).when(r).getPollingMode();
    doReturn(true).when(r).isCardPresentPing();
    doNothing().when(r).processCardRemoved();
    /* test */
    Runnable task = jobControl.getMonitoringJob(stateMock);
    Future future = executorService.submit(task);

    Thread.sleep(200);

    jobControl.stop();

    Thread.sleep(WAIT); // wait for the monitoring to act

    /* Assert */
    // Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentState().getMonitoringState());
    verify(r, times(0)).switchState(WAIT_FOR_START_DETECTION);
    Assert.assertTrue(future.isDone());
  }

  @Test
  public void smart_waitForRemoval_SINGLESHOT() throws Exception {
    /*
     * ------------ input polling mode is STOP card has been removed within timeout
     */
    BlankSmartPresenceTheadedReader r =
        AbsSmartPresenceTheadedReaderTest.getSmartSpy(PLUGIN_NAME, READER_NAME);
    WaitForCardRemovalState waitForCardRemovalState =
        new WaitForCardRemovalState(r, new SmartRemovalMonitoringJob(r), executorService);
    doReturn(ObservableReader.PollingMode.SINGLESHOT).when(r).getPollingMode();
    doReturn(true).when(r).waitForCardAbsentNative();
    doNothing().when(r).processCardRemoved();
    /* test */
    waitForCardRemovalState.onActivate();

    Thread.sleep(WAIT); // wait for the monitoring to act

    /* Assert */
    // Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentState().getMonitoringState());
    verify(r, times(1)).switchState(WAIT_FOR_START_DETECTION);
    waitForCardRemovalState.onDeactivate();
  }

  @Test
  public void smart_waitForRemoval_REPEATING() throws Exception {
    // flaky

    /*
     * ------------ input polling mode is CONTINUE card has been removed within timeout
     */
    BlankSmartPresenceTheadedReader r =
        AbsSmartPresenceTheadedReaderTest.getSmartSpy(PLUGIN_NAME, READER_NAME);
    // r.startCardDetection(ObservableReader.PollingMode.REPEATING);
    WaitForCardRemovalState waitForCardRemovalState =
        new WaitForCardRemovalState(r, new SmartRemovalMonitoringJob(r), executorService);
    doReturn(ObservableReader.PollingMode.REPEATING).when(r).getPollingMode();
    doReturn(true).when(r).waitForCardAbsentNative();
    doNothing().when(r).processCardRemoved();
    /* test */
    waitForCardRemovalState.onActivate();

    Thread.sleep(WAIT); // wait for the monitoring to act

    /* Assert */
    // Assert.assertEquals(WAIT_FOR_SE_INSERTION, r.getCurrentState().getMonitoringState());
    verify(r, times(1)).switchState(WAIT_FOR_SE_INSERTION);
    waitForCardRemovalState.onDeactivate();
  }
}
