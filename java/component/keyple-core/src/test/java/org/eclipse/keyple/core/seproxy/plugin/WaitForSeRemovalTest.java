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
package org.eclipse.keyple.core.seproxy.plugin;

import static org.eclipse.keyple.core.seproxy.plugin.AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION;
import static org.eclipse.keyple.core.seproxy.plugin.AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION;
import static org.mockito.Mockito.*;

import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class WaitForSeRemovalTest extends CoreBaseTest {

  private static final Logger logger = LoggerFactory.getLogger(WaitForSeRemovalTest.class);

  final String PLUGIN_NAME = "WaitForSeRemovalJobExecutorTestP";
  final String READER_NAME = "WaitForSeRemovalJobExecutorTest";

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
     * ------------ input polling mode is STOP SE has been removed within timeout
     */
    AbstractObservableLocalReader r = AbsSmartInsertionTheadedReaderTest.getMock(READER_NAME);
    WaitForSeRemoval waitForSeRemoval = new WaitForSeRemoval(r);
    doReturn(ObservableReader.PollingMode.SINGLESHOT).when(r).getPollingMode();
    doNothing().when(r).processSeRemoved();

    /* test */
    waitForSeRemoval.onActivate();

    waitForSeRemoval.onEvent(AbstractObservableLocalReader.InternalEvent.SE_REMOVED);

    /* Assert */
    verify(r, times(1)).switchState(WAIT_FOR_START_DETECTION);
  }

  @Test
  public void waitForRemoval_REPEATING() throws Exception {
    /*
     * ------------ input polling mode is CONTINUE SE has been removed within timeout
     */
    AbstractObservableLocalReader r = AbsSmartInsertionTheadedReaderTest.getMock(READER_NAME);
    WaitForSeRemoval waitForSeRemoval = new WaitForSeRemoval(r);
    doReturn(ObservableReader.PollingMode.REPEATING).when(r).getPollingMode();
    doNothing().when(r).processSeRemoved();

    /* test */
    waitForSeRemoval.onActivate();
    waitForSeRemoval.onEvent(AbstractObservableLocalReader.InternalEvent.SE_REMOVED);

    /* Assert */
    verify(r, times(1)).switchState(WAIT_FOR_SE_INSERTION);
  }
}
