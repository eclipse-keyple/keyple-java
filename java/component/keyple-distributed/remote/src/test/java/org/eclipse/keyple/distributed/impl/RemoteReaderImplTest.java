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
package org.eclipse.keyple.distributed.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.google.gson.reflect.TypeToken;
import java.util.*;
import org.eclipse.keyple.core.card.message.*;
import org.eclipse.keyple.core.card.selection.MultiSelectionProcessing;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.util.json.BodyError;
import org.eclipse.keyple.core.util.json.KeypleGsonParser;
import org.eclipse.keyple.distributed.MessageDto;
import org.eclipse.keyple.distributed.NodeCommunicationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RemoteReaderImplTest {

  static final String pluginName = "pluginName";
  static final String localReaderName = "localReaderName";

  RemoteReaderImpl reader;
  AbstractNode node;

  @Before
  public void setUp() {
    node = mock(AbstractNode.class);
    reader = new RemoteReaderImpl(pluginName, localReaderName, node, "val1", null);
  }

  @Test
  public void constructor_shouldGenerateName() {
    assertThat(reader.getName()).isNotEmpty();
  }

  @Test
  public void processCardRequest_whenOk_shouldCallTheHandlerAndReturnResponses() {

    // init request
    CardRequest cardRequest = SampleFactory.getACardRequest();
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    CardResponse cardResponse = SampleFactory.getACardResponse();

    MessageDto responseDto =
        new MessageDto() //
            .setAction(MessageDto.Action.TRANSMIT.name()) //
            .setRemoteReaderName(reader.getName()) //
            .setLocalReaderName(reader.getLocalReaderName()) //
            .setBody(KeypleGsonParser.getParser().toJson(cardResponse, CardResponse.class));

    doReturn(responseDto).when(node).sendRequest(any(MessageDto.class));

    // execute
    CardResponse cardResponseReturned = reader.processCardRequest(cardRequest, channelControl);

    // verify
    assertThat(cardResponseReturned).isEqualToComparingFieldByField(cardResponse);
  }

  @Test(expected = NodeCommunicationException.class)
  public void processCardRequest_whenNodeTimeout_shouldThrowNCE() {

    // init response
    mockTimeout();

    // init request
    CardRequest cardRequest = SampleFactory.getACardRequest();
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // execute
    reader.processCardRequest(cardRequest, channelControl);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void processCardRequest_whenError_shouldThrowOriginalException() {

    // init response
    mockError();

    // init request
    CardRequest cardRequest = SampleFactory.getACardRequest();
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // execute
    reader.processCardRequest(cardRequest, channelControl);
  }

  @Test
  public void processCardRequests_whenOk_shouldCallTheHandlerAndReturnResponses() {

    // init request
    List<CardSelectionRequest> cardSelectionRequests =
        SampleFactory.getACardRequestList_ISO14443_4();
    MultiSelectionProcessing multiCardRequestProcessing = MultiSelectionProcessing.FIRST_MATCH;
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // init response
    List<CardSelectionResponse> cardResponses = SampleFactory.getCompleteResponseList();

    MessageDto responseDto =
        new MessageDto() //
            .setAction(MessageDto.Action.TRANSMIT_CARD_SELECTION.name()) //
            .setRemoteReaderName(reader.getName()) //
            .setLocalReaderName(reader.getLocalReaderName()) //
            .setBody(
                KeypleGsonParser.getParser()
                    .toJson(cardResponses, new TypeToken<ArrayList<CardResponse>>() {}.getType()));

    doReturn(responseDto).when(node).sendRequest(any(MessageDto.class));

    // execute
    List<CardSelectionResponse> cardResponsesReturned =
        reader.processCardSelectionRequests(
            cardSelectionRequests, multiCardRequestProcessing, channelControl);

    // verify
    assertThat(cardResponsesReturned).hasSameElementsAs(cardResponses);
  }

  @Test(expected = NodeCommunicationException.class)
  public void processCardRequests_whenNodeTimeout_shouldThrowNCE() {

    // init response
    mockTimeout();

    // init request
    List<CardSelectionRequest> cardSelectionRequests =
        SampleFactory.getACardRequestList_ISO14443_4();
    MultiSelectionProcessing multiCardRequestProcessing = MultiSelectionProcessing.FIRST_MATCH;
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // execute
    reader.processCardSelectionRequests(
        cardSelectionRequests, multiCardRequestProcessing, channelControl);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void processCardRequests_whenError_shouldThrowOriginalException() {

    // init response
    mockError();

    // init request
    List<CardSelectionRequest> cardSelectionRequests =
        SampleFactory.getACardRequestList_ISO14443_4();
    MultiSelectionProcessing multiCardRequestProcessing = MultiSelectionProcessing.FIRST_MATCH;
    ChannelControl channelControl = ChannelControl.KEEP_OPEN;

    // execute
    reader.processCardSelectionRequests(
        cardSelectionRequests, multiCardRequestProcessing, channelControl);
  }

  @Test
  public void isSePresent_whenOk_shouldCallTheHandlerAndReturnResponses() {

    // init
    MessageDto responseDto =
        new MessageDto() //
            .setAction(MessageDto.Action.IS_CARD_PRESENT.name()) //
            .setRemoteReaderName(reader.getName()) //
            .setLocalReaderName(reader.getLocalReaderName()) //
            .setBody(KeypleGsonParser.getParser().toJson(true, Boolean.class));

    doReturn(responseDto).when(node).sendRequest(any(MessageDto.class));

    // execute
    boolean result = reader.isCardPresent();

    // verify
    assertThat(result).isTrue();
  }

  @Test(expected = NodeCommunicationException.class)
  public void isSePresent_whenNodeTimeout_shouldThrowNCE() {
    // init
    mockTimeout();
    // execute
    reader.isCardPresent();
  }

  @Test(expected = KeypleReaderIOException.class)
  public void isSePresent_whenError_shouldThrowOriginalException() {
    // init
    mockError();
    // execute
    reader.isCardPresent();
  }

  @Test
  public void isReaderContactless_whenOk_shouldCallTheHandlerAndReturnResponses() {

    // init
    MessageDto responseDto =
        new MessageDto() //
            .setAction(MessageDto.Action.IS_READER_CONTACTLESS.name()) //
            .setRemoteReaderName(reader.getName()) //
            .setLocalReaderName(reader.getLocalReaderName()) //
            .setBody(KeypleGsonParser.getParser().toJson(true, Boolean.class));

    doReturn(responseDto).when(node).sendRequest(any(MessageDto.class));

    // execute
    boolean result = reader.isContactless();

    // verify
    assertThat(result).isTrue();
  }

  @Test(expected = NodeCommunicationException.class)
  public void isReaderContactless_whenNodeTimeout_shouldThrowNCE() {
    // init
    mockTimeout();
    // execute
    reader.isContactless();
  }

  @Test(expected = KeypleReaderIOException.class)
  public void isReaderContactless_whenError_shouldThrowOriginalException() {
    // init
    mockError();
    // execute
    reader.isContactless();
  }

  @Test
  public void releaseChannel_whenOk_shouldCallTheNodeAndReturnNoResponses() {

    // init response
    MessageDto responseDto =
        new MessageDto() //
            .setAction(MessageDto.Action.RELEASE_CHANNEL.name()) //
            .setRemoteReaderName(reader.getName()) //
            .setLocalReaderName(reader.getLocalReaderName());

    doReturn(responseDto).when(node).sendRequest(any(MessageDto.class));

    // execute
    reader.releaseChannel();
    verify(node).sendRequest(any(MessageDto.class));
    verifyNoMoreInteractions(node);
  }

  @Test(expected = NodeCommunicationException.class)
  public void releaseChannel_whenNodeTimeout_shouldThrowNCE() {
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

  @Test(expected = UnsupportedOperationException.class)
  public void activateProtocol__shouldThrowUOE() {
    reader.activateProtocol("any", "any");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void deactivateProtocol__shouldThrowUOE() {
    reader.deactivateProtocol("any");
  }

  private void mockTimeout() {
    doThrow(new NodeCommunicationException("test")).when(node).sendRequest(any(MessageDto.class));
  }

  private void mockError() {

    KeypleReaderIOException error = SampleFactory.getASimpleKeypleException();

    MessageDto responseDto =
        new MessageDto() //
            .setAction(MessageDto.Action.ERROR.name()) //
            .setRemoteReaderName(reader.getName()) //
            .setLocalReaderName(reader.getLocalReaderName()) //
            .setBody(KeypleGsonParser.getParser().toJson(new BodyError(error)));

    doReturn(responseDto).when(node).sendRequest(any(MessageDto.class));
  }
}
