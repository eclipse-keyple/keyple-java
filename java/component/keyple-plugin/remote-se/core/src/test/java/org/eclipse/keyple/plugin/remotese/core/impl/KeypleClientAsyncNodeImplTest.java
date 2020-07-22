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
import org.eclipse.keyple.plugin.remotese.core.KeypleClientAsync;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.core.exception.KeypleTimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class KeypleClientAsyncNodeImplTest extends AbstractKeypleAsyncNode {

    static final String sessionId = "sessionId";
    static final String sessionIdUnknown = "sessionIdUnknown";
    static final Exception error = new Exception();

    KeypleClientAsync endpoint;
    KeypleClientAsyncNodeImpl node;

    class MessageScheduler extends Thread {

        public boolean isError;
        private Thread ownerThread;
        private String sessionId;
        private KeypleMessageDto msg;
        private int mode;

        MessageScheduler(final String sessionId, final KeypleMessageDto msg, final int mode) {
            this.ownerThread = Thread.currentThread();
            this.sessionId = sessionId;
            this.msg = msg;
            this.mode = mode;
        }

        @Override
        public void run() {
            await().atMost(5, TimeUnit.SECONDS).until(threadHasStateTimedWaiting(ownerThread));
            try {
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
            } catch (Exception e) {
                isError = true;
            }
        }
    }

    MessageScheduler scheduleOnOpen(String sessionId) {
        MessageScheduler t = new MessageScheduler(sessionId, null, 1);
        t.start();
        return t;
    }

    MessageScheduler scheduleOnMessage(String sessionId, KeypleMessageDto message) {
        MessageScheduler t = new MessageScheduler(sessionId, message, 2);
        t.start();
        return t;
    }

    MessageScheduler scheduleOnClose(String sessionId) {
        MessageScheduler t = new MessageScheduler(sessionId, null, 3);
        t.start();
        return t;
    }

    MessageScheduler scheduleOnError(String sessionId) {
        MessageScheduler t = new MessageScheduler(sessionId, null, 4);
        t.start();
        return t;
    }

    void openSessionSuccessfully(String sessionId) {
        try {
            node.openSession(sessionId);
        } catch (Exception e) {
            fail("Exception should not have thrown here");
        }
    }

    void setEndpointAnswer(boolean whenOpenSession, boolean whenSendMessage,
            boolean whenCloseSession) {
        if (whenOpenSession) {
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    node.onOpen(sessionId);
                    return null;
                }
            }).when(endpoint).openSession(sessionId);
        }
        if (whenSendMessage) {
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    node.onMessage(response);
                    return null;
                }
            }).when(endpoint).sendMessage(msg);
        }
        if (whenCloseSession) {
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    node.onClose(sessionId);
                    return null;
                }
            }).when(endpoint).closeSession(sessionId);
        }
    }

    void setEndpointErrorAnswer(boolean whenOpenSession, boolean whenSendMessage,
            boolean whenCloseSession) {
        if (whenOpenSession) {
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    node.onError(sessionId, error);
                    return null;
                }
            }).when(endpoint).openSession(sessionId);
        }
        if (whenSendMessage) {
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    node.onError(sessionId, error);
                    return null;
                }
            }).when(endpoint).sendMessage(msg);
        }
        if (whenCloseSession) {
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    node.onError(sessionId, error);
                    return null;
                }
            }).when(endpoint).closeSession(sessionId);
        }
    }

    void setEndpointError(boolean whenOpenSession, boolean whenSendMessage,
            boolean whenCloseSession) {
        if (whenOpenSession) {
            doThrow(new RuntimeException()).when(endpoint).openSession(sessionId);
        }
        if (whenSendMessage) {
            doThrow(new RuntimeException()).when(endpoint).sendMessage(msg);
        }
        if (whenCloseSession) {
            doThrow(new RuntimeException()).when(endpoint).closeSession(sessionId);
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
        setEndpointAnswer(true, false, false);
        node.openSession(sessionId);
        verify(endpoint).openSession(sessionId);
        verifyNoMoreInteractions(endpoint);
        verifyZeroInteractions(handler);
    }

    @Test(expected = KeypleTimeoutException.class)
    public void openSession_whenTimeout_shouldThrowKeypleTimeoutException() {
        setEndpointAnswer(false, false, true);
        node.openSession(sessionId);
    }

    @Test(expected = RuntimeException.class)
    public void openSession_whenEndpointError_shouldThrowEndpointError() {
        setEndpointAnswer(false, false, true);
        setEndpointError(true, false, false);
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
        setEndpointAnswer(true, false, false);
        openSessionSuccessfully(sessionId);
        node.onOpen(sessionId);
    }

    @Test
    public void onOpen_whenOkInThread1_shouldEndOpenSession() {
        setEndpointAnswer(true, false, false);
        node.openSession(sessionId);
        verify(endpoint).openSession(sessionId);
        verifyNoMoreInteractions(endpoint);
        verifyZeroInteractions(handler);
    }

    @Test
    public void onOpen_whenOkInThread2_shouldEndOpenSessionOnThread1() {
        scheduleOnOpen(sessionId);
        node.openSession(sessionId);
        verify(endpoint).openSession(sessionId);
        verifyNoMoreInteractions(endpoint);
        verifyZeroInteractions(handler);
    }

    @Test
    public void sendRequest_whenOk_shouldCallEndpointAndReturnResponse() {
        setEndpointAnswer(true, true, false);
        openSessionSuccessfully(sessionId);
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
        setEndpointAnswer(true, false, true);
        openSessionSuccessfully(sessionId);
        node.sendRequest(msg);
    }

    @Test(expected = RuntimeException.class)
    public void sendRequest_whenEndpointError_shouldThrowEndpointError() {
        setEndpointAnswer(true, false, true);
        setEndpointError(false, true, false);
        openSessionSuccessfully(sessionId);
        node.sendRequest(msg);
    }

    @Test
    public void sendMessage_whenOk_shouldCallEndpointAndReturn() {
        setEndpointAnswer(true, false, false);
        openSessionSuccessfully(sessionId);
        node.sendMessage(msg);
        verify(endpoint).openSession(sessionId);
        verify(endpoint).sendMessage(msg);
        verifyNoMoreInteractions(endpoint);
        verifyZeroInteractions(handler);
    }

    @Test(expected = RuntimeException.class)
    public void sendMessage_whenEndpointError_shouldThrowEndpointError() {
        setEndpointAnswer(true, false, true);
        setEndpointError(false, true, false);
        openSessionSuccessfully(sessionId);
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
        setEndpointAnswer(true, false, false);
        openSessionSuccessfully(sessionId);
        KeypleMessageDto message = new KeypleMessageDto(response).setAction("UNKNOWN");
        node.onMessage(message);
    }

    @Test(expected = IllegalStateException.class)
    public void onMessage_whenBadUse_shouldThrowISE() {
        setEndpointAnswer(true, false, false);
        openSessionSuccessfully(sessionId);
        node.onMessage(response);
    }

    @Test
    public void onMessage_whenActionIsPluginEvent_shouldCallHandler() {
        setEndpointAnswer(true, false, false);
        openSessionSuccessfully(sessionId);
        node.onMessage(pluginEvent);
        verify(handler).onMessage(pluginEvent);
        verifyNoMoreInteractions(handler);
    }

    @Test(expected = RuntimeException.class)
    public void onMessage_whenActionIsPluginEventAndHandlerError_shouldThrowHandlerError() {
        setEndpointAnswer(true, false, false);
        setHandlerError();
        openSessionSuccessfully(sessionId);
        node.onMessage(pluginEvent);
    }

    @Test
    public void onMessage_whenActionIsReaderEvent_shouldCallHandler() {
        setEndpointAnswer(true, false, false);
        openSessionSuccessfully(sessionId);
        node.onMessage(readerEvent);
        verify(handler).onMessage(readerEvent);
        verifyNoMoreInteractions(handler);
    }

    @Test(expected = RuntimeException.class)
    public void onMessage_whenActionIsReaderEventAndHandlerError_shouldThrowHandlerError() {
        setEndpointAnswer(true, false, false);
        setHandlerError();
        openSessionSuccessfully(sessionId);
        node.onMessage(readerEvent);
    }

    @Test
    public void onMessage_whenOkInThread1_shouldEndSendRequest() {
        setEndpointAnswer(true, true, false);
        openSessionSuccessfully(sessionId);
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
        setEndpointAnswer(true, false, false);
        openSessionSuccessfully(sessionId);
        scheduleOnMessage(sessionId, response);
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
        setEndpointAnswer(true, true, true);
        openSessionSuccessfully(sessionId);
        node.closeSession(sessionId);
        verify(endpoint).openSession(sessionId);
        verify(endpoint).closeSession(sessionId);
        verifyNoMoreInteractions(endpoint);
        verifyZeroInteractions(handler);
    }

    @Test(expected = KeypleTimeoutException.class)
    public void closeSession_whenTimeout_shouldThrowKeypleTimeoutException() {
        setEndpointAnswer(true, true, false);
        openSessionSuccessfully(sessionId);
        node.closeSession(sessionId);
    }

    @Test(expected = RuntimeException.class)
    public void closeSession_whenEndpointError_shouldThrowEndpointError() {
        setEndpointAnswer(true, true, false);
        setEndpointError(false, false, true);
        openSessionSuccessfully(sessionId);
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
        setEndpointAnswer(true, true, true);
        openSessionSuccessfully(sessionId);
        node.onClose(sessionId);
    }

    @Test
    public void onClose_whenOkInThread1_shouldEndCloseSession() {
        setEndpointAnswer(true, true, true);
        openSessionSuccessfully(sessionId);
        node.closeSession(sessionId);
        verify(endpoint).openSession(sessionId);
        verify(endpoint).closeSession(sessionId);
        verifyNoMoreInteractions(endpoint);
        verifyZeroInteractions(handler);
    }

    @Test
    public void onClose_whenOkInThread2_shouldEndCloseSessionOnThread1() {
        setEndpointAnswer(true, true, false);
        openSessionSuccessfully(sessionId);
        scheduleOnClose(sessionId);
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
    public void onError_whenOccursDuringOpenSessionInThread1_shouldEndOpenSessionWithErrorInsideARuntimeException() {
        setEndpointAnswer(false, false, true);
        setEndpointErrorAnswer(true, false, false);
        try {
            node.openSession(sessionId);
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
    public void onError_whenOccursDuringOpenSessionInThread2_shouldEndOpenSessionWithErrorInsideARuntimeException() {
        setEndpointAnswer(false, false, true);
        scheduleOnError(sessionId);
        try {
            node.openSession(sessionId);
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
    public void onError_whenOccursDuringSendRequestInThread1_shouldEndSendRequestWithErrorInsideARuntimeException() {
        setEndpointAnswer(true, false, true);
        setEndpointErrorAnswer(false, true, false);
        openSessionSuccessfully(sessionId);
        try {
            node.sendRequest(msg);
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
    public void onError_whenOccursDuringSendRequestInThread2_shouldEndSendRequestWithErrorInsideARuntimeException() {
        setEndpointAnswer(true, false, true);
        openSessionSuccessfully(sessionId);
        scheduleOnError(sessionId);
        try {
            node.sendRequest(msg);
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
    public void onError_whenOccursDuringSendMessageInThread1_shouldEndSendMessageWithErrorInsideARuntimeException() {
        setEndpointAnswer(true, false, true);
        setEndpointErrorAnswer(false, true, false);
        openSessionSuccessfully(sessionId);
        try {
            node.sendMessage(msg);
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
    public void onError_whenOccursDuringCloseSessionInThread1_shouldEndCloseSessionWithErrorInsideARuntimeException() {
        setEndpointAnswer(true, false, false);
        setEndpointErrorAnswer(false, false, true);
        openSessionSuccessfully(sessionId);
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
    public void onError_whenOccursDuringCloseSessionInThread2_shouldEndCloseSessionWithErrorInsideARuntimeException() {
        setEndpointAnswer(true, false, false);
        openSessionSuccessfully(sessionId);
        scheduleOnError(sessionId);
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
