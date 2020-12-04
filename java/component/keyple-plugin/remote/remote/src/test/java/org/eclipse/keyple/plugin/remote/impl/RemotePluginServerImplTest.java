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

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.ObservableRemoteReaderServer;
import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointServer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class RemotePluginServerImplTest extends RemoteServerBaseTest {

  String pluginName = "pluginName";

  @Before
  public void setUp() {
    pluginObserver = new MockPluginObserver(true);
    readerObserver = new MockReaderObserver();
    messageArgumentCaptor = ArgumentCaptor.forClass(MessageDto.class);
    remotePlugin = Mockito.spy(new RemotePluginServerImpl(remotePluginName, eventNotificationPool));
    remotePlugin.addObserver(pluginObserver);
    node = Mockito.mock(AbstractNode.class);
    remotePlugin.node = node;
    doAnswer(aVoid()).when(node).sendMessage(messageArgumentCaptor.capture());
  }

  /*
   * Tests
   */

  @Test
  public void registerSyncPlugin() {

    SmartCardService.getInstance()
        .registerPlugin(
            RemotePluginServerFactory.builder()
                .withPluginName(pluginName)
                .withSyncNode()
                .withPluginObserver(pluginObserver)
                .usingDefaultEventNotificationPool()
                .build());
    assertThat(RemotePluginServerUtils.getRemotePlugin(pluginName)).isNotNull();
    assertThat(RemotePluginServerUtils.getSyncNode(pluginName)).isNotNull();

    SmartCardService.getInstance().unregisterPlugin(pluginName);
  }

  @Test
  public void registerAsyncPlugin() {
    SmartCardService.getInstance()
        .registerPlugin(
            RemotePluginServerFactory.builder()
                .withPluginName(pluginName)
                .withAsyncNode(Mockito.mock(AsyncEndpointServer.class))
                .withPluginObserver(pluginObserver)
                .usingDefaultEventNotificationPool()
                .build());
    assertThat(RemotePluginServerUtils.getRemotePlugin(pluginName)).isNotNull();
    assertThat(RemotePluginServerUtils.getAsyncNode(pluginName)).isNotNull();

    SmartCardService.getInstance().unregisterPlugin(pluginName);
  }

  @Test
  public void addObserver_removeObserver() {
    assertThat(remotePlugin.countObservers()).isEqualTo(1);
    remotePlugin.removeObserver(pluginObserver);
    assertThat(remotePlugin.countObservers()).isEqualTo(0);
    remotePlugin.addObserver(pluginObserver);
    assertThat(remotePlugin.countObservers()).isEqualTo(1);
    remotePlugin.clearObservers();
    assertThat(remotePlugin.countObservers()).isEqualTo(0);
  }

  @Test
  public void onMessage_executeRemoteService_createRemoteReader_shouldRaisePluginEvent() {
    String sessionId = UUID.randomUUID().toString();
    MessageDto message = executeRemoteServiceMessage(sessionId, false);
    remotePlugin.onMessage(message);
    AbstractRemoteReaderServer remoteReader =
        (AbstractRemoteReaderServer) remotePlugin.getReaders().values().iterator().next();
    assertThat(remoteReader).isOfAnyClassIn(RemoteReaderServerImpl.class);
    assertThat(remoteReader).isNotExactlyInstanceOf(ObservableReader.class);
    assertThat(remoteReader.getServiceId()).isEqualTo(serviceId);
    assertThat(remoteReader.getName()).isNotEmpty();
    await().atMost(1, TimeUnit.SECONDS).until(validReaderConnectEvent());
  }

  @Test
  public void onMessage_executeRemoteService_createObservableRemoteReader_shouldRaisePluginEvent() {
    String sessionId = UUID.randomUUID().toString();
    MessageDto message = executeRemoteServiceMessage(sessionId, true);
    remotePlugin.onMessage(message);
    ObservableRemoteReaderServerImpl remoteReader =
        (ObservableRemoteReaderServerImpl) remotePlugin.getReaders().values().iterator().next();
    assertThat(remoteReader).isOfAnyClassIn(ObservableRemoteReaderServerImpl.class);
    assertThat(remoteReader.getServiceId()).isEqualTo(serviceId);
    assertThat(remoteReader.getName()).isNotEmpty();
    await().atMost(1, TimeUnit.SECONDS).until(validReaderConnectEvent());
  }

  @Test
  public void terminateService_onRemoteReader_withObserver_doNotdeleteRemoteReader() {

    // executing remote service creates a remote reader
    String sessionId0 = UUID.randomUUID().toString();
    remotePlugin.onMessage(executeRemoteServiceMessage(sessionId0, true));
    await().atMost(1, TimeUnit.SECONDS).until(validReaderConnectEvent());

    // terminate service without unregistering reader
    pluginObserver.terminateService(userOutputData);
    MessageDto terminateServiceMsg = messageArgumentCaptor.getValue();
    assertThat(terminateServiceMsg.getSessionId()).isEqualTo(sessionId0);
    validateTerminateServiceResponse(terminateServiceMsg, false);
    assertThat(remotePlugin.getReaders()).hasSize(1);
  }

  @Test
  public void terminateService_onRemoteReader_withoutObserver_shouldDeleteRemoteReader() {
    // do not attach a readerObserver
    remotePlugin.clearObservers();
    pluginObserver = new MockPluginObserver(false);
    remotePlugin.addObserver(pluginObserver);

    // executing a remote service creates a remote reader
    String sessionId0 = UUID.randomUUID().toString();
    remotePlugin.onMessage(executeRemoteServiceMessage(sessionId0, true));
    await().atMost(1, TimeUnit.SECONDS).until(validReaderConnectEvent());

    // terminate service without unregistering reader
    pluginObserver.terminateService(userOutputData);
    MessageDto terminateServiceMsg = messageArgumentCaptor.getValue();
    assertThat(terminateServiceMsg.getSessionId()).isEqualTo(sessionId0);
    validateTerminateServiceResponse(terminateServiceMsg, true);
    assertThat(remotePlugin.getReaders()).hasSize(0);
  }

  @Test
  public void onEvent_eachEvent_shouldCreateAReader() {
    String sessionId0 = UUID.randomUUID().toString();
    String sessionId1 = UUID.randomUUID().toString();
    String sessionId2 = UUID.randomUUID().toString();

    // execute remote service
    remotePlugin.onMessage(executeRemoteServiceMessage(sessionId0, true));
    await().atMost(1, TimeUnit.SECONDS).until(validReaderConnectEvent());

    assertThat(remotePlugin.getReaders()).hasSize(1); // one master remote reader

    // get the remoteReader name
    String remoteReaderName = remotePlugin.getReaders().values().iterator().next().getName();

    // send a SE_INSERTED event (1)
    MessageDto readerEventMessage = readerEventMessage(sessionId1, remoteReaderName);
    remotePlugin.onMessage(readerEventMessage);

    // validate the SE_INSERTED event (1)
    await().atMost(1, TimeUnit.SECONDS).until(validSeInsertedEvent(remoteReaderName, 1));

    assertThat(remotePlugin.getReaders())
        .hasSize(2); // one master remote reader, one slave remote reader

    // send another SE_INSERTED event (2)
    MessageDto readerEventMessage2 = readerEventMessage(sessionId2, remoteReaderName);
    remotePlugin.onMessage(readerEventMessage2);

    // validate the SE_INSERTED event (2)
    await().atMost(1, TimeUnit.SECONDS).until(validSeInsertedEvent(remoteReaderName, 2));

    assertThat(remotePlugin.getReaders())
        .hasSize(3); // one master remote reader, two slave remote readers
  }

  @Test
  public void terminateService_onSlaveReader_shouldSendOutput_keepRemoteReader() {
    String sessionId0 = UUID.randomUUID().toString();
    String sessionId1 = UUID.randomUUID().toString();

    // execute remote service
    remotePlugin.onMessage(executeRemoteServiceMessage(sessionId0, true));
    await().atMost(1, TimeUnit.SECONDS).until(validReaderConnectEvent());

    // get the remoteReader name
    String remoteReaderName = remotePlugin.getReaders().values().iterator().next().getName();

    // send a SE_INSERTED event (1)
    MessageDto readerEventMessage = readerEventMessage(sessionId1, remoteReaderName);
    remotePlugin.onMessage(readerEventMessage);

    // validate the SE_INSERTED event (1)
    await().atMost(1, TimeUnit.SECONDS).until(validSeInsertedEvent(remoteReaderName, 1));
    assertThat(remotePlugin.getReaders()).hasSize(2); // one remote reader, one session reader

    // terminate service on slave reader
    readerObserver.terminateService(userOutputData);
    MessageDto terminateServiceMsg = messageArgumentCaptor.getValue();
    assertThat(terminateServiceMsg.getRemoteReaderName()).isNotEqualTo(remoteReaderName);
    assertThat(terminateServiceMsg.getSessionId()).isEqualTo(sessionId1);
    validateTerminateServiceResponse(terminateServiceMsg, false);

    assertThat(remotePlugin.getReaders()).hasSize(1); // master reader is kept
  }

  @Test
  public void terminateService_onSlaveReader_shouldSendOutput_unregisterRemoteReader() {
    String sessionId0 = UUID.randomUUID().toString();
    String sessionId1 = UUID.randomUUID().toString();

    // execute remote service
    remotePlugin.onMessage(executeRemoteServiceMessage(sessionId0, true));
    await().atMost(1, TimeUnit.SECONDS).until(validReaderConnectEvent());

    // get the remoteReader name
    String remoteReaderName = remotePlugin.getReaders().values().iterator().next().getName();

    // send a SE_INSERTED event (1)
    MessageDto readerEventMessage = readerEventMessage(sessionId1, remoteReaderName);
    remotePlugin.onMessage(readerEventMessage);

    // validate the SE_INSERTED event (1)
    await().atMost(1, TimeUnit.SECONDS).until(validSeInsertedEvent(remoteReaderName, 1));
    assertThat(remotePlugin.getReaders()).hasSize(2); // one remote reader, one session reader

    // remove observers in remote reader
    ObservableRemoteReaderServer remoteReader =
        (ObservableRemoteReaderServer) remotePlugin.getReader(remoteReaderName);
    remoteReader.clearObservers();

    // terminate service on slave reader
    readerObserver.terminateService(userOutputData);
    MessageDto terminateServiceMsg = messageArgumentCaptor.getValue();
    assertThat(terminateServiceMsg.getRemoteReaderName()).isNotEqualTo(remoteReaderName);
    assertThat(terminateServiceMsg.getSessionId()).isEqualTo(sessionId1);
    validateTerminateServiceResponse(terminateServiceMsg, true);

    assertThat(remotePlugin.getReaders()).hasSize(0); // every reader is removed
  }
}
