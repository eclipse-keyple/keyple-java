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
package org.eclipse.keyple.core.seproxy.plugin.reader;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.CardRequest;
import org.eclipse.keyple.core.seproxy.message.CardRequestTest;
import org.eclipse.keyple.core.seproxy.message.ChannelControl;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.message.SeResponseTest;
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
    r.transmitSeRequests(null, MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.CLOSE_AFTER);
    // we're just waiting right here for no exceptions to be thrown.
    verify(r, times(1))
        .transmitSeRequests(null, MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.CLOSE_AFTER);
  }

  @Test
  public void ts_transmit2_null() throws Exception {
    AbstractReader r = getSpy(PLUGIN_NAME, READER_NAME);
    r.transmitSeRequests(null, MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.KEEP_OPEN);
    // we're just waiting right here for no exceptions to be thrown.
    verify(r, times(1))
        .transmitSeRequests(null, MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.KEEP_OPEN);
  }

  @Test
  public void ts_transmit() throws Exception {
    AbstractReader r = getSpy(PLUGIN_NAME, READER_NAME);
    List<CardRequest> cardRequests = getSeRequestList();
    List<SeResponse> responses =
        r.transmitSeRequests(
            cardRequests, MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.CLOSE_AFTER);
    verify(r, times(1))
        .processSeRequests(
            cardRequests, MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.CLOSE_AFTER);
    Assert.assertNotNull(responses);
  }

  /*
   * Transmit
   */

  @Test
  public void transmit() throws Exception {
    AbstractReader r = getSpy(PLUGIN_NAME, READER_NAME);
    CardRequest request = CardRequestTest.getSeRequestSample();
    SeResponse response = r.transmitSeRequest(request, ChannelControl.CLOSE_AFTER);
    verify(r, times(1)).processSeRequest(request, ChannelControl.CLOSE_AFTER);
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
    when(r.processSeRequest(any(CardRequest.class), any(ChannelControl.class)))
        .thenReturn(SeResponseTest.getASeResponse());
    when(r.processSeRequests(
            any(List.class), any(MultiSeRequestProcessing.class), any(ChannelControl.class)))
        .thenReturn(getSeResponses());
    return r;
  }

  public static List<CardRequest> getSeRequestList() {
    List<CardRequest> cardRequests = new ArrayList<CardRequest>();
    cardRequests.add(CardRequestTest.getSeRequestSample());
    return cardRequests;
  }

  public static List<SeResponse> getSeResponses() {
    List<SeResponse> responses = new ArrayList<SeResponse>();
    responses.add(SeResponseTest.getASeResponse());
    return responses;
  }
}
