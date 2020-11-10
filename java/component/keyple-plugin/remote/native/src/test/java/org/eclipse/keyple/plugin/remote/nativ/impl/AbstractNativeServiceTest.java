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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import com.google.gson.reflect.TypeToken;
import java.util.List;
import org.assertj.core.util.Lists;
import org.eclipse.keyple.core.card.message.*;
import org.eclipse.keyple.core.card.selection.MultiSelectionProcessing;
import org.eclipse.keyple.core.service.*;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.util.json.BodyError;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remote.core.KeypleMessageDto;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class AbstractNativeServiceTest extends BaseNativeTest {

  private static final Logger logger = LoggerFactory.getLogger(AbstractNativeServiceTest.class);

  final String pluginName = "pluginName";

  final CardResponse cardResponse;
  final List<CardSelectionResponse> cardSelectionResponses;
  final KeypleReaderIOException keypleReaderIOException;
  final KeypleReaderIOException keypleReaderIOExceptionWithCardResponse;
  final KeypleReaderIOException keypleReaderIOExceptionWithCardResponses;

  AbstractNativeService service;

  {
    cardResponse = getACardResponse();
    cardSelectionResponses = Lists.newArrayList(getACardSelectionResponse());

    keypleReaderIOException = new KeypleReaderIOException("io exception test");
    keypleReaderIOException.setCardResponse(cardResponse);
    keypleReaderIOException.setCardSelectionResponses(cardSelectionResponses);

    keypleReaderIOExceptionWithCardResponse =
        new KeypleReaderIOException("io exception test with CardResponse");
    keypleReaderIOExceptionWithCardResponse.setCardResponse(cardResponse);

    keypleReaderIOExceptionWithCardResponses =
        new KeypleReaderIOException("io exception test with CardResponses");
    keypleReaderIOExceptionWithCardResponses.setCardSelectionResponses(cardSelectionResponses);
  }

  @Before
  public void setUp() {

    init();

    // Service
    service = Mockito.spy(AbstractNativeService.class);

    // Plugin factory
    PluginFactory pluginFactoryMocked = Mockito.mock(PluginFactory.class);
    Plugin pluginMocked = Mockito.mock(Plugin.class);
    // ProxyReader mockReader = Mockito.mock(ProxyReader.class);
    doReturn(pluginMocked).when(pluginFactoryMocked).getPlugin();
    doReturn(pluginName).when(pluginFactoryMocked).getPluginName();
    doReturn(pluginName).when(pluginMocked).getName();
    doReturn(readerMocked).when(pluginMocked).getReader(readerName);
    doThrow(KeypleReaderNotFoundException.class).when(pluginMocked).getReader(readerNameUnknown);

    // Se Proxy Service
    SmartCardService.getInstance().registerPlugin(pluginFactoryMocked);
  }

  @Test(expected = KeypleReaderNotFoundException.class)
  public void findLocalReader_notFound() {
    service.findLocalReader(readerNameUnknown);
  }

  @Test
  public void findLocalReader_Found() {
    // execute
    ProxyReader seReader = service.findLocalReader(readerName);
    // results
    Assert.assertNotNull(seReader);
    SmartCardService.getInstance().unregisterPlugin(pluginName);
  }

  @Test
  public void transmit_returnsCardResponseDto() {
    // init
    doReturn(cardResponse)
        .when(readerMocked)
        .transmitCardRequest(any(CardRequest.class), any(ChannelControl.class));
    KeypleMessageDto requestDto = getTransmitDto("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(readerMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.TRANSMIT.name());
    assertThat(KeypleJsonParser.getParser().fromJson(responseDto.getBody(), CardResponse.class))
        .isEqualToComparingFieldByField(cardResponse);
  }

  @Test
  public void transmit_returnsIoException() {
    // init
    doThrow(keypleReaderIOException)
        .when(readerMocked)
        .transmitCardRequest(any(CardRequest.class), any(ChannelControl.class));
    KeypleMessageDto requestDto = getTransmitDto("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(readerMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.ERROR.name());
    // check embedded cardResponses
    BodyError bodyResponse =
        KeypleJsonParser.getParser().fromJson(responseDto.getBody(), BodyError.class);
    KeypleReaderIOException error = (KeypleReaderIOException) bodyResponse.getException();
    assertThat(error.getCardSelectionResponses()).hasSameElementsAs(cardSelectionResponses);
    assertThat(error.getCardResponse()).isEqualToComparingFieldByField(cardResponse);
  }

  @Test
  public void transmitSet_returnsCardResponseDto() {
    // init
    doReturn(cardSelectionResponses)
        .when(readerMocked)
        .transmitCardSelectionRequests( //
            any(List.class), //
            any(MultiSelectionProcessing.class), //
            any(ChannelControl.class));
    // execute
    KeypleMessageDto requestDto = getTransmitCardSelectionsDto("aSessionId");
    KeypleMessageDto responseDto = service.executeLocally(readerMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction())
        .isEqualTo(KeypleMessageDto.Action.TRANSMIT_CARD_SELECTION.name());
    assertThat(
            KeypleJsonParser.getParser()
                .fromJson(
                    responseDto.getBody(),
                    new TypeToken<List<CardSelectionResponse>>() {}.getType()))
        .isEqualToComparingFieldByField(cardSelectionResponses);
  }

  @Test
  public void transmitSet_returnsIoException() {
    // init
    doThrow(keypleReaderIOException)
        .when(readerMocked)
        .transmitCardSelectionRequests(
            any(List.class), //
            any(MultiSelectionProcessing.class), //
            any(ChannelControl.class));
    KeypleMessageDto requestDto = getTransmitCardSelectionsDto("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(readerMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.ERROR.name());
    // check embedded cardResponses
    BodyError bodyResponse =
        KeypleJsonParser.getParser().fromJson(responseDto.getBody(), BodyError.class);
    KeypleReaderIOException error = (KeypleReaderIOException) bodyResponse.getException();
    assertThat(error.getCardSelectionResponses()).isNotNull();
    assertThat(error.getCardResponse()).isNotNull();
  }

  @Test
  public void transmitSet_returnsIoException_withCardResponse() {
    // init
    doThrow(keypleReaderIOExceptionWithCardResponse)
        .when(readerMocked)
        .transmitCardSelectionRequests(
            any(List.class), //
            any(MultiSelectionProcessing.class), //
            any(ChannelControl.class));
    KeypleMessageDto requestDto = getTransmitCardSelectionsDto("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(readerMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.ERROR.name());
    // check embedded cardResponses
    BodyError bodyResponse =
        KeypleJsonParser.getParser().fromJson(responseDto.getBody(), BodyError.class);
    KeypleReaderIOException error = (KeypleReaderIOException) bodyResponse.getException();
    assertThat(error.getCardSelectionResponses()).isNull();
    assertThat(error.getCardResponse()).isEqualToComparingFieldByField(cardResponse);
  }

  @Test
  public void transmitSet_returnsIoException_withCardResponses() {
    // init
    doThrow(keypleReaderIOExceptionWithCardResponses)
        .when(readerMocked)
        .transmitCardSelectionRequests(
            any(List.class), //
            any(MultiSelectionProcessing.class), //
            any(ChannelControl.class));
    KeypleMessageDto requestDto = getTransmitCardSelectionsDto("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(readerMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.ERROR.name());
    // check embedded cardResponses
    BodyError bodyResponse =
        KeypleJsonParser.getParser().fromJson(responseDto.getBody(), BodyError.class);
    KeypleReaderIOException error = (KeypleReaderIOException) bodyResponse.getException();
    assertThat(error.getCardSelectionResponses()).hasSameElementsAs(cardSelectionResponses);
    assertThat(error.getCardResponse()).isNull();
  }

  @Test
  public void setDefaultSelection() {
    // init
    KeypleMessageDto requestDto = getSetDefaultSelectionDto("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(observableReaderMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction())
        .isEqualTo(KeypleMessageDto.Action.SET_DEFAULT_SELECTION.name());
    assertThat(responseDto.getBody()).isNull();
  }

  @Test
  public void isSePresent() {
    // init
    doReturn(true).when(readerMocked).isCardPresent();
    KeypleMessageDto requestDto = getIsCardPresentDto("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(readerMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.IS_CARD_PRESENT.name());
    boolean bodyValue = KeypleJsonParser.getParser().fromJson(responseDto.getBody(), Boolean.class);
    assertThat(bodyValue).isTrue();
  }

  @Test
  public void isContactless() {
    // init
    doReturn(true).when(readerMocked).isContactless();
    KeypleMessageDto requestDto = getIsContactless("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(readerMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction())
        .isEqualTo(KeypleMessageDto.Action.IS_READER_CONTACTLESS.name());
    boolean bodyValue = KeypleJsonParser.getParser().fromJson(responseDto.getBody(), Boolean.class);
    assertThat(bodyValue).isTrue();
  }

  @Test
  public void releaseChannelDto() {
    // init
    KeypleMessageDto requestDto = getReleaseChannelDto("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(readerMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.RELEASE_CHANNEL.name());
    assertThat(responseDto.getBody()).isNull();
  }

  @Test
  public void startSeDetection() {
    // init
    KeypleMessageDto requestDto = getStartCardDetection("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(observableReaderMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction())
        .isEqualTo(KeypleMessageDto.Action.START_CARD_DETECTION.name());
    assertThat(responseDto.getBody()).isNull();
  }

  @Test
  public void stopCardDetection() {
    // init
    KeypleMessageDto requestDto = getStopCardDetection("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(observableReaderMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction())
        .isEqualTo(KeypleMessageDto.Action.STOP_CARD_DETECTION.name());
    assertThat(responseDto.getBody()).isNull();
  }

  @Test
  public void finalizeCardProcessing() {
    // init
    KeypleMessageDto requestDto = getFinalizeCardProcessing("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(observableReaderMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction())
        .isEqualTo(KeypleMessageDto.Action.FINALIZE_CARD_PROCESSING.name());
    assertThat(responseDto.getBody()).isNull();
  }
}
