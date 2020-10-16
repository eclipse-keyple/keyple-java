/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.calypso.transaction;

import static org.eclipse.keyple.calypso.transaction.SamResourceManagerFactory.DEFAULT_SLEEP_TIME;
import static org.eclipse.keyple.calypso.transaction.SamResourceManagerFactory.MAX_BLOCKING_TIME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.eclipse.keyple.calypso.CalypsoBaseTest;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.exception.CalypsoNoSamResourceAvailableException;
import org.eclipse.keyple.core.reader.MultiSelectionProcessing;
import org.eclipse.keyple.core.reader.Plugin;
import org.eclipse.keyple.core.reader.Reader;
import org.eclipse.keyple.core.reader.message.AnswerToReset;
import org.eclipse.keyple.core.reader.message.CardResponse;
import org.eclipse.keyple.core.reader.message.ChannelControl;
import org.eclipse.keyple.core.reader.message.ProxyReader;
import org.eclipse.keyple.core.reader.message.SelectionStatus;
import org.eclipse.keyple.core.selection.CardResource;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class ManagedSamResourceManagerDefaultTest extends CalypsoBaseTest {

  private static final Logger logger =
      LoggerFactory.getLogger(ManagedSamResourceManagerDefaultTest.class);

  private static final String SAM_READER_NAME = "sam-reader-name";

  @Before
  public void setUp() {
    logger.info("------------------------------");
    logger.info("Test {}", name.getMethodName() + "");
    logger.info("------------------------------");
  }

  @Test
  public void waitResources() {
    // init SamResourceManager with a not mathching filter
    SamResourceManagerDefault srmSpy = srmSpy("notMatchingFilter");
    long start = System.currentTimeMillis();
    Boolean exceptionThrown = false;

    // test
    CardResource<CalypsoSam> out = null;
    try {
      out =
          srmSpy.allocateSamResource(
              SamResourceManager.AllocationMode.BLOCKING,
              SamIdentifier.builder()
                  .samRevision(SamRevision.AUTO)
                  .serialNumber("any")
                  .groupReference("any")
                  .build());

    } catch (CalypsoNoSamResourceAvailableException e) {
      exceptionThrown = true;
    }
    long stop = System.currentTimeMillis();

    // assert an exception is thrown after MAX_BLOCKING_TIME
    Assert.assertNull(out);
    Assert.assertTrue(exceptionThrown);
    Assert.assertTrue(stop - start > MAX_BLOCKING_TIME);
  }

  @Test
  public void getSamResource() {

    // init SamResourceManager with a mathching filter
    SamResourceManagerDefault srmSpy = srmSpy(".*");
    // doReturn(samResourceMock()).when(srmSpy).createSamResource(any(Reader.class));

    long start = System.currentTimeMillis();

    // test
    CardResource<CalypsoSam> out =
        srmSpy.allocateSamResource(
            SamResourceManager.AllocationMode.BLOCKING,
            SamIdentifier.builder().samRevision(SamRevision.AUTO).build());

    long stop = System.currentTimeMillis();

    // assert results
    Assert.assertNotNull(out);
    Assert.assertTrue(stop - start < MAX_BLOCKING_TIME);
  }

  /*
   * Helpers
   */

  CardResponse samSelectionSuccess() {
    SelectionStatus selectionStatus = Mockito.mock(SelectionStatus.class);
    when(selectionStatus.hasMatched()).thenReturn(true);
    when(selectionStatus.getAtr())
        .thenReturn(new AnswerToReset(ByteArrayUtil.fromHex(CalypsoSamTest.ATR1)));

    CardResponse cardResponse = Mockito.mock(CardResponse.class);
    when(cardResponse.getSelectionStatus()).thenReturn(selectionStatus);
    when(cardResponse.isLogicalChannelOpen()).thenReturn(true);

    return cardResponse;
  }

  // get a sam manager spy with a selectable sam
  SamResourceManagerDefault srmSpy(String samFilter) {

    List<CardResponse> selectionResponses = new ArrayList<CardResponse>();
    selectionResponses.add(samSelectionSuccess());

    // create a mock reader
    ProxyReader reader = Mockito.mock(ProxyReader.class);
    when(reader.getName()).thenReturn(SAM_READER_NAME);
    when(reader.isCardPresent()).thenReturn(true);
    doReturn(selectionResponses)
        .when(reader)
        .transmitCardRequests(
            any(List.class), any(MultiSelectionProcessing.class), any(ChannelControl.class));

    // create a list of mock readers
    ConcurrentMap<String, Reader> readers = new ConcurrentHashMap<String, Reader>();
    readers.put(reader.getName(), reader);

    // create the mock plugin
    Plugin plugin = Mockito.mock(Plugin.class);
    when(plugin.getReaders()).thenReturn(readers);
    when(plugin.getReader(SAM_READER_NAME)).thenReturn(reader);

    return Mockito.spy(
        new SamResourceManagerDefault(plugin, samFilter, MAX_BLOCKING_TIME, DEFAULT_SLEEP_TIME));
  }

  SamResourceManagerDefault.ManagedSamResource samResourceMock() {
    SamResourceManagerDefault.ManagedSamResource mock =
        Mockito.mock(SamResourceManagerDefault.ManagedSamResource.class);
    doReturn(true).when(mock).isSamMatching(any(SamIdentifier.class));
    doReturn(true).when(mock).isSamResourceFree();
    return mock;
  }

  Reader readerMock() {
    Reader mock = Mockito.mock(Reader.class);
    return mock;
  }
}
