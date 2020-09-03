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

import com.google.gson.JsonObject;

import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

public class RemoteSeServerPluginImplTest {

  RemoteSeServerPluginImpl remoteSePlugin;
  final MockPluginObserver observer = new MockPluginObserver();
  AbstractVirtualReader virtualReader;
  KeypleMessageDto message;
  String clientId = "client1";
  String nativeReaderName = "nativeReaderName1";
  String pluginName = "pluginName1";
  String serviceId = "1";
  ExecutorService eventNotificationPool = Executors.newCachedThreadPool();

  @Before
  public void setUp(){
    remoteSePlugin = new RemoteSeServerPluginImpl(pluginName,eventNotificationPool);
    remoteSePlugin.addObserver(observer);
  }


  @Test
  public void onMessage_executeRemoteService_createVirtualReader_shouldRaisePluginEvent() {
    message = executeRemoteService(false);
    remoteSePlugin.onMessage(message);
    AbstractServerVirtualReader virtualReader = (AbstractServerVirtualReader) remoteSePlugin.getReaders().values().iterator().next();
    assertThat(virtualReader).isOfAnyClassIn(ServerVirtualReader.class);
    assertThat(virtualReader).isNotExactlyInstanceOf(ObservableReader.class);
    assertThat(virtualReader.getServiceId()).isEqualTo(serviceId);
    assertThat(virtualReader.getName()).isNotEmpty();

    await()
            .atMost(1, TimeUnit.SECONDS)
            .until(
                    new Callable<Boolean>(){

                      @Override
                      public Boolean call() throws Exception {
                        return PluginEvent.EventType.READER_CONNECTED.compareTo(observer.event.getEventType()) == 0
                                && pluginName.equals(observer.event.getPluginName())
                                && pluginName.equals(observer.event.getPluginName());
                      }
                    });
  }

  private KeypleMessageDto executeRemoteService(boolean isObservable) {
    JsonObject body = new JsonObject();
    body.addProperty("serviceId", serviceId);
    body.addProperty("initialSeContent", "");
    body.addProperty("userInputData", "anyObject");
    body.addProperty("isObservable", isObservable);

    return new KeypleMessageDto().setSessionId("a").setAction(KeypleMessageDto.Action.EXECUTE_REMOTE_SERVICE.name())
            .setClientNodeId(clientId).setNativeReaderName(nativeReaderName).setBody(body.toString());
  }

  @Test
  public void
      onMessage_executeRemoteService_createObservableVirtualReader_shouldRaisePluginEvent() {}

  @Test
  public void onMessage_executeRemoteService_readerAlreadyExists_shouldRaisePluginEvent() {}

  @Test
  public void onMessage_response_FromNativeReader() {}

  @Test
  public void onMessage_event_shouldCreateSessionReader() {}

  @Test
  public void terminateService_shouldSendOutput_deleteVirtualReader() {}



  class MockPluginObserver implements ObservablePlugin.PluginObserver{
    PluginEvent event;
    @Override
    public void update(PluginEvent event) {
      this.event = event;
    }
  }
}
