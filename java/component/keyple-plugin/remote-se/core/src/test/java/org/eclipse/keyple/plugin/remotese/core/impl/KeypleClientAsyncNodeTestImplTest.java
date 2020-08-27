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

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientAsync;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.core.exception.KeypleRemoteCommunicationException;
import org.eclipse.keyple.plugin.remotese.core.exception.KeypleTimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class KeypleClientAsyncNodeTestImplTest extends AbstractKeypleAsyncNodeTest {

  KeypleClientAsync endpoint;
  KeypleClientAsyncNodeImpl node;

  KeypleMessageDto pluginEvent;
  KeypleMessageDto readerEvent;

  {
    pluginEvent = new KeypleMessageDto(msg).setAction(KeypleMessageDto.Action.PLUGIN_EVENT.name());

    readerEvent = new KeypleMessageDto(msg).setAction(KeypleMessageDto.Action.READER_EVENT.name());
  }

  class MessageScheduler extends Thread {

    Thread ownerThread;
    String sessionId;
    KeypleMessageDto msg;
    int mode;

    MessageScheduler(final String sessionId, final KeypleMessageDto msg, final int mode) {
      this.ownerThread = Thread.currentThread();
      this.sessionId = sessionId;
      this.msg = msg;
      this.mode = mode;
    }

    @Override
    public void run() {
      await().atMost(5, TimeUnit.SECONDS).until(threadHasStateTimedWaiting(ownerThread));
      switch (mode) {
        case 1:
          node.onOpen(sessionId);
          break;
        case 2:
          node.onMessage(msg);
          break;
        case 3:
          node.onClose(sessionId);
          break;
        case 4:
          node.onError(sessionId, error);
          break;
      }
    }
  }

  void scheduleOnOpen() {
    MessageScheduler t = new MessageScheduler(sessionId, null, 1);
    t.start();
  }

  void scheduleOnMessage(KeypleMessageDto message) {
    MessageScheduler t = new MessageScheduler(sessionId, message, 2);
    t.start();
  }

  void scheduleOnClose() {
    MessageScheduler t = new MessageScheduler(sessionId, null, 3);
    t.start();
  }

  void scheduleOnError() {
    MessageScheduler t = new MessageScheduler(sessionId, null, 4);
    t.start();
  }

  void openSessionInSafeMode() {
    try {
      node.openSession(sessionId);
    } catch (Exception e) {
      fail("Exception should not have thrown here");
    }
  }

  void doEndpointToReturnAnswer(
      boolean whenOpenSession, boolean whenSendMessage, boolean whenCloseSession) {
    if (whenOpenSession) {
      doAnswer(
              new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) {
                  node.onOpen(sessionId);
                  return null;
                }
              })
          .when(endpoint)
          .openSession(sessionId);
    }
    if (whenSendMessage) {
      doAnswer(
              new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) {
                  node.onMessage(response);
                  return null;
                }
              })
          .when(endpoint)
          .sendMessage(msg);
    }
    if (whenCloseSession) {
      doAnswer(
              new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) {
                  node.onClose(sessionId);
                  return null;
                }
              })
          .when(endpoint)
          .closeSession(sessionId);
    }
  }

  void doEndpointToCallOnError(
      boolean whenOpenSession, boolean whenSendMessage, boolean whenCloseSession) {
    if (whenOpenSession) {
      doAnswer(
              new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) {
                  node.onError(sessionId, error);
                  return null;
                }
              })
          .when(endpoint)
          .openSession(sessionId);
    }
    if (whenSendMessage) {
      doAnswer(
              new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) {
                  node.onError(sessionId, error);
                  return null;
                }
              })
          .when(endpoint)
          .sendMessage(msg);
    }
    if (whenCloseSession) {
      doAnswer(
              new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) {
                  node.onError(sessionId, error);
                  return null;
                }
              })
          .when(endpoint)
          .closeSession(sessionId);
    }
  }

  void doEndpointToThrowException(
      boolean whenOpenSession, boolean whenSendMessage, boolean whenCloseSession) {
    if (whenOpenSession) {
      doThrow(new KeypleRemoteCommunicationException("TEST")).when(endpoint).openSession(sessionId);
    }
    if (whenSendMessage) {
      doThrow(new KeypleRemoteCommunicationException("TEST")).when(endpoint).sendMessage(msg);
    }
    if (whenCloseSession) {
      doThrow(new KeypleRemoteCommunicationException("TEST"))
          .when(endpoint)
          .closeSession(sessionId);
    }
  }

  @Before
  public void setUp() {
    super.setUp();
    endpoint = mock(KeypleClientAsync.class);
    node = new KeypleClientAsyncNodeImpl(handler, endpoint, 1);
  }

  @Test
  public void openSession_whenOk_shouldCallEndpointAndReturn() {
    doEndpointToReturnAnswer(true, false, false);
    node.openSession(sessionId);
    verify(endpoint).openSession(sessionId);
    verifyNoMoreInteractions(endpoint);
    verifyZeroInteractions(handler);
  }

  @Test(expected = KeypleTimeoutException.class)
  public void openSession_whenTimeout_shouldThrowKeypleTimeoutException() {
    doEndpointToReturnAnswer(false, false, true);
    node.openSession(sessionId);
  }

  @Test(expected = RuntimeException.class)
  public void openSession_whenEndpointError_shouldThrowEndpointError() {
    doEndpointToReturnAnswer(false, false, true);
    doEndpointToThrowException(true, false, false);
    node.openSession(sessionId);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onOpen_whenSessionIdIsNull_shouldThrowIAE() {
    node.onOpen(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onOpen_whenSessionIdIsEmpty_shouldThrowIAE() {
    node.onOpen("");
  }

  @Test
  public void onOpen_whenSessionIdIsUnknown_shouldDoNothing() {
    node.onOpen(sessionIdUnknown);
  }

  @Test(expected = IllegalStateException.class)
  public void onOpen_whenBadUse_shouldThrowISE() {
    doEndpointToReturnAnswer(true, false, false);
    openSessionInSafeMode();
    node.onOpen(sessionId);
  }

  @Test
  public void onOpen_whenOkInThread1_shouldEndOpenSession() {
    doEndpointToReturnAnswer(true, false, false);
    node.openSession(sessionId);
    verify(endpoint).openSession(sessionId);
    verifyNoMoreInteractions(endpoint);
    verifyZeroInteractions(handler);
  }

  @Test
  public void onOpen_whenOkInThread2_shouldEndOpenSessionOnThread1() {
    scheduleOnOpen();
    node.openSession(sessionId);
    verify(endpoint).openSession(sessionId);
    verifyNoMoreInteractions(endpoint);
    verifyZeroInteractions(handler);
  }

  @Test
  public void sendRequest_whenOk_shouldCallEndpointAndReturnResponse() {
    doEndpointToReturnAnswer(true, true, false);
    openSessionInSafeMode();
    KeypleMessageDto result = node.sendRequest(msg);
    verify(endpoint).openSession(sessionId);
    verify(endpoint).sendMessage(msg);
    verifyNoMoreInteractions(endpoint);
    verifyZeroInteractions(handler);
    assertThat(result).isSameAs(response);
    assertThat(result).isEqualToComparingFieldByField(response);
  }

  @Test(expected = KeypleTimeoutException.class)
  public void sendRequest_whenTimeout_shouldThrowKeypleTimeoutException() {
    doEndpointToReturnAnswer(true, false, true);
    openSessionInSafeMode();
    node.sendRequest(msg);
  }

  @Test(expected = RuntimeException.class)
  public void sendRequest_whenEndpointError_shouldThrowEndpointError() {
    doEndpointToReturnAnswer(true, false, true);
    doEndpointToThrowException(false, true, false);
    openSessionInSafeMode();
    node.sendRequest(msg);
  }

  @Test
  public void sendMessage_whenOk_shouldCallEndpointAndReturn() {
    doEndpointToReturnAnswer(true, false, false);
    openSessionInSafeMode();
    node.sendMessage(msg);
    verify(endpoint).openSession(sessionId);
    verify(endpoint).sendMessage(msg);
    verifyNoMoreInteractions(endpoint);
    verifyZeroInteractions(handler);
  }

  @Test(expected = RuntimeException.class)
  public void sendMessage_whenEndpointError_shouldThrowEndpointError() {
    doEndpointToReturnAnswer(true, false, true);
    doEndpointToThrowException(false, true, false);
    openSessionInSafeMode();
    node.sendMessage(msg);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onMessage_whenMessageIsNull_shouldThrowIAE() {
    node.onMessage(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onMessage_whenSessionIdIsNull_shouldThrowIAE() {
    KeypleMessageDto message = new KeypleMessageDto(response).setSessionId(null);
    node.onMessage(message);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onMessage_whenSessionIdIsEmpty_shouldThrowIAE() {
    KeypleMessageDto message = new KeypleMessageDto(response).setSessionId("");
    node.onMessage(message);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onMessage_whenActionIsNull_shouldThrowIAE() {
    KeypleMessageDto message = new KeypleMessageDto(response).setAction(null);
    node.onMessage(message);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onMessage_whenActionIsEmpty_shouldThrowIAE() {
    KeypleMessageDto message = new KeypleMessageDto(response).setAction("");
    node.onMessage(message);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onMessage_whenClientNodeIdIsNull_shouldThrowIAE() {
    KeypleMessageDto message = new KeypleMessageDto(response).setClientNodeId(null);
    node.onMessage(message);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onMessage_whenClientNodeIdIsEmpty_shouldThrowIAE() {
    KeypleMessageDto message = new KeypleMessageDto(response).setClientNodeId("");
    node.onMessage(message);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onMessage_whenServerNodeIdIsNull_shouldThrowIAE() {
    KeypleMessageDto message = new KeypleMessageDto(response).setServerNodeId(null);
    node.onMessage(message);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onMessage_whenServerNodeIdIsEmpty_shouldThrowIAE() {
    KeypleMessageDto message = new KeypleMessageDto(response).setServerNodeId("");
    node.onMessage(message);
  }

  @Test
  public void onMessage_whenSessionIdIsUnknown_shouldDoNothing() {
    KeypleMessageDto message = new KeypleMessageDto(response).setSessionId(sessionIdUnknown);
    node.onMessage(message);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onMessage_whenActionIsUnknown_shouldThrowIAE() {
    doEndpointToReturnAnswer(true, false, false);
    openSessionInSafeMode();
    KeypleMessageDto message = new KeypleMessageDto(response).setAction("UNKNOWN");
    node.onMessage(message);
  }

  @Test(expected = IllegalStateException.class)
  public void onMessage_whenBadUse_shouldThrowISE() {
    doEndpointToReturnAnswer(true, false, false);
    openSessionInSafeMode();
    node.onMessage(response);
  }

  @Test
  public void onMessage_whenActionIsPluginEvent_shouldCallHandler() {
    doEndpointToReturnAnswer(true, false, false);
    openSessionInSafeMode();
    node.onMessage(pluginEvent);
    verify(handler).onMessage(pluginEvent);
    verifyNoMoreInteractions(handler);
  }

  @Test(expected = RuntimeException.class)
  public void onMessage_whenActionIsPluginEventAndHandlerError_shouldThrowHandlerError() {
    doEndpointToReturnAnswer(true, false, false);
    setHandlerError();
    openSessionInSafeMode();
    node.onMessage(pluginEvent);
  }

  @Test
  public void onMessage_whenActionIsReaderEvent_shouldCallHandler() {
    doEndpointToReturnAnswer(true, false, false);
    openSessionInSafeMode();
    node.onMessage(readerEvent);
    verify(handler).onMessage(readerEvent);
    verifyNoMoreInteractions(handler);
  }

  @Test(expected = RuntimeException.class)
  public void onMessage_whenActionIsReaderEventAndHandlerError_shouldThrowHandlerError() {
    doEndpointToReturnAnswer(true, false, false);
    setHandlerError();
    openSessionInSafeMode();
    node.onMessage(readerEvent);
  }

  @Test
  public void onMessage_whenOkInThread1_shouldEndSendRequest() {
    doEndpointToReturnAnswer(true, true, false);
    openSessionInSafeMode();
    KeypleMessageDto result = node.sendRequest(msg);
    verify(endpoint).openSession(sessionId);
    verify(endpoint).sendMessage(msg);
    verifyNoMoreInteractions(endpoint);
    verifyZeroInteractions(handler);
    assertThat(result).isSameAs(response);
    assertThat(result).isEqualToComparingFieldByField(response);
  }

  @Test
  public void onMessage_whenOkInThread2_shouldEndSendRequestOnThread1() {
    doEndpointToReturnAnswer(true, false, false);
    openSessionInSafeMode();
    scheduleOnMessage(response);
    KeypleMessageDto result = node.sendRequest(msg);
    verify(endpoint).openSession(sessionId);
    verify(endpoint).sendMessage(msg);
    verifyNoMoreInteractions(endpoint);
    verifyZeroInteractions(handler);
    assertThat(result).isSameAs(response);
    assertThat(result).isEqualToComparingFieldByField(response);
  }

  @Test
  public void closeSession_whenOk_shouldCallEndpointAndReturn() {
    doEndpointToReturnAnswer(true, true, true);
    openSessionInSafeMode();
    node.closeSession(sessionId);
    verify(endpoint).openSession(sessionId);
    verify(endpoint).closeSession(sessionId);
    verifyNoMoreInteractions(endpoint);
    verifyZeroInteractions(handler);
  }

  @Test(expected = KeypleTimeoutException.class)
  public void closeSession_whenTimeout_shouldThrowKeypleTimeoutException() {
    doEndpointToReturnAnswer(true, true, false);
    openSessionInSafeMode();
    node.closeSession(sessionId);
  }

  @Test(expected = RuntimeException.class)
  public void closeSession_whenEndpointError_shouldThrowEndpointError() {
    doEndpointToReturnAnswer(true, true, false);
    doEndpointToThrowException(false, false, true);
    openSessionInSafeMode();
    node.closeSession(sessionId);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onClose_whenSessionIdIsNull_shouldThrowIAE() {
    node.onClose(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onClose_whenSessionIdIsEmpty_shouldThrowIAE() {
    node.onClose("");
  }

  @Test
  public void onClose_whenSessionIdIsUnknown_shouldDoNothing() {
    node.onClose(sessionIdUnknown);
  }

  @Test(expected = IllegalStateException.class)
  public void onClose_whenBadUse_shouldThrowISE() {
    doEndpointToReturnAnswer(true, true, true);
    openSessionInSafeMode();
    node.onClose(sessionId);
  }

  @Test
  public void onClose_whenOkInThread1_shouldEndCloseSession() {
    doEndpointToReturnAnswer(true, true, true);
    openSessionInSafeMode();
    node.closeSession(sessionId);
    verify(endpoint).openSession(sessionId);
    verify(endpoint).closeSession(sessionId);
    verifyNoMoreInteractions(endpoint);
    verifyZeroInteractions(handler);
  }

  @Test
  public void onClose_whenOkInThread2_shouldEndCloseSessionOnThread1() {
    doEndpointToReturnAnswer(true, true, false);
    openSessionInSafeMode();
    scheduleOnClose();
    node.closeSession(sessionId);
    verify(endpoint).openSession(sessionId);
    verify(endpoint).closeSession(sessionId);
    verifyNoMoreInteractions(endpoint);
    verifyZeroInteractions(handler);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onError_whenSessionIdIsNull_shouldThrowIAE() {
    node.onError(null, error);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onError_whenSessionIdIsEmpty_shouldThrowIAE() {
    node.onError("", error);
  }

  @Test(expected = IllegalArgumentException.class)
  public void onError_whenErrorIsNull_shouldThrowIAE() {
    node.onError(sessionId, null);
  }

  @Test
  public void onError_whenSessionIdIsUnknown_shouldDoNothing() {
    node.onError(sessionIdUnknown, error);
  }

  @Test
  public void
      onError_whenOccursDuringOpenSessionInThread1_shouldEndOpenSessionWithErrorInsideARuntimeException() {
    doEndpointToReturnAnswer(false, false, true);
    doEndpointToCallOnError(true, false, false);
    try {
      node.openSession(sessionId);
      shouldHaveThrown(RuntimeException.class);
    } catch (RuntimeException e) {
      assertThat(e).hasCause(error);
    }
  }

  @Test
  public void
      onError_whenOccursDuringOpenSessionInThread2_shouldEndOpenSessionWithErrorInsideARuntimeException() {
    doEndpointToReturnAnswer(false, false, true);
    scheduleOnError();
    try {
      node.openSession(sessionId);
      shouldHaveThrown(RuntimeException.class);
    } catch (RuntimeException e) {
      assertThat(e).hasCause(error);
    }
  }

  @Test
  public void
      onError_whenOccursDuringSendRequestInThread1_shouldEndSendRequestWithErrorInsideARuntimeException() {
    doEndpointToReturnAnswer(true, false, true);
    doEndpointToCallOnError(false, true, false);
    openSessionInSafeMode();
    try {
      node.sendRequest(msg);
      shouldHaveThrown(RuntimeException.class);
    } catch (RuntimeException e) {
      assertThat(e).hasCause(error);
    }
  }

  @Test
  public void
      onError_whenOccursDuringSendRequestInThread2_shouldEndSendRequestWithErrorInsideARuntimeException() {
    doEndpointToReturnAnswer(true, false, true);
    openSessionInSafeMode();
    scheduleOnError();
    try {
      node.sendRequest(msg);
      shouldHaveThrown(RuntimeException.class);
    } catch (RuntimeException e) {
      assertThat(e).hasCause(error);
    }
  }

  @Test
  public void
      onError_whenOccursDuringSendMessageInThread1_shouldEndSendMessageWithErrorInsideARuntimeException() {
    doEndpointToReturnAnswer(true, false, true);
    doEndpointToCallOnError(false, true, false);
    openSessionInSafeMode();
    try {
      node.sendMessage(msg);
      shouldHaveThrown(RuntimeException.class);
    } catch (RuntimeException e) {
      assertThat(e).hasCause(error);
    }
  }

  @Test
  public void
      onError_whenOccursDuringCloseSessionInThread1_shouldEndCloseSessionWithErrorInsideARuntimeException() {
    doEndpointToReturnAnswer(true, false, false);
    doEndpointToCallOnError(false, false, true);
    openSessionInSafeMode();
    try {
      node.closeSession(sessionId);
      shouldHaveThrown(RuntimeException.class);
    } catch (RuntimeException e) {
      assertThat(e).hasCause(error);
    }
    try {
      node.closeSession(sessionId);
      shouldHaveThrown(NullPointerException.class);
    } catch (NullPointerException e) {
    }
  }

  @Test
  public void
      onError_whenOccursDuringCloseSessionInThread2_shouldEndCloseSessionWithErrorInsideARuntimeException() {
    doEndpointToReturnAnswer(true, false, false);
    openSessionInSafeMode();
    scheduleOnError();
    try {
      node.closeSession(sessionId);
      shouldHaveThrown(RuntimeException.class);
    } catch (RuntimeException e) {
      assertThat(e).hasCause(error);
    }
    try {
      node.closeSession(sessionId);
      shouldHaveThrown(NullPointerException.class);
    } catch (NullPointerException e) {
    }
  }
}
