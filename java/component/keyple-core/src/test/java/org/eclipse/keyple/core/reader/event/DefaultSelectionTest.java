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
package org.eclipse.keyple.core.service.event;

import static org.eclipse.keyple.core.plugin.AbsObservableLocalReaderTest.getNotMatchingResponses;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.core.card.message.CardSelectionRequest;
import org.eclipse.keyple.core.card.message.CardSelectionResponse;
import org.eclipse.keyple.core.card.message.ChannelControl;
import org.eclipse.keyple.core.card.message.DefaultSelectionsRequest;
import org.eclipse.keyple.core.card.selection.MultiSelectionProcessing;
import org.eclipse.keyple.core.plugin.AbsObservableLocalReaderTest;
import org.eclipse.keyple.core.plugin.BlankObservableLocalReader;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
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
  public void cardInserted() throws Exception {
    // empty reader
    BlankObservableLocalReader r =
        (BlankObservableLocalReader) AbsObservableLocalReaderTest.getSpy(PLUGIN_NAME, READER_NAME);

    // test
    ReaderEvent event = r.processCardInsertedTest();

    Assert.assertEquals(ReaderEvent.EventType.CARD_INSERTED, event.getEventType());
    Assert.assertNull(event.getDefaultSelectionsResponse());
    Assert.assertEquals(PLUGIN_NAME, event.getPluginName());
    Assert.assertEquals(READER_NAME, event.getReaderName());
  }

  /*
   * selection is not successful
   */
  @Test
  public void cardInserted_ALWAYS() throws Exception {
    BlankObservableLocalReader r =
        (BlankObservableLocalReader) AbsObservableLocalReaderTest.getSpy(PLUGIN_NAME, READER_NAME);

    // configure parameters
    List<CardSelectionRequest> selections = new ArrayList<CardSelectionRequest>();
    MultiSelectionProcessing multi = MultiSelectionProcessing.PROCESS_ALL;
    ChannelControl channel = ChannelControl.CLOSE_AFTER;
    ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.ALWAYS;

    // mock return matching selection
    List<CardSelectionResponse> cardSelectionResponses = getNotMatchingResponses();
    doReturn(cardSelectionResponses)
        .when(r)
        .transmitCardSelectionRequests(selections, multi, channel);

    // test
    r.setDefaultSelectionRequest(new DefaultSelectionsRequest(selections, multi, channel), mode);
    ReaderEvent event = r.processCardInsertedTest();

    // assert
    Assert.assertEquals(ReaderEvent.EventType.CARD_INSERTED, event.getEventType());
    Assert.assertEquals(
        cardSelectionResponses, event.getDefaultSelectionsResponse().getCardSelectionResponses());
    Assert.assertEquals(PLUGIN_NAME, event.getPluginName());
    Assert.assertEquals(READER_NAME, event.getReaderName());
  }

  /*
   * selection is successful
   */
  @Test
  public void cardMatched_MATCHED_ONLY() throws Exception {
    BlankObservableLocalReader r =
        (BlankObservableLocalReader) AbsObservableLocalReaderTest.getSpy(PLUGIN_NAME, READER_NAME);

    // configure parameters
    List<CardSelectionRequest> selections = new ArrayList<CardSelectionRequest>();
    MultiSelectionProcessing multi = MultiSelectionProcessing.PROCESS_ALL;
    ChannelControl channel = ChannelControl.CLOSE_AFTER;
    ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.MATCHED_ONLY;

    // mock
    // return success selection
    List<CardSelectionResponse> cardSelectionResponses =
        AbsObservableLocalReaderTest.getMatchingResponses();
    doReturn(cardSelectionResponses)
        .when(r)
        .transmitCardSelectionRequests(selections, multi, channel);

    // test
    r.setDefaultSelectionRequest(new DefaultSelectionsRequest(selections, multi, channel), mode);
    ReaderEvent event = r.processCardInsertedTest();

    Assert.assertEquals(ReaderEvent.EventType.CARD_MATCHED, event.getEventType());
    Assert.assertEquals(
        cardSelectionResponses, event.getDefaultSelectionsResponse().getCardSelectionResponses());
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
    List<CardSelectionRequest> selections = new ArrayList<CardSelectionRequest>();
    MultiSelectionProcessing multi = MultiSelectionProcessing.PROCESS_ALL;
    ChannelControl channel = ChannelControl.CLOSE_AFTER;
    ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.MATCHED_ONLY;

    // mock return matching selection
    doReturn(getNotMatchingResponses())
        .when(r)
        .transmitCardSelectionRequests(selections, multi, channel);

    // test
    r.setDefaultSelectionRequest(new DefaultSelectionsRequest(selections, multi, channel), mode);
    ReaderEvent event = r.processCardInsertedTest();

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
    List<CardSelectionRequest> selections = new ArrayList<CardSelectionRequest>();
    MultiSelectionProcessing multi = MultiSelectionProcessing.PROCESS_ALL;
    ChannelControl channel = ChannelControl.CLOSE_AFTER;
    ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.ALWAYS;

    // throw IO
    doThrow(new KeypleReaderIOException("io error when selecting"))
        .when(r)
        .transmitCardSelectionRequests(selections, multi, channel);

    // test
    r.setDefaultSelectionRequest(new DefaultSelectionsRequest(selections, multi, channel), mode);
    r.processCardInsertedTest();

    // test
    ReaderEvent event = r.processCardInsertedTest();
    Assert.assertEquals(null, event);
  }
}
