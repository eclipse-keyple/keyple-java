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
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.exception.KeypleTimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SyncNodeServerTest extends AbstractSyncNodeTest {

  static final String sessionId1 = "sessionId1";
  static final String pluginSessionId = "pluginSessionId";
  static final String readerSessionId = "readerSessionId";
  static final String clientNodeId1 = "clientNodeId1";
  static final String clientNodeId2 = "clientNodeId2";

  SyncNodeServerImpl node;

  MessageDto msg2;
  MessageDto msg3;
  MessageDto msg4;

  MessageDto pluginCheckPollingClient1;
  MessageDto pluginCheckLongPollingClient1;
  MessageDto pluginCheckLongPollingLongTimeoutClient1;
  MessageDto pluginEvent1Client1;
  MessageDto pluginEvent2Client1;

  MessageDto pluginCheckLongPollingClient2;
  MessageDto pluginEvent1Client2;

  MessageDto readerCheckPollingClient1;
  MessageDto readerCheckLongPollingClient1;
  MessageDto readerCheckLongPollingLongTimeoutClient1;
  MessageDto readerEvent1Client1;
  MessageDto readerEvent2Client1;

  MessageDto readerCheckLongPollingClient2;
  MessageDto readerEvent1Client2;

  {
    msg2 = new MessageDto(msg);
    msg3 = new MessageDto(msg);
    msg4 = new MessageDto(msg);

    pluginCheckPollingClient1 = buildCheckEventMessage(clientNodeId1, true, false, false);
    pluginCheckLongPollingClient1 = buildCheckEventMessage(clientNodeId1, true, true, false);
    pluginCheckLongPollingLongTimeoutClient1 =
        buildCheckEventMessage(clientNodeId1, true, true, true);
    pluginEvent1Client1 = buildEventMessage(clientNodeId1, true);
    pluginEvent2Client1 = buildEventMessage(clientNodeId1, true);

    pluginCheckLongPollingClient2 = buildCheckEventMessage(clientNodeId2, true, true, false);
    pluginEvent1Client2 = buildEventMessage(clientNodeId2, true);

    readerCheckPollingClient1 = buildCheckEventMessage(clientNodeId1, false, false, false);
    readerCheckLongPollingClient1 = buildCheckEventMessage(clientNodeId1, false, true, false);
    readerCheckLongPollingLongTimeoutClient1 =
        buildCheckEventMessage(clientNodeId1, false, true, true);
    readerEvent1Client1 = buildEventMessage(clientNodeId1, false);
    readerEvent2Client1 = buildEventMessage(clientNodeId1, false);

    readerCheckLongPollingClient2 = buildCheckEventMessage(clientNodeId2, false, true, false);
    readerEvent1Client2 = buildEventMessage(clientNodeId2, false);
  }

  MessageDto buildCheckEventMessage(
      String clientNodeId, boolean isPlugin, boolean isLongPolling, boolean withLongTimeout) {
    return new MessageDto() //
        .setSessionId(isPlugin ? pluginSessionId : readerSessionId) //
        .setAction(
            isPlugin
                ? MessageDto.Action.CHECK_PLUGIN_EVENT.name()
                : MessageDto.Action.CHECK_READER_EVENT.name()) //
        .setClientNodeId(clientNodeId) //
        .setBody(
            isLongPolling
                ? withLongTimeout ? bodyLongPollingLongTimeout : bodyLongPolling
                : bodyPolling);
  }

  MessageDto buildEventMessage(String clientNodeId, boolean isPlugin) {
    return new MessageDto() //
        .setAction(
            isPlugin
                ? MessageDto.Action.PLUGIN_EVENT.name()
                : MessageDto.Action.READER_EVENT.name()) //
        .setClientNodeId(clientNodeId);
  }

  MessageDto buildMinimalMessage() {
    return new MessageDto() //
        .setSessionId(sessionId1) //
        .setAction(MessageDto.Action.EXECUTE_REMOTE_SERVICE.name()) //
        .setClientNodeId(clientNodeId1);
  }

  class MessageScheduler extends Thread {

    Thread ownerThread;
    MessageDto msg;
    int mode;
    MessageDto response;
    List<MessageDto> responses;

    MessageScheduler(final MessageDto msg, final int mode) {
      this.ownerThread = Thread.currentThread();
      this.msg = msg;
      this.mode = mode;
    }

    @Override
    public void run() {
      await().atMost(5, TimeUnit.SECONDS).until(threadHasStateTimedWaiting(ownerThread));
      switch (mode) {
        case 1:
          responses = node.onRequest(msg);
          break;
        case 2:
          response = node.sendRequest(msg);
          break;
        case 3:
          node.sendMessage(msg);
          break;
      }
    }
  }

  MessageScheduler scheduleOnRequest(final MessageDto msg) {
    MessageScheduler t = new MessageScheduler(msg, 1);
    t.start();
    return t;
  }

  MessageScheduler scheduleSendRequest(final MessageDto msg) {
    MessageScheduler t = new MessageScheduler(msg, 2);
    t.start();
    return t;
  }

  void scheduleSendMessage(final MessageDto msg) {
    MessageScheduler t = new MessageScheduler(msg, 3);
    t.start();
  }

  class ClientMessageSender extends Thread {

    MessageDto msg;
    List<MessageDto> responses;

    ClientMessageSender(final MessageDto msg) {
      this.msg = msg;
    }

    @Override
    public void run() {
      responses = node.onRequest(msg);
    }
  }

  ClientMessageSender callOnRequestFromAnotherThread(final MessageDto msg) {
    ClientMessageSender t = new ClientMessageSender(msg);
    t.start();
    return t;
  }

  @Before
  public void setUp() {
    super.setUp();
    node = new SyncNodeServerImpl(handler, 10);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onRequest_whenRequestIsNull_shouldThrowIllegalArgumentException() {
    node.onRequest(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onRequest_whenSessionIdIsNull_shouldThrowIllegalArgumentException() {
    MessageDto msg = buildMinimalMessage().setSessionId(null);
    node.onRequest(msg);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onRequest_whenSessionIdIsEmpty_shouldThrowIllegalArgumentException() {
    MessageDto msg = buildMinimalMessage().setSessionId("");
    node.onRequest(msg);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onRequest_whenActionIsNull_shouldThrowIllegalArgumentException() {
    MessageDto msg = buildMinimalMessage().setAction(null);
    node.onRequest(msg);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onRequest_whenActionIsEmpty_shouldThrowIllegalArgumentException() {
    MessageDto msg = buildMinimalMessage().setAction("");
    node.onRequest(msg);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onRequest_whenActionIsUnknown_shouldThrowIllegalArgumentException() {
    MessageDto msg = buildMinimalMessage().setAction("TEST");
    node.onRequest(msg);
  }

  @Test(expected = IllegalArgumentException.class)
  public void
      onRequest_whenActionIsCheckPluginEventAndBodyIsNull_shouldThrowIllegalArgumentException() {
    MessageDto msg =
        buildMinimalMessage().setAction(MessageDto.Action.CHECK_PLUGIN_EVENT.name());
    node.onRequest(msg);
  }

  @Test(expected = IllegalArgumentException.class)
  public void
      onRequest_whenActionIsCheckPluginEventAndBodyIsMalformed_shouldThrowIllegalArgumentException() {
    MessageDto msg =
        buildMinimalMessage()
            .setAction(MessageDto.Action.CHECK_PLUGIN_EVENT.name())
            .setBody("TEST");
    node.onRequest(msg);
  }

  @Test
  public void
      onRequest_whenActionIsCheckPluginEventUsingPollingAndNoEventForClientNodeId_shouldReturnEmptyList() {
    node.sendMessage(pluginEvent1Client2);
    List<MessageDto> events = node.onRequest(pluginCheckPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test
  public void
      onRequest_whenActionIsCheckPluginEventUsingPollingAndOneEvent_shouldReturnEventAndClearEvent() {
    node.sendMessage(pluginEvent1Client1);
    node.sendMessage(pluginEvent1Client2);
    List<MessageDto> events = node.onRequest(pluginCheckPollingClient1);
    assertThat(events).containsExactly(pluginEvent1Client1);
    events = node.onRequest(pluginCheckPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test
  public void
      onRequest_whenActionIsCheckPluginEventUsingPollingAndTwoEvents_shouldReturnEventsAndClearEvents() {
    node.sendMessage(pluginEvent1Client1);
    node.sendMessage(pluginEvent1Client2);
    node.sendMessage(pluginEvent2Client1);
    List<MessageDto> events = node.onRequest(pluginCheckPollingClient1);
    assertThat(events).containsExactly(pluginEvent1Client1, pluginEvent2Client1);
    events = node.onRequest(pluginCheckPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test
  public void
      onRequest_whenActionIsCheckPluginEventUsingLongPollingAndNoEventAndTimeout_shouldReturnEmptyList() {
    node.sendMessage(pluginEvent1Client2);
    List<MessageDto> events = node.onRequest(pluginCheckLongPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test
  public void
      onRequest_whenActionIsCheckPluginEventUsingLongPollingAndOneEvent_shouldReturnEventAndClearEvent() {
    node.sendMessage(pluginEvent1Client1);
    node.sendMessage(pluginEvent1Client2);
    List<MessageDto> events = node.onRequest(pluginCheckLongPollingClient1);
    assertThat(events).containsExactly(pluginEvent1Client1);
    events = node.onRequest(pluginCheckLongPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test
  public void
      onRequest_whenActionIsCheckPluginEventUsingLongPollingAndNoEvent_shouldAwaitEventAndReturnEvent() {
    node.sendMessage(pluginEvent1Client2);
    scheduleSendMessage(pluginEvent1Client1);
    List<MessageDto> events = node.onRequest(pluginCheckLongPollingLongTimeoutClient1);
    assertThat(events).containsExactly(pluginEvent1Client1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void
      onRequest_whenActionIsCheckReaderEventAndBodyIsNull_shouldThrowIllegalArgumentException() {
    MessageDto msg =
        buildMinimalMessage().setAction(MessageDto.Action.CHECK_READER_EVENT.name());
    node.onRequest(msg);
  }

  @Test(expected = IllegalArgumentException.class)
  public void
      onRequest_whenActionIsCheckReaderEventAndBodyIsMalformed_shouldThrowIllegalArgumentException() {
    MessageDto msg =
        buildMinimalMessage()
            .setAction(MessageDto.Action.CHECK_READER_EVENT.name())
            .setBody("TEST");
    node.onRequest(msg);
  }

  @Test
  public void
      onRequest_whenActionIsCheckReaderEventUsingPollingAndNoEventForClientNodeId_shouldReturnEmptyList() {
    node.sendMessage(readerEvent1Client2);
    List<MessageDto> events = node.onRequest(readerCheckPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test
  public void
      onRequest_whenActionIsCheckReaderEventUsingPollingAndOneEvent_shouldReturnEventAndClearEvent() {
    node.sendMessage(readerEvent1Client1);
    node.sendMessage(readerEvent1Client2);
    List<MessageDto> events = node.onRequest(readerCheckPollingClient1);
    assertThat(events).containsExactly(readerEvent1Client1);
    events = node.onRequest(readerCheckPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test
  public void
      onRequest_whenActionIsCheckReaderEventUsingPollingAndTwoEvents_shouldReturnEventsAndClearEvents() {
    node.sendMessage(readerEvent1Client1);
    node.sendMessage(readerEvent1Client2);
    node.sendMessage(readerEvent2Client1);
    List<MessageDto> events = node.onRequest(readerCheckPollingClient1);
    assertThat(events).containsExactly(readerEvent1Client1, readerEvent2Client1);
    events = node.onRequest(readerCheckPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test
  public void
      onRequest_whenActionIsCheckReaderEventUsingLongPollingAndNoEventAndTimeout_shouldReturnEmptyList() {
    node.sendMessage(readerEvent1Client2);
    List<MessageDto> events = node.onRequest(readerCheckLongPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test
  public void
      onRequest_whenActionIsCheckReaderEventUsingLongPollingAndOneEvent_shouldReturnEventAndClearEvent() {
    node.sendMessage(readerEvent1Client1);
    node.sendMessage(readerEvent1Client2);
    List<MessageDto> events = node.onRequest(readerCheckLongPollingClient1);
    assertThat(events).containsExactly(readerEvent1Client1);
    events = node.onRequest(readerCheckLongPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test
  public void
      onRequest_whenActionIsCheckReaderEventUsingLongPollingAndNoEvent_shouldAwaitEventAndReturnEvent() {
    node.sendMessage(readerEvent1Client2);
    scheduleSendMessage(readerEvent1Client1);
    List<MessageDto> events = node.onRequest(readerCheckLongPollingLongTimeoutClient1);
    assertThat(events).containsExactly(readerEvent1Client1);
  }

  @Test
  public void
      onRequest_whenActionIsTxAndNoPendingServerTaskAndNoServerTimeout_shouldTransmitMessageToHandlerAndAwaitAResponse() {
    scheduleSendMessage(msg2);
    List<MessageDto> responses = node.onRequest(msg);
    verify(handler).onMessage(msg);
    verifyNoMoreInteractions(handler);
    assertThat(responses).containsExactly(msg2);
  }

  @Test(expected = KeypleTimeoutException.class)
  public void
      onRequest_whenActionIsTxAndNoPendingServerTaskAndNoServerTimeoutAndClientTimeout_shouldTransmitMessageToHandlerAndThrowKTE() {
    node = new SyncNodeServerImpl(handler, 1);
    node.onRequest(msg);
  }

  @Test(expected = KeypleTimeoutException.class)
  public void onRequest_whenActionIsTxAndNoPendingServerTaskAndServerTimeout_shouldThrowKTE() {
    node = new SyncNodeServerImpl(handler, 1);
    Thread serverTask = scheduleSendRequest(msg2);
    node.onRequest(msg);
    await().atMost(5, TimeUnit.SECONDS).until(threadHasStateTerminated(serverTask));
    node.onRequest(msg3);
  }

  @Test
  public void
      onRequest_whenActionIsTxAndPendingServerTaskAndNoClientTimeout_shouldTransmitMessageToPendingTaskAndAwaitAResponse() {
    MessageScheduler serverTask1 = scheduleSendRequest(msg2);
    node.onRequest(msg);
    scheduleSendMessage(msg4);
    List<MessageDto> responses = node.onRequest(msg3);
    verify(handler).onMessage(msg);
    verifyNoMoreInteractions(handler);
    assertThat(serverTask1.response).isSameAs(msg3);
    assertThat(serverTask1.response).isEqualToComparingFieldByField(msg3);
    assertThat(responses).containsExactly(msg4);
  }

  @Test(expected = KeypleTimeoutException.class)
  public void
      onRequest_whenActionIsTxAndPendingServerTaskAndClientTimeout_shouldTransmitMessageToPendingTaskAndThrowKTE() {
    node = new SyncNodeServerImpl(handler, 1);
    scheduleSendRequest(msg2);
    node.onRequest(msg);
    verify(handler).onMessage(msg);
    verifyNoMoreInteractions(handler);
    node.onRequest(msg3);
  }

  @Test(expected = RuntimeException.class)
  public void onRequest_whenActionIsTxHandlerInError_shouldThrowError() {
    setHandlerError();
    node.onRequest(msg);
  }

  @Test(expected = KeypleTimeoutException.class)
  public void
      sendRequest_whenNoPendingClientTaskAndClientTimeout_shouldThrowKeypleTimeoutException() {
    node = new SyncNodeServerImpl(handler, 1);
    node.onRequest(msg);
    node.sendRequest(msg2);
  }

  @Test(expected = NullPointerException.class)
  public void sendRequest_whenNoPendingClientTaskNoClientTimeout_shouldNPE() {
    node = new SyncNodeServerImpl(handler, 1);
    node.sendRequest(msg);
  }

  @Test(expected = KeypleTimeoutException.class)
  public void
      sendRequest_whenPendingClientTaskAndServerTimeout_shouldTransmitMessageToPendingTaskAndThrowKeypleTimeoutException() {
    node = new SyncNodeServerImpl(handler, 1);
    Thread clientTask = callOnRequestFromAnotherThread(msg);
    await().atMost(5, TimeUnit.SECONDS).until(threadHasStateTimedWaiting(clientTask));
    node.sendRequest(msg2);
  }

  @Test
  public void
      sendRequest_whenPendingClientTaskAndNoServerTimeout_shouldTransmitMessageToPendingTaskAndAwaitAResponse() {
    ClientMessageSender clientTask1 = callOnRequestFromAnotherThread(msg);
    await().atMost(5, TimeUnit.SECONDS).until(threadHasStateTimedWaiting(clientTask1));
    MessageScheduler clientTask2 = scheduleOnRequest(msg3);
    MessageDto response = node.sendRequest(msg2);
    node.sendMessage(msg4);
    verify(handler).onMessage(msg);
    verifyNoMoreInteractions(handler);
    assertThat(clientTask1.responses).containsExactly(msg2);
    assertThat(response).isSameAs(msg3);
    assertThat(clientTask2.responses).containsExactly(msg4);
  }

  @Test
  public void sendMessage_whenActionIsPluginEventAndPolling_shouldAddEvent() {
    List<MessageDto> events = node.onRequest(pluginCheckPollingClient1);
    assertThat(events).isEmpty();
    node.sendMessage(pluginEvent1Client1);
    node.sendMessage(pluginEvent2Client1);
    events = node.onRequest(pluginCheckPollingClient1);
    assertThat(events).containsExactly(pluginEvent1Client1, pluginEvent2Client1);
  }

  @Test
  public void sendMessage_whenActionIsPluginEventAndLongPollingAndNoClientTask_shouldAddEvent() {
    List<MessageDto> events = node.onRequest(pluginCheckLongPollingClient1);
    assertThat(events).isEmpty();
    node.sendMessage(pluginEvent1Client1);
    node.sendMessage(pluginEvent2Client1);
    events = node.onRequest(pluginCheckPollingClient1);
    assertThat(events).containsExactly(pluginEvent1Client1, pluginEvent2Client1);
  }

  @Test
  public void
      sendMessage_whenActionIsPluginEventAndLongPollingAndClientTask_shouldTransmitEventToClientTask() {
    Thread clientTask = callOnRequestFromAnotherThread(pluginCheckLongPollingClient1);
    await().atMost(5, TimeUnit.SECONDS).until(threadHasStateTimedWaiting(clientTask));
    node.sendMessage(pluginEvent1Client1);
    await().atMost(5, TimeUnit.SECONDS).until(threadHasStateTerminated(clientTask));
    List<MessageDto> events = node.onRequest(pluginCheckLongPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test
  public void sendMessage_whenActionIsReaderEventAndPolling_shouldAddEvent() {
    List<MessageDto> events = node.onRequest(readerCheckPollingClient1);
    assertThat(events).isEmpty();
    node.sendMessage(readerEvent1Client1);
    node.sendMessage(readerEvent2Client1);
    events = node.onRequest(readerCheckPollingClient1);
    assertThat(events).containsExactly(readerEvent1Client1, readerEvent2Client1);
  }

  @Test
  public void sendMessage_whenActionIsReaderEventAndLongPollingAndNoClientTask_shouldAddEvent() {
    List<MessageDto> events = node.onRequest(readerCheckLongPollingClient1);
    assertThat(events).isEmpty();
    node.sendMessage(readerEvent1Client1);
    node.sendMessage(readerEvent2Client1);
    events = node.onRequest(readerCheckPollingClient1);
    assertThat(events).containsExactly(readerEvent1Client1, readerEvent2Client1);
  }

  @Test
  public void
      sendMessage_whenActionIsReaderEventAndLongPollingAndClientTask_shouldTransmitEventToClientTask() {
    Thread clientTask = callOnRequestFromAnotherThread(readerCheckLongPollingClient1);
    await().atMost(5, TimeUnit.SECONDS).until(threadHasStateTimedWaiting(clientTask));
    node.sendMessage(readerEvent1Client1);
    await().atMost(5, TimeUnit.SECONDS).until(threadHasStateTerminated(clientTask));
    List<MessageDto> events = node.onRequest(readerCheckLongPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test(expected = KeypleTimeoutException.class)
  public void
      sendMessage_whenNoPendingClientTaskAndClientTimeout_shouldThrowKeypleTimeoutException() {
    node = new SyncNodeServerImpl(handler, 1);
    node.onRequest(msg);
    node.sendMessage(msg2);
  }

  @Test(expected = IllegalStateException.class)
  public void sendMessage_whenNoPendingClientTaskAndNoClientTimeout_shouldThrowISE() {
    node.sendMessage(msg);
  }

  @Test
  public void sendMessage_whenPendingClientTask_shouldTransmitMessageToPendingTaskAndReturn() {
    ClientMessageSender clientTask = callOnRequestFromAnotherThread(msg);
    await().atMost(5, TimeUnit.SECONDS).until(threadHasStateTimedWaiting(clientTask));
    node.sendMessage(msg2);
    await().atMost(5, TimeUnit.SECONDS).until(threadHasStateTerminated(clientTask));
    assertThat(clientTask.responses).containsExactly(msg2);
  }
}
