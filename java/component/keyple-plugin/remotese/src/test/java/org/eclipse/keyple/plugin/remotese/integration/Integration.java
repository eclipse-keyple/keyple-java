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
package org.eclipse.keyple.plugin.remotese.integration;


import org.eclipse.keyple.core.seproxy.ReaderPoolPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.nativese.SlaveAPI;
import org.eclipse.keyple.plugin.remotese.pluginse.MasterAPI;
import org.eclipse.keyple.plugin.remotese.transport.DtoHandler;
import org.eclipse.keyple.plugin.remotese.transport.DtoNode;
import org.eclipse.keyple.plugin.remotese.transport.impl.java.LocalTransportDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubPoolPlugin;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.junit.Assert;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedSet;
import java.util.TreeSet;

public class Integration {

    private static final Logger logger = LoggerFactory.getLogger(Integration.class);


    /**
     * Create a Spy MasterAPI
     * 
     * @param node
     * @return
     */
    public static MasterAPI bindMasterSpy(DtoNode node) {
        // Create Master services : masterAPI
        MasterAPI masterAPI = new MasterAPI(SeProxyService.getInstance(), node);
        return Mockito.spy(masterAPI);
    }

    /**
     * Create a Spy SlaveAPI
     * 
     * @param node
     * @return
     */
    public static SlaveAPI bindSlaveSpy(DtoNode node, String masterNodeId) {
        // Binds node for outgoing KeypleDto
        SlaveAPI slaveAPI = new SlaveAPI(SeProxyService.getInstance(), node, masterNodeId);
        SlaveAPI spy = Mockito.spy(slaveAPI);
        // Binds node for incoming KeypleDTo
        // spy.bindDtoEndpoint(node);
        return spy;
    }

    /**
     * Create a Stub reader
     * 
     * @param stubReaderName
     * @return
     * @throws InterruptedException
     * @throws KeypleReaderNotFoundException
     */
    public static StubReader createStubReader(String stubReaderName,
            TransmissionMode transmissionMode)
            throws InterruptedException, KeypleReaderNotFoundException {
        SeProxyService seProxyService = SeProxyService.getInstance();

        StubPlugin stubPlugin = StubPlugin.getInstance();
        seProxyService.addPlugin(stubPlugin);

        // add an stubPluginObserver to start the plugin monitoring thread
        // stubPlugin.addObserver(observer); //do not observe so the monitoring thread is not
        // created

        logger.debug("Stub plugin count observers : {}", stubPlugin.countObservers());

        logger.debug("Create a new StubReader : {}", stubReaderName);
        stubPlugin.plugStubReader(stubReaderName, transmissionMode, true);

        Thread.sleep(100);

        // Get the created proxy reader
        return (StubReader) stubPlugin.getReader(stubReaderName);
    }

    /**
     * Create a Stub Reader Pool Plugin
     *
     * @return
     * @throws InterruptedException
     * @throws KeypleReaderNotFoundException
     */
    public static ReaderPoolPlugin createReaderPoolStub()
            throws InterruptedException, KeypleReaderNotFoundException {

        SortedSet<String> groupReferences = new TreeSet<String>();
        StubPoolPlugin poolPlugin = new StubPoolPlugin(groupReferences);
        SeProxyService.getInstance().addPlugin(poolPlugin);
        return poolPlugin;
    }


    /**
     * Create a mock method for onDto() that checks that keypleDto contains an exception
     * 
     * @return
     */
    public static Answer<TransportDto> assertContainsException() {
        return new Answer<TransportDto>() {
            @Override
            public TransportDto answer(InvocationOnMock invocationOnMock) throws Throwable {
                TransportDto transportDto = invocationOnMock.getArgument(0);

                // assert that returning dto DOES contain an exception
                Assert.assertTrue(KeypleDtoHelper.containsException(transportDto.getKeypleDTO()));
                return new LocalTransportDto(KeypleDtoHelper.NoResponse(transportDto.getKeypleDTO().getId()), null);
            }
        };
    }

    public static DtoNode getFakeDtoNode() {
        return new DtoNode() {
            @Override
            public void setDtoHandler(DtoHandler handler) {

            }

            @Override
            public void sendDTO(TransportDto message) throws KeypleRemoteException {

            }

            @Override
            public void sendDTO(KeypleDto message) throws KeypleRemoteException {

            }

            @Override
            public String getNodeId() {
                return "";
            }
        };
    }

}
