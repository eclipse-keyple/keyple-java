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
package org.eclipse.keyple.plugin.remote.nativ.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.SortedSet;
import org.assertj.core.util.Sets;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.PluginFactory;
import org.eclipse.keyple.core.service.ReaderPoolPlugin;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.exception.KeypleAllocationReaderException;
import org.eclipse.keyple.core.service.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.util.json.BodyError;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remote.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remote.core.KeypleServerAsync;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NativePoolServerServiceTest extends BaseNativeTest {

  ReaderPoolPlugin poolPluginMock;
  KeypleServerAsync asyncServer;
  Gson parser;
  String groupReference = "1";
  final String clientNodeId = "clientNodeId1";
  final String virtualReaderName = "virtualReaderName1";
  final String sessionId = "session1";
  final SortedSet<String> groupReferences = Sets.newTreeSet(groupReference);
  final String poolPluginName = "poolPluginMock";
  KeypleMessageDto response;

  NativePoolServerServiceImpl service;

  @Captor ArgumentCaptor<KeypleMessageDto> responseCaptor;

  @Before
  public void setUp() {
    this.init();
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
        (NativePoolServerServiceImpl)
            new NativePoolServerServiceFactory()
                .builder()
                .withAsyncNode(asyncServer)
                .withPoolPlugins(poolPluginMock.getName())
                .getService();

    assertThat(service).isNotNull();
    assertThat(service).isEqualTo(NativePoolServerServiceImpl.getInstance());
  }

  @Test
  public void buildService_withSyncNode() {
    // test
    service =
        (NativePoolServerServiceImpl)
            new NativePoolServerServiceFactory()
                .builder()
                .withSyncNode()
                .withPoolPlugins(poolPluginMock.getName())
                .getService();

    assertThat(service).isNotNull();
    assertThat(service).isEqualTo(NativePoolServerServiceImpl.getInstance());
  }

  @Test(expected = IllegalArgumentException.class)
  public void buildService_withNull_AsyncNode_throwIAE() {
    // test
    service =
        (NativePoolServerServiceImpl)
            new NativePoolServerServiceFactory()
                .builder()
                .withAsyncNode(null)
                .withPoolPlugins(poolPluginMock.getName())
                .getService();
  }

  @Test(expected = IllegalArgumentException.class)
  public void buildService_withNoPluginName_throwIAE() {
    // test
    service =
        (NativePoolServerServiceImpl)
            new NativePoolServerServiceFactory()
                .builder()
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
        (NativePoolServerServiceImpl)
            new NativePoolServerServiceFactory()
                .builder()
                .withAsyncNode(null)
                .withPoolPlugins(poolPluginMock.getName(), readerMockName)
                .getService();
  }

  @Test
  public void onAllocateReader_shouldPropagate_toLocalPoolPlugin() {
    KeypleMessageDto request = getAllocateReaderDto();
    NativePoolServerUtils.getAsyncNode().onMessage(getAllocateReaderDto());
    response = captureResponse();
    assertMetadataMatches(request, response);
    assertThat(readerMocked.getName()).isEqualTo(response.getNativeReaderName());
  }

  @Test
  public void onAllocateReader_shouldPropagate_AllocationException() {
    KeypleAllocationReaderException e = new KeypleAllocationReaderException("");
    doThrow(e).when(poolPluginMock).allocateReader(groupReference);
    KeypleMessageDto request = getAllocateReaderDto();
    NativePoolServerUtils.getAsyncNode().onMessage(getAllocateReaderDto());
    response = captureResponse();
    assertMetadataMatches(request, response);
    assertThat(e).isEqualToComparingFieldByFieldRecursively(getExceptionFromDto(response));
  }

  @Test
  public void onAllocateReader_withNoPlugin_shouldThrow_KPNFE() {
    SmartCardService.getInstance().unregisterPlugin(poolPluginName);
    KeypleMessageDto request = getAllocateReaderDto();
    NativePoolServerUtils.getAsyncNode().onMessage(getAllocateReaderDto());

    response = captureResponse();
    assertMetadataMatches(request, response);
    assertThat(getExceptionFromDto(response)).isInstanceOf(KeyplePluginNotFoundException.class);
  }

  @Test
  public void onReleaseReader_shouldPropagate_toLocalPoolPlugin() {
    KeypleMessageDto request = getReleaseReaderDto();
    NativePoolServerUtils.getAsyncNode().onMessage(request);
    verify(poolPluginMock, times(1)).releaseReader(readerMocked);
  }

  @Test
  public void onReleaseReader_withNoPlugin_shouldThrow_KPNFE() {
    doReturn(Sets.newTreeSet()).when(poolPluginMock).getReaderNames();
    KeypleMessageDto request = getReleaseReaderDto();
    NativePoolServerUtils.getAsyncNode().onMessage(request);

    response = captureResponse();
    assertMetadataMatches(request, response);
    assertThat(getExceptionFromDto(response)).isInstanceOf(KeypleReaderNotFoundException.class);
  }

  @Test
  public void onGroupReferences_shouldPropagate_toLocalPoolPlugin() {
    KeypleMessageDto request = getGroupReferencesDto();
    NativePoolServerUtils.getAsyncNode().onMessage(request);

    response = captureResponse();
    assertMetadataMatches(request, response);
    assertThat(getReferenceGroupFromDto(response)).containsExactly(groupReference);
  }

  @Test
  public void onGroupReferences_shouldPropagate_AllocationError() {
    KeypleMessageDto request = getGroupReferencesDto();
    NativePoolServerUtils.getAsyncNode().onMessage(request);

    response = captureResponse();
    assertMetadataMatches(request, response);
    assertThat(getReferenceGroupFromDto(response)).containsExactly(groupReference);
  }

  @Test
  public void onIsPresent_shouldPropagate_toLocalPoolPlugin() {
    KeypleMessageDto request = getIsCardPresentDto(sessionId);
    NativePoolServerUtils.getAsyncNode().onMessage(request);

    response = captureResponse();
    KeypleMessageDto response = responseCaptor.getValue();
    assertMetadataMatches(request, response);
  }

  /*
   * Helpers
   */

  private void initMockService() {
    poolPluginMock = Mockito.mock(ReaderPoolPlugin.class);
    doReturn(readerMocked).when(poolPluginMock).allocateReader(groupReference);
    doReturn(readerMocked).when(poolPluginMock).getReader(readerName);
    doReturn(Sets.newTreeSet(readerName)).when(poolPluginMock).getReaderNames();
    doReturn(poolPluginName).when(poolPluginMock).getName();
    doReturn(groupReferences).when(poolPluginMock).getReaderGroupReferences();
    asyncServer = Mockito.mock(KeypleServerAsync.class);

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
        (NativePoolServerServiceImpl)
            new NativePoolServerServiceFactory()
                .builder()
                .withAsyncNode(asyncServer)
                .withPoolPlugins(poolPluginMock.getName())
                .getService();
  }

  private KeypleMessageDto getAllocateReaderDto() {
    JsonObject body = new JsonObject();
    body.addProperty("groupReference", groupReference);
    return new KeypleMessageDto()
        .setAction(KeypleMessageDto.Action.ALLOCATE_READER.name())
        .setClientNodeId(clientNodeId)
        .setSessionId(sessionId)
        .setBody(body.toString());
  }

  private KeypleMessageDto getReleaseReaderDto() {
    return new KeypleMessageDto()
        .setAction(KeypleMessageDto.Action.RELEASE_READER.name())
        .setClientNodeId(clientNodeId)
        .setSessionId(sessionId)
        .setNativeReaderName(readerName)
        .setVirtualReaderName(virtualReaderName)
        .setBody(null);
  }

  private KeypleMessageDto getGroupReferencesDto() {
    return new KeypleMessageDto()
        .setAction(KeypleMessageDto.Action.GET_READER_GROUP_REFERENCES.name())
        .setClientNodeId(clientNodeId)
        .setSessionId(sessionId)
        .setBody(null);
  }

  public static void assertMetadataMatches(KeypleMessageDto request, KeypleMessageDto response) {
    assertThat(response).isNotNull();
    assertThat(response.getSessionId()).isEqualTo(request.getSessionId());
    assertThat(response.getClientNodeId()).isEqualTo(request.getClientNodeId());
  }

  public static KeypleMessageDto getIsCardPresentDto(String sessionId) {
    return new KeypleMessageDto() //
        .setSessionId(sessionId) //
        .setAction(KeypleMessageDto.Action.IS_CARD_PRESENT.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setNativeReaderName(readerName)
        .setBody(null);
  }

  private SortedSet<String> getReferenceGroupFromDto(KeypleMessageDto msg) {
    String readerGroupReferencesJson =
        KeypleJsonParser.getParser()
            .fromJson(msg.getBody(), JsonObject.class)
            .get("readerGroupReferences")
            .toString();
    return KeypleJsonParser.getParser().fromJson(readerGroupReferencesJson, SortedSet.class);
  }

  private RuntimeException getExceptionFromDto(KeypleMessageDto msg) {
    String bodyResponse = msg.getBody();
    return KeypleJsonParser.getParser().fromJson(bodyResponse, BodyError.class).getException();
  }

  private KeypleMessageDto captureResponse() {
    Mockito.verify(asyncServer).sendMessage(responseCaptor.capture());
    return responseCaptor.getValue();
  }
}
