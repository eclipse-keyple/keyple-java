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
import org.eclipse.keyple.core.seproxy.ReaderPoolPlugin;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class NativePoolServerServiceTest extends BaseNativeTest {

  private static final Logger logger = LoggerFactory.getLogger(AbstractNativeServiceTest.class);
  ReaderPoolPlugin poolPluginMock;
  KeypleServerAsync asyncServer;
  Gson parser;
  String groupReference = "1";
  final String clientNodeId = "clientNodeId1";
  final String serverNodeId = "serverNodeId1";
  final String virtualReaderName = "virtualReaderName1";
  final String sessionId = "session1";
  final SortedSet<String> groupReferences = Sets.newTreeSet(groupReference);

  NativePoolServerServiceImpl service;

  @Captor ArgumentCaptor<KeypleMessageDto> responseCaptor;

  @Before
  public void setUp() {
    this.init();
    parser = KeypleJsonParser.getParser();
    initMockService();
  }

  @After
  public void tearDown() {}

  @Test
  public void buildService_withAsyncNode() {
    // test
    service =
        (NativePoolServerServiceImpl)
            new NativePoolServerServiceFactory()
                .builder()
                .withReaderPoolPlugin(poolPluginMock)
                .withAsyncNode(asyncServer)
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
                .withReaderPoolPlugin(poolPluginMock)
                .withSyncNode()
                .getService();

    assertThat(service).isNotNull();
    assertThat(service).isEqualTo(NativePoolServerServiceImpl.getInstance());
  }

  @Test(expected = IllegalArgumentException.class)
  public void buildService_withNull_SyncNode_throwIAE() {
    // test
    service =
        (NativePoolServerServiceImpl)
            new NativePoolServerServiceFactory()
                .builder()
                .withReaderPoolPlugin(poolPluginMock)
                .withAsyncNode(null)
                .getService();
  }

  @Test(expected = IllegalArgumentException.class)
  public void buildService_withNull_Plugin_throwIAE() {
    // test
    service =
        (NativePoolServerServiceImpl)
            new NativePoolServerServiceFactory()
                .builder()
                .withReaderPoolPlugin(null)
                .withAsyncNode(asyncServer)
                .getService();
  }

  @Test
  public void onAllocateReader_shouldPropagate_toLocalPoolPlugin() {
    KeypleMessageDto request = getAllocateReaderDto();
    NativePoolServerUtils.getAsyncNode().onMessage(getAllocateReaderDto());

    verify(poolPluginMock, times(1)).allocateReader(groupReference);
    Mockito.verify(asyncServer).sendMessage(responseCaptor.capture());
    KeypleMessageDto response = responseCaptor.getValue();
    assertMetadataMatches(request, response);
  }

  @Test
  public void onReleaseReader_shouldPropagate_toLocalPoolPlugin() {
    KeypleMessageDto request = getReleaseReaderDto();
    NativePoolServerUtils.getAsyncNode().onMessage(request);

    verify(poolPluginMock, times(1)).releaseReader(readerMocked);
    Mockito.verify(asyncServer).sendMessage(responseCaptor.capture());
    KeypleMessageDto response = responseCaptor.getValue();
    assertMetadataMatches(request, response);
  }

  @Test
  public void onGroupReferences_shouldPropagate_toLocalPoolPlugin() {
    KeypleMessageDto request = getGroupReferencesDto();
    NativePoolServerUtils.getAsyncNode().onMessage(request);

    verify(poolPluginMock, times(1)).getReaderGroupReferences();
    Mockito.verify(asyncServer).sendMessage(responseCaptor.capture());
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
    doReturn(groupReferences).when(poolPluginMock).getReaderGroupReferences();
    asyncServer = Mockito.mock(KeypleServerAsync.class);
    // service = Mockito.mock(NativePoolServerServiceImpl.class,
    // withSettings().useConstructor((ReaderPoolPlugin)poolPluginMock));

    service =
        (NativePoolServerServiceImpl)
            new NativePoolServerServiceFactory()
                .builder()
                .withReaderPoolPlugin(poolPluginMock)
                .withAsyncNode(asyncServer)
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
}
