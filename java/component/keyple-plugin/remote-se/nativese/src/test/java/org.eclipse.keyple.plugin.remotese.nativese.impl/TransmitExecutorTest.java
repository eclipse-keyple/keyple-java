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
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class TransmitExecutorTest extends BaseNativeSeTest {

    private static final Logger logger = LoggerFactory.getLogger(TransmitExecutorTest.class);

    KeypleReaderIOException keypleIOException;
    SeResponse seResponse;
    List<SeResponse> seResponses;
    KeypleMessageDto requestDto;

    @Before
    public void setUp() {
        this.init();
        seResponse = getASeResponse();
        seResponses = Lists.newArrayList(seResponse);
        keypleIOException = new KeypleReaderIOException("io exception test");
        keypleIOException.setSeResponse(seResponse);
        keypleIOException.setSeResponses(seResponses);
        requestDto = getTransmitDto();
    }

    @Test
    public void transmit_returnsSeResponseDto() {

        doReturn(seResponse).when(proxyReader).transmitSeRequest(any(SeRequest.class),
                any(ChannelControl.class));
        KeypleMessageDto responseDto = new TransmitExecutor(proxyReader).execute(requestDto);
        assertMetaDataMatches(requestDto, responseDto);
        assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.TRANSMIT.name());
        assertThat(responseDto.getErrorCode()).isNull();
        assertThat(responseDto.getErrorMessage()).isNull();
        assertThat(KeypleJsonParser.getParser().fromJson(responseDto.getBody(), SeResponse.class))
                .isEqualToComparingFieldByField(seResponse);
    }

    @Test
    public void transmit_returnsIoException() {
        doThrow(keypleIOException).when(proxyReader).transmitSeRequest(any(SeRequest.class),
                any(ChannelControl.class));
        KeypleMessageDto requestDto = getTransmitDto();
        KeypleMessageDto responseDto = new TransmitExecutor(proxyReader).execute(requestDto);
        assertMetaDataMatches(requestDto, responseDto);
        assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.ERROR.name());
        assertThat(responseDto.getErrorCode())
                .isEqualTo(KeypleMessageDto.ErrorCode.KeypleReaderIOException.getCode());
        assertThat(responseDto.getErrorMessage()).isEqualTo(keypleIOException.getMessage());

        // check embedded seResponses
        JsonObject bodyResponse =
                KeypleJsonParser.getParser().fromJson(responseDto.getBody(), JsonObject.class);
        SeResponse seResponseInException = KeypleJsonParser.getParser()
                .fromJson(bodyResponse.get("seResponse").getAsJsonObject(), SeResponse.class);
        List<SeResponse> seResponsesInException = KeypleJsonParser.getParser().fromJson(
                bodyResponse.get("seResponses"), new TypeToken<List<SeResponse>>() {}.getType());
        assertThat(seResponseInException).isEqualToComparingFieldByField(seResponse);
        assertThat(seResponsesInException).hasSameElementsAs(seResponses);
    }

}
