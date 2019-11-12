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


import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.event.DefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.plugin.remotese.integration.Integration;
import org.eclipse.keyple.plugin.remotese.integration.VirtualReaderBaseTest;
import org.eclipse.keyple.plugin.remotese.rm.json.SampleFactory;
import org.eclipse.keyple.plugin.stub.StubReaderTest;
import org.junit.*;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * Unit Test Observable Virtual Reader
 */
public class VirtualObservableReaderTest extends VirtualReaderBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(VirtualObservableReaderTest.class);

    private VirtualObservableReader virtualReader;
    private ObservableReader nativeReader;

    @Before
    public void setUp() throws Exception {
        Assert.assertEquals(0, SeProxyService.getInstance().getPlugins().size());
        initMasterNSlave();

        // configure and connect a Mock Reader
        nativeReader = connectMockObservableReader("mockObservableReader");


        // get the paired virtual reader
        virtualReader = (VirtualObservableReader) getVirtualReader();

    }

    @After
    public void tearDown() throws Exception {
        Integration.unregisterAllPlugin(RemoteSePluginImpl.DEFAULT_PLUGIN_NAME);

        Assert.assertEquals(0, SeProxyService.getInstance().getPlugins().size());
    }


    /**
     * Successful setDefaultSelectionRequest with NotificationMode
     *
     * @throws Exception
     */
    @Test
    @Ignore
    // TODO Mock does not work, see this#connectMockReader()
    // execute at hand and check logs
    public void setDefaultSelectionRequest_withNotificationMode() throws Exception {

        DefaultSelectionsRequest defaultSelectionsRequest = Mockito.mock(DefaultSelectionsRequest.class);

        // test setDefaultSelectionRequest with NotificationMode
        (virtualReader).setDefaultSelectionRequest(defaultSelectionsRequest, ObservableReader.NotificationMode.MATCHED_ONLY);


        // condition -> the nativeReader execute the method executed on the virtual reader
        verify(nativeReader, times(1)).setDefaultSelectionRequest(defaultSelectionsRequest,ObservableReader.NotificationMode.MATCHED_ONLY);
    }


    protected ObservableReader connectMockObservableReader(String readerName) throws Exception {

        // configure mock native reader
        ObservableReader mockReader = Mockito.spy(ObservableReader.class);
        doReturn(readerName).when(mockReader).getName();
        doReturn(TransmissionMode.CONTACTLESS).when(mockReader).getTransmissionMode();


        // Configure slaveAPI to find mockReader
        // TODO : findLocalReader real method is called, the mock does not work maybe due to
        // multiple thread...
        doReturn(mockReader).when(slaveAPI).findLocalReader(any(String.class));
        doCallRealMethod().when(slaveAPI).connectReader(any(SeReader.class));

        slaveAPI.connectReader(mockReader);

        return mockReader;
    }

}
