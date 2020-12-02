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

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.NodeCommunicationException;
import org.eclipse.keyple.plugin.remote.spi.SyncEndpointClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SyncNodeClientTest extends AbstractSyncNodeTest {

  KeypleMessageHandlerErrorMock handlerError;
  SyncEndpointClientPollingMock endpoint;
  SyncEndpointClientLongPollingMock endpointLongPolling;
  SyncEndpointClientErrorMock endpointError;

  List<MessageDto> responses;

  ServerPushEventStrategy pollingEventStrategy;
  ServerPushEventStrategy longPollingEventStrategy;

  {
    responses = new ArrayList<MessageDto>();
    responses.add(response);

    pollingEventStrategy =
        new ServerPushEventStrategy(ServerPushEventStrategy.Type.POLLING).setDuration(1);

    longPollingEventStrategy =
        new ServerPushEventStrategy(ServerPushEventStrategy.Type.LONG_POLLING).setDuration(1);
  }

  class KeypleMessageHandlerErrorMock extends AbstractMessageHandler {

    boolean isError = false;

    @Override
    void onMessage(MessageDto msg) {
      isError = true;
      throw new NodeCommunicationException("Handler error mocked");
    }
  }

  class SyncEndpointClientPollingMock implements SyncEndpointClient {

    List<MessageDto> messages = new ArrayList<MessageDto>();

    @Override
    public List<MessageDto> sendRequest(MessageDto msg) {
      messages.add(msg);
      return responses;
    }
  }

  class SyncEndpointClientLongPollingMock implements SyncEndpointClient {

    List<MessageDto> messages = new ArrayList<MessageDto>();

    @Override
    public List<MessageDto> sendRequest(MessageDto msg) {
      messages.add(msg);
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return responses;
    }
  }

  class SyncEndpointClientErrorMock implements SyncEndpointClient {

    List<MessageDto> messages = new ArrayList<MessageDto>();
    int cpt = 0;

    @Override
    public List<MessageDto> sendRequest(MessageDto msg) {
      cpt++;
      if (cpt >= 2 && cpt <= 3) {
        throw new NodeCommunicationException("Endpoint error mocked");
      }
      messages.add(msg);
      return responses;
    }
  }

  Callable<Boolean> handlerErrorOccurred() {
    return new Callable<Boolean>() {
      public Boolean call() {
        return handlerError.isError;
      }
    };
  }

  Callable<Boolean> endpointMessagesHasMinSize(final int size) {
    return new Callable<Boolean>() {
      public Boolean call() {
        return endpoint.messages.size() >= size;
      }
    };
  }

  Callable<Boolean> endpointLongPollingMessagesHasAtLeastOneElement() {
    return new Callable<Boolean>() {
      public Boolean call() {
        return endpointLongPolling.messages.size() >= 1;
      }
    };
  }

  Callable<Boolean> endpointErrorMessagesHasAtLeastTwoElements() {
    return new Callable<Boolean>() {
      public Boolean call() {
        return endpointError.messages.size() >= 2;
      }
    };
  }

  @Before
  public void setUp() {
    super.setUp();
    handlerError = new KeypleMessageHandlerErrorMock();
    endpoint = new SyncEndpointClientPollingMock();
    endpointLongPolling = new SyncEndpointClientLongPollingMock();
    endpointError = new SyncEndpointClientErrorMock();
  }

  public void checkEventDto(MessageDto msg1, MessageDto.Action action, String body) {
    assertThat(msg1.getSessionId()).isNotEmpty();
    assertThat(msg1.getAction()).isEqualTo(action.name());
    assertThat(msg1.getClientNodeId()).isNotEmpty();
    assertThat(msg1.getServerNodeId()).isNull();
    assertThat(msg1.getLocalReaderName()).isNull();
    assertThat(msg1.getRemoteReaderName()).isNull();
    assertThat(msg1.getBody()).isEqualTo(body);
    assertThat(msg1.getSessionId()).isNotEqualTo(msg1.getClientNodeId());
  }

  @Test
  public void constructor_whenPluginObservationStrategyIsProvided_shouldStartAPluginObserver() {
    new SyncNodeClientImpl(handler, endpoint, pollingEventStrategy, null);
    await().atMost(5, TimeUnit.SECONDS).until(endpointMessagesHasMinSize(2));
    MessageDto msg1 = endpoint.messages.get(0);
    MessageDto msg2 = endpoint.messages.get(1);
    assertThat(msg1).isSameAs(msg2);
    assertThat(msg1).isEqualToComparingFieldByField(msg2);
  }

  @Test
  public void constructor_whenReaderObservationStrategyIsProvided_shouldStartAReaderObserver() {
    new SyncNodeClientImpl(handler, endpoint, null, pollingEventStrategy);
    await().atMost(5, TimeUnit.SECONDS).until(endpointMessagesHasMinSize(2));
    MessageDto msg1 = endpoint.messages.get(0);
    MessageDto msg2 = endpoint.messages.get(1);
    assertThat(msg1).isSameAs(msg2);
    assertThat(msg1).isEqualToComparingFieldByField(msg2);
  }

  @Test
  public void
      constructor_whenPluginAndReaderObservationStrategyAreProvided_shouldStartAPluginAndReaderObservers() {
    new SyncNodeClientImpl(handler, endpoint, pollingEventStrategy, pollingEventStrategy);
    await().atMost(5, TimeUnit.SECONDS).until(endpointMessagesHasMinSize(4));
    Set<MessageDto> messageTypes = new HashSet<MessageDto>();
    messageTypes.add(endpoint.messages.get(0));
    messageTypes.add(endpoint.messages.get(1));
    messageTypes.add(endpoint.messages.get(2));
    messageTypes.add(endpoint.messages.get(3));
    assertThat(messageTypes).hasSize(2);
  }

  @Test
  public void constructor_whenPollingObservationStrategyIsProvided_shouldSendAPollingDto() {
    new SyncNodeClientImpl(handler, endpoint, pollingEventStrategy, null);
    await().atMost(5, TimeUnit.SECONDS).until(endpointMessagesHasMinSize(1));
    MessageDto msg = endpoint.messages.get(0);
    checkEventDto(msg, MessageDto.Action.CHECK_PLUGIN_EVENT, bodyPolling);
  }

  @Test
  public void constructor_whenLongPollingObservationStrategyIsProvided_shouldSendALongPollingDto() {
    new SyncNodeClientImpl(handler, endpointLongPolling, longPollingEventStrategy, null);
    await().atMost(5, TimeUnit.SECONDS).until(endpointLongPollingMessagesHasAtLeastOneElement());
    MessageDto msg = endpointLongPolling.messages.get(0);
    checkEventDto(msg, MessageDto.Action.CHECK_PLUGIN_EVENT, bodyLongPolling);
  }

  @Test
  public void constructor_whenObservationStrategyIsProvided_shouldCallOnMessageMethodOnHandler() {
    new SyncNodeClientImpl(handler, endpoint, pollingEventStrategy, null);
    await().atMost(5, TimeUnit.SECONDS).until(endpointMessagesHasMinSize(1));
    verify(handler).onMessage(response);
  }

  @Test
  public void
      constructor_whenObservationButHandlerInError_shouldInterruptObserverAndThrowException() {
    new SyncNodeClientImpl(handlerError, endpoint, pollingEventStrategy, null);
    await().atMost(5, TimeUnit.SECONDS).until(handlerErrorOccurred());
    await().atMost(2, TimeUnit.SECONDS);
    assertThat(endpoint.messages).hasSize(1);
  }

  @Test
  public void constructor_whenObservationButEndpointInError_shouldRetryUntilNoError() {
    new SyncNodeClientImpl(handler, endpointError, pollingEventStrategy, null);
    await().atMost(10, TimeUnit.SECONDS).until(endpointErrorMessagesHasAtLeastTwoElements());
    assertThat(endpointError.messages).hasSize(2);
  }

  @Test
  public void openSession_shouldDoNothing() {
    SyncNodeClientImpl node = new SyncNodeClientImpl(handler, endpoint, null, null);
    node.openSession(sessionId);
    verifyZeroInteractions(handler);
    assertThat(endpoint.messages).hasSize(0);
  }

  @Test
  public void sendRequest_shouldCallEndpointAndReturnEndpointResponse() {
    SyncNodeClientImpl node = new SyncNodeClientImpl(handler, endpoint, null, null);
    MessageDto result = node.sendRequest(msg);
    assertThat(endpoint.messages).hasSize(1);
    assertThat(endpoint.messages.get(0)).isSameAs(msg);
    assertThat(endpoint.messages.get(0)).isEqualToComparingFieldByField(msg);
    assertThat(result).isSameAs(response);
    assertThat(result).isEqualToComparingFieldByField(response);
    verifyZeroInteractions(handler);
  }

  @Test
  public void sendMessage_shouldCallEndpoint() {
    SyncNodeClientImpl node = new SyncNodeClientImpl(handler, endpoint, null, null);
    node.sendMessage(msg);
    assertThat(endpoint.messages).hasSize(1);
    assertThat(endpoint.messages.get(0)).isSameAs(msg);
    assertThat(endpoint.messages.get(0)).isEqualToComparingFieldByField(msg);
    verifyZeroInteractions(handler);
  }

  @Test
  public void closeSession_shouldDoNothing() {
    SyncNodeClientImpl node = new SyncNodeClientImpl(handler, endpoint, null, null);
    node.closeSession(sessionId);
    verifyZeroInteractions(handler);
    assertThat(endpoint.messages).hasSize(0);
  }
}
