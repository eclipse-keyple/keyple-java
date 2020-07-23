/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.nativese.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.seproxy.*;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remotese.core.*;
import org.eclipse.keyple.plugin.remotese.core.exception.KeypleDoNotPropagateEventException;
import org.eclipse.keyple.plugin.remotese.nativese.NativeSeClientService;
import org.eclipse.keyple.plugin.remotese.nativese.RemoteServiceParameters;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;


@RunWith(MockitoJUnitRunner.class)
public class NativeSeClientServiceFactoryTest extends BaseNativeSeTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractNativeSeServiceTest.class);
    KeypleClientSync syncClient;
    KeypleClientAsync asyncClient;
    KeypleClientReaderEventFilter eventFilter;
    MyKeypleUserData outputData;
    MyKeypleUserData inputData;
    MatchingSeImpl matchingSe;
    PluginFactory mockFactory;
    ReaderEvent readerEvent;
    String pluginName = "mockPlugin";
    String serviceId = "serviceId";
    Gson parser;

    @Before
    public void setUp() {
        this.init();
        mockFactory = Mockito.mock(PluginFactory.class);
        ReaderPlugin readerPlugin = Mockito.mock(ReaderPlugin.class);
        doReturn(readerPlugin).when(mockFactory).getPlugin();
        doReturn(pluginName).when(mockFactory).getPluginName();
        doReturn(pluginName).when(readerPlugin).getName();

        SeProxyService.getInstance().registerPlugin(mockFactory);
        syncClient = Mockito.mock(KeypleClientSync.class);
        asyncClient = Mockito.mock(KeypleClientAsync.class);
        eventFilter = Mockito.mock(KeypleClientReaderEventFilter.class);


        doReturn(getASeResponse()).when(proxyReader).transmitSeRequest(any(SeRequest.class),
                any(ChannelControl.class));
        doReturn(getASeResponse()).when(observableProxyReader)
                .transmitSeRequest(any(SeRequest.class), any(ChannelControl.class));
        doReturn(observableProxyReader).when(readerPlugin).getReader(observableProxyReaderName);
        outputData = new MyKeypleUserData("output1");
        inputData = new MyKeypleUserData("input1");
        matchingSe = new MatchingSeImpl(getASeResponse(), TransmissionMode.CONTACTLESS);
        readerEvent = new ReaderEvent(pluginName, //
                observableProxyReaderName, //
                ReaderEvent.EventType.SE_INSERTED, //
                null);
        parser = KeypleJsonParser.getParser();
    }

    @After
    public void tearDown() {
        SeProxyService.getInstance().unregisterPlugin(pluginName);
    }

    @Test
    public void buildService_withSyncNode_withoutObservation() {
        // test
        NativeSeClientService service = new NativeSeClientServiceFactory().builder()
                .withSyncNode(syncClient).withoutReaderObservation().getService();

        assertThat(service).isNotNull();
        assertThat(service).isEqualTo(NativeSeClientUtils.getService());
    }

    @Test
    public void buildService_withAsyncNode_withoutReaderObservation() {
        // test
        NativeSeClientService service = new NativeSeClientServiceFactory().builder()
                .withAsyncNode(asyncClient).withoutReaderObservation().getService();

        // assert
        assertThat(service).isNotNull();
        assertThat(service).isEqualTo(NativeSeClientUtils.getService());
    }

    @Test(expected = IllegalArgumentException.class)
    public void executeService_withNullParam_throwException() {
        KeypleClientSyncMock clientSyncMock = new KeypleClientSyncMock(1);
        final NativeSeClientService nativeSeClientService = new NativeSeClientServiceFactory()
                .builder().withSyncNode(clientSyncMock).withoutReaderObservation().getService();
        // test
        nativeSeClientService.executeRemoteService(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void executeService_withNullOutputData_throwException() {
        KeypleClientSyncMock clientSyncMock = new KeypleClientSyncMock(1);
        NativeSeClientService nativeSeClientService = new NativeSeClientServiceFactory().builder()
                .withSyncNode(clientSyncMock).withoutReaderObservation().getService();
        RemoteServiceParameters params = RemoteServiceParameters.builder(serviceId, proxyReader)
                .withUserInputData(inputData).build();
        // test
        nativeSeClientService.executeRemoteService(params, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void executeService_activateObservation_withoutFilter_throwException() {
        // init
        KeypleClientSyncMock clientSyncMock = new KeypleClientSyncMock(1);
        new NativeSeClientServiceFactory().builder().withSyncNode(clientSyncMock)
                .withReaderObservation(null).getService();
    }

    @Test
    public void executeService_activateObservation_withFilter_addObserver() {
        // init
        KeypleClientSyncMock clientSyncMock = new KeypleClientSyncMock(1);
        NativeSeClientService nativeSeClientService = new NativeSeClientServiceFactory().builder()
                .withSyncNode(clientSyncMock).withReaderObservation(eventFilter).getService();

        RemoteServiceParameters params =
                RemoteServiceParameters.builder(serviceId, observableProxyReader)//
                        .build();

        // test
        MyKeypleUserData output =
                nativeSeClientService.executeRemoteService(params, MyKeypleUserData.class);


        // verify service is added as observer
        verify(observableProxyReader)
                .addObserver((NativeSeClientServiceImpl) nativeSeClientService);

    }

    @Test(expected = IllegalArgumentException.class)
    public void executeService_activateObservation_onNotObservableReader_throwException() {
        // init
        KeypleClientSyncMock clientSyncMock = new KeypleClientSyncMock(1);
        NativeSeClientService nativeSeClientService = new NativeSeClientServiceFactory().builder()
                .withSyncNode(clientSyncMock).withReaderObservation(eventFilter).getService();
        RemoteServiceParameters params =
                RemoteServiceParameters.builder(serviceId, proxyReader).build();

        // test
        MyKeypleUserData output =
                nativeSeClientService.executeRemoteService(params, MyKeypleUserData.class);

        verify(observableProxyReader, times(1))
                .addObserver((NativeSeClientServiceImpl) nativeSeClientService);

    }


    @Test
    public void executeService_withSyncNode() {
        // init
        KeypleClientSyncMock clientSyncMock = new KeypleClientSyncMock(2);
        NativeSeClientService nativeSeClientService = new NativeSeClientServiceFactory().builder()//
                .withSyncNode(clientSyncMock)//
                .withoutReaderObservation()//
                .getService();

        RemoteServiceParameters params = RemoteServiceParameters.builder(serviceId, proxyReader)//
                .withUserInputData(inputData)//
                .withInitialSeContext(matchingSe)//
                .build();

        // test
        MyKeypleUserData output =
                nativeSeClientService.executeRemoteService(params, MyKeypleUserData.class);

        // verify EXECUTE_REMOTE_SERVICE request
        assertThat(clientSyncMock.getRequests().size()).isEqualTo(2);
        KeypleMessageDto dtoRequest = clientSyncMock.getRequests().get(0);
        assertThat(dtoRequest.getAction())
                .isEqualTo(KeypleMessageDto.Action.EXECUTE_REMOTE_SERVICE.name());
        assertThat(dtoRequest.getSessionId()).isNotEmpty();
        assertThat(dtoRequest.getNativeReaderName()).isEqualTo(nativeReaderName);
        JsonObject body = parser.fromJson(dtoRequest.getBody(), JsonObject.class);
        assertThat(body.get("serviceId").getAsString()).isEqualTo(serviceId);
        assertThat(parser.fromJson(body.get("userInputData"),MyKeypleUserData.class)).isEqualToComparingFieldByFieldRecursively(inputData);
        assertThat(parser.fromJson(body.get("initialSeContent"),MatchingSeImpl.class)).isEqualToComparingFieldByFieldRecursively(matchingSe);

        // verify output
        assertThat(output).isNotNull();
        assertThat(output).isEqualToComparingFieldByField(outputData);

    }

    @Test
    public void onUpdate_doNotPropagateEvent() {
        // init
        KeypleClientSyncMock clientSyncMock = new KeypleClientSyncMock(2);
        NativeSeClientServiceImpl nativeSeClientService =
                (NativeSeClientServiceImpl) new NativeSeClientServiceFactory().builder()//
                        .withSyncNode(clientSyncMock)//
                        .withReaderObservation(new MyEventFilter(false))//
                        .getService();

        // test
        nativeSeClientService.update(readerEvent);

        assertThat(clientSyncMock.getRequests().size()).isEqualTo(0);
    }


    @Test
    public void onUpdate_withSyncNode() {
        // init
        KeypleClientSyncMock clientSyncMock = new KeypleClientSyncMock(2);
        NativeSeClientServiceImpl nativeSeClientService =
                (NativeSeClientServiceImpl) new NativeSeClientServiceFactory().builder()//
                        .withSyncNode(clientSyncMock)//
                        .withReaderObservation(new MyEventFilter(true))//
                        .getService();


        // test
        nativeSeClientService.update(readerEvent);

        // verify READER_EVENT dto
        assertThat(clientSyncMock.getRequests().size()).isEqualTo(2);
        KeypleMessageDto dtoRequest = clientSyncMock.getRequests().get(0);
        assertThat(dtoRequest.getAction()).isEqualTo(KeypleMessageDto.Action.READER_EVENT.name());
        assertThat(dtoRequest.getSessionId()).isNotEmpty();
        assertThat(dtoRequest.getNativeReaderName()).isEqualTo(observableProxyReaderName);
        JsonObject body =
                KeypleJsonParser.getParser().fromJson(dtoRequest.getBody(), JsonObject.class);
        assertThat(KeypleJsonParser.getParser().fromJson(body.get("readerEvent").toString(),
                ReaderEvent.class)).isEqualToComparingFieldByField(readerEvent);
        assertThat(parser.fromJson(body.get("userInputData"),MyKeypleUserData.class))
                .isEqualToComparingFieldByFieldRecursively(inputData);

        // output is verified in eventFilter
    }

    /*
     * Helper
     */


    class KeypleClientSyncMock implements KeypleClientSync {

        final List<KeypleMessageDto> requests = new ArrayList<KeypleMessageDto>();
        Integer answerNumber;
        Integer answerIt;


        KeypleClientSyncMock(Integer answerNumber) {
            this.answerNumber = answerNumber;
            this.answerIt = 0;
        }

        @Override
        public List<KeypleMessageDto> sendRequest(KeypleMessageDto msg) {
            logger.trace("Mock send a KeypleMessageDto request {}", msg);
            requests.add(msg);

            List<KeypleMessageDto> responses = new ArrayList<KeypleMessageDto>();
            if (answerNumber == 1) {
                responses.add(terminateDto());
            }
            if (answerNumber > 1) {
                responses.add(getTransmitDto());
                this.answerNumber--;
            }
            return responses;
        }

        public List<KeypleMessageDto> getRequests() {
            return requests;
        }
    }


    public class MyKeypleUserData  {
        final String field;
        final Integer field2 = 2;
        MyKeypleUserData(String field) {
            this.field = field;
        }
    }


    public interface ObservableProxyReader extends ProxyReader, ObservableReader {
    };

    public class MatchingSeImpl extends AbstractMatchingSe {

        /**
         * Constructor.
         *
         * @param selectionResponse the response from the SE
         * @param transmissionMode the transmission mode, contact or contactless
         */
        protected MatchingSeImpl(SeResponse selectionResponse, TransmissionMode transmissionMode) {
            super(selectionResponse, transmissionMode);
        }
    }



    class MyEventFilter implements KeypleClientReaderEventFilter<MyKeypleUserData> {
        Boolean propagateEvent;

        MyEventFilter(Boolean propagateEvent) {
            this.propagateEvent = propagateEvent;
        }

        @Override
        public Type getUserOutputType() {
            return MyKeypleUserData.class;
        }

        @Override
        public MyKeypleUserData beforePropagation(ReaderEvent event) {
            if (propagateEvent) {
                return inputData;
            } else {
                throw new KeypleDoNotPropagateEventException("do not propagate event");
            }
        }

        @Override
        public void afterPropagation(MyKeypleUserData userOutputData) {
            assertThat(userOutputData).isNotNull();
            assertThat(userOutputData).isEqualToComparingFieldByFieldRecursively(outputData);
        }
    }

    public KeypleMessageDto terminateDto() {
        JsonObject body = new JsonObject();
        body.add("userOutputData", parser.toJsonTree(outputData, MyKeypleUserData.class));
        return new KeypleMessageDto().setAction(KeypleMessageDto.Action.TERMINATE_SERVICE.name())
                .setBody(body.toString());

    }
}
