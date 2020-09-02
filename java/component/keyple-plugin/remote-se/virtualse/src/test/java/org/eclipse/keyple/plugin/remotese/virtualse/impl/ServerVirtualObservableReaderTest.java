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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
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
public class ServerVirtualObservableReaderTest {

  static final String serviceId = "serviceId";
  static final String userInputDataJson = "userInputDataJson";
  static final String initialSeContentJson = "initialSeContentJson";

  VirtualObservableReader virtualObservableReaderMocked;
  ServerVirtualObservableReader reader;

  private static class MyMatchingSe extends AbstractMatchingSe {

    MyMatchingSe(SeResponse selectionResponse, TransmissionMode transmissionMode) {
      super(selectionResponse, transmissionMode);
    }
  }

  @Before
  public void setUp() {
    virtualObservableReaderMocked = mock(VirtualObservableReader.class);
    reader =
        new ServerVirtualObservableReader(
            virtualObservableReaderMocked, serviceId, userInputDataJson, initialSeContentJson);
  }

  @Test
  public void transmitSeRequest_shouldDelegateMethodToVirtualReader() {

    // init request
    SeRequest seRequest = SampleFactory.getASeRequest();
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    SeResponse seResponse = SampleFactory.getCompleteResponseList().get(0);
    doReturn(seResponse)
        .when(virtualObservableReaderMocked)
        .transmitSeRequest(seRequest, channelControl);

    // execute
    SeResponse seResponseReturned = reader.transmitSeRequest(seRequest, channelControl);

    // verify
    verify(virtualObservableReaderMocked).transmitSeRequest(seRequest, channelControl);
    verifyNoMoreInteractions(virtualObservableReaderMocked);
    assertThat(seResponseReturned).isEqualToComparingFieldByField(seResponse);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void transmitSeRequest_whenError_shouldThrowOriginalException() {

    // init request
    SeRequest seRequest = SampleFactory.getASeRequest();
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualObservableReaderMocked)
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
        .when(virtualObservableReaderMocked)
        .transmitSeRequests(seRequests, multiSeRequestProcessing, channelControl);

    // execute
    List<SeResponse> seResponsesReturned =
        reader.transmitSeRequests(seRequests, multiSeRequestProcessing, channelControl);

    // verify
    verify(virtualObservableReaderMocked)
        .transmitSeRequests(seRequests, multiSeRequestProcessing, channelControl);
    verifyNoMoreInteractions(virtualObservableReaderMocked);
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
        .when(virtualObservableReaderMocked)
        .transmitSeRequests(seRequests, multiSeRequestProcessing, channelControl);

    // execute
    reader.transmitSeRequests(seRequests, multiSeRequestProcessing, channelControl);
  }

  @Test
  public void isSePresent_shouldDelegateMethodToVirtualReader() {

    // init
    doReturn(true).when(virtualObservableReaderMocked).isSePresent();

    // execute
    boolean result = reader.isSePresent();

    // verify
    verify(virtualObservableReaderMocked).isSePresent();
    verifyNoMoreInteractions(virtualObservableReaderMocked);
    assertThat(result).isTrue();
  }

  @Test(expected = KeypleReaderIOException.class)
  public void isSePresent_whenError_shouldThrowOriginalException() {

    // init
    doThrow(new KeypleReaderIOException("test")).when(virtualObservableReaderMocked).isSePresent();

    // execute
    reader.isSePresent();
  }

  @Test
  public void addSeProtocolSetting_shouldDelegateMethodToVirtualReader() {

    // init request
    SeProtocol seProtocol = SampleFactory.getSeProtocol();
    String protocolRule = "protocolRule";

    // init response
    doNothing().when(virtualObservableReaderMocked).addSeProtocolSetting(seProtocol, protocolRule);

    // execute
    reader.addSeProtocolSetting(seProtocol, protocolRule);

    // verify
    verify(virtualObservableReaderMocked).addSeProtocolSetting(seProtocol, protocolRule);
    verifyNoMoreInteractions(virtualObservableReaderMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void addSeProtocolSetting_whenError_shouldThrowOriginalException() {

    // init request
    SeProtocol seProtocol = SampleFactory.getSeProtocol();
    String protocolRule = "protocolRule";

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualObservableReaderMocked)
        .addSeProtocolSetting(seProtocol, protocolRule);

    // execute
    reader.addSeProtocolSetting(seProtocol, protocolRule);
  }

  @Test
  public void setSeProtocolSetting_shouldDelegateMethodToVirtualReader() {

    // init request
    Map<SeProtocol, String> seProtocolSetting = SampleFactory.getSeProtocolSetting();

    // init response
    doNothing().when(virtualObservableReaderMocked).setSeProtocolSetting(seProtocolSetting);

    // execute
    reader.setSeProtocolSetting(seProtocolSetting);

    // verify
    verify(virtualObservableReaderMocked).setSeProtocolSetting(seProtocolSetting);
    verifyNoMoreInteractions(virtualObservableReaderMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void setSeProtocolSetting_whenError_shouldThrowOriginalException() {

    // init request
    Map<SeProtocol, String> seProtocolSetting = SampleFactory.getSeProtocolSetting();

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualObservableReaderMocked)
        .setSeProtocolSetting(seProtocolSetting);

    // execute
    reader.setSeProtocolSetting(seProtocolSetting);
  }

  @Test
  public void getTransmissionMode_shouldDelegateMethodToVirtualReader() {

    // init
    doReturn(TransmissionMode.CONTACTS).when(virtualObservableReaderMocked).getTransmissionMode();

    // execute
    TransmissionMode result = reader.getTransmissionMode();

    // verify
    verify(virtualObservableReaderMocked).getTransmissionMode();
    verifyNoMoreInteractions(virtualObservableReaderMocked);
    assertThat(result).isEqualTo(TransmissionMode.CONTACTS);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void getTransmissionMode_whenError_shouldThrowOriginalException() {

    // init
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualObservableReaderMocked)
        .getTransmissionMode();

    // execute
    reader.getTransmissionMode();
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  public void getParameters_shouldDelegateMethodToVirtualReader() {

    // init response
    Map<String, String> parameters = new HashMap<String, String>();
    doReturn(parameters).when(virtualObservableReaderMocked).getParameters();

    // execute
    Map<String, String> result = reader.getParameters();

    // verify
    verify(virtualObservableReaderMocked).getParameters();
    verifyNoMoreInteractions(virtualObservableReaderMocked);
    assertThat(result).isSameAs(parameters);
  }

  @Test
  public void setParameter_shouldDelegateMethodToVirtualReader() {

    // init request
    String key = "key1";
    String value = "value1";

    // init response
    doNothing().when(virtualObservableReaderMocked).setParameter(key, value);

    // execute
    reader.setParameter(key, value);

    // verify
    verify(virtualObservableReaderMocked).setParameter(key, value);
    verifyNoMoreInteractions(virtualObservableReaderMocked);
  }

  @Test
  public void setParameters_shouldDelegateMethodToVirtualReader() {

    // init request
    Map<String, String> parameters = new HashMap<String, String>();

    // init response
    doNothing().when(virtualObservableReaderMocked).setParameters(parameters);

    // execute
    reader.setParameters(parameters);

    // verify
    verify(virtualObservableReaderMocked).setParameters(parameters);
    verifyNoMoreInteractions(virtualObservableReaderMocked);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  public void getName_shouldDelegateMethodToVirtualReader() {

    // init response
    String name = "name1";
    doReturn(name).when(virtualObservableReaderMocked).getName();

    // execute
    String result = reader.getName();

    // verify
    verify(virtualObservableReaderMocked).getName();
    verifyNoMoreInteractions(virtualObservableReaderMocked);
    assertThat(result).isSameAs(name);
  }

  @Test
  public void getServiceId_shouldReturnInitialValue() {

    // execute
    String result = reader.getServiceId();

    // verify
    verifyZeroInteractions(virtualObservableReaderMocked);
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
    reader =
        new ServerVirtualObservableReader(
            virtualObservableReaderMocked, serviceId, null, initialSeContentJson);

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
    reader =
        new ServerVirtualObservableReader(
            virtualObservableReaderMocked, serviceId, userInputDataJson, null);

    // execute
    AbstractMatchingSe result = reader.getInitialSeContent(AbstractMatchingSe.class);

    // verify
    assertThat(result).isNull();
  }

  @Test
  public void getInitialSeContent_whenDataIsNotNull_shouldReturnParsedData() {

    // init
    MyMatchingSe matchingSe =
        new MyMatchingSe(
            SampleFactory.getCompleteResponseList().get(0), TransmissionMode.CONTACTLESS);

    String initialSeContentJson = KeypleJsonParser.getParser().toJson(matchingSe);

    reader =
        new ServerVirtualObservableReader(
            virtualObservableReaderMocked, serviceId, userInputDataJson, initialSeContentJson);

    // execute
    MyMatchingSe result = reader.getInitialSeContent(MyMatchingSe.class);

    // verify
    assertThat(result).isEqualToComparingFieldByField(matchingSe);
  }

  @Test
  public void notifyObservers_shouldDelegateMethodToVirtualReader() {

    // init request
    ReaderEvent event = mock(ReaderEvent.class);

    // init response
    doNothing().when(virtualObservableReaderMocked).notifyObservers(event);

    // execute
    reader.notifyObservers(event);

    // verify
    verify(virtualObservableReaderMocked).notifyObservers(event);
    verifyNoMoreInteractions(virtualObservableReaderMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void notifyObservers_whenError_shouldThrowOriginalException() {

    // init request
    ReaderEvent event = mock(ReaderEvent.class);

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualObservableReaderMocked)
        .notifyObservers(event);

    // execute
    reader.notifyObservers(event);
  }

  @Test
  public void addObserver_shouldDelegateMethodToVirtualReader() {

    // init request
    ObservableReader.ReaderObserver observer = mock(ObservableReader.ReaderObserver.class);

    // init response
    doNothing().when(virtualObservableReaderMocked).addObserver(observer);

    // execute
    reader.addObserver(observer);

    // verify
    verify(virtualObservableReaderMocked).addObserver(observer);
    verifyNoMoreInteractions(virtualObservableReaderMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void addObserver_whenError_shouldThrowOriginalException() {

    // init request
    ObservableReader.ReaderObserver observer = mock(ObservableReader.ReaderObserver.class);

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualObservableReaderMocked)
        .addObserver(observer);

    // execute
    reader.addObserver(observer);
  }

  @Test
  public void removeObserver_shouldDelegateMethodToVirtualReader() {

    // init request
    ObservableReader.ReaderObserver observer = mock(ObservableReader.ReaderObserver.class);

    // init response
    doNothing().when(virtualObservableReaderMocked).removeObserver(observer);

    // execute
    reader.removeObserver(observer);

    // verify
    verify(virtualObservableReaderMocked).removeObserver(observer);
    verifyNoMoreInteractions(virtualObservableReaderMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void removeObserver_whenError_shouldThrowOriginalException() {

    // init request
    ObservableReader.ReaderObserver observer = mock(ObservableReader.ReaderObserver.class);

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualObservableReaderMocked)
        .removeObserver(observer);

    // execute
    reader.removeObserver(observer);
  }

  @Test
  public void clearObservers_shouldDelegateMethodToVirtualReader() {

    // init response
    doNothing().when(virtualObservableReaderMocked).clearObservers();

    // execute
    reader.clearObservers();

    // verify
    verify(virtualObservableReaderMocked).clearObservers();
    verifyNoMoreInteractions(virtualObservableReaderMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void clearObservers_whenError_shouldThrowOriginalException() {

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualObservableReaderMocked)
        .clearObservers();

    // execute
    reader.clearObservers();
  }

  @Test
  public void countObservers_shouldDelegateMethodToVirtualReader() {

    // init response
    int nbObservers = 2;
    doReturn(nbObservers).when(virtualObservableReaderMocked).countObservers();

    // execute
    int result = reader.countObservers();

    // verify
    verify(virtualObservableReaderMocked).countObservers();
    verifyNoMoreInteractions(virtualObservableReaderMocked);
    assertThat(result).isEqualTo(nbObservers);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test(expected = KeypleReaderIOException.class)
  public void countObservers_whenError_shouldThrowOriginalException() {

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualObservableReaderMocked)
        .countObservers();

    // execute
    reader.countObservers();
  }

  @Test
  public void startSeDetection_shouldDelegateMethodToVirtualReader() {

    // init request
    ObservableReader.PollingMode pollingMode = ObservableReader.PollingMode.REPEATING;

    // init response
    doNothing().when(virtualObservableReaderMocked).startSeDetection(pollingMode);

    // execute
    reader.startSeDetection(pollingMode);

    // verify
    verify(virtualObservableReaderMocked).startSeDetection(pollingMode);
    verifyNoMoreInteractions(virtualObservableReaderMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void startSeDetection_whenError_shouldThrowOriginalException() {

    // init request
    ObservableReader.PollingMode pollingMode = ObservableReader.PollingMode.REPEATING;

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualObservableReaderMocked)
        .startSeDetection(pollingMode);

    // execute
    reader.startSeDetection(pollingMode);
  }

  @Test
  public void stopSeDetection_shouldDelegateMethodToVirtualReader() {

    // init response
    doNothing().when(virtualObservableReaderMocked).stopSeDetection();

    // execute
    reader.stopSeDetection();

    // verify
    verify(virtualObservableReaderMocked).stopSeDetection();
    verifyNoMoreInteractions(virtualObservableReaderMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void stopSeDetection_whenError_shouldThrowOriginalException() {

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualObservableReaderMocked)
        .stopSeDetection();

    // execute
    reader.stopSeDetection();
  }

  @Test
  public void setDefaultSelectionRequest2P_shouldDelegateMethodToVirtualReader() {

    // init request
    AbstractDefaultSelectionsRequest defaultSelectionsRequest =
        mock(AbstractDefaultSelectionsRequest.class);
    ObservableReader.NotificationMode notificationMode = ObservableReader.NotificationMode.ALWAYS;

    // init response
    doNothing()
        .when(virtualObservableReaderMocked)
        .setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode);

    // execute
    reader.setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode);

    // verify
    verify(virtualObservableReaderMocked)
        .setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode);
    verifyNoMoreInteractions(virtualObservableReaderMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void setDefaultSelectionRequest2P_whenError_shouldThrowOriginalException() {

    // init request
    AbstractDefaultSelectionsRequest defaultSelectionsRequest =
        mock(AbstractDefaultSelectionsRequest.class);
    ObservableReader.NotificationMode notificationMode = ObservableReader.NotificationMode.ALWAYS;

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualObservableReaderMocked)
        .setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode);

    // execute
    reader.setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode);
  }

  @Test
  public void setDefaultSelectionRequest3P_shouldDelegateMethodToVirtualReader() {

    // init request
    AbstractDefaultSelectionsRequest defaultSelectionsRequest =
        mock(AbstractDefaultSelectionsRequest.class);
    ObservableReader.NotificationMode notificationMode = ObservableReader.NotificationMode.ALWAYS;
    ObservableReader.PollingMode pollingMode = ObservableReader.PollingMode.REPEATING;

    // init response
    doNothing()
        .when(virtualObservableReaderMocked)
        .setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode, pollingMode);

    // execute
    reader.setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode, pollingMode);

    // verify
    verify(virtualObservableReaderMocked)
        .setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode, pollingMode);
    verifyNoMoreInteractions(virtualObservableReaderMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void setDefaultSelectionRequest3P_whenError_shouldThrowOriginalException() {

    // init request
    AbstractDefaultSelectionsRequest defaultSelectionsRequest =
        mock(AbstractDefaultSelectionsRequest.class);
    ObservableReader.NotificationMode notificationMode = ObservableReader.NotificationMode.ALWAYS;
    ObservableReader.PollingMode pollingMode = ObservableReader.PollingMode.REPEATING;

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualObservableReaderMocked)
        .setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode, pollingMode);

    // execute
    reader.setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode, pollingMode);
  }

  @Test
  public void finalizeSeProcessing_shouldDelegateMethodToVirtualReader() {

    // init response
    doNothing().when(virtualObservableReaderMocked).finalizeSeProcessing();

    // execute
    reader.finalizeSeProcessing();

    // verify
    verify(virtualObservableReaderMocked).finalizeSeProcessing();
    verifyNoMoreInteractions(virtualObservableReaderMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void finalizeSeProcessing_whenError_shouldThrowOriginalException() {

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualObservableReaderMocked)
        .finalizeSeProcessing();

    // execute
    reader.finalizeSeProcessing();
  }
}
