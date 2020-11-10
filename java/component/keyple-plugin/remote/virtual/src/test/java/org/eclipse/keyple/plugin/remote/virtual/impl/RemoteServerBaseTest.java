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
package org.eclipse.keyple.plugin.remote.virtual.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.keyple.plugin.remote.virtual.impl.SampleFactory.getDefaultSelectionsResponse;

import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservablePlugin;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.PluginEvent;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remote.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remote.core.impl.AbstractKeypleNode;
import org.eclipse.keyple.plugin.remote.virtual.RemoteServerObservableReader;
import org.eclipse.keyple.plugin.remote.virtual.RemoteServerReader;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class RemoteServerBaseTest {

  static String clientId = "client1";
  static String nativeReaderName = "nativeReaderName1";
  static String remotePluginName = RemoteServerPluginFactory.DEFAULT_PLUGIN_NAME;
  static String nativePluginName = "nativePluginName1";
  static String serviceId = "1";
  static RemoteServerPluginImplTest.MockUserOutputData userOutputData =
      new RemoteServerPluginImplTest.MockUserOutputData();
  static ExecutorService eventNotificationPool = Executors.newCachedThreadPool();

  RemoteServerPluginImpl remotePlugin;
  AbstractKeypleNode node;
  RemoteServerPluginImplTest.MockPluginObserver pluginObserver;
  RemoteServerPluginImplTest.MockReaderObserver readerObserver;
  ArgumentCaptor<KeypleMessageDto> messageArgumentCaptor;

  /*
   * Private helpers
   */

  class MockReaderObserver implements ObservableReader.ReaderObserver {
    ReaderEvent event;
    Set<String> virtualReaderNames = new HashSet<String>();

    @Override
    public void update(ReaderEvent event) {
      if (virtualReaderNames.add(event.getReaderName())) {
        this.event = event;
      }
      ; // verify that each event targets a new virtual reader
    }

    public void terminateService(Object userOutputData) {
      remotePlugin.terminateService(event.getReaderName(), userOutputData);
    }
  }

  class MockPluginObserver implements ObservablePlugin.PluginObserver {
    PluginEvent event;
    Boolean attachObserver;

    MockPluginObserver(Boolean attachReaderObserver) {
      this.attachObserver = attachReaderObserver;
    }

    @Override
    public void update(PluginEvent event) {
      this.event = event;
      // attach an observer to the VirtualReader
      RemoteServerReader virtualReader = remotePlugin.getReader(event.getReaderNames().first());
      if (virtualReader instanceof RemoteServerObservableReader && attachObserver) {
        ((RemoteServerObservableReader) virtualReader).addObserver(readerObserver);
      }
    }

    public void terminateService(Object userOutputData) {
      remotePlugin.terminateService(event.getReaderNames().first(), userOutputData);
    }
  }

  KeypleMessageDto executeRemoteServiceMessage(String sessionId, boolean isObservable) {
    JsonObject body = new JsonObject();
    body.addProperty("serviceId", serviceId);
    body.addProperty("initialCardContent", "");
    body.addProperty("userInputData", "anyObject");
    body.addProperty("isObservable", isObservable);

    return new KeypleMessageDto()
        .setSessionId(sessionId)
        .setAction(KeypleMessageDto.Action.EXECUTE_REMOTE_SERVICE.name())
        .setClientNodeId(clientId)
        .setNativeReaderName(nativeReaderName)
        .setBody(body.toString());
  }

  Callable<Boolean> validReaderConnectEvent() {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {

        return PluginEvent.EventType.READER_CONNECTED.compareTo(pluginObserver.event.getEventType())
                == 0
            && remotePluginName.equals(pluginObserver.event.getPluginName())
            && remotePlugin.getReader(pluginObserver.event.getReaderNames().first()) != null;
      }
    };
  }

  KeypleMessageDto readerEventMessage(String sessionId, String virtualReaderName) {
    JsonObject body = new JsonObject();
    body.addProperty("userInputData", "anyObject");
    body.add(
        "readerEvent",
        KeypleJsonParser.getParser()
            .toJsonTree(
                new ReaderEvent(
                    nativePluginName,
                    nativeReaderName,
                    ReaderEvent.EventType.CARD_INSERTED,
                    getDefaultSelectionsResponse())));

    return new KeypleMessageDto()
        .setSessionId(sessionId)
        .setAction(KeypleMessageDto.Action.READER_EVENT.name())
        .setClientNodeId(clientId)
        .setNativeReaderName(nativeReaderName)
        .setVirtualReaderName(virtualReaderName)
        .setBody(body.toString());
  }

  Callable<Boolean> validSeInsertedEvent(final String virtualReaderName, final int messageNumber) {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return ReaderEvent.EventType.CARD_INSERTED.compareTo(readerObserver.event.getEventType())
                == 0
            && remotePluginName.equals(pluginObserver.event.getPluginName())
            && !readerObserver.event.getReaderName().equals(virtualReaderName)
            && readerObserver.virtualReaderNames.size()
                == messageNumber; // event is targeted to the sessionReader
      }
    };
  }

  static class MockUserOutputData {
    String data = "data";
  }

  Answer aVoid() {
    return new Answer() {
      @Override
      public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
        return null;
      }
    };
  }

  void validateTerminateServiceResponse(
      KeypleMessageDto terminateServiceMsg, boolean shouldUnregister) {

    assertThat(terminateServiceMsg.getAction())
        .isEqualTo(KeypleMessageDto.Action.TERMINATE_SERVICE.name());
    JsonObject body =
        KeypleJsonParser.getParser().fromJson(terminateServiceMsg.getBody(), JsonObject.class);
    MockUserOutputData userOutputResponse =
        KeypleJsonParser.getParser()
            .fromJson(body.get("userOutputData").getAsString(), MockUserOutputData.class);
    boolean unregisterVirtualReader = body.get("unregisterVirtualReader").getAsBoolean();
    assertThat(userOutputData).isEqualToComparingFieldByFieldRecursively(userOutputResponse);
    assertThat(unregisterVirtualReader).isEqualTo(shouldUnregister);
  }

  void registerSyncPlugin() {
    SmartCardService.getInstance()
        .registerPlugin(
            RemoteServerPluginFactory.builder()
                .withSyncNode()
                .withPluginObserver(pluginObserver)
                .usingDefaultEventNotificationPool()
                .build());
    remotePlugin =
        (RemoteServerPluginImpl) SmartCardService.getInstance().getPlugin(remotePluginName);
  }

  void unregisterPlugin() {
    SmartCardService.getInstance().unregisterPlugin(remotePluginName);
  }
}
