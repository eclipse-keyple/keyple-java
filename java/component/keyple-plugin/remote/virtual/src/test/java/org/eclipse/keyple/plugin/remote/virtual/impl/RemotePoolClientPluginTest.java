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
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import org.assertj.core.util.Sets;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleAllocationNoReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.util.json.BodyError;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remote.core.KeypleClientAsync;
import org.eclipse.keyple.plugin.remote.core.KeypleClientSync;
import org.eclipse.keyple.plugin.remote.core.KeypleMessageDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class RemotePoolClientPluginTest {
  SeReader virtualReader;

  KeypleClientSync syncEndpoint;
  KeypleClientAsync asyncEndpoint;

  RemotePoolClientPluginImpl remotePoolPlugin;
  String groupReference = "groupReference1";
  String serverNodeId = "serverNodeId";

  @Before
  public void setUp() {
    asyncEndpoint = Mockito.mock(KeypleClientAsync.class);
    syncEndpoint = Mockito.mock(KeypleClientSync.class);
  }

  @Test
  public void factory_withSyncEndpoint_shouldCreate_PluginWith_SyncNode() {
    SeProxyService.getInstance()
        .registerPlugin(
            RemotePoolClientPluginFactory.builder()
                .withSyncNode(syncEndpoint)
                .usingCustomTimeout(10)
                .build());
    assertThat(RemotePoolClientUtils.getSyncPlugin()).isNotNull();
    assertThat(RemotePoolClientUtils.getSyncNode()).isNotNull();

    // unregister plugin
    SeProxyService.getInstance().unregisterPlugin(RemotePoolClientUtils.getSyncPlugin().getName());
  }

  @Test
  public void factory_withAsyncEndpoint_shouldCreate_PluginWith_AsyncNode() {
    SeProxyService.getInstance()
        .registerPlugin(
            RemotePoolClientPluginFactory.builder()
                .withAsyncNode(asyncEndpoint)
                .usingDefaultTimeout()
                .build());
    assertThat(RemotePoolClientUtils.getAsyncPlugin()).isNotNull();
    assertThat(RemotePoolClientUtils.getAsyncNode()).isNotNull();

    // unregister plugin
    SeProxyService.getInstance().unregisterPlugin(RemotePoolClientUtils.getAsyncPlugin().getName());
  }

  @Test
  public void allocateReader_onSuccess_shouldCreate_virtualReader() {
    syncEndpoint = new MockSyncEndpoint();
    remotePoolPlugin =
        (RemotePoolClientPluginImpl)
            RemotePoolClientPluginFactory.builder()
                .withSyncNode(syncEndpoint)
                .usingDefaultTimeout()
                .build()
                .getPlugin();
    virtualReader = remotePoolPlugin.allocateReader(groupReference);
    assertThat(remotePoolPlugin.getReader(virtualReader.getName())).isNotNull();
  }

  @Test(expected = KeypleAllocationNoReaderException.class)
  public void allocateReader_onFailure_shouldThrow_exception() {
    syncEndpoint =
        new MockSyncEndpoint().setException(new KeypleAllocationNoReaderException("msg"));
    remotePoolPlugin =
        (RemotePoolClientPluginImpl)
            RemotePoolClientPluginFactory.builder()
                .withSyncNode(syncEndpoint)
                .usingDefaultTimeout()
                .build()
                .getPlugin();
    remotePoolPlugin.allocateReader(groupReference);
  }

  @Test
  public void releaseReader_onSuccess_shouldDelete_virtualReader() {
    syncEndpoint = new MockSyncEndpoint();
    remotePoolPlugin =
        (RemotePoolClientPluginImpl)
            RemotePoolClientPluginFactory.builder()
                .withSyncNode(syncEndpoint)
                .usingDefaultTimeout()
                .build()
                .getPlugin();
    virtualReader = remotePoolPlugin.allocateReader(groupReference);
    remotePoolPlugin.releaseReader(virtualReader);
    assertThat(remotePoolPlugin.getReaders()).isEmpty();
  }

  @Test(expected = KeypleReaderNotFoundException.class)
  public void releaseReader_onFailure_shouldThrow_exception() {
    syncEndpoint = new MockSyncEndpoint().setException(new KeypleReaderNotFoundException("msg"));
    remotePoolPlugin =
        (RemotePoolClientPluginImpl)
            RemotePoolClientPluginFactory.builder()
                .withSyncNode(syncEndpoint)
                .usingDefaultTimeout()
                .build()
                .getPlugin();
    virtualReader = remotePoolPlugin.allocateReader(groupReference);
    remotePoolPlugin.releaseReader(virtualReader);
  }

  @Test(expected = IllegalArgumentException.class)
  public void releaseReader_onWrongReader_shouldThrow_exception() {
    // mock reader
    SeReader reader = Mockito.mock(SeReader.class);
    when(reader.getName()).thenReturn("mock");

    syncEndpoint = new MockSyncEndpoint().setException(new KeypleReaderNotFoundException("msg"));
    remotePoolPlugin =
        (RemotePoolClientPluginImpl)
            RemotePoolClientPluginFactory.builder()
                .withSyncNode(syncEndpoint)
                .usingDefaultTimeout()
                .build()
                .getPlugin();
    remotePoolPlugin.releaseReader(reader);
  }

  @Test
  public void getReferenceGroups_onSuccess_shouldReturn_result() {
    syncEndpoint = new MockSyncEndpoint();
    remotePoolPlugin =
        (RemotePoolClientPluginImpl)
            RemotePoolClientPluginFactory.builder()
                .withSyncNode(syncEndpoint)
                .usingDefaultTimeout()
                .build()
                .getPlugin();
    SortedSet groupReferences = remotePoolPlugin.getReaderGroupReferences();
    assertThat(groupReferences).containsExactly(groupReference);
  }

  @Test(expected = KeyplePluginNotFoundException.class)
  public void getReferenceGroups_onFailure_shouldThrow_exception() {
    syncEndpoint = new MockSyncEndpoint().setException(new KeyplePluginNotFoundException("hsm"));
    remotePoolPlugin =
        (RemotePoolClientPluginImpl)
            RemotePoolClientPluginFactory.builder()
                .withSyncNode(syncEndpoint)
                .usingDefaultTimeout()
                .build()
                .getPlugin();
    remotePoolPlugin.getReaderGroupReferences();
  }

  @Test
  public void anAPIcall_onVirtualReader_shouldReturn_result_onAsyncNode() {
    asyncEndpoint = new MockAsyncEndpoint();
    remotePoolPlugin =
        (RemotePoolClientPluginImpl)
            SeProxyService.getInstance()
                .registerPlugin(
                    RemotePoolClientPluginFactory.builder()
                        .withAsyncNode(asyncEndpoint)
                        .usingDefaultTimeout()
                        .build());
    virtualReader = remotePoolPlugin.allocateReader(groupReference);
    assertThat(remotePoolPlugin.getReader(virtualReader.getName())).isNotNull();
    assertThat(virtualReader.isSePresent()).isTrue();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void onMessage_shouldThrow_exception() {
    asyncEndpoint = new MockAsyncEndpoint();
    remotePoolPlugin =
        (RemotePoolClientPluginImpl)
            SeProxyService.getInstance()
                .registerPlugin(
                    RemotePoolClientPluginFactory.builder()
                        .withAsyncNode(asyncEndpoint)
                        .usingDefaultTimeout()
                        .build());
    remotePoolPlugin.onMessage(new KeypleMessageDto());
  }

  /*
   *
   */

  class MockAsyncEndpoint implements KeypleClientAsync {

    RuntimeException exception;

    public MockAsyncEndpoint setException(RuntimeException exception) {
      this.exception = exception;
      return this;
    }

    @Override
    public void openSession(String sessionId) {
      RemotePoolClientUtils.getAsyncNode().onOpen(sessionId);
    }

    @Override
    public void sendMessage(final KeypleMessageDto msg) {
      new Thread(
              new Runnable() {
                @Override
                public void run() {
                  RemotePoolClientUtils.getAsyncNode().onMessage(getResponse(msg, exception));
                }
              })
          .start();
    }

    @Override
    public void closeSession(String sessionId) {
      RemotePoolClientUtils.getAsyncNode().onClose(sessionId);
    }
  }

  class MockSyncEndpoint implements KeypleClientSync {

    RuntimeException exception;

    public MockSyncEndpoint setException(RuntimeException exception) {
      this.exception = exception;
      return this;
    }

    @Override
    public List<KeypleMessageDto> sendRequest(KeypleMessageDto msg) {
      return Arrays.asList(getResponse(msg, exception));
    }
  }

  private KeypleMessageDto getResponse(KeypleMessageDto msg, RuntimeException exception) {
    JsonObject body;
    if (exception != null) {
      return new KeypleMessageDto(msg)
          .setAction(KeypleMessageDto.Action.ERROR.name())
          .setBody(KeypleJsonParser.getParser().toJson(new BodyError(exception)))
          .setServerNodeId(serverNodeId);
    }

    switch (KeypleMessageDto.Action.valueOf(msg.getAction())) {
      case RELEASE_READER:
        return new KeypleMessageDto(msg).setBody(null).setServerNodeId(serverNodeId);
      case ALLOCATE_READER:
        return new KeypleMessageDto(msg)
            .setNativeReaderName("nativeReaderName")
            .setBody(null)
            .setServerNodeId(serverNodeId);
      case GET_READER_GROUP_REFERENCES:
        SortedSet<String> groupReferences = Sets.newTreeSet(groupReference);
        body = new JsonObject();
        body.add("readerGroupReferences", KeypleJsonParser.getParser().toJsonTree(groupReferences));
        return new KeypleMessageDto(msg).setBody(body.toString()).setServerNodeId(serverNodeId);
      case IS_CARD_PRESENT:
        String bodyJson = KeypleJsonParser.getParser().toJson(true, Boolean.class);

        return new KeypleMessageDto(msg).setBody(bodyJson).setServerNodeId(serverNodeId);

      default:
        return null;
    }
  }
}
