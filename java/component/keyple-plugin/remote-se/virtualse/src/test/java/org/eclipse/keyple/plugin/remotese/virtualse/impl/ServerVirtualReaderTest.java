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
import static org.mockito.Mockito.*;

import java.util.*;
import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.ChannelControl;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServerVirtualReaderTest {

  static final String serviceId = "serviceId";
  static final String userInputDataJson = "userInputDataJson";
  static final String initialSeContentJson = "initialSeContentJson";

  VirtualReader virtualReaderMocked;
  ServerVirtualReader reader;

  private static class MyMatchingSe extends AbstractMatchingSe {

    MyMatchingSe(SeResponse selectionResponse) {
      super(selectionResponse);
    }
  }

  @Before
  public void setUp() {
    virtualReaderMocked = mock(VirtualReader.class);
    reader =
        new ServerVirtualReader(
            virtualReaderMocked, serviceId, userInputDataJson, initialSeContentJson);
  }

  @Test
  public void transmitSeRequest_shouldDelegateMethodToVirtualReader() {

    // init request
    SeRequest seRequest = SampleFactory.getASeRequest();
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    SeResponse seResponse = SampleFactory.getCompleteResponseList().get(0);
    doReturn(seResponse).when(virtualReaderMocked).transmitSeRequest(seRequest, channelControl);

    // execute
    SeResponse seResponseReturned = reader.transmitSeRequest(seRequest, channelControl);

    // verify
    verify(virtualReaderMocked).transmitSeRequest(seRequest, channelControl);
    verifyNoMoreInteractions(virtualReaderMocked);
    assertThat(seResponseReturned).isEqualToComparingFieldByField(seResponse);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void transmitSeRequest_whenError_shouldThrowOriginalException() {

    // init request
    SeRequest seRequest = SampleFactory.getASeRequest();
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualReaderMocked)
        .transmitSeRequest(seRequest, channelControl);

    // execute
    reader.transmitSeRequest(seRequest, channelControl);
  }

  @Test
  public void transmitSeRequests_shouldDelegateMethodToVirtualReader() {

    // init request
    List<SeRequest> seRequests = SampleFactory.getCompleteRequestList();
    MultiSeRequestProcessing multiSeRequestProcessing = MultiSeRequestProcessing.FIRST_MATCH;
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    List<SeResponse> seResponses = SampleFactory.getCompleteResponseList();
    doReturn(seResponses)
        .when(virtualReaderMocked)
        .transmitSeRequests(seRequests, multiSeRequestProcessing, channelControl);

    // execute
    List<SeResponse> seResponsesReturned =
        reader.transmitSeRequests(seRequests, multiSeRequestProcessing, channelControl);

    // verify
    verify(virtualReaderMocked)
        .transmitSeRequests(seRequests, multiSeRequestProcessing, channelControl);
    verifyNoMoreInteractions(virtualReaderMocked);
    assertThat(seResponsesReturned).hasSameElementsAs(seResponses);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void transmitSeRequests_whenError_shouldThrowOriginalException() {

    // init request
    List<SeRequest> seRequests = SampleFactory.getCompleteRequestList();
    MultiSeRequestProcessing multiSeRequestProcessing = MultiSeRequestProcessing.FIRST_MATCH;
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualReaderMocked)
        .transmitSeRequests(seRequests, multiSeRequestProcessing, channelControl);

    // execute
    reader.transmitSeRequests(seRequests, multiSeRequestProcessing, channelControl);
  }

  @Test
  public void isSePresent_shouldDelegateMethodToVirtualReader() {

    // init
    doReturn(true).when(virtualReaderMocked).isSePresent();

    // execute
    boolean result = reader.isSePresent();

    // verify
    verify(virtualReaderMocked).isSePresent();
    verifyNoMoreInteractions(virtualReaderMocked);
    assertThat(result).isTrue();
  }

  @Test(expected = KeypleReaderIOException.class)
  public void isSePresent_whenError_shouldThrowOriginalException() {

    // init
    doThrow(new KeypleReaderIOException("test")).when(virtualReaderMocked).isSePresent();

    // execute
    reader.isSePresent();
  }

  @Test
  public void addSeProtocolSetting_shouldDelegateMethodToVirtualReader() {

    // init request
    SeProtocol seProtocol = SampleFactory.getSeProtocol();
    String protocolRule = "protocolRule";

    // init response
    doNothing().when(virtualReaderMocked).addSeProtocolSetting(seProtocol, protocolRule);

    // execute
    reader.addSeProtocolSetting(seProtocol, protocolRule);

    // verify
    verify(virtualReaderMocked).addSeProtocolSetting(seProtocol, protocolRule);
    verifyNoMoreInteractions(virtualReaderMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void addSeProtocolSetting_whenError_shouldThrowOriginalException() {

    // init request
    SeProtocol seProtocol = SampleFactory.getSeProtocol();
    String protocolRule = "protocolRule";

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualReaderMocked)
        .addSeProtocolSetting(seProtocol, protocolRule);

    // execute
    reader.addSeProtocolSetting(seProtocol, protocolRule);
  }

  @Test
  public void setSeProtocolSetting_shouldDelegateMethodToVirtualReader() {

    // init request
    Map<SeProtocol, String> seProtocolSetting = SampleFactory.getSeProtocolSetting();

    // init response
    doNothing().when(virtualReaderMocked).setSeProtocolSetting(seProtocolSetting);

    // execute
    reader.setSeProtocolSetting(seProtocolSetting);

    // verify
    verify(virtualReaderMocked).setSeProtocolSetting(seProtocolSetting);
    verifyNoMoreInteractions(virtualReaderMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void setSeProtocolSetting_whenError_shouldThrowOriginalException() {

    // init request
    Map<SeProtocol, String> seProtocolSetting = SampleFactory.getSeProtocolSetting();

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualReaderMocked)
        .setSeProtocolSetting(seProtocolSetting);

    // execute
    reader.setSeProtocolSetting(seProtocolSetting);
  }

  @Test
  public void getTransmissionMode_shouldDelegateMethodToVirtualReader() {

    // init
    doReturn(TransmissionMode.CONTACTS).when(virtualReaderMocked).getTransmissionMode();

    // execute
    TransmissionMode result = reader.getTransmissionMode();

    // verify
    verify(virtualReaderMocked).getTransmissionMode();
    verifyNoMoreInteractions(virtualReaderMocked);
    assertThat(result).isEqualTo(TransmissionMode.CONTACTS);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void getTransmissionMode_whenError_shouldThrowOriginalException() {

    // init
    doThrow(new KeypleReaderIOException("test")).when(virtualReaderMocked).getTransmissionMode();

    // execute
    reader.getTransmissionMode();
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  public void getName_shouldDelegateMethodToVirtualReader() {

    // init response
    String name = "name1";
    doReturn(name).when(virtualReaderMocked).getName();

    // execute
    String result = reader.getName();

    // verify
    verify(virtualReaderMocked).getName();
    verifyNoMoreInteractions(virtualReaderMocked);
    assertThat(result).isSameAs(name);
  }

  @Test
  public void getServiceId_shouldReturnInitialValue() {

    // execute
    String result = reader.getServiceId();

    // verify
    verifyZeroInteractions(virtualReaderMocked);
    assertThat(result).isSameAs(serviceId);
  }

  @Test(expected = IllegalArgumentException.class)
  public void getUserInputData_whenClassIsNull_shouldThrowIAE() {

    // execute
    reader.getUserInputData(null);
  }

  @Test
  public void getUserInputData_whenDataIsNull_shouldReturnNull() {

    // init
    reader = new ServerVirtualReader(virtualReaderMocked, serviceId, null, initialSeContentJson);

    // execute
    String result = reader.getUserInputData(String.class);

    // verify
    assertThat(result).isNull();
  }

  @Test
  public void getUserInputData_whenDataIsNotNull_shouldReturnParsedData() {

    // execute
    String result = reader.getUserInputData(String.class);

    // verify
    assertThat(result).isEqualTo(userInputDataJson);
  }

  @Test(expected = RuntimeException.class)
  public void getUserInputData_whenClassIsNotConform_shouldThrowRuntimeException() {

    // execute
    reader.getUserInputData(VirtualReader.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void getInitialSeContent_whenClassIsNull_shouldThrowIAE() {

    // execute
    reader.getInitialSeContent(null);
  }

  @Test
  public void getInitialSeContent_whenDataIsNull_shouldReturnNull() {

    // init
    reader = new ServerVirtualReader(virtualReaderMocked, serviceId, userInputDataJson, null);

    // execute
    AbstractMatchingSe result = reader.getInitialSeContent(AbstractMatchingSe.class);

    // verify
    assertThat(result).isNull();
  }

  @Test
  public void getInitialSeContent_whenDataIsNotNull_shouldReturnParsedData() {

    // init
    MyMatchingSe matchingSe = new MyMatchingSe(SampleFactory.getCompleteResponseList().get(0));

    String initialSeContentJson = KeypleJsonParser.getParser().toJson(matchingSe);

    reader =
        new ServerVirtualReader(
            virtualReaderMocked, serviceId, userInputDataJson, initialSeContentJson);

    // execute
    MyMatchingSe result = reader.getInitialSeContent(MyMatchingSe.class);

    // verify
    assertThat(result).isEqualToComparingFieldByField(matchingSe);
  }
}
