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
import static org.mockito.Mockito.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.SortedSet;
import org.assertj.core.util.Sets;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.PluginFactory;
import org.eclipse.keyple.core.service.PoolPlugin;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.exception.KeypleAllocationReaderException;
import org.eclipse.keyple.core.service.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.util.json.BodyError;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PoolLocalServiceServerTest extends BaseLocalTest {

  PoolPlugin poolPluginMock;
  AsyncEndpointServer asyncServer;
  Gson parser;
  String groupReference = "1";
  final String clientNodeId = "clientNodeId1";
  final String remoteReaderName = "remoteReaderName1";
  final String sessionId = "session1";
  final SortedSet<String> groupReferences = Sets.newTreeSet(groupReference);
  final String poolPluginName = "poolPluginMock";

  MessageDto response;
  String localServiceName;

  PoolLocalServiceServerImpl service;

  @Captor ArgumentCaptor<MessageDto> responseCaptor;
  @Rule public TestName testName = new TestName();

  @Before
  public void setUp() {
    this.init();
    localServiceName = testName.getMethodName();
    parser = KeypleJsonParser.getParser();
    initMockService();
  }

  @After
  public void tearDown() {
    SmartCardService service = SmartCardService.getInstance();
    if (service.isRegistered(poolPluginName)) {
      SmartCardService.getInstance().unregisterPlugin(poolPluginName);
    }
  }

  @Test
  public void buildService_withAsyncNode() {
    // test
    service =
        (PoolLocalServiceServerImpl)
            PoolLocalServiceServerFactory.builder()
                .withServiceName("aService_withAsyncNode")
                .withAsyncNode(asyncServer)
                .withPoolPlugins(poolPluginMock.getName())
                .getService();

    assertThat(service).isNotNull();
    assertThat(service).isEqualTo(PoolLocalServiceServerImpl.getInstance("aService_withAsyncNode"));
  }

  @Test
  public void buildService_withSyncNode() {
    // test
    service =
        (PoolLocalServiceServerImpl)
            PoolLocalServiceServerFactory.builder()
                .withServiceName("aService_withSyncNode")
                .withSyncNode()
                .withPoolPlugins(poolPluginMock.getName())
                .getService();

    assertThat(service).isNotNull();
    assertThat(service).isEqualTo(PoolLocalServiceServerImpl.getInstance("aService_withSyncNode"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void buildService_withNull_AsyncNode_throwIAE() {
    // test
    service =
        (PoolLocalServiceServerImpl)
            PoolLocalServiceServerFactory.builder()
                .withServiceName(localServiceName)
                .withAsyncNode(null)
                .withPoolPlugins(poolPluginMock.getName())
                .getService();
  }

  @Test(expected = IllegalArgumentException.class)
  public void buildService_withNoPluginName_throwIAE() {
    // test
    service =
        (PoolLocalServiceServerImpl)
            PoolLocalServiceServerFactory.builder()
                .withServiceName(localServiceName)
                .withAsyncNode(null)
                .withPoolPlugins()
                .getService();
  }

  @Test(expected = IllegalArgumentException.class)
  public void buildService_withWrong_pluginType_throwIAE() {
    final String readerMockName = "readerPlugin";
    SmartCardService.getInstance()
        .registerPlugin(
            new PluginFactory() {
              @Override
              public String getPluginName() {
                return readerMockName;
              }

              @Override
              public Plugin getPlugin() {
                Plugin plugin = Mockito.mock(Plugin.class);
                return plugin;
              }
            });

    // test
    service =
        (PoolLocalServiceServerImpl)
            PoolLocalServiceServerFactory.builder()
                .withServiceName(localServiceName)
                .withAsyncNode(null)
                .withPoolPlugins(poolPluginMock.getName(), readerMockName)
                .getService();
  }

  @Test
  public void onAllocateReader_shouldPropagate_toLocalPoolPlugin() {
    MessageDto request = getAllocateReaderDto();
    PoolLocalServiceServerUtils.getAsyncNode(localServiceName).onMessage(getAllocateReaderDto());
    response = captureResponse();
    assertMetadataMatches(request, response);
    assertThat(readerMocked.getName()).isEqualTo(response.getLocalReaderName());
  }

  @Test
  public void onAllocateReader_shouldPropagate_AllocationException() {
    KeypleAllocationReaderException e = new KeypleAllocationReaderException("");
    doThrow(e).when(poolPluginMock).allocateReader(groupReference);
    MessageDto request = getAllocateReaderDto();
    PoolLocalServiceServerUtils.getAsyncNode(localServiceName).onMessage(getAllocateReaderDto());
    response = captureResponse();
    assertMetadataMatches(request, response);
    assertThat(e).isEqualToComparingFieldByFieldRecursively(getExceptionFromDto(response));
  }

  @Test
  public void onAllocateReader_withNoPlugin_shouldThrow_KPNFE() {
    SmartCardService.getInstance().unregisterPlugin(poolPluginName);
    MessageDto request = getAllocateReaderDto();
    PoolLocalServiceServerUtils.getAsyncNode(localServiceName).onMessage(getAllocateReaderDto());

    response = captureResponse();
    assertMetadataMatches(request, response);
    assertThat(getExceptionFromDto(response)).isInstanceOf(KeyplePluginNotFoundException.class);
  }

  @Test
  public void onReleaseReader_shouldPropagate_toLocalPoolPlugin() {
    MessageDto request = getReleaseReaderDto();
    PoolLocalServiceServerUtils.getAsyncNode(localServiceName).onMessage(request);
    verify(poolPluginMock, times(1)).releaseReader(readerMocked);
  }

  @Test
  public void onReleaseReader_withNoPlugin_shouldThrow_KPNFE() {
    doReturn(Sets.newTreeSet()).when(poolPluginMock).getReaderNames();
    MessageDto request = getReleaseReaderDto();
    PoolLocalServiceServerUtils.getAsyncNode(localServiceName).onMessage(request);

    response = captureResponse();
    assertMetadataMatches(request, response);
    assertThat(getExceptionFromDto(response)).isInstanceOf(KeypleReaderNotFoundException.class);
  }

  @Test
  public void onGroupReferences_shouldPropagate_toLocalPoolPlugin() {
    MessageDto request = getGroupReferencesDto();
    PoolLocalServiceServerUtils.getAsyncNode(localServiceName).onMessage(request);

    response = captureResponse();
    assertMetadataMatches(request, response);
    assertThat(getReferenceGroupFromDto(response)).containsExactly(groupReference);
  }

  @Test
  public void onGroupReferences_shouldPropagate_AllocationError() {
    MessageDto request = getGroupReferencesDto();
    PoolLocalServiceServerUtils.getAsyncNode(localServiceName).onMessage(request);

    response = captureResponse();
    assertMetadataMatches(request, response);
    assertThat(getReferenceGroupFromDto(response)).containsExactly(groupReference);
  }

  @Test
  public void onIsPresent_shouldPropagate_toLocalPoolPlugin() {
    MessageDto request = getIsCardPresentDto(sessionId);
    PoolLocalServiceServerUtils.getAsyncNode(localServiceName).onMessage(request);

    response = captureResponse();
    MessageDto response = responseCaptor.getValue();
    assertMetadataMatches(request, response);
  }

  /*
   * Helpers
   */

  private void initMockService() {
    poolPluginMock = Mockito.mock(PoolPlugin.class);
    doReturn(readerMocked).when(poolPluginMock).allocateReader(groupReference);
    doReturn(readerMocked).when(poolPluginMock).getReader(readerName);
    doReturn(Sets.newTreeSet(readerName)).when(poolPluginMock).getReaderNames();
    doReturn(poolPluginName).when(poolPluginMock).getName();
    doReturn(groupReferences).when(poolPluginMock).getReaderGroupReferences();
    asyncServer = Mockito.mock(AsyncEndpointServer.class);

    SmartCardService.getInstance()
        .registerPlugin(
            new PluginFactory() {
              @Override
              public String getPluginName() {
                return poolPluginName;
              }

              @Override
              public Plugin getPlugin() {
                return poolPluginMock;
              }
            });

    service =
        (PoolLocalServiceServerImpl)
            PoolLocalServiceServerFactory.builder()
                .withServiceName(localServiceName)
                .withAsyncNode(asyncServer)
                .withPoolPlugins(poolPluginMock.getName())
                .getService();
  }

  private MessageDto getAllocateReaderDto() {
    JsonObject body = new JsonObject();
    body.addProperty("groupReference", groupReference);
    return new MessageDto()
        .setAction(MessageDto.Action.ALLOCATE_READER.name())
        .setClientNodeId(clientNodeId)
        .setSessionId(sessionId)
        .setBody(body.toString());
  }

  private MessageDto getReleaseReaderDto() {
    return new MessageDto()
        .setAction(MessageDto.Action.RELEASE_READER.name())
        .setClientNodeId(clientNodeId)
        .setSessionId(sessionId)
        .setLocalReaderName(readerName)
        .setRemoteReaderName(remoteReaderName)
        .setBody(null);
  }

  private MessageDto getGroupReferencesDto() {
    return new MessageDto()
        .setAction(MessageDto.Action.GET_READER_GROUP_REFERENCES.name())
        .setClientNodeId(clientNodeId)
        .setSessionId(sessionId)
        .setBody(null);
  }

  public static void assertMetadataMatches(MessageDto request, MessageDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getSessionId()).isEqualTo(request.getSessionId());
    assertThat(response.getClientNodeId()).isEqualTo(request.getClientNodeId());
  }

  public static MessageDto getIsCardPresentDto(String sessionId) {
    return new MessageDto() //
        .setSessionId(sessionId) //
        .setAction(MessageDto.Action.IS_CARD_PRESENT.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setLocalReaderName(readerName)
        .setBody(null);
  }

  private SortedSet<String> getReferenceGroupFromDto(MessageDto msg) {
    String readerGroupReferencesJson =
        KeypleJsonParser.getParser()
            .fromJson(msg.getBody(), JsonObject.class)
            .get("readerGroupReferences")
            .toString();
    return KeypleJsonParser.getParser().fromJson(readerGroupReferencesJson, SortedSet.class);
  }

  private RuntimeException getExceptionFromDto(MessageDto msg) {
    String bodyResponse = msg.getBody();
    return KeypleJsonParser.getParser().fromJson(bodyResponse, BodyError.class).getException();
  }

  private MessageDto captureResponse() {
    Mockito.verify(asyncServer).sendMessage(responseCaptor.capture());
    return responseCaptor.getValue();
  }
}
