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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.keyple.core.card.message.*;
import org.eclipse.keyple.core.card.selection.AbstractSmartCard;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.PluginFactory;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remote.LocalServiceClient;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.ObservableReaderEventFilter;
import org.eclipse.keyple.plugin.remote.RemoteServiceParameters;
import org.eclipse.keyple.plugin.remote.exception.KeypleDoNotPropagateEventException;
import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointClient;
import org.eclipse.keyple.plugin.remote.spi.SyncEndpointClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class LocalServiceClientTest extends BaseLocalTest {

  private static final Logger logger = LoggerFactory.getLogger(AbstractLocalServiceTest.class);
  SyncEndpointClient syncClientEndpoint;
  AsyncEndpointClient asyncClient;
  ObservableReaderEventFilter eventFilter;
  MyKeypleUserData outputData;
  MyKeypleUserData inputData;
  MatchingSeImpl matchingCard;
  PluginFactory mockFactory;
  ReaderEvent readerEvent;
  String pluginName = "mockPlugin";
  String serviceId = "serviceId";
  String remoteReaderName = "remoteReaderName";
  Gson parser;

  @Before
  public void setUp() {
    this.init();
    mockFactory = Mockito.mock(PluginFactory.class);
    Plugin readerPlugin = Mockito.mock(Plugin.class);
    doReturn(readerPlugin).when(mockFactory).getPlugin();
    doReturn(pluginName).when(mockFactory).getPluginName();
    doReturn(pluginName).when(readerPlugin).getName();

    SmartCardService.getInstance().registerPlugin(mockFactory);
    syncClientEndpoint = Mockito.mock(SyncEndpointClient.class);
    asyncClient = Mockito.mock(AsyncEndpointClient.class);
    eventFilter = Mockito.mock(ObservableReaderEventFilter.class);

    doReturn(getACardResponse())
        .when(readerMocked)
        .transmitCardRequest(any(CardRequest.class), any(ChannelControl.class));
    doReturn(getACardResponse())
        .when(observableReaderMocked)
        .transmitCardRequest(any(CardRequest.class), any(ChannelControl.class));
    doReturn(observableReaderMocked).when(readerPlugin).getReader(observableReaderName);
    outputData = new MyKeypleUserData("output1");
    inputData = new MyKeypleUserData("input1");
    matchingCard = new MatchingSeImpl(getACardSelectionResponse());
    readerEvent =
        new ReaderEvent(
            pluginName, //
            observableReaderName, //
            ReaderEvent.EventType.CARD_INSERTED, //
            null);
    parser = KeypleJsonParser.getParser();
  }

  @After
  public void tearDown() {
    SmartCardService.getInstance().unregisterPlugin(pluginName);
  }

  @Test
  public void buildService_withSyncNode_withoutObservation() {
    // test
    LocalServiceClient service =
        new LocalServiceClientFactory()
            .builder()
            .withSyncNode(syncClientEndpoint)
            .withoutReaderObservation()
            .getService();

    assertThat(service).isNotNull();
    assertThat(service).isEqualTo(LocalServiceClientUtils.getLocalService());
  }

  @Test
  public void buildService_withAsyncNode_withoutReaderObservation() {
    // test
    LocalServiceClient service =
        new LocalServiceClientFactory()
            .builder()
            .withAsyncNode(asyncClient)
            .usingDefaultTimeout()
            .withoutReaderObservation()
            .getService();

    // assert
    assertThat(service).isNotNull();
    assertThat(service).isEqualTo(LocalServiceClientUtils.getLocalService());
  }

  @Test(expected = IllegalArgumentException.class)
  public void executeService_withNullParam_throwException() {
    syncClientEndpoint = new SyncEndpointClientMock(1);
    final LocalServiceClient localServiceClient =
        new LocalServiceClientFactory()
            .builder()
            .withSyncNode(syncClientEndpoint)
            .withoutReaderObservation()
            .getService();
    // test
    localServiceClient.executeRemoteService(null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void executeService_withNullOutputData_throwException() {
    syncClientEndpoint = new SyncEndpointClientMock(1);
    LocalServiceClient localServiceClient =
        new LocalServiceClientFactory()
            .builder()
            .withSyncNode(syncClientEndpoint)
            .withoutReaderObservation()
            .getService();
    RemoteServiceParameters params =
        RemoteServiceParameters.builder(serviceId, readerMocked)
            .withUserInputData(inputData)
            .build();
    // test
    localServiceClient.executeRemoteService(params, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void executeService_activateObservation_withoutFilter_throwException() {
    // init
    syncClientEndpoint = new SyncEndpointClientMock(1);
    new LocalServiceClientFactory()
        .builder()
        .withSyncNode(syncClientEndpoint)
        .withReaderObservation(null)
        .getService();
  }

  @Test
  public void executeService_activateObservation_withFilter_addObserver() {
    // init
    syncClientEndpoint = new SyncEndpointClientMock(1);
    LocalServiceClient localServiceClient =
        new LocalServiceClientFactory()
            .builder()
            .withSyncNode(syncClientEndpoint)
            .withReaderObservation(eventFilter)
            .getService();

    RemoteServiceParameters params =
        RemoteServiceParameters.builder(serviceId, observableReaderMocked) //
            .build();

    // test
    localServiceClient.executeRemoteService(params, MyKeypleUserData.class);

    // verify service is added as observer
    verify(observableReaderMocked).addObserver((LocalServiceClientImpl) localServiceClient);
  }

  @Test(expected = IllegalArgumentException.class)
  public void executeService_activateObservation_onNotObservableReader_throwException() {
    // init
    syncClientEndpoint = new SyncEndpointClientMock(1);
    LocalServiceClient localServiceClient =
        new LocalServiceClientFactory()
            .builder()
            .withSyncNode(syncClientEndpoint)
            .withReaderObservation(eventFilter)
            .getService();
    RemoteServiceParameters params =
        RemoteServiceParameters.builder(serviceId, readerMocked).build();

    // test
    localServiceClient.executeRemoteService(params, MyKeypleUserData.class);

    verify(observableReaderMocked, times(1))
        .addObserver((LocalServiceClientImpl) localServiceClient);
  }

  @Test
  public void executeService_withSyncNode() {
    // init
    syncClientEndpoint = new SyncEndpointClientMock(2);
    LocalServiceClient localServiceClient =
        new LocalServiceClientFactory()
            .builder() //
            .withSyncNode(syncClientEndpoint) //
            .withoutReaderObservation() //
            .getService();

    RemoteServiceParameters params =
        RemoteServiceParameters.builder(serviceId, readerMocked) //
            .withUserInputData(inputData) //
            .withInitialSeContext(matchingCard) //
            .build();

    // test
    MyKeypleUserData output =
        localServiceClient.executeRemoteService(params, MyKeypleUserData.class);

    // verify EXECUTE_REMOTE_SERVICE request
    assertThat(((SyncEndpointClientMock) syncClientEndpoint).getRequests().size()).isEqualTo(2);
    MessageDto dtoRequest = ((SyncEndpointClientMock) syncClientEndpoint).getRequests().get(0);
    assertThat(dtoRequest.getAction()).isEqualTo(MessageDto.Action.EXECUTE_REMOTE_SERVICE.name());
    assertThat(dtoRequest.getSessionId()).isNotEmpty();
    assertThat(dtoRequest.getLocalReaderName()).isEqualTo(readerName);
    JsonObject body = parser.fromJson(dtoRequest.getBody(), JsonObject.class);
    assertThat(body.get("serviceId").getAsString()).isEqualTo(serviceId);
    assertThat(parser.fromJson(body.get("userInputData"), MyKeypleUserData.class))
        .isEqualToComparingFieldByFieldRecursively(inputData);
    assertThat(parser.fromJson(body.get("initialCardContent"), MatchingSeImpl.class))
        .isEqualToComparingFieldByFieldRecursively(matchingCard);

    // verify output
    assertThat(output).isNotNull();
    assertThat(output).isEqualToComparingFieldByField(outputData);
  }

  @Test
  public void onUpdate_doNotPropagateEvent() {
    // init
    syncClientEndpoint = new SyncEndpointClientMock(2);
    LocalServiceClientImpl localClientService =
        (LocalServiceClientImpl)
            new LocalServiceClientFactory()
                .builder() //
                .withSyncNode(syncClientEndpoint) //
                .withReaderObservation(new MyEventFilter(false)) //
                .getService();

    // test
    localClientService.update(readerEvent);

    assertThat(((SyncEndpointClientMock) syncClientEndpoint).getRequests().size()).isEqualTo(0);
  }

  @Test
  public void onUpdate_withSyncNode_unregisterReader()
      throws NoSuchFieldException, IllegalAccessException {
    // init
    syncClientEndpoint = new SyncEndpointClientMock(2);
    LocalServiceClientImpl localClientService =
        (LocalServiceClientImpl)
            new LocalServiceClientFactory()
                .builder() //
                .withSyncNode(syncClientEndpoint) //
                .withReaderObservation(new MyEventFilter(true)) //
                .getService();

    // send a readerEvent
    localClientService.update(readerEvent);

    // verify READER_EVENT dto
    assertThat(((SyncEndpointClientMock) syncClientEndpoint).getRequests().size()).isEqualTo(2);
    MessageDto dtoRequest = ((SyncEndpointClientMock) syncClientEndpoint).getRequests().get(0);
    assertThat(dtoRequest.getAction()).isEqualTo(MessageDto.Action.READER_EVENT.name());
    assertThat(dtoRequest.getSessionId()).isNotEmpty();
    assertThat(dtoRequest.getLocalReaderName()).isEqualTo(observableReaderName);
    JsonObject body = KeypleJsonParser.getParser().fromJson(dtoRequest.getBody(), JsonObject.class);
    assertThat(
            KeypleJsonParser.getParser()
                .fromJson(body.get("readerEvent").toString(), ReaderEvent.class))
        .isEqualToComparingFieldByField(readerEvent);
    assertThat(parser.fromJson(body.get("userInputData"), MyKeypleUserData.class))
        .isEqualToComparingFieldByFieldRecursively(inputData);

    // output is verified in eventFilter

    // assert that remote reader is unregister as required by the terminate service
    assertThat(getRemoteReaders(localClientService)).hasSize(0);
  }

  /*
   *
   * Helper
   *
   * */

  public static Map<String, String> getRemoteReaders(LocalServiceClient service) {
    try {
      Field privateStringField = LocalServiceClientImpl.class.getDeclaredField("remoteReaders");
      privateStringField.setAccessible(true);
      return (Map<String, String>) privateStringField.get(service);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }
    return null;
  }

  class SyncEndpointClientMock implements SyncEndpointClient {

    final List<MessageDto> requests = new ArrayList<MessageDto>();
    Integer answerNumber;
    Integer answerIt;

    SyncEndpointClientMock(Integer answerNumber) {
      this.answerNumber = answerNumber;
      this.answerIt = 0;
    }

    @Override
    public List<MessageDto> sendRequest(MessageDto msg) {
      logger.trace("Mock send a MessageDto request {}", msg);
      requests.add(msg);

      List<MessageDto> responses = new ArrayList<MessageDto>();
      if (answerNumber == 1) {
        responses.add(getTerminateDto(msg.getSessionId(), true));
      }
      if (answerNumber > 1) {
        responses.add(getTransmitDto(msg.getSessionId()));
        this.answerNumber--;
      }
      return responses;
    }

    public List<MessageDto> getRequests() {
      return requests;
    }
  }

  public static class MyKeypleUserData {
    final String field;

    MyKeypleUserData(String field) {
      this.field = field;
    }
  }

  public interface ObservableProxyReader extends ProxyReader, ObservableReader {}

  public static class MatchingSeImpl extends AbstractSmartCard {

    /**
     * Constructor.
     *
     * @param selectionResponse the response from the card
     */
    protected MatchingSeImpl(CardSelectionResponse selectionResponse) {
      super(selectionResponse);
    }
  }

  class MyEventFilter implements ObservableReaderEventFilter {

    Boolean propagateEvent;

    MyEventFilter(Boolean propagateEvent) {
      this.propagateEvent = propagateEvent;
    }

    @Override
    public Object beforePropagation(ReaderEvent event) {
      if (propagateEvent) {
        return inputData;
      } else {
        throw new KeypleDoNotPropagateEventException("do not propagate event");
      }
    }

    @Override
    public Class<?> getUserOutputDataClass() {
      return MyKeypleUserData.class;
    }

    @Override
    public void afterPropagation(Object userOutputData) {
      assertThat(userOutputData).isNotNull();
      assertThat(userOutputData).isEqualToComparingFieldByFieldRecursively(outputData);
    }
  }

  public MessageDto getTerminateDto(String sessionId, boolean unregister) {
    JsonObject body = new JsonObject();
    body.addProperty("userOutputData", parser.toJson(outputData, MyKeypleUserData.class));
    body.addProperty("unregisterRemoteReader", parser.toJson(unregister, Boolean.class));
    return new MessageDto()
        .setSessionId(sessionId) //
        .setAction(MessageDto.Action.TERMINATE_SERVICE.name()) //
        .setServerNodeId("serverNodeId") //
        .setClientNodeId("clientNodeId") //
        .setLocalReaderName(readerName)
        .setRemoteReaderName(remoteReaderName)
        .setBody(body.toString());
  }
}
