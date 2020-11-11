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
package org.eclipse.keyple.plugin.remote.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.google.gson.JsonObject;
import java.util.*;
import org.assertj.core.util.Lists;
import org.eclipse.keyple.core.card.message.*;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.card.selection.MultiSelectionProcessing;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.mockito.Mockito;

public abstract class BaseLocalTest {

  protected static final String readerName = "readerName";
  final String readerNameUnknown = "readerNameUnknown";
  final String observableReaderName = "observableReaderName";

  ProxyReader readerMocked;
  LocalServiceClientTest.ObservableProxyReader observableReaderMocked;

  public void init() {
    readerMocked = Mockito.mock(ProxyReader.class);
    observableReaderMocked = Mockito.mock(LocalServiceClientTest.ObservableProxyReader.class);
    doReturn(readerName).when(readerMocked).getName();
  }

  public static MessageDto getTransmitDto(String sessionId) {
    JsonObject body = new JsonObject();
    body.addProperty("channelControl", ChannelControl.CLOSE_AFTER.name());
    body.addProperty("cardRequest", KeypleJsonParser.getParser().toJson(getACardRequest()));
    return new MessageDto() //
        .setSessionId(sessionId) //
        .setAction(MessageDto.Action.TRANSMIT.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setBody(body.toString());
  }

  public static MessageDto getSetDefaultSelectionDto(String sessionId) {
    JsonObject body = new JsonObject();
    body.add(
        "defaultSelectionsRequest",
        KeypleJsonParser.getParser()
            .toJsonTree(
                new DefaultSelectionsRequest(
                    Lists.newArrayList(getACardSelectionRequest()),
                    MultiSelectionProcessing.FIRST_MATCH,
                    ChannelControl.CLOSE_AFTER)));
    body.addProperty("notificationMode", ObservableReader.NotificationMode.MATCHED_ONLY.name());
    body.addProperty("pollingMode", ObservableReader.PollingMode.REPEATING.name());

    return new MessageDto()
        .setSessionId(sessionId) //
        .setAction(MessageDto.Action.SET_DEFAULT_SELECTION.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setBody(body.toString());
  }

  public static MessageDto getTransmitCardSelectionsDto(String sessionId) {
    JsonObject body = new JsonObject();
    body.addProperty("channelControl", ChannelControl.CLOSE_AFTER.name());
    body.addProperty(
        "cardSelectionRequests",
        KeypleJsonParser.getParser().toJson(Lists.newArrayList(getACardRequest())));
    body.addProperty("multiSelectionProcessing", MultiSelectionProcessing.FIRST_MATCH.name());
    return new MessageDto() //
        .setSessionId(sessionId) //
        .setAction(MessageDto.Action.TRANSMIT_CARD_SELECTION.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setBody(body.toString());
  }

  public static MessageDto getIsCardPresentDto(String sessionId) {
    return new MessageDto() //
        .setSessionId(sessionId) //
        .setAction(MessageDto.Action.IS_CARD_PRESENT.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setBody(null);
  }

  public static MessageDto getIsContactless(String sessionId) {
    return new MessageDto() //
        .setSessionId(sessionId) //
        .setAction(MessageDto.Action.IS_READER_CONTACTLESS.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setBody(null);
  }

  public static MessageDto getStartCardDetection(String sessionId) {
    JsonObject body = new JsonObject();
    body.addProperty("pollingMode", ObservableReader.PollingMode.REPEATING.name());

    return new MessageDto() //
        .setSessionId(sessionId) //
        .setAction(MessageDto.Action.START_CARD_DETECTION.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setBody(body.toString());
  }

  public static MessageDto getStopCardDetection(String sessionId) {
    JsonObject body = new JsonObject();
    return new MessageDto() //
        .setSessionId(sessionId) //
        .setAction(MessageDto.Action.STOP_CARD_DETECTION.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setBody(body.toString());
  }

  public static MessageDto getFinalizeCardProcessing(String sessionId) {
    JsonObject body = new JsonObject();
    return new MessageDto() //
        .setSessionId(sessionId) //
        .setAction(MessageDto.Action.FINALIZE_CARD_PROCESSING.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setBody(body.toString());
  }

  public static MessageDto getReleaseChannelDto(String sessionId) {
    return new MessageDto() //
        .setSessionId(sessionId) //
        .setAction(MessageDto.Action.RELEASE_CHANNEL.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setBody(null);
  }

  public static CardRequest getACardRequest() {
    String poAid = "A000000291A000000191";
    List<ApduRequest> poApduRequests;
    poApduRequests =
        Collections.singletonList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));
    return new CardRequest(poApduRequests);
  }

  public static CardSelectionRequest getACardSelectionRequest() {
    String poAid = "A000000291A000000191";
    return new CardSelectionRequest(
        CardSelector.builder()
            .aidSelector(CardSelector.AidSelector.builder().aidToSelect(poAid).build())
            .build());
  }

  public static CardResponse getACardResponse() {
    ApduResponse apdu = new ApduResponse(ByteArrayUtil.fromHex("9000"), new HashSet<Integer>());
    ApduResponse apdu2 = new ApduResponse(ByteArrayUtil.fromHex("9000"), new HashSet<Integer>());

    List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
    apduResponses.add(apdu);
    apduResponses.add(apdu2);

    return new CardResponse(true, apduResponses);
  }

  public static CardSelectionResponse getACardSelectionResponse() {
    ApduResponse apdu = new ApduResponse(ByteArrayUtil.fromHex("9000"), new HashSet<Integer>());
    ApduResponse apdu2 = new ApduResponse(ByteArrayUtil.fromHex("9000"), new HashSet<Integer>());

    AnswerToReset atr = new AnswerToReset(ByteArrayUtil.fromHex("9000"));

    List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
    apduResponses.add(apdu);
    apduResponses.add(apdu2);

    return new CardSelectionResponse(new SelectionStatus(atr, null, true), getACardResponse());
  }

  public static void assertMetadataMatches(MessageDto request, MessageDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getSessionId()).isEqualTo(request.getSessionId());
    assertThat(response.getLocalReaderName()).isEqualTo(request.getLocalReaderName());
    assertThat(response.getVirtualReaderName()).isEqualTo(request.getVirtualReaderName());
    assertThat(response.getClientNodeId()).isEqualTo(request.getClientNodeId());
    assertThat(response.getServerNodeId()).isEqualTo(request.getServerNodeId());
  }
}
