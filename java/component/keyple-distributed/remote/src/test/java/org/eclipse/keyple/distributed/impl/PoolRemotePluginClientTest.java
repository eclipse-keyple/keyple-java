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
package org.eclipse.keyple.distributed.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import org.assertj.core.util.Sets;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.exception.KeypleAllocationNoReaderException;
import org.eclipse.keyple.core.service.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.util.json.BodyError;
import org.eclipse.keyple.core.util.json.KeypleGsonParser;
import org.eclipse.keyple.distributed.MessageDto;
import org.eclipse.keyple.distributed.spi.AsyncEndpointClient;
import org.eclipse.keyple.distributed.spi.SyncEndpointClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class PoolRemotePluginClientTest {
  Reader remoteReader;

  SyncEndpointClient syncEndpoint;
  AsyncEndpointClient asyncEndpoint;

  PoolRemotePluginClientImpl remotePoolPlugin;
  String groupReference = "groupReference1";
  String serverNodeId = "serverNodeId";

  String pluginName = "pluginName";

  @Before
  public void setUp() {
    asyncEndpoint = Mockito.mock(AsyncEndpointClient.class);
    syncEndpoint = Mockito.mock(SyncEndpointClient.class);
  }

  @After
  public void tearDown() {
    try {
      // unregister plugin
      SmartCardService.getInstance()
          .unregisterPlugin(PoolRemotePluginClientUtils.getRemotePlugin(pluginName).getName());
    } catch (KeyplePluginNotFoundException e) {
      // plugin not found, was not register
    }
  }

  @Test
  public void factory_withSyncEndpoint_shouldCreate_PluginWith_SyncNode() {
    remotePoolPlugin =
        (PoolRemotePluginClientImpl)
            SmartCardService.getInstance()
                .registerPlugin(
                    PoolRemotePluginClientFactory.builder()
                        .withPluginName(pluginName)
                        .withSyncNode(syncEndpoint)
                        .build());
    assertThat(PoolRemotePluginClientUtils.getRemotePlugin(pluginName)).isNotNull();

    // unregister plugin
    SmartCardService.getInstance()
        .unregisterPlugin(PoolRemotePluginClientUtils.getRemotePlugin(pluginName).getName());
  }

  @Test
  public void factory_withAsyncEndpoint_shouldCreate_PluginWith_AsyncNode() {
    remotePoolPlugin =
        (PoolRemotePluginClientImpl)
            SmartCardService.getInstance()
                .registerPlugin(
                    PoolRemotePluginClientFactory.builder()
                        .withPluginName(pluginName)
                        .withAsyncNode(asyncEndpoint)
                        .usingDefaultTimeout()
                        .build());
    assertThat(PoolRemotePluginClientUtils.getRemotePlugin(pluginName)).isNotNull();
    assertThat(PoolRemotePluginClientUtils.getAsyncNode(pluginName)).isNotNull();

    // unregister plugin
    SmartCardService.getInstance()
        .unregisterPlugin(PoolRemotePluginClientUtils.getRemotePlugin(pluginName).getName());
  }

  @Test
  public void allocateReader_onSuccess_shouldCreate_remoteReader() {
    syncEndpoint = new MockSyncEndpoint();
    remotePoolPlugin =
        (PoolRemotePluginClientImpl)
            PoolRemotePluginClientFactory.builder()
                .withPluginName(pluginName)
                .withSyncNode(syncEndpoint)
                .build()
                .getPlugin();
    remoteReader = remotePoolPlugin.allocateReader(groupReference);
    assertThat(remotePoolPlugin.getReader(remoteReader.getName())).isNotNull();
  }

  @Test(expected = KeypleAllocationNoReaderException.class)
  public void allocateReader_onFailure_shouldThrow_exception() {
    syncEndpoint =
        new MockSyncEndpoint().setException(new KeypleAllocationNoReaderException("msg"));
    remotePoolPlugin =
        (PoolRemotePluginClientImpl)
            PoolRemotePluginClientFactory.builder()
                .withPluginName(pluginName)
                .withSyncNode(syncEndpoint)
                .build()
                .getPlugin();
    remotePoolPlugin.allocateReader(groupReference);
  }

  @Test
  public void releaseReader_onSuccess_shouldDelete_remoteReader() {
    syncEndpoint = new MockSyncEndpoint();
    remotePoolPlugin =
        (PoolRemotePluginClientImpl)
            PoolRemotePluginClientFactory.builder()
                .withPluginName(pluginName)
                .withSyncNode(syncEndpoint)
                .build()
                .getPlugin();
    remoteReader = remotePoolPlugin.allocateReader(groupReference);
    remotePoolPlugin.releaseReader(remoteReader);
    assertThat(remotePoolPlugin.getReaders()).isEmpty();
  }

  @Test(expected = KeypleReaderNotFoundException.class)
  public void releaseReader_onFailure_shouldThrow_exception() {
    syncEndpoint = new MockSyncEndpoint().setException(new KeypleReaderNotFoundException("msg"));
    remotePoolPlugin =
        (PoolRemotePluginClientImpl)
            PoolRemotePluginClientFactory.builder()
                .withPluginName(pluginName)
                .withSyncNode(syncEndpoint)
                .build()
                .getPlugin();
    remoteReader = remotePoolPlugin.allocateReader(groupReference);
    remotePoolPlugin.releaseReader(remoteReader);
  }

  @Test(expected = IllegalArgumentException.class)
  public void releaseReader_onWrongReader_shouldThrow_exception() {
    // mock reader
    Reader reader = Mockito.mock(Reader.class);
    when(reader.getName()).thenReturn("mock");

    syncEndpoint = new MockSyncEndpoint().setException(new KeypleReaderNotFoundException("msg"));
    remotePoolPlugin =
        (PoolRemotePluginClientImpl)
            PoolRemotePluginClientFactory.builder()
                .withPluginName(pluginName)
                .withSyncNode(syncEndpoint)
                .build()
                .getPlugin();
    remotePoolPlugin.releaseReader(reader);
  }

  @Test
  public void getReferenceGroups_onSuccess_shouldReturn_result() {
    syncEndpoint = new MockSyncEndpoint();
    remotePoolPlugin =
        (PoolRemotePluginClientImpl)
            PoolRemotePluginClientFactory.builder()
                .withPluginName(pluginName)
                .withSyncNode(syncEndpoint)
                .build()
                .getPlugin();
    SortedSet groupReferences = remotePoolPlugin.getReaderGroupReferences();
    assertThat(groupReferences).containsExactly(groupReference);
  }

  @Test(expected = KeyplePluginNotFoundException.class)
  public void getReferenceGroups_onFailure_shouldThrow_exception() {
    syncEndpoint = new MockSyncEndpoint().setException(new KeyplePluginNotFoundException("hsm"));
    remotePoolPlugin =
        (PoolRemotePluginClientImpl)
            PoolRemotePluginClientFactory.builder()
                .withPluginName(pluginName)
                .withSyncNode(syncEndpoint)
                .build()
                .getPlugin();
    remotePoolPlugin.getReaderGroupReferences();
  }

  @Test
  public void anAPIcall_onRemoteReader_shouldReturn_result_onAsyncNode() {
    asyncEndpoint = new MockAsyncEndpoint();
    remotePoolPlugin =
        (PoolRemotePluginClientImpl)
            SmartCardService.getInstance()
                .registerPlugin(
                    PoolRemotePluginClientFactory.builder()
                        .withPluginName(pluginName)
                        .withAsyncNode(asyncEndpoint)
                        .usingDefaultTimeout()
                        .build());
    remoteReader = remotePoolPlugin.allocateReader(groupReference);
    assertThat(remotePoolPlugin.getReader(remoteReader.getName())).isNotNull();
    assertThat(remoteReader.isCardPresent()).isTrue();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void onMessage_shouldThrow_exception() {
    asyncEndpoint = new MockAsyncEndpoint();
    remotePoolPlugin =
        (PoolRemotePluginClientImpl)
            SmartCardService.getInstance()
                .registerPlugin(
                    PoolRemotePluginClientFactory.builder()
                        .withPluginName(pluginName)
                        .withAsyncNode(asyncEndpoint)
                        .usingDefaultTimeout()
                        .build());
    remotePoolPlugin.onMessage(new MessageDto());
  }

  /*
   *
   */

  class MockAsyncEndpoint implements AsyncEndpointClient {

    RuntimeException exception;

    public MockAsyncEndpoint setException(RuntimeException exception) {
      this.exception = exception;
      return this;
    }

    @Override
    public void openSession(String sessionId) {
      PoolRemotePluginClientUtils.getAsyncNode(pluginName).onOpen(sessionId);
    }

    @Override
    public void sendMessage(final MessageDto msg) {
      new Thread(
              new Runnable() {
                @Override
                public void run() {
                  PoolRemotePluginClientUtils.getAsyncNode(pluginName)
                      .onMessage(getResponse(msg, exception));
                }
              })
          .start();
    }

    @Override
    public void closeSession(String sessionId) {
      PoolRemotePluginClientUtils.getAsyncNode(pluginName).onClose(sessionId);
    }
  }

  class MockSyncEndpoint implements SyncEndpointClient {

    RuntimeException exception;

    public MockSyncEndpoint setException(RuntimeException exception) {
      this.exception = exception;
      return this;
    }

    @Override
    public List<MessageDto> sendRequest(MessageDto msg) {
      return Arrays.asList(getResponse(msg, exception));
    }
  }

  private MessageDto getResponse(MessageDto msg, RuntimeException exception) {
    JsonObject body;
    if (exception != null) {
      return new MessageDto(msg)
          .setAction(MessageDto.Action.ERROR.name())
          .setBody(KeypleGsonParser.getParser().toJson(new BodyError(exception)))
          .setServerNodeId(serverNodeId);
    }

    switch (MessageDto.Action.valueOf(msg.getAction())) {
      case RELEASE_READER:
        return new MessageDto(msg).setBody(null).setServerNodeId(serverNodeId);
      case ALLOCATE_READER:
        return new MessageDto(msg)
            .setLocalReaderName("localReaderName")
            .setBody(null)
            .setServerNodeId(serverNodeId);
      case GET_READER_GROUP_REFERENCES:
        SortedSet<String> groupReferences = Sets.newTreeSet(groupReference);
        body = new JsonObject();
        body.add("readerGroupReferences", KeypleGsonParser.getParser().toJsonTree(groupReferences));
        return new MessageDto(msg).setBody(body.toString()).setServerNodeId(serverNodeId);
      case IS_CARD_PRESENT:
        String bodyJson = KeypleGsonParser.getParser().toJson(true, Boolean.class);

        return new MessageDto(msg).setBody(bodyJson).setServerNodeId(serverNodeId);

      default:
        return null;
    }
  }
}
