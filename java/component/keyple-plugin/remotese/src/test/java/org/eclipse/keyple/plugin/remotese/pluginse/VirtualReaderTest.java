/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.pluginse;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.plugin.remotese.integration.Integration;
import org.eclipse.keyple.plugin.remotese.integration.VirtualReaderBaseTest;
import org.eclipse.keyple.plugin.remotese.rm.json.SampleFactory;
import org.eclipse.keyple.plugin.stub.StubProtocolSetting;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.plugin.stub.StubReaderTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit Test Virtual Reader -> Native Reader
 */
public class VirtualReaderTest extends VirtualReaderBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(VirtualReaderTest.class);

    final String NATIVE_READER_NAME = "testStubReader";
    final String CLIENT_NODE_ID = "testClientNodeId";
    final String SERVER_NODE_ID = "testServerNodeId";

    final long RPC_TIMEOUT = 1000;

    private VirtualReader virtualReader;
    private ProxyReader nativeReader;


    @Before
    public void setUp() throws Exception {
        Assert.assertEquals(0, SeProxyService.getInstance().getPlugins().size());

        initMasterNSlave();

        // configure and connect a Mock Reader
        nativeReader = connectMockReader("mockReader");


        // get the paired virtual reader
        virtualReader = getVirtualReader();

    }

    @After
    public void tearDown() throws Exception {
        disconnectReader(NATIVE_READER_NAME);
        clearMasterNSlave();
        unregisterPlugins();
        Assert.assertEquals(0, SeProxyService.getInstance().getPlugins().size());
    }

    @Test(expected = KeypleReaderException.class)
    public void transmit_timeout() throws KeypleReaderException {
        MasterAPI masterAPI = new MasterAPI(SeProxyService.getInstance(),
                Integration.getFakeDtoNode(), RPC_TIMEOUT);

        RemoteSePluginImpl plugin = (RemoteSePluginImpl) masterAPI.getPlugin();

        ProxyReader reader = plugin.createVirtualReader(CLIENT_NODE_ID, NATIVE_READER_NAME,
                Integration.getFakeDtoNode(), TransmissionMode.CONTACTLESS,
                new HashMap<String, String>());

        reader.transmitSet(StubReaderTest.getRequestIsoDepSetSample());
    }

    /**
     * Successful Transmit with MultiSeRequestProcessing and ChannelControl
     * @throws Exception
     */
    @Test
    public void transmitSet_withParameters() throws Exception {
        Set<SeRequest> requestSet = SampleFactory.getASeRequestSet();

        //test transmitSet with Parameters
        ((ProxyReader) virtualReader)
            .transmitSet(
                    requestSet,
                    MultiSeRequestProcessing.FIRST_MATCH,
                    ChannelControl.CLOSE_AFTER);

        //condition -> the nativeReader execute the method executed on the virtual reader
        verify(nativeReader, times(1))
                .transmitSet(
                        requestSet,
                        MultiSeRequestProcessing.FIRST_MATCH,
                        ChannelControl.CLOSE_AFTER);
    }


    protected ProxyReader connectMockReader(String readerName) throws Exception {

        // configure mock native reader
        ProxyReader mockReader = Mockito.spy(ProxyReader.class);
        doReturn(readerName).when(mockReader).getName();
        doReturn(TransmissionMode.CONTACTLESS).when(mockReader).getTransmissionMode();
        doReturn(new ArrayList<SeResponse>()).when(mockReader).transmitSet(ArgumentMatchers.<SeRequest>anySet(),any(MultiSeRequestProcessing.class),any(ChannelControl.class));

        //Configure slaveAPI to find mockReader
        doReturn(mockReader).when(slaveAPI).findLocalReader(readerName);

        this.slaveAPI.connectReader(mockReader);
        return mockReader;
    }




}
