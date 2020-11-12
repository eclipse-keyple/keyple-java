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
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RemoteReaderServerImplTest {

  static final String serviceId = "serviceId";
  static final String userInputDataJson = "userInputDataJson";
  static final String initialCardContentJson = "initialCardContentJson";

  RemoteReaderImpl remoteReaderImplMocked;
  RemoteReaderServerImpl reader;

  private static class MyMatchingCard extends AbstractSmartCard {

    MyMatchingCard(CardSelectionResponse selectionResponse) {
      super(selectionResponse);
    }
  }

  @Before
  public void setUp() {
    remoteReaderImplMocked = mock(RemoteReaderImpl.class);
    reader =
        new RemoteReaderServerImpl(
            remoteReaderImplMocked, serviceId, userInputDataJson, initialCardContentJson);
  }

  @Test
  public void transmitCardRequest_shouldDelegateMethodToRemoteReader() {

    // init request
    CardRequest cardRequest = SampleFactory.getACardRequest();
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    CardResponse cardResponse = SampleFactory.getACardResponse();
    doReturn(cardResponse)
        .when(remoteReaderImplMocked)
        .transmitCardRequest(cardRequest, channelControl);

    // execute
    CardResponse cardResponseReturned = reader.transmitCardRequest(cardRequest, channelControl);

    // verify
    verify(remoteReaderImplMocked).transmitCardRequest(cardRequest, channelControl);
    verifyNoMoreInteractions(remoteReaderImplMocked);
    assertThat(cardResponseReturned).isEqualToComparingFieldByField(cardResponse);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void transmitCardRequest_whenError_shouldThrowOriginalException() {

    // init request
    CardRequest cardRequest = SampleFactory.getACardRequest();
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(remoteReaderImplMocked)
        .transmitCardRequest(cardRequest, channelControl);

    // execute
    reader.transmitCardRequest(cardRequest, channelControl);
  }

  @Test
  public void transmitCardRequests_shouldDelegateMethodToRemoteReader() {

    // init request
    List<CardSelectionRequest> cardSelectionRequests =
        SampleFactory.getACardRequestList_ISO14443_4();
    MultiSelectionProcessing multiCardRequestProcessing = MultiSelectionProcessing.FIRST_MATCH;
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    List<CardSelectionResponse> cardResponses = SampleFactory.getCompleteResponseList();
    doReturn(cardResponses)
        .when(remoteReaderImplMocked)
        .transmitCardSelectionRequests(
            cardSelectionRequests, multiCardRequestProcessing, channelControl);

    // execute
    List<CardSelectionResponse> cardResponsesReturned =
        reader.transmitCardSelectionRequests(
            cardSelectionRequests, multiCardRequestProcessing, channelControl);

    // verify
    verify(remoteReaderImplMocked)
        .transmitCardSelectionRequests(
            cardSelectionRequests, multiCardRequestProcessing, channelControl);
    verifyNoMoreInteractions(remoteReaderImplMocked);
    assertThat(cardResponsesReturned).hasSameElementsAs(cardResponses);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void transmitCardRequests_whenError_shouldThrowOriginalException() {

    // init request
    List<CardSelectionRequest> cardSelectionRequests =
        SampleFactory.getACardRequestList_ISO14443_4();
    MultiSelectionProcessing multiCardRequestProcessing = MultiSelectionProcessing.FIRST_MATCH;
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    doThrow(new KeypleReaderIOException("test"))
        .when(remoteReaderImplMocked)
        .transmitCardSelectionRequests(
            cardSelectionRequests, multiCardRequestProcessing, channelControl);

    // execute
    reader.transmitCardSelectionRequests(
        cardSelectionRequests, multiCardRequestProcessing, channelControl);
  }

  @Test
  public void isCardPresent_shouldDelegateMethodToRemoteReader() {

    // init
    doReturn(true).when(remoteReaderImplMocked).isCardPresent();

    // execute
    boolean result = reader.isCardPresent();

    // verify
    verify(remoteReaderImplMocked).isCardPresent();
    verifyNoMoreInteractions(remoteReaderImplMocked);
    assertThat(result).isTrue();
  }

  @Test(expected = KeypleReaderIOException.class)
  public void isCardPresent_whenError_shouldThrowOriginalException() {

    // init
    doThrow(new KeypleReaderIOException("test")).when(remoteReaderImplMocked).isCardPresent();

    // execute
    reader.isCardPresent();
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  public void getName_shouldDelegateMethodToRemoteReader() {

    // init response
    String name = "name1";
    doReturn(name).when(remoteReaderImplMocked).getName();

    // execute
    String result = reader.getName();

    // verify
    verify(remoteReaderImplMocked).getName();
    verifyNoMoreInteractions(remoteReaderImplMocked);
    assertThat(result).isSameAs(name);
  }

  @Test
  public void isContactless_shouldDelegateMethodToRemoteReader() {

    // init
    doReturn(true).when(remoteReaderImplMocked).isContactless();

    // execute
    boolean result = reader.isContactless();

    // verify
    verify(remoteReaderImplMocked).isContactless();
    verifyNoMoreInteractions(remoteReaderImplMocked);
    assertThat(result).isTrue();
  }

  @Test(expected = KeypleReaderIOException.class)
  public void isContactless_whenError_shouldThrowOriginalException() {

    // init
    doThrow(new KeypleReaderIOException("test")).when(remoteReaderImplMocked).isContactless();

    // execute
    reader.isContactless();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void activateProtocol__shouldThrowUOE() {
    reader.activateProtocol("any", "any");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void deactivateProtocol__shouldThrowUOE() {
    reader.deactivateProtocol("any");
  }

  @Test
  public void getServiceId_shouldReturnInitialValue() {

    // execute
    String result = reader.getServiceId();

    // verify
    verifyZeroInteractions(remoteReaderImplMocked);
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
        new RemoteReaderServerImpl(remoteReaderImplMocked, serviceId, null, initialCardContentJson);

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
  public void getinitialCardContent_whenClassIsNull_shouldThrowIAE() {

    // execute
    reader.getInitialCardContent(null);
  }

  @Test
  public void getInitialSeContent_whenDataIsNull_shouldReturnNull() {

    // init
    reader = new RemoteReaderServerImpl(remoteReaderImplMocked, serviceId, userInputDataJson, null);

    // execute
    AbstractSmartCard result = reader.getInitialCardContent(AbstractSmartCard.class);

    // verify
    assertThat(result).isNull();
  }

  @Test
  public void getInitialSeContent_whenDataIsNotNull_shouldReturnParsedData() {

    // init
    MyMatchingCard matchingSe = new MyMatchingCard(SampleFactory.getCompleteResponseList().get(0));

    String initialCardContentJson = KeypleJsonParser.getParser().toJson(matchingSe);

    reader =
        new RemoteReaderServerImpl(
            remoteReaderImplMocked, serviceId, userInputDataJson, initialCardContentJson);

    // execute
    MyMatchingCard result = reader.getInitialCardContent(MyMatchingCard.class);

    // verify
    assertThat(result).isEqualToComparingFieldByField(matchingSe);
  }
}
