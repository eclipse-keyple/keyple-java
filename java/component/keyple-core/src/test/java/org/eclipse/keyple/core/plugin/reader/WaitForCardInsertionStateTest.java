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

import static org.eclipse.keyple.core.plugin.reader.AbstractObservableState.MonitoringState.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class WaitForCardInsertionStateTest extends CoreBaseTest {

  private static final Logger logger = LoggerFactory.getLogger(WaitForCardInsertionStateTest.class);

  final String READER_NAME = "WaitForCardInsertionStateTest";

  AbstractObservableState waitForInsert;
  AbstractObservableLocalReader r;

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

    r = AbsSmartInsertionTheadedReaderTest.getMock(READER_NAME);
    waitForInsert = new WaitForCardInsertionState(r);
  }

  @Before
  public void tearDown() {
    logger.info("******************************");
    logger.info("End of Test {}", name.getMethodName() + "");
    logger.info("\"******************************");

    waitForInsert.onDeactivate();
  }

  @Test
  public void insertSe_matched() throws Exception {
    /*
     * input card inserted card matched
     */
    doReturn(new ReaderEvent("", "", ReaderEvent.EventType.CARD_MATCHED, null))
        .when(r)
        .processCardInserted();

    /* test */
    waitForInsert.onActivate();
    waitForInsert.onEvent(AbstractObservableLocalReader.InternalEvent.CARD_INSERTED);

    Thread.sleep(WAIT); // wait for the monitoring to act

    /* Assert */
    verify(r, times(1)).switchState(WAIT_FOR_SE_PROCESSING);

    // Assert.assertEquals(WAIT_FOR_SE_PROCESSING, r.getCurrentState().getMonitoringState());

  }

  @Test
  public void testInsertSe_Notmatched() throws Exception {
    /*
     * input card inserted card doesnt matched Back to Detection
     */
    doReturn(new ReaderEvent("", "", ReaderEvent.EventType.CARD_INSERTED, null))
        .when(r)
        .processCardInserted();

    /* test */
    waitForInsert.onActivate();
    waitForInsert.onEvent(AbstractObservableLocalReader.InternalEvent.CARD_INSERTED);
    /* Assert */

    Thread.sleep(WAIT); // wait for the monitoring to act

    // switched to the same state to relaunch the monitoring job
    verify(r, times(1)).switchState(WAIT_FOR_SE_PROCESSING);
  }

  // @Test
  // public void testTimeout() throws Exception {
  // /*
  // * input no card inserted within timeout
  // */
  //
  // /* test */
  // waitForInsert.onActivate();
  // waitForInsert.onEvent(AbstractObservableLocalReader.InternalEvent.TIME_OUT);
  //
  // /* Assert */
  // verify(r, times(1)).switchState(WAIT_FOR_SE_INSERTION);
  // }

}
