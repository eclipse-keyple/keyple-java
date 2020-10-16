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
package org.eclipse.keyple.plugin.remotese.rm.json;

import com.google.gson.Gson;
import java.util.List;
import java.util.Set;
import org.eclipse.keyple.core.reader.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.reader.event.ObservableReader;
import org.eclipse.keyple.core.reader.event.ReaderEvent;
import org.eclipse.keyple.core.reader.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.reader.message.CardRequest;
import org.eclipse.keyple.core.reader.message.CardResponse;
import org.eclipse.keyple.core.reader.message.DefaultSelectionsRequest;
import org.eclipse.keyple.core.reader.message.DefaultSelectionsResponse;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class JsonParserTest {

  private static final Logger logger = LoggerFactory.getLogger(JsonParserTest.class);

  /** Test Serialization of Keyple Card Proxy Objects */
  @Test
  public void testHoplinkCardRequestList() {
    List<CardRequest> cardRequests = SampleFactory.getACardRequestList_ISO14443_4();
    testSerializeDeserializeObj(cardRequests, Set.class);
  }

  @Test
  public void testCompleteCardRequestList() {
    List<CardRequest> cardRequests = SampleFactory.getCompleteRequestList();
    testSerializeDeserializeObj(cardRequests, Set.class);
  }

  @Test
  public void testCardResponses() {
    List<CardResponse> cardResponse = SampleFactory.getCompleteResponseSet();
    testSerializeDeserializeObj(cardResponse, List.class);
  }

  @Test
  public void testSelectionByAidRequest() {
    AbstractDefaultSelectionsRequest defaultSelectionsRequest = SampleFactory.getSelectionRequest();
    testSerializeDeserializeObj(defaultSelectionsRequest, DefaultSelectionsRequest.class);
  }

  @Test
  public void testSelectionByAtrRequest() {
    AbstractDefaultSelectionsRequest defaultSelectionsRequest = SampleFactory.getSelectionRequest();
    testSerializeDeserializeObj(defaultSelectionsRequest, DefaultSelectionsRequest.class);
  }

  @Test
  public void testNotificationMode() {
    ObservableReader.NotificationMode notificationMode = SampleFactory.getNotificationMode();
    testSerializeDeserializeObj(notificationMode, ObservableReader.NotificationMode.class);
  }

  /** Test Serialization of Keyple Reader Exceptions */
  @Test
  public void testSimpleKeypleException() {
    KeypleReaderIOException exception = SampleFactory.getASimpleKeypleException();
    testSerializeDeserializeObj(exception, KeypleReaderIOException.class);
  }

  @Test
  public void testStackedKeypleException() {
    KeypleReaderIOException exception = SampleFactory.getAStackedKeypleException();
    testSerializeDeserializeObj(exception, KeypleReaderIOException.class);
  }

  @Test
  public void testReaderEvent() {
    ReaderEvent readerEvent =
        new ReaderEvent(
            "PLUGIN",
            "READER",
            ReaderEvent.EventType.CARD_INSERTED,
            new DefaultSelectionsResponse(SampleFactory.getCompleteResponseSet()));
    testSerializeDeserializeObj(readerEvent, ReaderEvent.class);
  }

  /*
   * Utility Method
   */

  public Object testSerializeDeserializeObj(Object obj, Class objectClass) {
    Gson gson = JsonParser.getGson();
    String json = gson.toJson(obj);
    logger.debug("json 1 : {}", json);
    Object deserializeObj = gson.fromJson(json, objectClass);
    logger.debug("deserializeObj : {}", deserializeObj.toString());
    String json2 = gson.toJson(deserializeObj);
    logger.debug("json 2 : {}", json2);
    assert json.equals(json2);
    return deserializeObj;
  }
}
