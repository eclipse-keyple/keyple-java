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
package org.eclipse.keyple.plugin.remotese.core.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.core.exception.KeypleTimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class KeypleServerSyncNodeTestImplTest extends AbstractKeypleSyncNodeTest {

  static final String sessionId1 = "sessionId1";
  static final String pluginSessionId = "pluginSessionId";
  static final String readerSessionId = "readerSessionId";
  static final String clientNodeId1 = "clientNodeId1";
  static final String clientNodeId2 = "clientNodeId2";

  KeypleServerSyncNodeImpl node;

  KeypleMessageDto msg2;
  KeypleMessageDto msg3;
  KeypleMessageDto msg4;

  KeypleMessageDto pluginCheckPollingClient1;
  KeypleMessageDto pluginCheckLongPollingClient1;
  KeypleMessageDto pluginCheckLongPollingLongTimeoutClient1;
  KeypleMessageDto pluginEvent1Client1;
  KeypleMessageDto pluginEvent2Client1;

  KeypleMessageDto pluginCheckLongPollingClient2;
  KeypleMessageDto pluginEvent1Client2;

  KeypleMessageDto readerCheckPollingClient1;
  KeypleMessageDto readerCheckLongPollingClient1;
  KeypleMessageDto readerCheckLongPollingLongTimeoutClient1;
  KeypleMessageDto readerEvent1Client1;
  KeypleMessageDto readerEvent2Client1;

  KeypleMessageDto readerCheckLongPollingClient2;
  KeypleMessageDto readerEvent1Client2;

  {
    msg2 = new KeypleMessageDto(msg);
    msg3 = new KeypleMessageDto(msg);
    msg4 = new KeypleMessageDto(msg);

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

  KeypleMessageDto buildCheckEventMessage(
      String clientNodeId, boolean isPlugin, boolean isLongPolling, boolean withLongTimeout) {
    return new KeypleMessageDto() //
        .setSessionId(isPlugin ? pluginSessionId : readerSessionId) //
        .setAction(
            isPlugin
                ? KeypleMessageDto.Action.CHECK_PLUGIN_EVENT.name()
                : KeypleMessageDto.Action.CHECK_READER_EVENT.name()) //
        .setClientNodeId(clientNodeId) //
        .setBody(
            isLongPolling
                ? withLongTimeout ? bodyLongPollingLongTimeout : bodyLongPolling
                : bodyPolling);
  }

  KeypleMessageDto buildEventMessage(String clientNodeId, boolean isPlugin) {
    return new KeypleMessageDto() //
        .setAction(
            isPlugin
                ? KeypleMessageDto.Action.PLUGIN_EVENT.name()
                : KeypleMessageDto.Action.READER_EVENT.name()) //
        .setClientNodeId(clientNodeId);
  }

  KeypleMessageDto buildMinimalMessage() {
    return new KeypleMessageDto() //
        .setSessionId(sessionId1) //
        .setAction(KeypleMessageDto.Action.EXECUTE_REMOTE_SERVICE.name()) //
        .setClientNodeId(clientNodeId1);
  }

  class MessageScheduler extends Thread {

    Thread ownerThread;
    KeypleMessageDto msg;
    int mode;
    KeypleMessageDto response;
    List<KeypleMessageDto> responses;

    MessageScheduler(final KeypleMessageDto msg, final int mode) {
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

  MessageScheduler scheduleOnRequest(final KeypleMessageDto msg) {
    MessageScheduler t = new MessageScheduler(msg, 1);
    t.start();
    return t;
  }

  MessageScheduler scheduleSendRequest(final KeypleMessageDto msg) {
    MessageScheduler t = new MessageScheduler(msg, 2);
    t.start();
    return t;
  }

  void scheduleSendMessage(final KeypleMessageDto msg) {
    MessageScheduler t = new MessageScheduler(msg, 3);
    t.start();
  }

  class ClientMessageSender extends Thread {

    KeypleMessageDto msg;
    List<KeypleMessageDto> responses;

    ClientMessageSender(final KeypleMessageDto msg) {
      this.msg = msg;
    }

    @Override
    public void run() {
      responses = node.onRequest(msg);
    }
  }

  ClientMessageSender callOnRequestFromAnotherThread(final KeypleMessageDto msg) {
    ClientMessageSender t = new ClientMessageSender(msg);
    t.start();
    return t;
  }

  @Before
  public void setUp() {
    super.setUp();
    node = new KeypleServerSyncNodeImpl(handler, 10);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onRequest_whenRequestIsNull_shouldThrowIllegalArgumentException() {
    node.onRequest(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onRequest_whenSessionIdIsNull_shouldThrowIllegalArgumentException() {
    KeypleMessageDto msg = buildMinimalMessage().setSessionId(null);
    node.onRequest(msg);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onRequest_whenSessionIdIsEmpty_shouldThrowIllegalArgumentException() {
    KeypleMessageDto msg = buildMinimalMessage().setSessionId("");
    node.onRequest(msg);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onRequest_whenActionIsNull_shouldThrowIllegalArgumentException() {
    KeypleMessageDto msg = buildMinimalMessage().setAction(null);
    node.onRequest(msg);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onRequest_whenActionIsEmpty_shouldThrowIllegalArgumentException() {
    KeypleMessageDto msg = buildMinimalMessage().setAction("");
    node.onRequest(msg);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onRequest_whenActionIsUnknown_shouldThrowIllegalArgumentException() {
    KeypleMessageDto msg = buildMinimalMessage().setAction("TEST");
    node.onRequest(msg);
  }

  @Test(expected = IllegalArgumentException.class)
  public void
      onRequest_whenActionIsCheckPluginEventAndBodyIsNull_shouldThrowIllegalArgumentException() {
    KeypleMessageDto msg =
        buildMinimalMessage().setAction(KeypleMessageDto.Action.CHECK_PLUGIN_EVENT.name());
    node.onRequest(msg);
  }

  @Test(expected = IllegalArgumentException.class)
  public void
      onRequest_whenActionIsCheckPluginEventAndBodyIsMalformed_shouldThrowIllegalArgumentException() {
    KeypleMessageDto msg =
        buildMinimalMessage()
            .setAction(KeypleMessageDto.Action.CHECK_PLUGIN_EVENT.name())
            .setBody("TEST");
    node.onRequest(msg);
  }

  @Test
  public void
      onRequest_whenActionIsCheckPluginEventUsingPollingAndNoEventForClientNodeId_shouldReturnEmptyList() {
    node.sendMessage(pluginEvent1Client2);
    List<KeypleMessageDto> events = node.onRequest(pluginCheckPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test
  public void
      onRequest_whenActionIsCheckPluginEventUsingPollingAndOneEvent_shouldReturnEventAndClearEvent() {
    node.sendMessage(pluginEvent1Client1);
    node.sendMessage(pluginEvent1Client2);
    List<KeypleMessageDto> events = node.onRequest(pluginCheckPollingClient1);
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
    List<KeypleMessageDto> events = node.onRequest(pluginCheckPollingClient1);
    assertThat(events).containsExactly(pluginEvent1Client1, pluginEvent2Client1);
    events = node.onRequest(pluginCheckPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test
  public void
      onRequest_whenActionIsCheckPluginEventUsingLongPollingAndNoEventAndTimeout_shouldReturnEmptyList() {
    node.sendMessage(pluginEvent1Client2);
    List<KeypleMessageDto> events = node.onRequest(pluginCheckLongPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test
  public void
      onRequest_whenActionIsCheckPluginEventUsingLongPollingAndOneEvent_shouldReturnEventAndClearEvent() {
    node.sendMessage(pluginEvent1Client1);
    node.sendMessage(pluginEvent1Client2);
    List<KeypleMessageDto> events = node.onRequest(pluginCheckLongPollingClient1);
    assertThat(events).containsExactly(pluginEvent1Client1);
    events = node.onRequest(pluginCheckLongPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test
  public void
      onRequest_whenActionIsCheckPluginEventUsingLongPollingAndNoEvent_shouldAwaitEventAndReturnEvent() {
    node.sendMessage(pluginEvent1Client2);
    scheduleSendMessage(pluginEvent1Client1);
    List<KeypleMessageDto> events = node.onRequest(pluginCheckLongPollingLongTimeoutClient1);
    assertThat(events).containsExactly(pluginEvent1Client1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void
      onRequest_whenActionIsCheckReaderEventAndBodyIsNull_shouldThrowIllegalArgumentException() {
    KeypleMessageDto msg =
        buildMinimalMessage().setAction(KeypleMessageDto.Action.CHECK_READER_EVENT.name());
    node.onRequest(msg);
  }

  @Test(expected = IllegalArgumentException.class)
  public void
      onRequest_whenActionIsCheckReaderEventAndBodyIsMalformed_shouldThrowIllegalArgumentException() {
    KeypleMessageDto msg =
        buildMinimalMessage()
            .setAction(KeypleMessageDto.Action.CHECK_READER_EVENT.name())
            .setBody("TEST");
    node.onRequest(msg);
  }

  @Test
  public void
      onRequest_whenActionIsCheckReaderEventUsingPollingAndNoEventForClientNodeId_shouldReturnEmptyList() {
    node.sendMessage(readerEvent1Client2);
    List<KeypleMessageDto> events = node.onRequest(readerCheckPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test
  public void
      onRequest_whenActionIsCheckReaderEventUsingPollingAndOneEvent_shouldReturnEventAndClearEvent() {
    node.sendMessage(readerEvent1Client1);
    node.sendMessage(readerEvent1Client2);
    List<KeypleMessageDto> events = node.onRequest(readerCheckPollingClient1);
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
    List<KeypleMessageDto> events = node.onRequest(readerCheckPollingClient1);
    assertThat(events).containsExactly(readerEvent1Client1, readerEvent2Client1);
    events = node.onRequest(readerCheckPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test
  public void
      onRequest_whenActionIsCheckReaderEventUsingLongPollingAndNoEventAndTimeout_shouldReturnEmptyList() {
    node.sendMessage(readerEvent1Client2);
    List<KeypleMessageDto> events = node.onRequest(readerCheckLongPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test
  public void
      onRequest_whenActionIsCheckReaderEventUsingLongPollingAndOneEvent_shouldReturnEventAndClearEvent() {
    node.sendMessage(readerEvent1Client1);
    node.sendMessage(readerEvent1Client2);
    List<KeypleMessageDto> events = node.onRequest(readerCheckLongPollingClient1);
    assertThat(events).containsExactly(readerEvent1Client1);
    events = node.onRequest(readerCheckLongPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test
  public void
      onRequest_whenActionIsCheckReaderEventUsingLongPollingAndNoEvent_shouldAwaitEventAndReturnEvent() {
    node.sendMessage(readerEvent1Client2);
    scheduleSendMessage(readerEvent1Client1);
    List<KeypleMessageDto> events = node.onRequest(readerCheckLongPollingLongTimeoutClient1);
    assertThat(events).containsExactly(readerEvent1Client1);
  }

  @Test
  public void
      onRequest_whenActionIsTxAndNoPendingServerTaskAndNoServerTimeout_shouldTransmitMessageToHandlerAndAwaitAResponse() {
    scheduleSendMessage(msg2);
    List<KeypleMessageDto> responses = node.onRequest(msg);
    verify(handler).onMessage(msg);
    verifyNoMoreInteractions(handler);
    assertThat(responses).containsExactly(msg2);
  }

  @Test(expected = KeypleTimeoutException.class)
  public void
      onRequest_whenActionIsTxAndNoPendingServerTaskAndNoServerTimeoutAndClientTimeout_shouldTransmitMessageToHandlerAndThrowKTE() {
    node = new KeypleServerSyncNodeImpl(handler, 1);
    node.onRequest(msg);
  }

  @Test(expected = KeypleTimeoutException.class)
  public void onRequest_whenActionIsTxAndNoPendingServerTaskAndServerTimeout_shouldThrowKTE() {
    node = new KeypleServerSyncNodeImpl(handler, 1);
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
    List<KeypleMessageDto> responses = node.onRequest(msg3);
    verify(handler).onMessage(msg);
    verifyNoMoreInteractions(handler);
    assertThat(serverTask1.response).isSameAs(msg3);
    assertThat(serverTask1.response).isEqualToComparingFieldByField(msg3);
    assertThat(responses).containsExactly(msg4);
  }

  @Test(expected = KeypleTimeoutException.class)
  public void
      onRequest_whenActionIsTxAndPendingServerTaskAndClientTimeout_shouldTransmitMessageToPendingTaskAndThrowKTE() {
    node = new KeypleServerSyncNodeImpl(handler, 1);
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
    node = new KeypleServerSyncNodeImpl(handler, 1);
    node.onRequest(msg);
    node.sendRequest(msg2);
  }

  @Test(expected = NullPointerException.class)
  public void sendRequest_whenNoPendingClientTaskNoClientTimeout_shouldNPE() {
    node = new KeypleServerSyncNodeImpl(handler, 1);
    node.sendRequest(msg);
  }

  @Test(expected = KeypleTimeoutException.class)
  public void
      sendRequest_whenPendingClientTaskAndServerTimeout_shouldTransmitMessageToPendingTaskAndThrowKeypleTimeoutException() {
    node = new KeypleServerSyncNodeImpl(handler, 1);
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
    KeypleMessageDto response = node.sendRequest(msg2);
    node.sendMessage(msg4);
    verify(handler).onMessage(msg);
    verifyNoMoreInteractions(handler);
    assertThat(clientTask1.responses).containsExactly(msg2);
    assertThat(response).isSameAs(msg3);
    assertThat(clientTask2.responses).containsExactly(msg4);
  }

  @Test
  public void sendMessage_whenActionIsPluginEventAndPolling_shouldAddEvent() {
    List<KeypleMessageDto> events = node.onRequest(pluginCheckPollingClient1);
    assertThat(events).isEmpty();
    node.sendMessage(pluginEvent1Client1);
    node.sendMessage(pluginEvent2Client1);
    events = node.onRequest(pluginCheckPollingClient1);
    assertThat(events).containsExactly(pluginEvent1Client1, pluginEvent2Client1);
  }

  @Test
  public void sendMessage_whenActionIsPluginEventAndLongPollingAndNoClientTask_shouldAddEvent() {
    List<KeypleMessageDto> events = node.onRequest(pluginCheckLongPollingClient1);
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
    List<KeypleMessageDto> events = node.onRequest(pluginCheckLongPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test
  public void sendMessage_whenActionIsReaderEventAndPolling_shouldAddEvent() {
    List<KeypleMessageDto> events = node.onRequest(readerCheckPollingClient1);
    assertThat(events).isEmpty();
    node.sendMessage(readerEvent1Client1);
    node.sendMessage(readerEvent2Client1);
    events = node.onRequest(readerCheckPollingClient1);
    assertThat(events).containsExactly(readerEvent1Client1, readerEvent2Client1);
  }

  @Test
  public void sendMessage_whenActionIsReaderEventAndLongPollingAndNoClientTask_shouldAddEvent() {
    List<KeypleMessageDto> events = node.onRequest(readerCheckLongPollingClient1);
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
    List<KeypleMessageDto> events = node.onRequest(readerCheckLongPollingClient1);
    assertThat(events).isEmpty();
  }

  @Test(expected = KeypleTimeoutException.class)
  public void
      sendMessage_whenNoPendingClientTaskAndClientTimeout_shouldThrowKeypleTimeoutException() {
    node = new KeypleServerSyncNodeImpl(handler, 1);
    node.onRequest(msg);
    node.sendMessage(msg2);
  }

  @Test(expected = IllegalStateException.class)
  public void sendMessage_whenNoPendingClientTaskAndNoClientTimeout_shouldThrowNPE() {
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
