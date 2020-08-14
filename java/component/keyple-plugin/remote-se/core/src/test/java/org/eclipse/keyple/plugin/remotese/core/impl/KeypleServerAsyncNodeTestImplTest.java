/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.core.impl;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.core.KeypleServerAsync;
import org.eclipse.keyple.plugin.remotese.core.exception.KeypleClosedSessionException;
import org.eclipse.keyple.plugin.remotese.core.exception.KeypleRemoteCommunicationException;
import org.eclipse.keyple.plugin.remotese.core.exception.KeypleTimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class KeypleServerAsyncNodeTestImplTest extends AbstractKeypleAsyncNodeTest {

    KeypleServerAsync endpoint;
    KeypleServerAsyncNodeImpl node;

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
                case 2:
                    node.onMessage(msg);
                    break;
                case 4:
                    node.onError(sessionId, error);
                    break;
            }
        }
    }

    void scheduleOnMessage(KeypleMessageDto message) {
        MessageScheduler t = new MessageScheduler(sessionId, message, 2);
        t.start();
    }

    void scheduleOnError() {
        MessageScheduler t = new MessageScheduler(sessionId, null, 4);
        t.start();
    }

    void doEndpointToReturnAnswer() {
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                node.onMessage(response);
                return null;
            }
        }).when(endpoint).sendMessage(msg);
    }

    void doEndpointToCallOnError() {
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                node.onError(sessionId, error);
                return null;
            }
        }).when(endpoint).sendMessage(msg);
    }

    void doEndpointToThrowException() {
        doThrow(new KeypleRemoteCommunicationException("TEST")).when(endpoint).sendMessage(msg);
    }

    void initSession() {
        node.onMessage(msg);
    }

    @Before
    public void setUp() {
        super.setUp();
        endpoint = mock(KeypleServerAsync.class);
        node = new KeypleServerAsyncNodeImpl(handler, endpoint, 1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void openSession_shouldThrowUOE() {
        node.openSession(sessionId);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void closeSession_shouldThrowUOE() {
        node.closeSession(sessionId);
    }

    @Test
    public void sendRequest_whenOk_shouldCallEndpointAndReturnResponse() {
        doEndpointToReturnAnswer();
        initSession();
        KeypleMessageDto result = node.sendRequest(msg);
        verify(endpoint).sendMessage(msg);
        verifyNoMoreInteractions(endpoint);
        assertThat(result).isSameAs(response);
        assertThat(result).isEqualToComparingFieldByField(response);
    }

    @Test(expected = KeypleTimeoutException.class)
    public void sendRequest_whenTimeout_shouldThrowKeypleTimeoutException() {
        initSession();
        node.sendRequest(msg);
    }

    @Test(expected = RuntimeException.class)
    public void sendRequest_whenEndpointError_shouldThrowEndpointError() {
        doEndpointToThrowException();
        initSession();
        node.sendRequest(msg);
    }

    @Test(expected = KeypleClosedSessionException.class)
    public void sendRequest_whenSessionIdIsUnknown_shouldThrowKCSE() {
        node.sendRequest(msg);
    }

    @Test
    public void sendMessage_whenOk_shouldCallEndpointAndReturn() {
        initSession();
        node.sendMessage(msg);
        verify(endpoint).sendMessage(msg);
        verifyNoMoreInteractions(endpoint);
    }

    @Test(expected = RuntimeException.class)
    public void sendMessage_whenEndpointError_shouldThrowEndpointError() {
        doEndpointToThrowException();
        initSession();
        node.sendMessage(msg);
    }

    @Test(expected = KeypleClosedSessionException.class)
    public void sendMessage_whenSessionIdIsUnknown_shouldThrowKCSE() {
        node.sendMessage(msg);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onMessage_whenMessageIsNull_shouldThrowIAE() {
        initSession();
        node.onMessage(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onMessage_whenSessionIdIsNull_shouldThrowIAE() {
        initSession();
        KeypleMessageDto message = new KeypleMessageDto(msg).setSessionId(null);
        node.onMessage(message);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onMessage_whenSessionIdIsEmpty_shouldThrowIAE() {
        initSession();
        KeypleMessageDto message = new KeypleMessageDto(msg).setSessionId("");
        node.onMessage(message);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onMessage_whenActionIsNull_shouldThrowIAE() {
        initSession();
        KeypleMessageDto message = new KeypleMessageDto(msg).setAction(null);
        node.onMessage(message);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onMessage_whenActionIsEmpty_shouldThrowIAE() {
        initSession();
        KeypleMessageDto message = new KeypleMessageDto(msg).setAction("");
        node.onMessage(message);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onMessage_whenClientNodeIdIsNull_shouldThrowIAE() {
        initSession();
        KeypleMessageDto message = new KeypleMessageDto(msg).setClientNodeId(null);
        node.onMessage(message);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onMessage_whenClientNodeIdIsEmpty_shouldThrowIAE() {
        initSession();
        KeypleMessageDto message = new KeypleMessageDto(msg).setClientNodeId("");
        node.onMessage(message);
    }

    @Test
    public void onMessage_whenSessionIdIsUnknown_shouldInitSessionAndCallHandler() {
        KeypleMessageDto message = new KeypleMessageDto(msg).setSessionId(sessionIdUnknown);
        node.onMessage(message);
        verify(handler).onMessage(message);
        verifyNoMoreInteractions(handler);
    }

    @Test(expected = IllegalStateException.class)
    public void onMessage_whenBadUse_shouldThrowISE() {
        initSession();
        node.onError(msg.getSessionId(), new Exception());
        node.onMessage(response);
    }

    @Test
    public void onMessage_whenIsAResponseInThread1_shouldEndSendRequest() {
        doEndpointToReturnAnswer();
        initSession();
        KeypleMessageDto result = node.sendRequest(msg);
        verify(endpoint).sendMessage(msg);
        verifyNoMoreInteractions(endpoint);
        assertThat(result).isSameAs(response);
        assertThat(result).isEqualToComparingFieldByField(response);
    }

    @Test
    public void onMessage_whenIsAResponseInThread2_shouldEndSendRequestOnThread1() {
        initSession();
        scheduleOnMessage(response);
        KeypleMessageDto result = node.sendRequest(msg);
        verify(endpoint).sendMessage(msg);
        verifyNoMoreInteractions(endpoint);
        assertThat(result).isSameAs(response);
        assertThat(result).isEqualToComparingFieldByField(response);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onClose_whenSessionIdIsNull_shouldThrowIAE() {
        initSession();
        node.onClose(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onClose_whenSessionIdIsEmpty_shouldThrowIAE() {
        initSession();
        node.onClose("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void onClose_whenSessionIdIsUnknown_shouldThrowIAE() {
        initSession();
        node.onClose(sessionIdUnknown);
    }

    @Test
    public void onClose_whenOk_shouldEndSession() {
        initSession();
        node.onClose(sessionId);
        try {
            node.onClose(sessionId);
            shouldHaveThrown(IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void onError_whenSessionIdIsNull_shouldThrowIAE() {
        initSession();
        node.onError(null, error);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onError_whenSessionIdIsEmpty_shouldThrowIAE() {
        initSession();
        node.onError("", error);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onError_whenErrorIsNull_shouldThrowIAE() {
        initSession();
        node.onError(sessionId, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onError_whenSessionIdIsUnknown_shouldThrowIAE() {
        initSession();
        node.onError(sessionIdUnknown, error);
    }

    @Test
    public void onError_whenOccursDuringSendRequestInThread1_shouldEndSendRequestWithErrorInsideARuntimeException() {
        doEndpointToCallOnError();
        initSession();
        try {
            node.sendRequest(msg);
            shouldHaveThrown(RuntimeException.class);
        } catch (RuntimeException e) {
            assertThat(e).hasCause(error);
        }
        try {
            node.onMessage(msg);
            shouldHaveThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
        }
    }

    @Test
    public void onError_whenOccursDuringSendRequestInThread2_shouldEndSendRequestWithErrorInsideARuntimeException() {
        initSession();
        scheduleOnError();
        try {
            node.sendRequest(msg);
            shouldHaveThrown(RuntimeException.class);
        } catch (RuntimeException e) {
            assertThat(e).hasCause(error);
        }
        try {
            node.onMessage(msg);
            shouldHaveThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
        }
    }

    @Test
    public void onError_whenOccursDuringSendMessageInThread1_shouldEndSendMessageWithErrorInsideARuntimeException() {
        doEndpointToCallOnError();
        initSession();
        try {
            node.sendMessage(msg);
            shouldHaveThrown(RuntimeException.class);
        } catch (RuntimeException e) {
            assertThat(e).hasCause(error);
        }
        try {
            node.onMessage(msg);
            shouldHaveThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
        }
    }

    @Test
    public void onError_whenOccursDuringSendMessageInThread2_shouldPostponedErrorWithErrorInsideARuntimeException() {
        initSession();
        scheduleOnError();
        node.sendMessage(msg);
        try {
            node.sendRequest(msg);
            shouldHaveThrown(RuntimeException.class);
        } catch (RuntimeException e) {
            assertThat(e).hasCause(error);
        }
        try {
            node.onMessage(msg);
            shouldHaveThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
        }
    }

}