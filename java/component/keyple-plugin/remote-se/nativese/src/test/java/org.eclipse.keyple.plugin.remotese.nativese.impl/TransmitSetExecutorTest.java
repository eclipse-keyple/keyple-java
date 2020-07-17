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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import java.util.List;
import org.assertj.core.util.Lists;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class TransmitSetExecutorTest extends BaseNativeSeTest {

    private static final Logger logger = LoggerFactory.getLogger(TransmitSetExecutorTest.class);

    KeypleReaderIOException keypleIOException;
    KeypleReaderIOException keypleIOExceptionWithSeResponse;
    KeypleReaderIOException keypleIOExceptionWithSeResponses;

    SeResponse seResponse;
    List<SeResponse> seResponses;
    KeypleMessageDto requestDto;

    @Before
    public void setUp() {
        this.init();
        seResponse = getASeResponse();
        seResponses = Lists.newArrayList(seResponse);

        keypleIOException = new KeypleReaderIOException("io exception test");

        keypleIOExceptionWithSeResponse =
                new KeypleReaderIOException("io exception test with SeResponse");
        keypleIOExceptionWithSeResponse.setSeResponse(seResponse);

        keypleIOExceptionWithSeResponses =
                new KeypleReaderIOException("io exception test with SeResponses");
        keypleIOExceptionWithSeResponses.setSeResponses(seResponses);

        requestDto = getTransmitSetDto();
    }

    @Test
    public void transmitSet_returnsSeResponseDto() {
        doReturn(seResponses).when(proxyReader).transmitSeRequests(//
                any(List.class), //
                any(MultiSeRequestProcessing.class), //
                any(ChannelControl.class));
        KeypleMessageDto responseDto = new TransmitSetExecutor(proxyReader).execute(requestDto);
        assertMetaDataMatches(requestDto, responseDto);
        assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.TRANSMIT_SET.name());
        assertThat(responseDto.getErrorCode()).isNull();
        assertThat(responseDto.getErrorMessage()).isNull();
        assertThat(KeypleJsonParser.getParser().fromJson(responseDto.getBody(),
                new TypeToken<List<SeResponse>>() {}.getType()))
                        .isEqualToComparingFieldByField(seResponses);
    }

    @Test
    public void transmit_returnsIoException() {
        doThrow(keypleIOException).when(proxyReader).transmitSeRequests(any(List.class), //
                any(MultiSeRequestProcessing.class), //
                any(ChannelControl.class));
        KeypleMessageDto responseDto = new TransmitSetExecutor(proxyReader).execute(requestDto);
        assertMetaDataMatches(requestDto, responseDto);
        assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.ERROR.name());
        assertThat(responseDto.getErrorCode())
                .isEqualTo(KeypleMessageDto.ErrorCode.KeypleReaderIOException.getCode());
        assertThat(responseDto.getErrorMessage()).isEqualTo(keypleIOException.getMessage());

        // check embedded seResponses
        JsonObject bodyResponse =
                KeypleJsonParser.getParser().fromJson(responseDto.getBody(), JsonObject.class);
        assertThat(bodyResponse.has("seResponse")).isFalse();
        assertThat(bodyResponse.has("seResponses")).isFalse();

    }

    @Test
    public void transmit_returnsIoException_withSeResponse() {
        doThrow(keypleIOExceptionWithSeResponse).when(proxyReader).transmitSeRequests(
                any(List.class), //
                any(MultiSeRequestProcessing.class), //
                any(ChannelControl.class));
        KeypleMessageDto responseDto = new TransmitSetExecutor(proxyReader).execute(requestDto);
        assertMetaDataMatches(requestDto, responseDto);
        assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.ERROR.name());
        assertThat(responseDto.getErrorCode())
                .isEqualTo(KeypleMessageDto.ErrorCode.KeypleReaderIOException.getCode());
        assertThat(responseDto.getErrorMessage())
                .isEqualTo(keypleIOExceptionWithSeResponse.getMessage());

        // check embedded seResponses
        JsonObject bodyResponse =
                KeypleJsonParser.getParser().fromJson(responseDto.getBody(), JsonObject.class);
        SeResponse seResponseInException = KeypleJsonParser.getParser()
                .fromJson(bodyResponse.get("seResponse").getAsJsonObject(), SeResponse.class);
        assertThat(seResponseInException).isEqualToComparingFieldByField(seResponse);
        assertThat(bodyResponse.has("seResponses")).isFalse();

    }

    @Test
    public void transmit_returnsIoException_withSeResponses() {
        doThrow(keypleIOExceptionWithSeResponses).when(proxyReader).transmitSeRequests(
                any(List.class), //
                any(MultiSeRequestProcessing.class), //
                any(ChannelControl.class));
        KeypleMessageDto responseDto = new TransmitSetExecutor(proxyReader).execute(requestDto);
        assertMetaDataMatches(requestDto, responseDto);
        assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.ERROR.name());
        assertThat(responseDto.getErrorCode())
                .isEqualTo(KeypleMessageDto.ErrorCode.KeypleReaderIOException.getCode());
        assertThat(responseDto.getErrorMessage())
                .isEqualTo(keypleIOExceptionWithSeResponses.getMessage());

        // check embedded seResponses
        JsonObject bodyResponse =
                KeypleJsonParser.getParser().fromJson(responseDto.getBody(), JsonObject.class);
        List<SeResponse> seResponsesInException = KeypleJsonParser.getParser().fromJson(
                bodyResponse.get("seResponses"), new TypeToken<List<SeResponse>>() {}.getType());
        assertThat(seResponsesInException).hasSameElementsAs(seResponses);
        assertThat(bodyResponse.has("seResponse")).isFalse();
    }



}
