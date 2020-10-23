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
package org.eclipse.keyple.plugin.remote.nativ.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.google.gson.JsonObject;
import java.util.*;
import org.assertj.core.util.Lists;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remote.core.KeypleMessageDto;
import org.mockito.Mockito;

public abstract class BaseNativeTest {

  protected static final String readerName = "readerName";
  final String readerNameUnknown = "readerNameUnknown";
  final String observableReaderName = "observableReaderName";

  ProxyReader readerMocked;
  NativeClientServiceTest.ObservableProxyReader observableReaderMocked;

  public void init() {
    readerMocked = Mockito.mock(ProxyReader.class);
    observableReaderMocked = Mockito.mock(NativeClientServiceTest.ObservableProxyReader.class);
    doReturn(readerName).when(readerMocked).getName();
  }

  public static KeypleMessageDto getTransmitDto(String sessionId) {
    JsonObject body = new JsonObject();
    body.addProperty("channelControl", ChannelControl.CLOSE_AFTER.name());
    body.addProperty("seRequest", KeypleJsonParser.getParser().toJson(getASeRequest()));
    return new KeypleMessageDto() //
        .setSessionId(sessionId) //
        .setAction(KeypleMessageDto.Action.TRANSMIT.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setBody(body.toString());
  }

  public static KeypleMessageDto getSetDefaultSelectionDto(String sessionId) {
    JsonObject body = new JsonObject();
    body.add(
        "defaultSelectionsRequest",
        KeypleJsonParser.getParser()
            .toJsonTree(
                new DefaultSelectionsRequest(
                    Lists.newArrayList(getASeRequest()),
                    MultiSeRequestProcessing.FIRST_MATCH,
                    ChannelControl.CLOSE_AFTER)));
    body.addProperty("notificationMode", ObservableReader.NotificationMode.MATCHED_ONLY.name());
    body.addProperty("pollingMode", ObservableReader.PollingMode.REPEATING.name());

    return new KeypleMessageDto()
        .setSessionId(sessionId) //
        .setAction(KeypleMessageDto.Action.SET_DEFAULT_SELECTION.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setBody(body.toString());
  }

  public static KeypleMessageDto getTransmitSetDto(String sessionId) {
    JsonObject body = new JsonObject();
    body.addProperty("channelControl", ChannelControl.CLOSE_AFTER.name());
    body.addProperty(
        "seRequests", KeypleJsonParser.getParser().toJson(Lists.newArrayList(getASeRequest())));
    body.addProperty("multiSeRequestProcessing", MultiSeRequestProcessing.FIRST_MATCH.name());
    return new KeypleMessageDto() //
        .setSessionId(sessionId) //
        .setAction(KeypleMessageDto.Action.TRANSMIT_SET.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setBody(body.toString());
  }

  public static KeypleMessageDto getIsSePresentDto(String sessionId) {
    return new KeypleMessageDto() //
        .setSessionId(sessionId) //
        .setAction(KeypleMessageDto.Action.IS_CARD_PRESENT.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setBody(null);
  }

  public static KeypleMessageDto getIsContactless(String sessionId) {
    return new KeypleMessageDto() //
        .setSessionId(sessionId) //
        .setAction(KeypleMessageDto.Action.IS_READER_CONTACTLESS.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setBody(null);
  }

  public static KeypleMessageDto getStartSeDetection(String sessionId) {
    JsonObject body = new JsonObject();
    body.addProperty("pollingMode", ObservableReader.PollingMode.REPEATING.name());

    return new KeypleMessageDto() //
        .setSessionId(sessionId) //
        .setAction(KeypleMessageDto.Action.START_CARD_DETECTION.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setBody(body.toString());
  }

  public static KeypleMessageDto getStopSeDetection(String sessionId) {
    JsonObject body = new JsonObject();
    return new KeypleMessageDto() //
        .setSessionId(sessionId) //
        .setAction(KeypleMessageDto.Action.STOP_CARD_DETECTION.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setBody(body.toString());
  }

  public static KeypleMessageDto getFinalizeSeProcessing(String sessionId) {
    JsonObject body = new JsonObject();
    return new KeypleMessageDto() //
        .setSessionId(sessionId) //
        .setAction(KeypleMessageDto.Action.FINALIZE_CARD_PROCESSING.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setBody(body.toString());
  }

  public static KeypleMessageDto getReleaseChannelDto(String sessionId) {
    return new KeypleMessageDto() //
        .setSessionId(sessionId) //
        .setAction(KeypleMessageDto.Action.RELEASE_CHANNEL.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setBody(null);
  }

  public static SeRequest getASeRequest() {
    String poAid = "A000000291A000000191";
    List<ApduRequest> poApduRequests;
    poApduRequests =
        Collections.singletonList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));
    return new SeRequest(poApduRequests);
  }

  public static SeResponse getASeResponse() {
    ApduResponse apdu = new ApduResponse(ByteArrayUtil.fromHex("9000"), new HashSet<Integer>());
    ApduResponse apdu2 = new ApduResponse(ByteArrayUtil.fromHex("9000"), new HashSet<Integer>());

    AnswerToReset atr = new AnswerToReset(ByteArrayUtil.fromHex("9000"));

    List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
    apduResponses.add(apdu);
    apduResponses.add(apdu2);

    return new SeResponse(true, true, new SelectionStatus(atr, apdu, true), apduResponses);
  }

  public static void assertMetadataMatches(KeypleMessageDto request, KeypleMessageDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getSessionId()).isEqualTo(request.getSessionId());
    assertThat(response.getNativeReaderName()).isEqualTo(request.getNativeReaderName());
    assertThat(response.getVirtualReaderName()).isEqualTo(request.getVirtualReaderName());
    assertThat(response.getClientNodeId()).isEqualTo(request.getClientNodeId());
    assertThat(response.getServerNodeId()).isEqualTo(request.getServerNodeId());
  }
}