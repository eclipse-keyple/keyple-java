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
package org.eclipse.keyple.plugin.remotese.virtualse.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

import com.google.gson.JsonObject;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.plugin.reader.ObservableReaderNotifier;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.core.impl.AbstractKeypleNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class RemoteSeServerPluginImplTest {

  RemoteSeServerPluginImpl remoteSePlugin;
  AbstractKeypleNode node;
  final MockPluginObserver pluginObserver = new MockPluginObserver();
  String clientId = "client1";
  String nativeReaderName = "nativeReaderName1";
  String pluginName = "pluginName1";
  String nativePluginName = "nativePluginName1";
  MockUserOutputData userOutputData = new MockUserOutputData();
  String serviceId = "1";
  ExecutorService eventNotificationPool = Executors.newCachedThreadPool();

  final ArgumentCaptor<KeypleMessageDto> messageArgumentCaptor =
      ArgumentCaptor.forClass(KeypleMessageDto.class);

  @Before
  public void setUp() {
    remoteSePlugin = Mockito.spy(new RemoteSeServerPluginImpl(pluginName, eventNotificationPool));
    remoteSePlugin.addObserver(pluginObserver);
    node = Mockito.mock(AbstractKeypleNode.class);
  }

  /*
   * Tests
   */

  @Test
  public void onMessage_executeRemoteService_createVirtualReader_shouldRaisePluginEvent() {
    String sessionId = UUID.randomUUID().toString();
    KeypleMessageDto message = executeRemoteServiceMessage(sessionId, false);
    remoteSePlugin.onMessage(message);
    AbstractServerVirtualReader virtualReader =
        (AbstractServerVirtualReader) remoteSePlugin.getReaders().values().iterator().next();
    assertThat(virtualReader).isOfAnyClassIn(ServerVirtualReader.class);
    assertThat(virtualReader).isNotExactlyInstanceOf(ObservableReader.class);
    assertThat(virtualReader.getServiceId()).isEqualTo(serviceId);
    assertThat(virtualReader.getName()).isNotEmpty();
    await().atMost(1, TimeUnit.SECONDS).until(validReaderConnectEvent());
  }

  @Test
  public void
      onMessage_executeRemoteService_createObservableVirtualReader_shouldRaisePluginEvent() {
    String sessionId = UUID.randomUUID().toString();
    KeypleMessageDto message = executeRemoteServiceMessage(sessionId, true);
    remoteSePlugin.onMessage(message);
    ServerVirtualObservableReader virtualReader =
        (ServerVirtualObservableReader) remoteSePlugin.getReaders().values().iterator().next();
    assertThat(virtualReader).isOfAnyClassIn(ServerVirtualObservableReader.class);
    assertThat(virtualReader.getServiceId()).isEqualTo(serviceId);
    assertThat(virtualReader.getName()).isNotEmpty();
    await().atMost(1, TimeUnit.SECONDS).until(validReaderConnectEvent());
  }

  @Test
  public void terminateService_shouldSendOutput_deleteVirtualReader() {
    // create a VirtualReader
    doReturn(node).when(remoteSePlugin).getNode();
    doAnswer(aVoid()).when(node).sendMessage(messageArgumentCaptor.capture());
    String sessionId1 = UUID.randomUUID().toString();
    KeypleMessageDto message = executeRemoteServiceMessage(sessionId1, true);
    remoteSePlugin.onMessage(message);
    await().atMost(1, TimeUnit.SECONDS).until(validReaderConnectEvent());

    // attach an observer to the VirtualReader
    ((ObservableReaderNotifier) remoteSePlugin.getReaders().values().iterator().next())
        .addObserver(new MockReaderObserver());

    // terminate service
    pluginObserver.terminateService(userOutputData);
    KeypleMessageDto terminateServiceMsg = messageArgumentCaptor.getValue();
    assertThat(terminateServiceMsg).isNotNull();
    assertThat(terminateServiceMsg.getAction())
        .isEqualTo(KeypleMessageDto.Action.TERMINATE_SERVICE.name());
    JsonObject body =
        KeypleJsonParser.getParser().fromJson(terminateServiceMsg.getBody(), JsonObject.class);
    MockUserOutputData userOutputResponse =
        KeypleJsonParser.getParser()
            .fromJson(body.get("userOutputData").getAsString(), MockUserOutputData.class);
    Boolean unregisterVirtualReader = body.get("unregisterVirtualReader").getAsBoolean();
    assertThat(userOutputData).isEqualToComparingFieldByFieldRecursively(userOutputResponse);
    assertThat(unregisterVirtualReader).isEqualTo(true); // reader is unregister
  }

  /*
   * Private helpers
   */

  private class MockReaderObserver implements ObservableReader.ReaderObserver {
    @Override
    public void update(ReaderEvent event) {}
  }

  private class MockPluginObserver implements ObservablePlugin.PluginObserver {
    PluginEvent event;

    @Override
    public void update(PluginEvent event) {
      this.event = event;
    }

    public void terminateService(Object userOutputData) {
      remoteSePlugin.terminateService(event.getReaderNames().first(), userOutputData);
    }
  }

  private Callable<Boolean> validReaderConnectEvent() {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {

        return PluginEvent.EventType.READER_CONNECTED.compareTo(pluginObserver.event.getEventType())
                == 0
            && pluginName.equals(pluginObserver.event.getPluginName())
            && remoteSePlugin.getReader(pluginObserver.event.getReaderNames().first()) != null;
      }
    };
  }

  private KeypleMessageDto executeRemoteServiceMessage(String sessionId, boolean isObservable) {
    JsonObject body = new JsonObject();
    body.addProperty("serviceId", serviceId);
    body.addProperty("initialSeContent", "");
    body.addProperty("userInputData", "anyObject");
    body.addProperty("isObservable", isObservable);

    return new KeypleMessageDto()
        .setSessionId(sessionId)
        .setAction(KeypleMessageDto.Action.EXECUTE_REMOTE_SERVICE.name())
        .setClientNodeId(clientId)
        .setNativeReaderName(nativeReaderName)
        .setBody(body.toString());
  }

  private KeypleMessageDto readerEventMessage(String sessionId, String virtualReaderName) {
    JsonObject body = new JsonObject();
    body.addProperty("userInputData", "anyObject");
    body.add(
        "readerEvent",
        KeypleJsonParser.getParser()
            .toJsonTree(
                new ReaderEvent(
                    nativePluginName, nativeReaderName, ReaderEvent.EventType.SE_INSERTED, null)));

    return new KeypleMessageDto()
        .setSessionId(sessionId)
        .setAction(KeypleMessageDto.Action.READER_EVENT.name())
        .setClientNodeId(clientId)
        .setNativeReaderName(nativeReaderName)
        .setVirtualReaderName(virtualReaderName)
        .setBody(body.toString());
  }

  private class MockUserOutputData {
    String data = "data";
  }

  private Answer aVoid() {
    return new Answer() {
      @Override
      public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
        return null;
      }
    };
  }
}
