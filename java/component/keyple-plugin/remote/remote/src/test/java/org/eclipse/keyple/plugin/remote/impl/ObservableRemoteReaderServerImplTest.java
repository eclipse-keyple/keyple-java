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
package org.eclipse.keyple.plugin.remote.impl;

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
import org.eclipse.keyple.core.util.json.KeypleGsonParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ObservableRemoteReaderServerImplTest extends RemoteServerBaseTest {

  static final String serviceId = "serviceId";
  static final String userInputDataJson = "userInputDataJson";
  static final String initialCardContentJson = "initialCardContentJson";

  ObservableRemoteReaderImpl observableRemoteReaderImplMocked;
  ObservableRemoteReaderServerImpl reader;

  private static class MyMatchingCard extends AbstractSmartCard {

    MyMatchingCard(CardSelectionResponse selectionResponse) {
      super(selectionResponse);
    }
  }

  @Before
  public void setUp() {
    observableRemoteReaderImplMocked = mock(ObservableRemoteReaderImpl.class);
    pluginObserver = new MockPluginObserver(true);
    registerSyncPlugin();
    reader =
        new ObservableRemoteReaderServerImpl(
            observableRemoteReaderImplMocked,
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
  public void transmitCardRequest_shouldDelegateMethodToRemoteReader() {

    // init request
    CardRequest cardRequest = SampleFactory.getACardRequest();
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    CardResponse cardResponse = SampleFactory.getACardResponse();
    doReturn(cardResponse)
        .when(observableRemoteReaderImplMocked)
        .transmitCardRequest(cardRequest, channelControl);

    // execute
    CardResponse cardResponseReturned = reader.transmitCardRequest(cardRequest, channelControl);

    // verify
    verify(observableRemoteReaderImplMocked).transmitCardRequest(cardRequest, channelControl);
    verifyNoMoreInteractions(observableRemoteReaderImplMocked);
    assertThat(cardResponseReturned).isEqualToComparingFieldByField(cardResponse);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void transmitCardRequest_whenError_shouldThrowOriginalException() {

    // init request
    CardRequest cardRequest = SampleFactory.getACardRequest();
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(observableRemoteReaderImplMocked)
        .transmitCardRequest(cardRequest, channelControl);

    // execute
    reader.transmitCardRequest(cardRequest, channelControl);
  }

  @Test
  public void transmitCardSelectionRequests_shouldDelegateMethodToRemoteReader() {

    // init request
    List<CardSelectionRequest> cardSelectionRequests =
        SampleFactory.getACardRequestList_ISO14443_4();
    MultiSelectionProcessing multiCardRequestProcessing = MultiSelectionProcessing.FIRST_MATCH;
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    List<CardSelectionResponse> cardResponses = SampleFactory.getCompleteResponseList();
    doReturn(cardResponses)
        .when(observableRemoteReaderImplMocked)
        .transmitCardSelectionRequests(
            cardSelectionRequests, multiCardRequestProcessing, channelControl);

    // execute
    List<CardSelectionResponse> cardResponsesReturned =
        reader.transmitCardSelectionRequests(
            cardSelectionRequests, multiCardRequestProcessing, channelControl);

    // verify
    verify(observableRemoteReaderImplMocked)
        .transmitCardSelectionRequests(
            cardSelectionRequests, multiCardRequestProcessing, channelControl);
    verifyNoMoreInteractions(observableRemoteReaderImplMocked);
    assertThat(cardResponsesReturned).hasSameElementsAs(cardResponses);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void transmitCardSelectionRequests_whenError_shouldThrowOriginalException() {

    // init request
    List<CardSelectionRequest> cardSelectionRequests =
        SampleFactory.getACardRequestList_ISO14443_4();
    MultiSelectionProcessing multiCardRequestProcessing = MultiSelectionProcessing.FIRST_MATCH;
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(observableRemoteReaderImplMocked)
        .transmitCardSelectionRequests(
            cardSelectionRequests, multiCardRequestProcessing, channelControl);

    // execute
    reader.transmitCardSelectionRequests(
        cardSelectionRequests, multiCardRequestProcessing, channelControl);
  }

  @Test
  public void isCardPresent_shouldDelegateMethodToRemoteReader() {

    // init
    doReturn(true).when(observableRemoteReaderImplMocked).isCardPresent();

    // execute
    boolean result = reader.isCardPresent();

    // verify
    verify(observableRemoteReaderImplMocked).isCardPresent();
    verifyNoMoreInteractions(observableRemoteReaderImplMocked);
    assertThat(result).isTrue();
  }

  @Test(expected = KeypleReaderIOException.class)
  public void isCardPresent_whenError_shouldThrowOriginalException() {

    // init
    doThrow(new KeypleReaderIOException("test"))
        .when(observableRemoteReaderImplMocked)
        .isCardPresent();

    // execute
    reader.isCardPresent();
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  public void getName_shouldDelegateMethodToRemoteReader() {

    // init response
    String name = "name1";
    doReturn(name).when(observableRemoteReaderImplMocked).getName();

    // execute
    String result = reader.getName();

    // verify
    verify(observableRemoteReaderImplMocked).getName();
    verifyNoMoreInteractions(observableRemoteReaderImplMocked);
    assertThat(result).isSameAs(name);
  }

  @Test
  public void getServiceId_shouldReturnInitialValue() {

    // execute
    String result = reader.getServiceId();

    // verify
    verifyZeroInteractions(observableRemoteReaderImplMocked);
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
        new ObservableRemoteReaderServerImpl(
            observableRemoteReaderImplMocked, serviceId, null, initialCardContentJson, null);

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
    reader.getUserInputData(RemoteReaderImpl.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void getInitialCardContent_whenClassIsNull_shouldThrowIAE() {

    // execute
    reader.getInitialCardContent(null);
  }

  @Test
  public void getInitialCardContent_whenDataIsNull_shouldReturnNull() {

    // init
    reader =
        new ObservableRemoteReaderServerImpl(
            observableRemoteReaderImplMocked, serviceId, userInputDataJson, null, null);

    // execute
    AbstractSmartCard result = reader.getInitialCardContent(AbstractSmartCard.class);

    // verify
    assertThat(result).isNull();
  }

  @Test
  public void getInitialCardContent_whenDataIsNotNull_shouldReturnParsedData() {

    // init
    MyMatchingCard matchingCard =
        new MyMatchingCard(SampleFactory.getCompleteResponseList().get(0));

    String initialCardContentJson = KeypleGsonParser.getParser().toJson(matchingCard);

    reader =
        new ObservableRemoteReaderServerImpl(
            observableRemoteReaderImplMocked,
            serviceId,
            userInputDataJson,
            initialCardContentJson,
            null);

    // execute
    MyMatchingCard result = reader.getInitialCardContent(MyMatchingCard.class);

    // verify
    assertThat(result).isEqualToComparingFieldByField(matchingCard);
  }

  @Test
  public void notifyObservers_shouldDelegateMethodToRemoteReader() {

    // init request
    ReaderEvent event = mock(ReaderEvent.class);

    // init response
    doNothing().when(observableRemoteReaderImplMocked).notifyObservers(event);

    // execute
    reader.notifyObservers(event);

    // verify
    verify(observableRemoteReaderImplMocked).notifyObservers(event);
    verifyNoMoreInteractions(observableRemoteReaderImplMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void notifyObservers_whenError_shouldThrowOriginalException() {

    // init request
    ReaderEvent event = mock(ReaderEvent.class);

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(observableRemoteReaderImplMocked)
        .notifyObservers(event);

    // execute
    reader.notifyObservers(event);
  }

  @Test
  public void addObserver_shouldDelegateMethodToRemoteReader() {

    // init request
    ObservableReader.ReaderObserver observer = mock(ObservableReader.ReaderObserver.class);

    // init response
    doNothing().when(observableRemoteReaderImplMocked).addObserver(observer);

    // execute
    reader.addObserver(observer);

    // verify
    verify(observableRemoteReaderImplMocked).addObserver(observer);
    verifyNoMoreInteractions(observableRemoteReaderImplMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void addObserver_whenError_shouldThrowOriginalException() {

    // init request
    ObservableReader.ReaderObserver observer = mock(ObservableReader.ReaderObserver.class);

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(observableRemoteReaderImplMocked)
        .addObserver(observer);

    // execute
    reader.addObserver(observer);
  }

  @Test
  public void removeObserver_shouldDelegateMethodToRemoteReader() {

    // init request
    ObservableReader.ReaderObserver observer = mock(ObservableReader.ReaderObserver.class);

    // init response
    doNothing().when(observableRemoteReaderImplMocked).removeObserver(observer);

    // execute
    reader.removeObserver(observer);

    // verify
    verify(observableRemoteReaderImplMocked).removeObserver(observer);
    verifyNoMoreInteractions(observableRemoteReaderImplMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void removeObserver_whenError_shouldThrowOriginalException() {
    // init request
    ObservableReader.ReaderObserver observer = mock(ObservableReader.ReaderObserver.class);

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(observableRemoteReaderImplMocked)
        .removeObserver(observer);

    // execute
    reader.removeObserver(observer);
  }

  @Test
  public void clearObservers_shouldDelegateMethodToRemoteReader() {

    // init response
    doNothing().when(observableRemoteReaderImplMocked).clearObservers();

    // execute
    reader.clearObservers();

    // verify
    verify(observableRemoteReaderImplMocked).clearObservers();
    verifyNoMoreInteractions(observableRemoteReaderImplMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void clearObservers_whenError_shouldThrowOriginalException() {

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(observableRemoteReaderImplMocked)
        .clearObservers();

    // execute
    reader.clearObservers();
  }

  @Test
  public void countObservers_shouldDelegateMethodToRemoteReader() {

    // init response
    int nbObservers = 2;
    doReturn(nbObservers).when(observableRemoteReaderImplMocked).countObservers();

    // execute
    int result = reader.countObservers();

    // verify
    verify(observableRemoteReaderImplMocked).countObservers();
    verifyNoMoreInteractions(observableRemoteReaderImplMocked);
    assertThat(result).isEqualTo(nbObservers);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void countObservers_whenError_shouldThrowOriginalException() {

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(observableRemoteReaderImplMocked)
        .countObservers();

    // execute
    reader.countObservers();
  }

  @Test
  public void startCardDetection_shouldDelegateMethodToRemoteReader() {

    // init request
    ObservableReader.PollingMode pollingMode = ObservableReader.PollingMode.REPEATING;

    // init response
    doNothing().when(observableRemoteReaderImplMocked).startCardDetection(pollingMode);

    // execute
    reader.startCardDetection(pollingMode);

    // verify
    verify(observableRemoteReaderImplMocked).startCardDetection(pollingMode);
    verifyNoMoreInteractions(observableRemoteReaderImplMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void startCardDetection_whenError_shouldThrowOriginalException() {

    // init request
    ObservableReader.PollingMode pollingMode = ObservableReader.PollingMode.REPEATING;

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(observableRemoteReaderImplMocked)
        .startCardDetection(pollingMode);

    // execute
    reader.startCardDetection(pollingMode);
  }

  @Test
  public void stopCardDetection_shouldDelegateMethodToRemoteReader() {

    // init response
    doNothing().when(observableRemoteReaderImplMocked).stopCardDetection();

    // execute
    reader.stopCardDetection();

    // verify
    verify(observableRemoteReaderImplMocked).stopCardDetection();
    verifyNoMoreInteractions(observableRemoteReaderImplMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void stopCardDetection_whenError_shouldThrowOriginalException() {

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(observableRemoteReaderImplMocked)
        .stopCardDetection();

    // execute
    reader.stopCardDetection();
  }

  @Test
  public void setDefaultSelectionRequest2P_shouldDelegateMethodToRemoteReader() {

    // init request
    AbstractDefaultSelectionsRequest defaultSelectionsRequest =
        mock(AbstractDefaultSelectionsRequest.class);
    ObservableReader.NotificationMode notificationMode = ObservableReader.NotificationMode.ALWAYS;

    // init response
    doNothing()
        .when(observableRemoteReaderImplMocked)
        .setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode);

    // execute
    reader.setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode);

    // verify
    verify(observableRemoteReaderImplMocked)
        .setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode);
    verifyNoMoreInteractions(observableRemoteReaderImplMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void setDefaultSelectionRequest2P_whenError_shouldThrowOriginalException() {

    // init request
    AbstractDefaultSelectionsRequest defaultSelectionsRequest =
        mock(AbstractDefaultSelectionsRequest.class);
    ObservableReader.NotificationMode notificationMode = ObservableReader.NotificationMode.ALWAYS;

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(observableRemoteReaderImplMocked)
        .setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode);

    // execute
    reader.setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode);
  }

  @Test
  public void setDefaultSelectionRequest3P_shouldDelegateMethodToRemoteReader() {

    // init request
    AbstractDefaultSelectionsRequest defaultSelectionsRequest =
        mock(AbstractDefaultSelectionsRequest.class);
    ObservableReader.NotificationMode notificationMode = ObservableReader.NotificationMode.ALWAYS;
    ObservableReader.PollingMode pollingMode = ObservableReader.PollingMode.REPEATING;

    // init response
    doNothing()
        .when(observableRemoteReaderImplMocked)
        .setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode, pollingMode);

    // execute
    reader.setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode, pollingMode);

    // verify
    verify(observableRemoteReaderImplMocked)
        .setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode, pollingMode);
    verifyNoMoreInteractions(observableRemoteReaderImplMocked);
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
        .when(observableRemoteReaderImplMocked)
        .setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode, pollingMode);

    // execute
    reader.setDefaultSelectionRequest(defaultSelectionsRequest, notificationMode, pollingMode);
  }

  @Test
  public void finalizeCardProcessing_shouldDelegateMethodToRemoteReader() {

    // init response
    doNothing().when(observableRemoteReaderImplMocked).finalizeCardProcessing();

    // execute
    reader.finalizeCardProcessing();

    // verify
    verify(observableRemoteReaderImplMocked).finalizeCardProcessing();
    verifyNoMoreInteractions(observableRemoteReaderImplMocked);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void finalizeCardProcessing_whenError_shouldThrowOriginalException() {

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(observableRemoteReaderImplMocked)
        .finalizeCardProcessing();

    // execute
    reader.finalizeCardProcessing();
  }
}
