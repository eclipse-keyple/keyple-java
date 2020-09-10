/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.stub;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StubPluginAsyncTest extends BaseStubTest {

  Logger logger = LoggerFactory.getLogger(StubPluginAsyncTest.class);

  @Before
  public void setupStub() throws Exception {
    super.setupStub();
  }

  @After
  public void clearStub()
      throws InterruptedException, KeypleReaderException, KeyplePluginNotFoundException {
    super.clearStub();
  }

  /** Plug one reader asynchronously Check: Event thrown */
  @Test
  public void plugOneReaderAsync_success() throws InterruptedException, KeypleReaderException {
    final CountDownLatch lock = new CountDownLatch(1);
    final String READER_NAME = "plugOneReaderAsync_sucess";

    // add READER_CONNECTED assert observer
    stubPlugin.addObserver(
        new ObservablePlugin.PluginObserver() {
          @Override
          public void update(PluginEvent event) {
            Assert.assertEquals(PluginEvent.EventType.READER_CONNECTED, event.getEventType());
            Assert.assertEquals(1, event.getReaderNames().size());
            Assert.assertEquals(READER_NAME, event.getReaderNames().first());
            lock.countDown();
          }
        });

    stubPlugin.plugStubReader(READER_NAME, TransmissionMode.CONTACTLESS, false);
    lock.await(2, TimeUnit.SECONDS);
    Assert.assertEquals(0, lock.getCount());
  }

  /** Unplug one reader and wait for event */
  @Test
  public void unplugOneReaderAsync_success() throws InterruptedException, KeypleReaderException {
    final CountDownLatch connectedLock = new CountDownLatch(1);
    final CountDownLatch disconnectedLock = new CountDownLatch(1);
    final String READER_NAME = "unplugOneReaderAsync_success";

    ObservablePlugin.PluginObserver disconnected_obs =
        new ObservablePlugin.PluginObserver() {
          int event_i = 1;

          @Override
          public void update(PluginEvent event) {
            logger.info(
                "event {} #readers {}", event.getEventType(), event.getReaderNames().size());
            // first event, should be a READER_CONNECTED
            if (event_i == 1) {
              Assert.assertEquals(PluginEvent.EventType.READER_CONNECTED, event.getEventType());
              connectedLock.countDown();
            }
            // analyze the second event, should be a READER_DISCONNECTED
            if (event_i == 2) {
              Assert.assertEquals(PluginEvent.EventType.READER_DISCONNECTED, event.getEventType());
              Assert.assertEquals(1, event.getReaderNames().size());
              Assert.assertEquals(READER_NAME, event.getReaderNames().first());
              disconnectedLock.countDown();
            }
            event_i++;
          }
        };

    // add ReaderEvent observer
    stubPlugin.addObserver(disconnected_obs);

    // plug a reader
    stubPlugin.plugStubReader(READER_NAME, false);

    connectedLock.await(2, TimeUnit.SECONDS);

    // unplug reader
    stubPlugin.unplugStubReader(READER_NAME, false);

    // wait for event to be raised
    disconnectedLock.await(2, TimeUnit.SECONDS);
    Assert.assertEquals(0, disconnectedLock.getCount());
  }

  /** Plug many readers at once async Check : check event, count event */
  @Test
  public void plugMultiReadersAsync_success() throws InterruptedException, KeypleReaderException {
    final Set<String> READERS =
        new HashSet<String>(Arrays.asList("E_Reader1", "E_Reader2", "E_Reader3"));

    // lock test until message is received
    final CountDownLatch readerConnected = new CountDownLatch(3);

    // add READER_CONNECTED assert observer
    stubPlugin.addObserver(
        new ObservablePlugin.PluginObserver() {
          @Override
          public void update(PluginEvent event) {
            logger.info(
                "event {} #readers {}", event.getEventType(), event.getReaderNames().size());
            Assert.assertEquals(PluginEvent.EventType.READER_CONNECTED, event.getEventType());
            Assert.assertTrue(event.getReaderNames().size() >= 1); // can be one or three
            // we are waiting for 3 notifications of reader insertion
            for (int i = 0; i < event.getReaderNames().size(); i++) {
              readerConnected.countDown();
            }
          }
        });

    // connect readers
    stubPlugin.plugStubReaders(READERS, false);

    // wait for event to be raised
    readerConnected.await(2, TimeUnit.SECONDS);
    Assert.assertEquals(0, readerConnected.getCount());
  }

  /** Plug and unplug many readers at once asynchronously Check : event and count events */
  @Test
  public void plugUnplugMultiReadersAsync_success()
      throws InterruptedException, KeypleReaderException {
    final Set<String> READERS =
        new HashSet<String>(Arrays.asList("F_Reader1", "F_Reader2", "F_Reader3"));

    // lock test until message is received
    final CountDownLatch connectedLock = new CountDownLatch(1);
    final CountDownLatch disconnectedLock = new CountDownLatch(1);

    ObservablePlugin.PluginObserver assertDisconnect =
        new ObservablePlugin.PluginObserver() {
          int event_i = 1;

          @Override
          public void update(PluginEvent event) {
            logger.info(
                "event {} #readers {}", event.getEventType(), event.getReaderNames().size());
            if (event_i == 1) {
              Assert.assertEquals(PluginEvent.EventType.READER_CONNECTED, event.getEventType());
              connectedLock.countDown();
            }
            // analyze the second event, should be a READER_DISCONNECTED
            if (event_i == 2) {
              Assert.assertEquals(PluginEvent.EventType.READER_DISCONNECTED, event.getEventType());
              // Assert.assertEquals(3, event.getReaderNames().size());
              Assert.assertEquals(READERS, event.getReaderNames());
              disconnectedLock.countDown();
            }
            event_i++;
          }
        };
    // add assert DISCONNECT assert observer
    stubPlugin.addObserver(assertDisconnect);

    // connect reader
    stubPlugin.plugStubReaders(READERS, false);

    Assert.assertTrue(connectedLock.await(2, TimeUnit.SECONDS));

    Thread.sleep(1000);

    stubPlugin.unplugStubReaders(READERS, false);

    Assert.assertTrue(disconnectedLock.await(2, TimeUnit.SECONDS));

    // Thread.sleep(1000);

    Thread.sleep(1000);
    logger.debug("Stub Readers connected {}", stubPlugin.getReaderNames());
    Assert.assertEquals(0, stubPlugin.getReaders().size());
    Assert.assertEquals(0, connectedLock.getCount());
    Assert.assertEquals(0, disconnectedLock.getCount());
  }
}
