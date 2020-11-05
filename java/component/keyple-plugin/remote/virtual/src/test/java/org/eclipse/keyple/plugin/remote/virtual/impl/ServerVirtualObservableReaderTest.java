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
package org.eclipse.keyple.plugin.remote.virtual.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;

import org.eclipse.keyple.core.card.message.*;
import org.eclipse.keyple.core.card.selection.AbstractSmartCard;
import org.eclipse.keyple.core.card.selection.MultiSelectionProcessing;
import org.eclipse.keyple.core.service.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServerVirtualObservableReaderTest extends RemoteServerBaseTest {

  static final String serviceId = "serviceId";
  static final String userInputDataJson = "userInputDataJson";
  static final String initialCardContentJson = "initialCardContentJson";

  VirtualObservableReader virtualObservableReaderMocked;
  ServerVirtualObservableReader reader;

  private static class MyMatchingSe extends AbstractSmartCard {

    MyMatchingSe(CardSelectionResponse selectionResponse) {
      super(selectionResponse);
    }
  }

  @Before
  public void setUp() {
    virtualObservableReaderMocked = mock(VirtualObservableReader.class);
    pluginObserver = new MockPluginObserver(true);
    registerSyncPlugin();
    reader =
        new ServerVirtualObservableReader(
            virtualObservableReaderMocked,
            serviceId,
            userInputDataJson,
            initialCardContentJson,
            null);
  }

  @After
  public void tearDown() {
    unregisterPlugin();
  }

  @Test
  public void transmitCardRequest_shouldDelegateMethodToVirtualReader() {

    // init request
    CardRequest seRequest = SampleFactory.getACardRequest();
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    CardResponse seResponse = SampleFactory.getACardResponse();
    doReturn(seResponse)
        .when(virtualObservableReaderMocked)
        .transmitCardRequest(seRequest, channelControl);

    // execute
    CardResponse seResponseReturned = reader.transmitCardRequest(seRequest, channelControl);

    // verify
    verify(virtualObservableReaderMocked).transmitCardRequest(seRequest, channelControl);
    verifyNoMoreInteractions(virtualObservableReaderMocked);
    assertThat(seResponseReturned).isEqualToComparingFieldByField(seResponse);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void transmitCardRequest_whenError_shouldThrowOriginalException() {

    // init request
    CardRequest seRequest = SampleFactory.getACardRequest();
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualObservableReaderMocked)
        .transmitCardRequest(seRequest, channelControl);

    // execute
    reader.transmitCardRequest(seRequest, channelControl);
  }

  @Test
  public void transmitCardSelectionRequests_shouldDelegateMethodToVirtualReader() {

    // init request
    List<CardSelectionRequest> seRequests = SampleFactory.getACardRequestList_ISO14443_4();
    MultiSelectionProcessing multiCardRequestProcessing = MultiSelectionProcessing.FIRST_MATCH;
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    List<CardSelectionResponse> seResponses = SampleFactory.getCompleteResponseList();
    doReturn(seResponses)
        .when(virtualObservableReaderMocked)
        .transmitCardSelectionRequests(seRequests, multiCardRequestProcessing, channelControl);

    // execute
    List<CardSelectionResponse> seResponsesReturned =
        reader.transmitCardSelectionRequests(seRequests, multiCardRequestProcessing, channelControl);

    // verify
    verify(virtualObservableReaderMocked)
        .transmitCardSelectionRequests(seRequests, multiCardRequestProcessing, channelControl);
    verifyNoMoreInteractions(virtualObservableReaderMocked);
    assertThat(seResponsesReturned).hasSameElementsAs(seResponses);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void transmitCardSelectionRequests_whenError_shouldThrowOriginalException() {

    // init request
    List<CardSelectionRequest> seRequests = SampleFactory.getACardRequestList_ISO14443_4();
    MultiSelectionProcessing multiCardRequestProcessing = MultiSelectionProcessing.FIRST_MATCH;
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualObservableReaderMocked)
        .transmitCardSelectionRequests(seRequests, multiCardRequestProcessing, channelControl);

    // execute
    reader.transmitCardSelectionRequests(seRequests, multiCardRequestProcessing, channelControl);
  }

  @Test
  public void isCardPresent_shouldDelegateMethodToVirtualReader() {

    // init
    doReturn(true).when(virtualObservableReaderMocked).isCardPresent();

    // execute
    boolean result = reader.isCardPresent();

    // verify
    verify(virtualObservableReaderMocked).isCardPresent();
    verifyNoMoreInteractions(virtualObservableReaderMocked);
    assertThat(result).isTrue();
  }

  @Test(expected = KeypleReaderIOException.class)
  public void isCardPresent_whenError_shouldThrowOriginalException() {

    // init
    doThrow(new KeypleReaderIOException("test")).when(virtualObservableReaderMocked).isCardPresent();

    // execute
    reader.isCardPresent();
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
            virtualObservableReaderMocked, serviceId, null, initialCardContentJson, null);

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
    reader.getInitialCardContent(null);
  }

  @Test
  public void getInitialSeContent_whenDataIsNull_shouldReturnNull() {

    // init
    reader =
        new ServerVirtualObservableReader(
            virtualObservableReaderMocked, serviceId, userInputDataJson, null, null);

    // execute
    AbstractSmartCard result = reader.getInitialCardContent(AbstractSmartCard.class);

    // verify
    assertThat(result).isNull();
  }

  @Test
  public void getInitialSeContent_whenDataIsNotNull_shouldReturnParsedData() {

    // init
    MyMatchingSe matchingSe = new MyMatchingSe(SampleFactory.getCompleteResponseList().get(0));

    String initialCardContentJson = KeypleJsonParser.getParser().toJson(matchingSe);

    reader =
        new ServerVirtualObservableReader(
            virtualObservableReaderMocked,
            serviceId,
            userInputDataJson,
            initialCardContentJson,
            null);

    // execute
    MyMatchingSe result = reader.getInitialCardContent(MyMatchingSe.class);

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
  public void startCardDetection_shouldDelegateMethodToVirtualReader() {

    // init request
    ObservableReader.PollingMode pollingMode = ObservableReader.PollingMode.REPEATING;

    // init response
    doNothing().when(virtualObservableReaderMocked).startCardDetection(pollingMode);

    // execute
    reader.startCardDetection(pollingMode);

    // verify
    verify(virtualObservableReaderMocked).startCardDetection(pollingMode);
    verifyNoMoreInteractions(virtualObservableReaderMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void startCardDetection_whenError_shouldThrowOriginalException() {

    // init request
    ObservableReader.PollingMode pollingMode = ObservableReader.PollingMode.REPEATING;

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualObservableReaderMocked)
        .startCardDetection(pollingMode);

    // execute
    reader.startCardDetection(pollingMode);
  }

  @Test
  public void stopCardDetection_shouldDelegateMethodToVirtualReader() {

    // init response
    doNothing().when(virtualObservableReaderMocked).stopCardDetection();

    // execute
    reader.stopCardDetection();

    // verify
    verify(virtualObservableReaderMocked).stopCardDetection();
    verifyNoMoreInteractions(virtualObservableReaderMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void stopCardDetection_whenError_shouldThrowOriginalException() {

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualObservableReaderMocked)
        .stopCardDetection();

    // execute
    reader.stopCardDetection();
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
  public void finalizeCardProcessing_shouldDelegateMethodToVirtualReader() {

    // init response
    doNothing().when(virtualObservableReaderMocked).finalizeCardProcessing();

    // execute
    reader.finalizeCardProcessing();

    // verify
    verify(virtualObservableReaderMocked).finalizeCardProcessing();
    verifyNoMoreInteractions(virtualObservableReaderMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void finalizeCardProcessing_whenError_shouldThrowOriginalException() {

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(virtualObservableReaderMocked)
        .finalizeCardProcessing();

    // execute
    reader.finalizeCardProcessing();
  }
}
