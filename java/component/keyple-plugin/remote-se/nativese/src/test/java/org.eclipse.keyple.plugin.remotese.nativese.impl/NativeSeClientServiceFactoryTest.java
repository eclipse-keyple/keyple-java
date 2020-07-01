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

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientAsync;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientSync;
import org.eclipse.keyple.plugin.remotese.core.KeypleUserData;
import org.eclipse.keyple.plugin.remotese.core.KeypleUserDataFactory;
import org.eclipse.keyple.plugin.remotese.nativese.NativeSeClientService;
import org.eclipse.keyple.plugin.remotese.nativese.RemoteServiceParameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class NativeSeClientServiceFactoryTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractNativeSeServiceTest.class);
    KeypleClientSync syncClient;
    KeypleClientAsync asyncClient;
    ProxyReader nativeReader;
    AbstractMatchingSe matchingSe;
    KeypleUserDataFactory keypleUserDataFactory;

    @Before
    public void setUp() {
        syncClient = Mockito.mock(KeypleClientSync.class);
        asyncClient = Mockito.mock(KeypleClientAsync.class);
        nativeReader = Mockito.mock(ProxyReader.class);
        matchingSe = Mockito.mock(AbstractMatchingSe.class);
        keypleUserDataFactory = Mockito.mock(KeypleUserDataFactory.class);
    }



    @Test
    public void buildService_withSyncNode_withoutObservation() {
        // test
        NativeSeClientService service = new NativeSeClientServiceFactory().builder()
                .withSyncNode(syncClient).withoutReaderObservation().getService();

        // assert
        assertThat(service).isNotNull();
    }

    @Test
    public void buildService_withSyncNode__withReaderObservation() {
        // test
        NativeSeClientService service =
                new NativeSeClientServiceFactory().builder().withSyncNode(syncClient)
                        .withReaderObservation(Mockito.mock(
                                NativeSeClientServiceFactory.KeypleClientReaderEventFilter.class))
                        .getService();

        // assert
        assertThat(service).isNotNull();

    }

    @Test
    public void buildService_withAsyncNode_withoutReaderObservation() {

        // test
        NativeSeClientService service = new NativeSeClientServiceFactory().builder()
                .withAsyncNode(asyncClient).withoutReaderObservation().getService();

        // assert
        assertThat(service).isNotNull();

    }

    @Test
    public void buildService_withAsyncNode_withReaderObservation() {
        // test
        NativeSeClientService service =
                new NativeSeClientServiceFactory().builder().withAsyncNode(asyncClient)
                        .withReaderObservation(Mockito.mock(
                                NativeSeClientServiceFactory.KeypleClientReaderEventFilter.class))
                        .getService();

        // assert
        assertThat(service).isNotNull();

    }


    public void executeService_withAsyncNode() {
        // init
        NativeSeClientService nativeSeClientService = new NativeSeClientServiceFactory().builder()
                .withAsyncNode(asyncClient).withoutReaderObservation().getService();

        RemoteServiceParameters params =
                RemoteServiceParameters.builder("SERVICE_ID_1", nativeReader).build();

        // test
        KeypleUserData output =
                nativeSeClientService.executeRemoteService(params, keypleUserDataFactory);

        // assert
        assertThat(output).isNotNull();

    }



    /*
     * Helper
     */


}
