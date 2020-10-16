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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.card.message.CardRequest;
import org.eclipse.keyple.core.card.message.CardRequestTest;
import org.eclipse.keyple.core.card.message.CardResponse;
import org.eclipse.keyple.core.card.message.CardResponseTest;
import org.eclipse.keyple.core.card.message.ChannelControl;
import org.eclipse.keyple.core.card.selection.MultiSelectionProcessing;
import org.eclipse.keyple.core.reader.exception.KeypleReaderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class AbsReaderTest extends CoreBaseTest {

  private static final Logger logger = LoggerFactory.getLogger(AbsReaderTest.class);

  final String PLUGIN_NAME = "AbstractReaderTestP";
  final String READER_NAME = "AbstractReaderTest";

  @Before
  public void setUp() {
    logger.info("------------------------------");
    logger.info("Test {}", name.getMethodName() + "");
    logger.info("------------------------------");
  }

  @Test
  public void testConstructor() throws Exception {
    AbstractReader r = getSpy(PLUGIN_NAME, READER_NAME);
    Assert.assertEquals(PLUGIN_NAME, r.getPluginName());
    Assert.assertEquals(READER_NAME, r.getName());
  }

  /*
   * TransmitSet "ts_"
   */

  @Test
  public void ts_transmit_null() throws Exception {
    AbstractReader r = getSpy(PLUGIN_NAME, READER_NAME);
    r.transmitCardRequests(null, MultiSelectionProcessing.FIRST_MATCH, ChannelControl.CLOSE_AFTER);
    // we're just waiting right here for no exceptions to be thrown.
    verify(r, times(1))
        .transmitCardRequests(
            null, MultiSelectionProcessing.FIRST_MATCH, ChannelControl.CLOSE_AFTER);
  }

  @Test
  public void ts_transmit2_null() throws Exception {
    AbstractReader r = getSpy(PLUGIN_NAME, READER_NAME);
    r.transmitCardRequests(null, MultiSelectionProcessing.FIRST_MATCH, ChannelControl.KEEP_OPEN);
    // we're just waiting right here for no exceptions to be thrown.
    verify(r, times(1))
        .transmitCardRequests(null, MultiSelectionProcessing.FIRST_MATCH, ChannelControl.KEEP_OPEN);
  }

  @Test
  public void ts_transmit() throws Exception {
    AbstractReader r = getSpy(PLUGIN_NAME, READER_NAME);
    List<CardRequest> cardRequests = getCardRequestList();
    List<CardResponse> responses =
        r.transmitCardRequests(
            cardRequests, MultiSelectionProcessing.FIRST_MATCH, ChannelControl.CLOSE_AFTER);
    verify(r, times(1))
        .processCardRequests(
            cardRequests, MultiSelectionProcessing.FIRST_MATCH, ChannelControl.CLOSE_AFTER);
    Assert.assertNotNull(responses);
  }

  /*
   * Transmit
   */

  @Test
  public void transmit() throws Exception {
    AbstractReader r = getSpy(PLUGIN_NAME, READER_NAME);
    CardRequest request = CardRequestTest.getCardRequestSample();
    CardResponse response = r.transmitCardRequest(request, ChannelControl.CLOSE_AFTER);
    verify(r, times(1)).processCardRequest(request, ChannelControl.CLOSE_AFTER);
    Assert.assertNotNull(response);
  }

  /*
   * Helpers
   */

  /**
   * Return a basic spy reader
   *
   * @param pluginName
   * @param readerName
   * @return basic spy reader
   * @throws KeypleReaderException
   */
  public static AbstractReader getSpy(String pluginName, String readerName) {
    AbstractReader r = Mockito.spy(new BlankAbstractReader(pluginName, readerName));
    when(r.processCardRequest(any(CardRequest.class), any(ChannelControl.class)))
        .thenReturn(CardResponseTest.getACardResponse());
    when(r.processCardRequests(
            any(List.class), any(MultiSelectionProcessing.class), any(ChannelControl.class)))
        .thenReturn(getCardResponses());
    return r;
  }

  public static List<CardRequest> getCardRequestList() {
    List<CardRequest> cardRequests = new ArrayList<CardRequest>();
    cardRequests.add(CardRequestTest.getCardRequestSample());
    return cardRequests;
  }

  public static List<CardResponse> getCardResponses() {
    List<CardResponse> responses = new ArrayList<CardResponse>();
    responses.add(CardResponseTest.getACardResponse());
    return responses;
  }
}
