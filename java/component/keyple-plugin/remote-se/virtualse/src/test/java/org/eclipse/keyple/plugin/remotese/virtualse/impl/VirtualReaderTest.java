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
package org.eclipse.keyple.plugin.remotese.virtualse.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.google.gson.reflect.TypeToken;
import java.util.*;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.json.BodyError;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.core.exception.KeypleTimeoutException;
import org.eclipse.keyple.plugin.remotese.core.impl.AbstractKeypleNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VirtualReaderTest {

  static final String pluginName = "pluginName";
  static final String nativeReaderName = "nativeReaderName";

  VirtualReader reader;
  AbstractKeypleNode node;

  @Before
  public void setUp() {
    node = mock(AbstractKeypleNode.class);
    reader = new VirtualReader(pluginName, nativeReaderName, node, "val1", null);
  }

  @Test
  public void constructor_shouldGenerateName() {
    assertThat(reader.getName()).isNotEmpty();
  }

  @Test
  public void processSeRequest_whenOk_shouldCallTheHandlerAndReturnResponses() {

    // init request
    SeRequest seRequest = SampleFactory.getASeRequest();
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    SeResponse seResponse = SampleFactory.getCompleteResponseList().get(0);

    KeypleMessageDto responseDto =
        new KeypleMessageDto() //
            .setAction(KeypleMessageDto.Action.TRANSMIT.name()) //
            .setVirtualReaderName(reader.getName()) //
            .setNativeReaderName(reader.nativeReaderName) //
            .setBody(KeypleJsonParser.getParser().toJson(seResponse, SeResponse.class));

    doReturn(responseDto).when(node).sendRequest(any(KeypleMessageDto.class));

    // execute
    SeResponse seResponseReturned = reader.processSeRequest(seRequest, channelControl);

    // verify
    assertThat(seResponseReturned).isEqualToComparingFieldByField(seResponse);
  }

  @Test(expected = KeypleTimeoutException.class)
  public void processSeRequest_whenNodeTimeout_shouldThrowKTE() {

    // init response
    mockTimeout();

    // init request
    SeRequest seRequest = SampleFactory.getASeRequest();
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // execute
    reader.processSeRequest(seRequest, channelControl);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void processSeRequest_whenError_shouldThrowOriginalException() {

    // init response
    mockError();

    // init request
    SeRequest seRequest = SampleFactory.getASeRequest();
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // execute
    reader.processSeRequest(seRequest, channelControl);
  }

  @Test
  public void processSeRequests_whenOk_shouldCallTheHandlerAndReturnResponses() {

    // init request
    List<SeRequest> seRequests = SampleFactory.getCompleteRequestList();
    MultiSeRequestProcessing multiSeRequestProcessing = MultiSeRequestProcessing.FIRST_MATCH;
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    List<SeResponse> seResponses = SampleFactory.getCompleteResponseList();

    KeypleMessageDto responseDto =
        new KeypleMessageDto() //
            .setAction(KeypleMessageDto.Action.TRANSMIT_SET.name()) //
            .setVirtualReaderName(reader.getName()) //
            .setNativeReaderName(reader.nativeReaderName) //
            .setBody(
                KeypleJsonParser.getParser()
                    .toJson(seResponses, new TypeToken<ArrayList<SeResponse>>() {}.getType()));

    doReturn(responseDto).when(node).sendRequest(any(KeypleMessageDto.class));

    // execute
    List<SeResponse> seResponsesReturned =
        reader.processSeRequests(seRequests, multiSeRequestProcessing, channelControl);

    // verify
    assertThat(seResponsesReturned).hasSameElementsAs(seResponses);
  }

  @Test(expected = KeypleTimeoutException.class)
  public void processSeRequests_whenNodeTimeout_shouldThrowKTE() {

    // init response
    mockTimeout();

    // init request
    List<SeRequest> seRequests = SampleFactory.getCompleteRequestList();
    MultiSeRequestProcessing multiSeRequestProcessing = MultiSeRequestProcessing.FIRST_MATCH;
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // execute
    reader.processSeRequests(seRequests, multiSeRequestProcessing, channelControl);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void processSeRequests_whenError_shouldThrowOriginalException() {

    // init response
    mockError();

    // init request
    List<SeRequest> seRequests = SampleFactory.getCompleteRequestList();
    MultiSeRequestProcessing multiSeRequestProcessing = MultiSeRequestProcessing.FIRST_MATCH;
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // execute
    reader.processSeRequests(seRequests, multiSeRequestProcessing, channelControl);
  }

  @Test
  public void isSePresent_whenOk_shouldCallTheHandlerAndReturnResponses() {

    // init
    KeypleMessageDto responseDto =
        new KeypleMessageDto() //
            .setAction(KeypleMessageDto.Action.IS_SE_PRESENT.name()) //
            .setVirtualReaderName(reader.getName()) //
            .setNativeReaderName(reader.nativeReaderName) //
            .setBody(KeypleJsonParser.getParser().toJson(true, Boolean.class));

    doReturn(responseDto).when(node).sendRequest(any(KeypleMessageDto.class));

    // execute
    boolean result = reader.isSePresent();

    // verify
    assertThat(result).isTrue();
  }

  @Test(expected = KeypleTimeoutException.class)
  public void isSePresent_whenNodeTimeout_shouldThrowKTE() {
    // init
    mockTimeout();
    // execute
    reader.isSePresent();
  }

  @Test(expected = KeypleReaderIOException.class)
  public void isSePresent_whenError_shouldThrowOriginalException() {
    // init
    mockError();
    // execute
    reader.isSePresent();
  }

  @Test
  public void addSeProtocolSetting_whenOk_shouldCallTheHandlerAndReturnResponses() {

    // init request
    SeProtocol seProtocol = SampleFactory.getSeProtocol();

    // init response
    KeypleMessageDto responseDto =
        new KeypleMessageDto() //
            .setAction(KeypleMessageDto.Action.ADD_SE_PROTOCOL_SETTING.name()) //
            .setVirtualReaderName(reader.getName()) //
            .setNativeReaderName(reader.nativeReaderName);

    doReturn(responseDto).when(node).sendRequest(any(KeypleMessageDto.class));

    // execute
    reader.addSeProtocolSetting(seProtocol, "protocolRule");
  }

  @Test(expected = KeypleTimeoutException.class)
  public void addSeProtocolSetting_whenNodeTimeout_shouldThrowKTE() {

    // init response
    mockTimeout();

    // init request
    SeProtocol seProtocol = SampleFactory.getSeProtocol();

    // execute
    reader.addSeProtocolSetting(seProtocol, "protocolRule");
  }

  @Test(expected = KeypleReaderIOException.class)
  public void addSeProtocolSetting_whenError_shouldThrowOriginalException() {

    // init response
    mockError();

    // init request
    SeProtocol seProtocol = SampleFactory.getSeProtocol();

    // execute
    reader.addSeProtocolSetting(seProtocol, "protocolRule");
  }

  @Test
  public void setSeProtocolSetting_whenOk_shouldCallTheHandlerAndReturnResponses() {

    // init request
    Map<SeProtocol, String> seProtocolSetting = SampleFactory.getSeProtocolSetting();

    // init response
    KeypleMessageDto responseDto =
        new KeypleMessageDto() //
            .setAction(KeypleMessageDto.Action.SET_SE_PROTOCOL_SETTING.name()) //
            .setVirtualReaderName(reader.getName()) //
            .setNativeReaderName(reader.nativeReaderName);

    doReturn(responseDto).when(node).sendRequest(any(KeypleMessageDto.class));

    // execute
    reader.setSeProtocolSetting(seProtocolSetting);
  }

  @Test(expected = KeypleTimeoutException.class)
  public void setSeProtocolSetting_whenNodeTimeout_shouldThrowKTE() {

    // init response
    mockTimeout();

    // init request
    Map<SeProtocol, String> seProtocolSetting = SampleFactory.getSeProtocolSetting();

    // execute
    reader.setSeProtocolSetting(seProtocolSetting);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void setSeProtocolSetting_whenError_shouldThrowOriginalException() {

    // init response
    mockError();

    // init request
    Map<SeProtocol, String> seProtocolSetting = SampleFactory.getSeProtocolSetting();

    // execute
    reader.setSeProtocolSetting(seProtocolSetting);
  }

  @Test
  public void getTransmissionMode_whenOk_shouldCallTheHandlerAndReturnResponses() {

    // init
    KeypleMessageDto responseDto =
        new KeypleMessageDto() //
            .setAction(KeypleMessageDto.Action.GET_TRANSMISSION_MODE.name()) //
            .setVirtualReaderName(reader.getName()) //
            .setNativeReaderName(reader.nativeReaderName) //
            .setBody(
                KeypleJsonParser.getParser()
                    .toJson(TransmissionMode.CONTACTS, TransmissionMode.class));

    doReturn(responseDto).when(node).sendRequest(any(KeypleMessageDto.class));

    // execute
    TransmissionMode result = reader.getTransmissionMode();

    // verify
    assertThat(result).isEqualTo(TransmissionMode.CONTACTS);
  }

  @Test(expected = KeypleTimeoutException.class)
  public void getTransmissionMode_whenNodeTimeout_shouldThrowKTE() {
    // init
    mockTimeout();
    // execute
    reader.getTransmissionMode();
  }

  @Test(expected = KeypleReaderIOException.class)
  public void getTransmissionMode_whenError_shouldThrowOriginalException() {
    // init
    mockError();
    // execute
    reader.getTransmissionMode();
  }

  @Test
  public void releaseChannel_whenOk_shouldCallTheHandlerAndReturnResponses() {

     // init response
    KeypleMessageDto responseDto =
            new KeypleMessageDto() //
                    .setAction(KeypleMessageDto.Action.RELEASE_CHANNEL.name()) //
                    .setVirtualReaderName(reader.getName()) //
                    .setNativeReaderName(reader.nativeReaderName);

    doReturn(responseDto).when(node).sendRequest(any(KeypleMessageDto.class));

    // execute
    reader.releaseChannel();
  }

  @Test(expected = KeypleTimeoutException.class)
  public void releaseChannel_whenNodeTimeout_shouldThrowKTE() {
    // init
    mockTimeout();
    // execute
    reader.releaseChannel();
  }

  @Test(expected = KeypleReaderIOException.class)
  public void releaseChannel_whenError_shouldThrowOriginalException() {
    // init
    mockError();
    // execute
    reader.releaseChannel();
  }

  @Test
  public void getSessionId_whenIsSet_shouldReturnCurrentValue() {
    String sessionId = reader.getSessionId();
    assertThat(sessionId).isEqualTo("val1");
  }

  private void mockTimeout() {
    doThrow(new KeypleTimeoutException("test")).when(node).sendRequest(any(KeypleMessageDto.class));
  }

  private void mockError() {

    KeypleReaderIOException error = SampleFactory.getASimpleKeypleException();

    KeypleMessageDto responseDto =
        new KeypleMessageDto() //
            .setAction(KeypleMessageDto.Action.ERROR.name()) //
            .setVirtualReaderName(reader.getName()) //
            .setNativeReaderName(reader.nativeReaderName) //
            .setBody(KeypleJsonParser.getParser().toJson(new BodyError(error)));

    doReturn(responseDto).when(node).sendRequest(any(KeypleMessageDto.class));
  }
}
