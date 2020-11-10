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
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.event.ObservablePlugin;
import org.eclipse.keyple.core.service.event.PluginEvent;
import org.eclipse.keyple.core.service.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.service.exception.KeypleReaderException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StubPluginTest extends BaseStubTest {

  Logger logger = LoggerFactory.getLogger(StubPluginTest.class);

  @Before
  public void registerStub() throws Exception {
    super.registerStub();
  }

  @After
  public void unregisterStub()
      throws InterruptedException, KeypleReaderException, KeyplePluginNotFoundException {
    super.unregisterStub();
  }

  @Test
  public void instantiatePlugin() {
    final String PLUGIN_NAME = "test1";

    StubPluginFactory factory = new StubPluginFactory(PLUGIN_NAME);

    Plugin plugin = factory.getPlugin();

    Assert.assertEquals(PLUGIN_NAME, plugin.getName());
  }

  /** Plug one reader synchronously Check: Count if created */
  @Test
  public void plugOneReader_synchronously_withoutObservation_success() {
    final String READER_NAME = "plugOneReader_synchronously_withObservation_success";

    // connect reader
    stubPlugin.plugStubReader(READER_NAME, true, true);

    Assert.assertEquals(1, stubPlugin.getReaders().size());
    StubReader stubReader = (StubReader) stubPlugin.getReaders().values().toArray()[0];
    Assert.assertEquals(READER_NAME, stubReader.getName());
    Assert.assertEquals(true, stubReader.isContactless());
  }

  /** Plug one reader synchronously Check: Event thrown */
  @Test
  public void plugOneReader_synchronously_withObservation_success()
      throws InterruptedException, KeypleReaderException {
    final CountDownLatch readerConnected = new CountDownLatch(1);
    final String READER_NAME = "plugOneReader_synchronously_withObservation_success";

    // add READER_CONNECTED assert observer
    stubPlugin.addObserver(
        new ObservablePlugin.PluginObserver() {
          @Override
          public void update(PluginEvent event) {
            Assert.assertEquals(PluginEvent.EventType.READER_CONNECTED, event.getEventType());
            Assert.assertEquals(1, event.getReaderNames().size());
            Assert.assertEquals(READER_NAME, event.getReaderNames().first());
            readerConnected.countDown();
          }
        });

    stubPlugin.plugStubReader(READER_NAME, true);
    readerConnected.await(2, TimeUnit.SECONDS);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    Assert.assertEquals(0, readerConnected.getCount());
  }

  @Test
  public void plugMultiReaders_synchronously_withoutObservation_success()
      throws InterruptedException, KeypleReaderException {
    Set<String> newReaders =
        new HashSet<String>(Arrays.asList("EC_reader1", "EC_reader2", "EC_reader3"));
    // connect readers at once
    stubPlugin.plugStubReaders(newReaders, true);
    logger.info("Stub Readers connected {}", stubPlugin.getReaderNames());
    Assert.assertEquals(newReaders, stubPlugin.getReaderNames());
    Assert.assertEquals(3, stubPlugin.getReaders().size());
  }

  @Test
  public void plugMultiReaders_synchronously_withObservation_success()
      throws InterruptedException, KeypleReaderException {
    Set<String> newReaders =
        new HashSet<String>(Arrays.asList("EC_reader1", "EC_reader2", "EC_reader3"));
    final CountDownLatch readerConnected = new CountDownLatch(1);

    // add READER_CONNECTED assert observer
    stubPlugin.addObserver(
        new ObservablePlugin.PluginObserver() {
          @Override
          public void update(PluginEvent event) {
            Assert.assertEquals(PluginEvent.EventType.READER_CONNECTED, event.getEventType());
            Assert.assertEquals(3, event.getReaderNames().size());
            readerConnected.countDown();
          }
        });
    // connect readers at once
    stubPlugin.plugStubReaders(newReaders, true);
    readerConnected.await(2, TimeUnit.SECONDS);
    Assert.assertEquals(newReaders, stubPlugin.getReaderNames());
    Assert.assertEquals(3, stubPlugin.getReaders().size());
  }

  /** Unplug one reader synchronously Check: Count if removed */
  @Test
  public void unplugOneReader_synchronously_withoutObservation_success() {
    final String READER_NAME = "unplugOneReader_success";
    // connect reader
    stubPlugin.plugStubReader(READER_NAME, true);
    Assert.assertEquals(1, stubPlugin.getReaders().size());
    stubPlugin.unplugStubReader(READER_NAME, true);
    Assert.assertEquals(0, stubPlugin.getReaders().size());
  }

  /** Plug and unplug many readers at once synchronously Check : count */
  @Test
  public void unplugMultiReaders_synchronously_withoutObservation_success()
      throws InterruptedException, KeypleReaderException {
    final Set<String> READERS =
        new HashSet<String>(Arrays.asList("FC_Reader1", "FC_Reader2", "FC_Reader3"));
    // connect readers at once
    stubPlugin.plugStubReaders(READERS, true);
    Assert.assertEquals(3, stubPlugin.getReaders().size());
    stubPlugin.unplugStubReaders(READERS, true);
    Assert.assertEquals(0, stubPlugin.getReaders().size());
  }

  /** Plug and unplug many readers at once synchronously Check : count */
  @Test
  public void unplugMultiReaders_synchronously_withObservation_success()
      throws KeypleReaderException {
    final CountDownLatch readerDisconnected = new CountDownLatch(1);
    final Set<String> READERS =
        new HashSet<String>(Arrays.asList("FC_Reader1", "FC_Reader2", "FC_Reader3"));
    // connect readers at once
    stubPlugin.plugStubReaders(READERS, true);
    Assert.assertEquals(3, stubPlugin.getReaders().size());

    // add READER_DISCONNECTED assert observer
    stubPlugin.addObserver(
        new ObservablePlugin.PluginObserver() {
          @Override
          public void update(PluginEvent event) {
            Assert.assertEquals(PluginEvent.EventType.READER_DISCONNECTED, event.getEventType());
            Assert.assertEquals(3, event.getReaderNames().size());
            readerDisconnected.countDown();
          }
        });

    stubPlugin.unplugStubReaders(READERS, true);
    Assert.assertEquals(0, stubPlugin.getReaders().size());
  }

  /** Plug same reader twice Check : only one reader */
  @Test
  public void plugSameReaderTwice_synchronously_fail()
      throws InterruptedException, KeypleReaderException {
    final String READER_NAME = "testC_PlugSameReaderTwice";

    stubPlugin.plugStubReader(READER_NAME, true);
    stubPlugin.plugStubReader(READER_NAME, true);
    logger.debug("Stubplugin readers size {} ", stubPlugin.getReaders().size());

    Assert.assertEquals(1, stubPlugin.getReaders().size());
  }

  /** Get name */
  @Test
  public void getName_success() {
    Assert.assertNotNull(stubPlugin.getName());
  }
}
