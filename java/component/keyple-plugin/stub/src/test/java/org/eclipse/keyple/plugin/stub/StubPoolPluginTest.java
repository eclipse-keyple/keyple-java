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

  /** Plug a pool reader */
  @Test
  public void plugStubPoolReader_success() {
    StubPoolPluginImpl stubPoolPlugin =
        (StubPoolPluginImpl) new StubPoolPluginFactory(POOL_PLUGIN_NAME, null, null).getPlugin();

    Reader reader = stubPoolPlugin.plugStubPoolReader("anyGroup", "reader", stubCard);
    Reader reader2 = stubPoolPlugin.plugStubPoolReader("anyGroup2", "reader2", stubCard);
    Reader reader3 = stubPoolPlugin.plugStubPoolReader("anyGroup2", "reader3", stubCard);

    Assert.assertEquals(3, stubPoolPlugin.getReaders().size());
    Assert.assertEquals(true, reader.isCardPresent());
    Assert.assertEquals(2, stubPoolPlugin.getReaderGroupReferences().size());
    Assert.assertEquals(false, stubPoolPlugin.isAllocated(reader.getName()));
  }

  /** Unplug a pool reader */
  @Test
  public void unplugStubPoolReader_success() throws Exception {
    StubPoolPluginImpl stubPoolPlugin =
        (StubPoolPluginImpl) new StubPoolPluginFactory(POOL_PLUGIN_NAME, null, null).getPlugin();

    // plug a reader
    stubPoolPlugin.plugStubPoolReader("anyGroup", "reader1", stubCard);
    stubPoolPlugin.plugStubPoolReader("anyGroup", "reader2", stubCard);
    stubPoolPlugin.plugStubPoolReader("anyGroup2", "reader3", stubCard);

    // unplug the reader
    stubPoolPlugin.unplugStubPoolReadersByGroupReference("anyGroup");

    Assert.assertEquals(1, stubPoolPlugin.getReaders().size());
    Assert.assertEquals(1, stubPoolPlugin.getReaderGroupReferences().size());
  }

  /** Allocate one reader */
  @Test
  public void allocate_success() throws Exception {
    // init stubPoolPlugin
    StubPoolPluginImpl stubPoolPlugin =
        (StubPoolPluginImpl) new StubPoolPluginFactory(POOL_PLUGIN_NAME, null, null).getPlugin();

    // plug readers
    stubPoolPlugin.plugStubPoolReader("group1", "stub1", stubCard);
    stubPoolPlugin.plugStubPoolReader("group2", "stub2", stubCard);

    // allocate Reader
    Reader reader = stubPoolPlugin.allocateReader("group1");

    // check allocate result is correct
    Assert.assertTrue(reader.getName().startsWith("stub1"));

    // check allocate list is correct
    Assert.assertTrue(stubPoolPlugin.isAllocated("stub1"));
  }

  /** Allocate twice the same reader */
  @Test(expected = KeypleAllocationNoReaderException.class)
  public void allocate_twice() throws Exception {
    // init stubPoolPlugin
    StubPoolPluginImpl stubPoolPlugin =
        (StubPoolPluginImpl) new StubPoolPluginFactory(POOL_PLUGIN_NAME, null, null).getPlugin();

    // plug readers
    stubPoolPlugin.plugStubPoolReader("group1", "stub1", stubCard);
    stubPoolPlugin.plugStubPoolReader("group1", "stub2", stubCard);
    stubPoolPlugin.plugStubPoolReader("group2", "stub3", stubCard);

    // allocate Reader
    Reader reader = stubPoolPlugin.allocateReader("group1");
    Reader reader2 = stubPoolPlugin.allocateReader("group1");
    Reader reader3 = stubPoolPlugin.allocateReader("group1");

    // should throw exception
  }

  /** Release one reader */
  @Test
  public void release_success() throws Exception {
    // init stubPoolPlugin
    StubPoolPluginImpl stubPoolPlugin =
        (StubPoolPluginImpl) new StubPoolPluginFactory(POOL_PLUGIN_NAME, null, null).getPlugin();

    // plug readers
    stubPoolPlugin.plugStubPoolReader("group1", "stub1", stubCard);

    // allocate Reader
    Reader reader = stubPoolPlugin.allocateReader("group1");

    Assert.assertEquals(true, stubPoolPlugin.isAllocated("stub1"));

    // release reader
    stubPoolPlugin.releaseReader(reader);

    // assert no reader is allocated
    Assert.assertEquals(false, stubPoolPlugin.isAllocated("stub1"));
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
