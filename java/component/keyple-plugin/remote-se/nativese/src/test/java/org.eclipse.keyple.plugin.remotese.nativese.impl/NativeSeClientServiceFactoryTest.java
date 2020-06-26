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
import org.eclipse.keyple.plugin.remotese.core.KeypleClientAsync;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientSync;
import org.eclipse.keyple.plugin.remotese.core.impl.ServerPushEventStrategy;
import org.eclipse.keyple.plugin.remotese.nativese.NativeSeClientService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class NativeSeClientServiceFactoryTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractNativeSeServiceTest.class);

    @Test
    public void buildService_withSyncNode_withoutObservation() throws Exception {
        // init
        KeypleClientSync syncClient = Mockito.mock(KeypleClientSync.class);
        // test
        NativeSeClientService service = new NativeSeClientServiceFactory().builder()
                .withSyncNode(syncClient)
                .withoutPluginObservation()
                .withoutReaderObservation()
                .getService();

        // assert
        assertThat(service).isNotNull();
    }

    @Test
    public void buildService_withSyncNode_withoutPluginObservation_withReaderObservation() throws Exception {
        // init
        KeypleClientSync syncClient = Mockito.mock(KeypleClientSync.class);
        // test
        NativeSeClientService service = new NativeSeClientServiceFactory().builder()
                .withSyncNode(syncClient)
                .withoutPluginObservation()
                .withReaderObservation(new ServerPushEventStrategy(ServerPushEventStrategy.Type.LONG_POLLING))
                .getService();

        // assert
        assertThat(service).isNotNull();

    }

    @Test
    public void buildService_withAsyncNode__withPluginObservation_withoutReaderObservation() throws Exception {
        // init
        KeypleClientAsync asyncClient = Mockito.mock(KeypleClientAsync.class);
        // test
        NativeSeClientService service = new NativeSeClientServiceFactory().builder()
                .withAsyncNode(asyncClient).withPluginObservation().withoutReaderObservation().getService();

        // assert
        assertThat(service).isNotNull();

    }

    @Test
    public void buildService_withAsyncNode__withoutPluginObservation_withReaderObservation() throws Exception {
        // init
        KeypleClientAsync asyncClient = Mockito.mock(KeypleClientAsync.class);
        // test
        NativeSeClientService service = new NativeSeClientServiceFactory().builder()
                .withAsyncNode(asyncClient).withoutPluginObservation().withReaderObservation().getService();

        // assert
        assertThat(service).isNotNull();

    }

}
