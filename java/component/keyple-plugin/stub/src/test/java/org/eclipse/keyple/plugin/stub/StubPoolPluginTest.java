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

import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.event.PluginObservationExceptionHandler;
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler;
import org.eclipse.keyple.core.service.exception.KeypleAllocationNoReaderException;
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
public class StubPoolPluginTest extends BaseStubTest {

  public static final String POOL_PLUGIN_NAME = "pool1";

  Logger logger = LoggerFactory.getLogger(StubPoolPluginTest.class);

  @Before
  public void registerStub() throws Exception {
    super.registerStub();
  }

  @After
  public void unregisterStub()
      throws InterruptedException, KeypleReaderException, KeyplePluginNotFoundException {
    super.unregisterStub();
  }

  /** Plugin observation exception handler */
  class PluginExceptionHandler implements PluginObservationExceptionHandler {

    @Override
    public void onPluginObservationError(String pluginName, Throwable e) {}
  }

  /** Reader observation exception handler */
  class ReaderExceptionHandler implements ReaderObservationExceptionHandler {
    @Override
    public void onReaderObservationError(String pluginName, String readerName, Throwable e) {}
  }

  /** Plug a pool reader */
  @Test
  public void plugStubPoolReader_success() {
    StubPoolPluginImpl stubPoolPlugin =
        (StubPoolPluginImpl)
            new StubPoolPluginFactory(
                    POOL_PLUGIN_NAME, new PluginExceptionHandler(), new ReaderExceptionHandler())
                .getPlugin();

    stubPoolPlugin.register();

    Reader reader = stubPoolPlugin.plugStubPoolReader("anyGroup", "anyName", stubCard);

    Assert.assertEquals(1, stubPoolPlugin.getReaders().size());
    Assert.assertEquals(true, reader.isCardPresent());
    Assert.assertEquals(1, stubPoolPlugin.getReaderGroupReferences().size());
  }

  /** Unplug a pool reader */
  @Test
  public void unplugStubPoolReader_success() throws Exception {
    StubPoolPluginImpl stubPoolPlugin =
        (StubPoolPluginImpl)
            new StubPoolPluginFactory(
                    POOL_PLUGIN_NAME, new PluginExceptionHandler(), new ReaderExceptionHandler())
                .getPlugin();

    stubPoolPlugin.register();

    // plug a reader
    stubPoolPlugin.plugStubPoolReader("anyGroup", "anyName", stubCard);

    // unplug the reader
    stubPoolPlugin.unplugStubPoolReader("anyGroup");

    Assert.assertEquals(0, stubPoolPlugin.getReaders().size());
    Assert.assertEquals(0, stubPoolPlugin.getReaderGroupReferences().size());
  }

  /** Allocate one reader */
  @Test
  public void allocate_success() throws Exception {
    // init stubPoolPlugin
    StubPoolPluginImpl stubPoolPlugin =
        (StubPoolPluginImpl)
            new StubPoolPluginFactory(
                    POOL_PLUGIN_NAME, new PluginExceptionHandler(), new ReaderExceptionHandler())
                .getPlugin();

    stubPoolPlugin.register();

    // plug readers
    stubPoolPlugin.plugStubPoolReader("group1", "stub1", stubCard);
    stubPoolPlugin.plugStubPoolReader("group2", "stub2", stubCard);

    // allocate Reader
    Reader reader = stubPoolPlugin.allocateReader("group1");

    // check allocate result is correct
    Assert.assertTrue(reader.getName().startsWith("stub1"));

    // check allocate list is correct
    Assert.assertTrue(stubPoolPlugin.listAllocatedReaders().containsKey("stub1"));
    Assert.assertEquals(1, stubPoolPlugin.listAllocatedReaders().size());
  }

  /** Allocate twice the same reader */
  @Test(expected = KeypleAllocationNoReaderException.class)
  public void allocate_twice() throws Exception {
    // init stubPoolPlugin
    StubPoolPluginImpl stubPoolPlugin =
        (StubPoolPluginImpl)
            new StubPoolPluginFactory(
                    POOL_PLUGIN_NAME, new PluginExceptionHandler(), new ReaderExceptionHandler())
                .getPlugin();

    stubPoolPlugin.register();

    // plug readers
    stubPoolPlugin.plugStubPoolReader("group1", "stub1", stubCard);
    stubPoolPlugin.plugStubPoolReader("group2", "stub2", stubCard);

    // allocate Reader
    Reader reader = stubPoolPlugin.allocateReader("group1");
    Reader reader2 = stubPoolPlugin.allocateReader("group1");
  }

  /** Release one reader */
  @Test
  public void release_success() throws Exception {
    // init stubPoolPlugin
    StubPoolPluginImpl stubPoolPlugin =
        (StubPoolPluginImpl)
            new StubPoolPluginFactory(
                    POOL_PLUGIN_NAME, new PluginExceptionHandler(), new ReaderExceptionHandler())
                .getPlugin();

    stubPoolPlugin.register();

    // plug readers
    stubPoolPlugin.plugStubPoolReader("group1", "stub1", stubCard);
    stubPoolPlugin.plugStubPoolReader("group2", "stub2", stubCard);

    // allocate Reader
    Reader reader = stubPoolPlugin.allocateReader("group1");

    // release reader
    stubPoolPlugin.releaseReader(reader);

    // assert no reader is allocated
    Assert.assertEquals(0, stubPoolPlugin.listAllocatedReaders().size());
  }

  /** Stub Card */
  private static final StubSmartCard stubCard =
      new StubSmartCard() {
        @Override
        public byte[] getATR() {
          return new byte[0];
        }

        @Override
        public String getCardProtocol() {
          return "PROTOCOL_ISO7816_3";
        }
      };
}
