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
package org.eclipse.keyple.core.plugin;

import static org.eclipse.keyple.core.plugin.AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION;
import static org.mockito.Mockito.doReturn;

import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * test the feature of SmartRemoval methods
 */
@RunWith(Parameterized.class)
public class AbsSmartPresenceTheadedReaderTest extends CoreBaseTest {

  private static final Logger logger =
      LoggerFactory.getLogger(AbsSmartPresenceTheadedReaderTest.class);

  final String PLUGIN_NAME = "AbsSmartPresenceTheadedReaderTestP";
  final String READER_NAME = "AbsSmartPresenceTheadedReaderTest";

  BlankPresenceWaitForCardBlockingThreadedReader r;

  // Execute tests 10 times
  @Parameterized.Parameters
  public static Object[][] data() {
    int x = 1;
    return new Object[x][0];
  }

  @Before
  public void setUp() {
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
    r = null;
  }

  @Test
  public void startRemovalSequence() throws Exception {

    // card matched
    doReturn(new ReaderEvent("", "", ReaderEvent.EventType.CARD_REMOVED, null))
        .when(r)
        .processCardInserted();

    r.addObserver(getObs());
    Thread.sleep(100);

    r.finalizeCardProcessing();
    Thread.sleep(100);

    // does nothing
    Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentMonitoringState());
  }

  @Test
  public void startRemovalSequence_CONTINUE() throws Exception {

    // card matched
    doReturn(new ReaderEvent("", "", ReaderEvent.EventType.CARD_REMOVED, null))
        .when(r)
        .processCardInserted();
    // use mocked BlankPresenceWaitForCardBlockingThreadedReader methods

    r.addObserver(getObs());
    r.startCardDetection(ObservableReader.PollingMode.REPEATING); // WAIT_FOR_SE_INSERTION
    Thread.sleep(100);

    r.finalizeCardProcessing();
    Thread.sleep(100);

    Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentMonitoringState());
  }

  @Test
  public void startRemovalSequence_noping_STOP() throws Exception {

    // card matched
    doReturn(new ReaderEvent("", "", ReaderEvent.EventType.CARD_REMOVED, null))
        .when(r)
        .processCardInserted();
    doReturn(false).when(r).isCardPresentPing();

    r.addObserver(getObs());
    r.startCardDetection(ObservableReader.PollingMode.SINGLESHOT);
    Thread.sleep(100);

    r.finalizeCardProcessing();
    Thread.sleep(100);

    Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentMonitoringState());
  }

  @Test
  public void startRemovalSequence_ping_STOP() throws Exception {
    // card matched
    doReturn(new ReaderEvent("", "", ReaderEvent.EventType.CARD_REMOVED, null))
        .when(r)
        .processCardInserted();
    // doReturn(true).when(r).isCardPresentPing();
    doReturn(true).when(r).isCardPresent();

    r.addObserver(getObs());
    r.startCardDetection(ObservableReader.PollingMode.SINGLESHOT);
    Thread.sleep(100);

    r.finalizeCardProcessing();
    Thread.sleep(100);

    Assert.assertEquals(WAIT_FOR_START_DETECTION, r.getCurrentMonitoringState());
  }

  /*
   * Helpers
   */

  public static BlankPresenceWaitForCardBlockingThreadedReader getSmartSpy(
      String pluginName, String readerName) {
    BlankPresenceWaitForCardBlockingThreadedReader r =
        Mockito.spy(new BlankPresenceWaitForCardBlockingThreadedReader(pluginName, readerName, 1));
    return r;
  }

  public static BlankPresenceWaitForCardBlockingThreadedReader getSmartPresenceMock(
      String pluginName, String readerName) {
    BlankPresenceWaitForCardBlockingThreadedReader r =
        Mockito.mock(BlankPresenceWaitForCardBlockingThreadedReader.class);
    doReturn("test").when(r).getName();
    return r;
  }

  public static ObservableReader.ReaderObserver getObs() {
    return new ObservableReader.ReaderObserver() {
      @Override
      public void update(ReaderEvent event) {}
    };
  }
}
