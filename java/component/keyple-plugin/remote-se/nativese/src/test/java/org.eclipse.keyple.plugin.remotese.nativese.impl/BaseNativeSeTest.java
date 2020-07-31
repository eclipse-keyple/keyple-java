/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.nativese.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.assertj.core.util.Lists;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.mockito.Mockito;
import com.google.gson.JsonObject;

public abstract class BaseNativeSeTest {

    ProxyReader proxyReader;
    NativeSeClientServiceFactoryTest.ObservableProxyReader observableProxyReader;
    String nativeReaderName = "nativeReaderName";
    String observableProxyReaderName = "observableProxyReaderName";

    public void init() {
        proxyReader = Mockito.mock(ProxyReader.class);
        observableProxyReader =
                Mockito.mock(NativeSeClientServiceFactoryTest.ObservableProxyReader.class);
        doReturn(nativeReaderName).when(proxyReader).getName();
        doReturn(observableProxyReaderName).when(observableProxyReader).getName();
    }


    public static KeypleMessageDto getTransmitDto(String sessionId) {
        JsonObject body = new JsonObject();
        body.addProperty("channelControl", ChannelControl.CLOSE_AFTER.name());
        body.addProperty("seRequest", KeypleJsonParser.getParser().toJson(getASeRequest()));
        return new KeypleMessageDto()//
                .setSessionId(sessionId)//
                .setAction(KeypleMessageDto.Action.TRANSMIT.name())//
                .setServerNodeId("serverNodeId")//
                .setClientNodeId("clientNodeId")//
                .setBody(body.toString());
    }

    public static KeypleMessageDto getSetDefaultSelectionDto(String sessionId) {
        JsonObject body = new JsonObject();
        body.add("defaultSelectionsRequest",
                KeypleJsonParser.getParser()
                        .toJsonTree(new DefaultSelectionsRequest(
                                Lists.newArrayList(getASeRequest()),
                                MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.CLOSE_AFTER)));
        body.addProperty("notificationMode", ObservableReader.NotificationMode.ALWAYS.name());
        body.addProperty("pollingMode", ObservableReader.PollingMode.REPEATING.name());

        return new KeypleMessageDto().setSessionId(sessionId)//
                .setAction(KeypleMessageDto.Action.SET_DEFAULT_SELECTION.name())//
                .setServerNodeId("serverNodeId")//
                .setClientNodeId("clientNodeId")//
                .setBody(body.toString());
    }

    public static KeypleMessageDto getTransmitSetDto(String sessionId) {
        JsonObject body = new JsonObject();
        body.addProperty("channelControl", ChannelControl.CLOSE_AFTER.name());
        body.addProperty("seRequests",
                KeypleJsonParser.getParser().toJson(Lists.newArrayList(getASeRequest())));
        body.addProperty("multiSeRequestProcessing", MultiSeRequestProcessing.FIRST_MATCH.name());
        return new KeypleMessageDto()//
                .setSessionId(sessionId)//
                .setAction(KeypleMessageDto.Action.TRANSMIT_SET.name())//
                .setServerNodeId("serverNodeId")//
                .setClientNodeId("clientNodeId")//
                .setBody(body.toString());
    }

    public static SeRequest getASeRequest() {
        String poAid = "A000000291A000000191";
        List<ApduRequest> poApduRequests;
        poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));
        SeRequest seRequest = new SeRequest(poApduRequests);
        return seRequest;
    }


    public static SeResponse getASeResponse() {
        ApduResponse apdu = new ApduResponse(ByteArrayUtil.fromHex("9000"), new HashSet<Integer>());
        ApduResponse apdu2 =
                new ApduResponse(ByteArrayUtil.fromHex("9000"), new HashSet<Integer>());

        AnswerToReset atr = new AnswerToReset(ByteArrayUtil.fromHex("9000"));

        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        apduResponses.add(apdu);
        apduResponses.add(apdu2);

        return new SeResponse(true, true, new SelectionStatus(atr, apdu, true), apduResponses);
    }

    static public void assertMetaDataMatches(KeypleMessageDto request, KeypleMessageDto response) {
        assertThat(response).isNotNull();
        assertThat(response.getSessionId()).isEqualTo(request.getSessionId());
        assertThat(response.getNativeReaderName()).isEqualTo(request.getNativeReaderName());
        assertThat(response.getVirtualReaderName()).isEqualTo(request.getVirtualReaderName());
        assertThat(response.getClientNodeId()).isEqualTo(request.getClientNodeId());
        assertThat(response.getServerNodeId()).isEqualTo(request.getServerNodeId());
    }

}
