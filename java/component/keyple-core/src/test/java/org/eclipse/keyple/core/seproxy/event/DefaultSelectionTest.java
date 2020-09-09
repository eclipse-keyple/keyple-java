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
package org.eclipse.keyple.core.seproxy.event;

import static org.eclipse.keyple.core.seproxy.plugin.reader.AbsObservableLocalReaderTest.getNotMatchingResponses;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.ChannelControl;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.plugin.reader.AbsObservableLocalReaderTest;
import org.eclipse.keyple.core.seproxy.plugin.reader.BlankObservableLocalReader;
import org.junit.Assert;
import org.junit.Test;

public class DefaultSelectionTest {

  /** ==== Card event ====================================== */
  final String PLUGIN_NAME = "AbsObservableLocalReaderTestP";

  final String READER_NAME = "AbsObservableLocalReaderTest";

  /*
   * no default selection
   */
  @Test
  public void seInserted() throws Exception {
    // empty reader
    BlankObservableLocalReader r =
        (BlankObservableLocalReader) AbsObservableLocalReaderTest.getSpy(PLUGIN_NAME, READER_NAME);

    // test
    ReaderEvent event = r.processSeInsertedTest();

    Assert.assertEquals(ReaderEvent.EventType.SE_INSERTED, event.getEventType());
    Assert.assertNull(event.getDefaultSelectionsResponse());
    Assert.assertEquals(PLUGIN_NAME, event.getPluginName());
    Assert.assertEquals(READER_NAME, event.getReaderName());
  }

  /*
   * selection is not successful
   */
  @Test
  public void seInserted_ALWAYS() throws Exception {
    BlankObservableLocalReader r =
        (BlankObservableLocalReader) AbsObservableLocalReaderTest.getSpy(PLUGIN_NAME, READER_NAME);

    // configure parameters
    List<SeRequest> selections = new ArrayList<SeRequest>();
    MultiSeRequestProcessing multi = MultiSeRequestProcessing.PROCESS_ALL;
    ChannelControl channel = ChannelControl.CLOSE_AFTER;
    ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.ALWAYS;

    // mock return matching selection
    List<SeResponse> responses = getNotMatchingResponses();
    doReturn(responses).when(r).transmitSeRequests(selections, multi, channel);

    // test
    r.setDefaultSelectionRequest(new DefaultSelectionsRequest(selections, multi, channel), mode);
    ReaderEvent event = r.processSeInsertedTest();

    // assert
    Assert.assertEquals(ReaderEvent.EventType.SE_INSERTED, event.getEventType());
    Assert.assertEquals(responses, event.getDefaultSelectionsResponse().getSelectionSeResponses());
    Assert.assertEquals(PLUGIN_NAME, event.getPluginName());
    Assert.assertEquals(READER_NAME, event.getReaderName());
  }

  /*
   * selection is successful
   */
  @Test
  public void seMatched_MATCHED_ONLY() throws Exception {
    BlankObservableLocalReader r =
        (BlankObservableLocalReader) AbsObservableLocalReaderTest.getSpy(PLUGIN_NAME, READER_NAME);

    // configure parameters
    List<SeRequest> selections = new ArrayList<SeRequest>();
    MultiSeRequestProcessing multi = MultiSeRequestProcessing.PROCESS_ALL;
    ChannelControl channel = ChannelControl.CLOSE_AFTER;
    ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.MATCHED_ONLY;

    // mock
    // return success selection
    List<SeResponse> responses = AbsObservableLocalReaderTest.getMatchingResponses();
    doReturn(responses).when(r).transmitSeRequests(selections, multi, channel);

    // test
    r.setDefaultSelectionRequest(new DefaultSelectionsRequest(selections, multi, channel), mode);
    ReaderEvent event = r.processSeInsertedTest();

    Assert.assertEquals(ReaderEvent.EventType.SE_MATCHED, event.getEventType());
    Assert.assertEquals(responses, event.getDefaultSelectionsResponse().getSelectionSeResponses());
    Assert.assertEquals(PLUGIN_NAME, event.getPluginName());
    Assert.assertEquals(READER_NAME, event.getReaderName());
  }

  /*
   * selection is not successful
   */
  @Test
  public void noEvent_MATCHED_ONLY() throws Exception {
    BlankObservableLocalReader r =
        (BlankObservableLocalReader) AbsObservableLocalReaderTest.getSpy(PLUGIN_NAME, READER_NAME);

    // configure parameters
    List<SeRequest> selections = new ArrayList<SeRequest>();
    MultiSeRequestProcessing multi = MultiSeRequestProcessing.PROCESS_ALL;
    ChannelControl channel = ChannelControl.CLOSE_AFTER;
    ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.MATCHED_ONLY;

    // mock return matching selection
    doReturn(getNotMatchingResponses()).when(r).transmitSeRequests(selections, multi, channel);

    // test
    r.setDefaultSelectionRequest(new DefaultSelectionsRequest(selections, multi, channel), mode);
    ReaderEvent event = r.processSeInsertedTest();

    Assert.assertEquals(null, event);
  }

  /*
   * Simulate an IOException while selecting Do not throw any event Nor an exception
   */
  @Test
  public void noEvent_IOError() throws Exception {
    BlankObservableLocalReader r =
        (BlankObservableLocalReader) AbsObservableLocalReaderTest.getSpy(PLUGIN_NAME, READER_NAME);

    // configure parameters
    List<SeRequest> selections = new ArrayList<SeRequest>();
    MultiSeRequestProcessing multi = MultiSeRequestProcessing.PROCESS_ALL;
    ChannelControl channel = ChannelControl.CLOSE_AFTER;
    ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.ALWAYS;

    // throw IO
    doThrow(new KeypleReaderIOException("io error when selecting"))
        .when(r)
        .transmitSeRequests(selections, multi, channel);

    // test
    r.setDefaultSelectionRequest(new DefaultSelectionsRequest(selections, multi, channel), mode);
    r.processSeInsertedTest();

    // test
    ReaderEvent event = r.processSeInsertedTest();
    Assert.assertEquals(null, event);
  }
}
