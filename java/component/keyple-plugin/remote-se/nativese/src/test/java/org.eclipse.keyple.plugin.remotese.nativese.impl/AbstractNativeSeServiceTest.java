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
package org.eclipse.keyple.plugin.remotese.nativese.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import com.google.gson.reflect.TypeToken;
import java.util.List;
import org.assertj.core.util.Lists;
import org.eclipse.keyple.core.seproxy.*;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.message.ChannelControl;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.json.BodyError;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class AbstractNativeSeServiceTest extends BaseNativeSeTest {

  private static final Logger logger = LoggerFactory.getLogger(AbstractNativeSeServiceTest.class);

  final String pluginName = "pluginName";

  final SeResponse seResponse;
  final List<SeResponse> seResponses;
  final KeypleReaderIOException keypleReaderIOException;
  final KeypleReaderIOException keypleReaderIOExceptionWithSeResponse;
  final KeypleReaderIOException keypleReaderIOExceptionWithSeResponses;

  AbstractNativeSeService service;

  {
    seResponse = getASeResponse();
    seResponses = Lists.newArrayList(seResponse);

    keypleReaderIOException = new KeypleReaderIOException("io exception test");
    keypleReaderIOException.setSeResponse(seResponse);
    keypleReaderIOException.setSeResponses(seResponses);

    keypleReaderIOExceptionWithSeResponse =
        new KeypleReaderIOException("io exception test with SeResponse");
    keypleReaderIOExceptionWithSeResponse.setSeResponse(seResponse);

    keypleReaderIOExceptionWithSeResponses =
        new KeypleReaderIOException("io exception test with SeResponses");
    keypleReaderIOExceptionWithSeResponses.setSeResponses(seResponses);
  }

  @Before
  public void setUp() {

    init();

    // Service
    service = Mockito.spy(AbstractNativeSeService.class);

    // Plugin factory
    PluginFactory pluginFactoryMocked = Mockito.mock(PluginFactory.class);
    ReaderPlugin pluginMocked = Mockito.mock(ReaderPlugin.class);
    // ProxyReader mockReader = Mockito.mock(ProxyReader.class);
    doReturn(pluginMocked).when(pluginFactoryMocked).getPlugin();
    doReturn(pluginName).when(pluginFactoryMocked).getPluginName();
    doReturn(pluginName).when(pluginMocked).getName();
    doReturn(readerMocked).when(pluginMocked).getReader(readerName);
    doThrow(KeypleReaderNotFoundException.class).when(pluginMocked).getReader(readerNameUnknown);

    // Se Proxy Service
    SeProxyService.getInstance().registerPlugin(pluginFactoryMocked);
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
    SeProxyService.getInstance().unregisterPlugin(pluginName);
  }

  @Test
  public void transmit_returnsSeResponseDto() {
    // init
    doReturn(seResponse)
        .when(readerMocked)
        .transmitSeRequest(any(SeRequest.class), any(ChannelControl.class));
    KeypleMessageDto requestDto = getTransmitDto("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(readerMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.TRANSMIT.name());
    assertThat(KeypleJsonParser.getParser().fromJson(responseDto.getBody(), SeResponse.class))
        .isEqualToComparingFieldByField(seResponse);
  }

  @Test
  public void transmit_returnsIoException() {
    // init
    doThrow(keypleReaderIOException)
        .when(readerMocked)
        .transmitSeRequest(any(SeRequest.class), any(ChannelControl.class));
    KeypleMessageDto requestDto = getTransmitDto("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(readerMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.ERROR.name());
    // check embedded seResponses
    BodyError bodyResponse =
        KeypleJsonParser.getParser().fromJson(responseDto.getBody(), BodyError.class);
    KeypleReaderIOException error = (KeypleReaderIOException) bodyResponse.getException();
    assertThat(error.getSeResponses()).hasSameElementsAs(seResponses);
    assertThat(error.getSeResponse()).isEqualToComparingFieldByField(seResponse);
  }

  @Test
  public void transmitSet_returnsSeResponseDto() {
    // init
    doReturn(seResponses)
        .when(readerMocked)
        .transmitSeRequests( //
            any(List.class), //
            any(MultiSeRequestProcessing.class), //
            any(ChannelControl.class));
    // execute
    KeypleMessageDto requestDto = getTransmitSetDto("aSessionId");
    KeypleMessageDto responseDto = service.executeLocally(readerMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.TRANSMIT_SET.name());
    assertThat(
            KeypleJsonParser.getParser()
                .fromJson(responseDto.getBody(), new TypeToken<List<SeResponse>>() {}.getType()))
        .isEqualToComparingFieldByField(seResponses);
  }

  @Test
  public void transmitSet_returnsIoException() {
    // init
    doThrow(keypleReaderIOException)
        .when(readerMocked)
        .transmitSeRequests(
            any(List.class), //
            any(MultiSeRequestProcessing.class), //
            any(ChannelControl.class));
    KeypleMessageDto requestDto = getTransmitSetDto("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(readerMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.ERROR.name());
    // check embedded seResponses
    BodyError bodyResponse =
        KeypleJsonParser.getParser().fromJson(responseDto.getBody(), BodyError.class);
    KeypleReaderIOException error = (KeypleReaderIOException) bodyResponse.getException();
    assertThat(error.getSeResponses()).isNotNull();
    assertThat(error.getSeResponse()).isNotNull();
  }

  @Test
  public void transmitSet_returnsIoException_withSeResponse() {
    // init
    doThrow(keypleReaderIOExceptionWithSeResponse)
        .when(readerMocked)
        .transmitSeRequests(
            any(List.class), //
            any(MultiSeRequestProcessing.class), //
            any(ChannelControl.class));
    KeypleMessageDto requestDto = getTransmitSetDto("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(readerMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.ERROR.name());
    // check embedded seResponses
    BodyError bodyResponse =
        KeypleJsonParser.getParser().fromJson(responseDto.getBody(), BodyError.class);
    KeypleReaderIOException error = (KeypleReaderIOException) bodyResponse.getException();
    assertThat(error.getSeResponses()).isNull();
    assertThat(error.getSeResponse()).isEqualToComparingFieldByField(seResponse);
  }

  @Test
  public void transmitSet_returnsIoException_withSeResponses() {
    // init
    doThrow(keypleReaderIOExceptionWithSeResponses)
        .when(readerMocked)
        .transmitSeRequests(
            any(List.class), //
            any(MultiSeRequestProcessing.class), //
            any(ChannelControl.class));
    KeypleMessageDto requestDto = getTransmitSetDto("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(readerMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.ERROR.name());
    // check embedded seResponses
    BodyError bodyResponse =
        KeypleJsonParser.getParser().fromJson(responseDto.getBody(), BodyError.class);
    KeypleReaderIOException error = (KeypleReaderIOException) bodyResponse.getException();
    assertThat(error.getSeResponses()).hasSameElementsAs(seResponses);
    assertThat(error.getSeResponse()).isNull();
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
    doReturn(true).when(readerMocked).isSePresent();
    KeypleMessageDto requestDto = getIsSePresentDto("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(readerMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.IS_SE_PRESENT.name());
    boolean bodyValue = KeypleJsonParser.getParser().fromJson(responseDto.getBody(), Boolean.class);
    assertThat(bodyValue).isTrue();
  }

  @Test
  public void addSeProtocolSetting() {
    // init
    KeypleMessageDto requestDto = getAddSeProtocolSettingDto("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(readerMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction())
        .isEqualTo(KeypleMessageDto.Action.ADD_SE_PROTOCOL_SETTING.name());
    assertThat(responseDto.getBody()).isNull();
  }

  @Test
  public void setSeProtocolSetting() {
    // init
    KeypleMessageDto requestDto = getSetSeProtocolSettingDto("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(readerMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction())
        .isEqualTo(KeypleMessageDto.Action.SET_SE_PROTOCOL_SETTING.name());
    assertThat(responseDto.getBody()).isNull();
  }

  @Test
  public void getTransmissionMode() {
    // init
    doReturn(TransmissionMode.CONTACTS).when(readerMocked).getTransmissionMode();
    KeypleMessageDto requestDto = getGetTransmissionModeDto("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(readerMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction())
        .isEqualTo(KeypleMessageDto.Action.GET_TRANSMISSION_MODE.name());
    TransmissionMode bodyValue =
        KeypleJsonParser.getParser().fromJson(responseDto.getBody(), TransmissionMode.class);
    assertThat(bodyValue).isEqualTo(TransmissionMode.CONTACTS);
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
    KeypleMessageDto requestDto = getStartSeDetection("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(observableReaderMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction())
        .isEqualTo(KeypleMessageDto.Action.START_SE_DETECTION.name());
    assertThat(responseDto.getBody()).isNull();
  }

  @Test
  public void stopSeDetection() {
    // init
    KeypleMessageDto requestDto = getStopSeDetection("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(observableReaderMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction()).isEqualTo(KeypleMessageDto.Action.STOP_SE_DETECTION.name());
    assertThat(responseDto.getBody()).isNull();
  }

  @Test
  public void finalizeSeProcessing() {
    // init
    KeypleMessageDto requestDto = getFinalizeSeProcessing("aSessionId");
    // execute
    KeypleMessageDto responseDto = service.executeLocally(observableReaderMocked, requestDto);
    // results
    assertMetadataMatches(requestDto, responseDto);
    assertThat(responseDto.getAction())
        .isEqualTo(KeypleMessageDto.Action.FINALIZE_SE_PROCESSING.name());
    assertThat(responseDto.getBody()).isNull();
  }
}
