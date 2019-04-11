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


import org.eclipse.keyple.plugin.remotese.nativese.SlaveAPI;
import org.eclipse.keyple.plugin.remotese.pluginse.MasterAPI;
import org.eclipse.keyple.plugin.remotese.transport.DtoNode;
import org.eclipse.keyple.plugin.remotese.transport.impl.java.LocalTransportDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.junit.Assert;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Integration {

    private static final Logger logger = LoggerFactory.getLogger(Integration.class);


    /**
     * Create a Virtual Reader Service
     * 
     * @param node
     * @return
     */
    public static MasterAPI bindMaster(DtoNode node) {
        // Create Master services : masterAPI
        MasterAPI masterAPI = new MasterAPI(SeProxyService.getInstance(), node);

        // observe remote se plugin for events
        ReaderPlugin rsePlugin = masterAPI.getPlugin();

        // Binds masterAPI to a
        // masterAPI.bindDtoEndpoint(node);

        return masterAPI;
    }

    /**
     * Create a Native Reader Service
     * 
     * @param node
     * @return
     */
    public static SlaveAPI bindSlave(DtoNode node, String masterNodeId) {
        // Binds node for outgoing KeypleDto
        SlaveAPI slaveAPI = new SlaveAPI(SeProxyService.getInstance(), node, masterNodeId);

        // Binds node for incoming KeypleDTo
        // slaveAPI.bindDtoEndpoint(node);

        return slaveAPI;
    }

    /**
     * Create a Spy Native Reader Service
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
    public static StubReader createStubReader(String stubReaderName)
            throws InterruptedException, KeypleReaderNotFoundException {
        SeProxyService seProxyService = SeProxyService.getInstance();

        StubPlugin stubPlugin = StubPlugin.getInstance();
        seProxyService.addPlugin(stubPlugin);

        // add an stubPluginObserver to start the plugin monitoring thread
        // stubPlugin.addObserver(observer); //do not observe so the monitoring thread is not
        // created

        logger.debug("Stub plugin count observers : {}", stubPlugin.countObservers());

        logger.debug("Create a new StubReader : {}", stubReaderName);
        stubPlugin.plugStubReader(stubReaderName, true);

        Thread.sleep(100);

        // get the created proxy reader
        return (StubReader) stubPlugin.getReader(stubReaderName);
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
                return new LocalTransportDto(KeypleDtoHelper.NoResponse(), null);
            }
        };
    }

}
