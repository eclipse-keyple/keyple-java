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
package org.eclipse.keyple.core.util.json;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.List;
import org.eclipse.keyple.core.card.command.AbstractIso7816CommandBuilderTest;
import org.eclipse.keyple.core.card.command.exception.KeypleCardCommandException;
import org.eclipse.keyple.core.card.command.exception.KeypleCardCommandUnknownStatusException;
import org.eclipse.keyple.core.card.message.*;
import org.eclipse.keyple.core.service.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.service.exception.KeypleReaderException;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class JsonParserTest {

  private static final Logger logger = LoggerFactory.getLogger(JsonParserTest.class);

  /** Test Serialization of Keyple Se Proxy Objects */
  @Test
  public void serialize_HoplinkCardRequestList() {
    List<CardSelectionRequest> selectionRequests = SampleFactory.getCardSelectionRequests();
    assertSerialization_forList(selectionRequests, List.class);
  }

  @Test
  public void serialize_cardSelectionResponses() {
    List<CardSelectionResponse> cardSelectionResponses = SampleFactory.getCompleteResponseSet();
    assertSerialization_forList(cardSelectionResponses, List.class);
  }

  @Test
  public void serialize_SelectionByAidRequest() {
    AbstractDefaultSelectionsRequest defaultSelectionsRequest = SampleFactory.getSelectionRequest();
    assertSerialization(defaultSelectionsRequest, DefaultSelectionsRequest.class);
  }

  @Test
  public void serialize_SelectionByAtrRequest() {
    AbstractDefaultSelectionsRequest defaultSelectionsRequest = SampleFactory.getSelectionRequest();
    assertSerialization(defaultSelectionsRequest, DefaultSelectionsRequest.class);
  }

  @Test
  public void serialize_NotificationMode() {
    ObservableReader.NotificationMode notificationMode = SampleFactory.getNotificationMode();
    assertSerialization(notificationMode, ObservableReader.NotificationMode.class);
  }

  /** Test Serialization of Keyple Reader Exceptions */
  @Test
  public void serialize_ReaderEvent() {
    ReaderEvent readerEvent =
        new ReaderEvent(
            "PLUGIN",
            "READER",
            ReaderEvent.EventType.CARD_INSERTED,
            new DefaultSelectionsResponse(SampleFactory.getCompleteResponseSet()));
    assertSerialization(readerEvent, ReaderEvent.class);
  }

  @Test
  public void addCustomAdapter_serializeCustomObject_shouldUseCustomAdapter() {

    MyKeypleUserDataMockAdapter adapter = new MyKeypleUserDataMockAdapter();
    Gson parser =
        KeypleGsonParser.registerTypeAdapter(SampleFactory.MyKeypleUserData.class, adapter, false);
    SampleFactory.MyKeypleUserData data = new SampleFactory.MyKeypleUserData("value");
    String json = parser.toJson(data);
    assertThat(json).contains(adapter.aDefinedJson);
    SampleFactory.MyKeypleUserData target =
        parser.fromJson(json, SampleFactory.MyKeypleUserData.class);
    assertThat(target.getField()).isEqualTo(adapter.aDefinedResult);
  }

  @Test
  public void serialize_readerException() {
    KeypleReaderException source =
        (KeypleReaderException) SampleFactory.getAReaderKeypleException();
    assertSerialization_forException(new BodyError(source), BodyError.class);
  }

  @Test
  public void serialize_ioException_withResponses() {
    KeypleReaderIOException source = SampleFactory.getIOExceptionWithResponses();
    assertSerialization_forException(new BodyError(source), BodyError.class);
  }

  @Test
  public void serialize_ioException_withResponse() {
    RuntimeException source = SampleFactory.getIOExceptionWithResponse();
    assertSerialization_forException(new BodyError(source), BodyError.class);
  }

  @Test
  public void serialize_keypleCardCommandException() {
    KeypleCardCommandException source =
        new KeypleCardCommandUnknownStatusException(
            "message", AbstractIso7816CommandBuilderTest.CommandRef.COMMAND_1, 1);
    assertSerialization_forException(new BodyError(source), BodyError.class);
  }

  @Test
  public void serialize_IllegalArgumentException() {
    RuntimeException source = new IllegalArgumentException("IAE message");
    assertSerialization_forException(new BodyError(source), BodyError.class);
  }

  /*
   * Utility Method
   */

  public static void assertSerialization(Object source, Class objectClass) {
    Gson gson = KeypleGsonParser.getParser();
    String json = gson.toJson(source);
    logger.debug("json : {}", json);
    Object target = gson.fromJson(json, objectClass);
    assertThat(source).isEqualToComparingFieldByFieldRecursively(target);
  }

  public static void assertSerialization_forList(Object source, Class<? extends List> objectClass) {
    Gson gson = KeypleGsonParser.getParser();
    String json = gson.toJson(source);
    logger.debug("json : {}", json);
    List target = gson.fromJson(json, objectClass);
    assertThat(target).hasSameElementsAs(target);
  }

  public static void assertSerialization_forException(
      Object source, Class<? extends BodyError> objectClass) {
    Gson gson = KeypleGsonParser.getParser();
    String json = gson.toJson(source);
    assertThat(json).doesNotContain("suppressedExceptions");
    assertThat(json).doesNotContain("stackTrace");
    logger.debug("json : {}", json);
    BodyError target = gson.fromJson(json, objectClass);
    logger.debug(
        "deserialize exception className : {}", target.getException().getClass().getName());
    assertThat(target).isEqualToComparingFieldByFieldRecursively(target);
  }

  public class MyKeypleUserDataMockAdapter
      implements JsonDeserializer<SampleFactory.MyKeypleUserData>,
          JsonSerializer<SampleFactory.MyKeypleUserData> {

    public String aDefinedJson = "aDefinedJson";
    public String aDefinedResult = "aDefinedResult";

    @Override
    public JsonElement serialize(
        SampleFactory.MyKeypleUserData src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(aDefinedJson);
    }

    @Override
    public SampleFactory.MyKeypleUserData deserialize(
        JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return new SampleFactory.MyKeypleUserData(aDefinedResult);
    }
  }
}
