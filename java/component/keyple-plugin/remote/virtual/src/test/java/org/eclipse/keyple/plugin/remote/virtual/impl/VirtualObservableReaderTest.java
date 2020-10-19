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
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.google.gson.JsonObject;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ObservableReader.PollingMode;
import org.eclipse.keyple.core.seproxy.event.ObservableReader.ReaderObserver;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsRequest;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remote.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remote.core.impl.AbstractKeypleNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VirtualObservableReaderTest {

  static final String pluginName = "pluginName";
  static final String nativeReaderName = "nativeReaderName";

  VirtualObservableReader reader;
  AbstractKeypleNode node;
  ReaderObserver observer;

  static final DefaultSelectionsRequest abstractDefaultSelectionsRequest =
      SampleFactory.getSelectionRequest();;
  static final ObservableReader.NotificationMode notificationMode =
      SampleFactory.getNotificationMode();;
  static final PollingMode pollingMode = PollingMode.REPEATING;;
  static final ReaderEvent event =
      new ReaderEvent(pluginName, nativeReaderName, ReaderEvent.EventType.SE_INSERTED, null);;
  static final KeypleMessageDto response =
      new KeypleMessageDto().setAction(KeypleMessageDto.Action.SET_DEFAULT_SELECTION.name());;

  static final ExecutorService notificationPool = Executors.newCachedThreadPool();;

  final ArgumentCaptor<KeypleMessageDto> messageArgumentCaptor =
      ArgumentCaptor.forClass(KeypleMessageDto.class);

  @Before
  public void setUp() {
    node = Mockito.mock(AbstractKeypleNode.class);
    doReturn(response).when(node).sendRequest(any(KeypleMessageDto.class));
    observer = new MockObserver();
    reader =
        new VirtualObservableReader(
            pluginName, nativeReaderName, node, "sessionId", null, notificationPool);
  }

  @Test
  public void addObserver_count_removeObserver() {
    assertThat(reader.countObservers()).isEqualTo(0);
    reader.addObserver(observer);
    assertThat(reader.countObservers()).isEqualTo(1);
    reader.removeObserver(observer);
    assertThat(reader.countObservers()).isEqualTo(0);
    reader.addObserver(observer);
    assertThat(reader.countObservers()).isEqualTo(1);
    reader.clearObservers();
    assertThat(reader.countObservers()).isEqualTo(0);
  }

  @Test
  public void notifyEvent_to_OneObserver() {
    reader.addObserver(observer);
    reader.notifyObservers(event);
    await()
        .atMost(1, TimeUnit.SECONDS)
        .until(
            new Callable<Boolean>() {
              @Override
              public Boolean call() throws Exception {
                return ((MockObserver) observer).getEvent().equals(event);
              }
            });
  }

  @Test
  public void notifyEvent_to_ZeroObserver_doNothing() {
    reader.notifyObservers(event);
  }

  @Test
  public void setDefaultSelectionRequest_shouldSendDto() {
    reader.setDefaultSelectionRequest(
        abstractDefaultSelectionsRequest, notificationMode, pollingMode);
    verify(node).sendRequest(messageArgumentCaptor.capture());
    KeypleMessageDto request = messageArgumentCaptor.getValue();
    assertThat(request.getAction()).isEqualTo(KeypleMessageDto.Action.SET_DEFAULT_SELECTION.name());
    JsonObject body = KeypleJsonParser.getParser().fromJson(request.getBody(), JsonObject.class);
    assertThat(
            KeypleJsonParser.getParser()
                .fromJson(body.get("defaultSelectionsRequest"), DefaultSelectionsRequest.class))
        .isEqualToComparingFieldByFieldRecursively(abstractDefaultSelectionsRequest);
    assertThat(
            ObservableReader.NotificationMode.valueOf(body.get("notificationMode").getAsString()))
        .isEqualToComparingFieldByFieldRecursively(notificationMode);
    assertThat(PollingMode.valueOf(body.get("pollingMode").getAsString()))
        .isEqualToComparingFieldByFieldRecursively(pollingMode);
  }

  @Test(expected = IllegalArgumentException.class)
  public void setDefaultSelectionRequest_withNullDefaultSelectionRequest_shouldThrowIAE() {
    reader.setDefaultSelectionRequest(null, notificationMode, pollingMode);
  }

  @Test(expected = IllegalArgumentException.class)
  public void setDefaultSelectionRequest_withNullNotificationMode_shouldThrowIAE() {
    reader.setDefaultSelectionRequest(abstractDefaultSelectionsRequest, null);
  }

  @Test
  public void startSeDetection_shouldSendDto() {
    reader.startSeDetection(pollingMode);
    verify(node).sendRequest(messageArgumentCaptor.capture());
    KeypleMessageDto request = messageArgumentCaptor.getValue();
    assertThat(request.getAction()).isEqualTo(KeypleMessageDto.Action.START_CARD_DETECTION.name());
    JsonObject body = KeypleJsonParser.getParser().fromJson(request.getBody(), JsonObject.class);
    assertThat(
            KeypleJsonParser.getParser()
                .fromJson(body.get("pollingMode").getAsString(), PollingMode.class))
        .isEqualToComparingFieldByFieldRecursively(pollingMode);
  }

  @Test
  public void stopSeDetection_shouldSendDto() {
    reader.stopSeDetection();
    verify(node).sendRequest(messageArgumentCaptor.capture());
    KeypleMessageDto request = messageArgumentCaptor.getValue();
    assertThat(request.getAction()).isEqualTo(KeypleMessageDto.Action.STOP_CARD_DETECTION.name());
    JsonObject body = KeypleJsonParser.getParser().fromJson(request.getBody(), JsonObject.class);
    assertThat(body).isNull();
  }

  @Test
  public void finalizeSeProcessing_shouldSendDto() {
    reader.finalizeSeProcessing();
    verify(node).sendRequest(messageArgumentCaptor.capture());
    KeypleMessageDto request = messageArgumentCaptor.getValue();
    assertThat(request.getAction())
        .isEqualTo(KeypleMessageDto.Action.FINALIZE_CARD_PROCESSING.name());
    JsonObject body = KeypleJsonParser.getParser().fromJson(request.getBody(), JsonObject.class);
    assertThat(body).isNull();
  }

  private static class MockObserver implements ReaderObserver {

    ReaderEvent event;

    @Override
    public void update(ReaderEvent event) {
      this.event = event;
    }

    public ReaderEvent getEvent() {
      return event;
    }
  }
}
