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



import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginInstanciationException;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
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
import org.eclipse.keyple.plugin.stub.*;
import org.junit.Assert;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Integration {

    private static final Logger logger = LoggerFactory.getLogger(Integration.class);
    public static final String SLAVE_STUB = "SLAVE_STUB";
    public static final String SLAVE_POOL_STUB = "SLAVE_POOL_STUB";

    /**
     * Create a Spy MasterAPI
     * 
     * @param node
     * @return
     */
    public static MasterAPI createSpyMasterAPI(DtoNode node, String pluginName)
            throws KeyplePluginInstanciationException {

        // Create Master services : masterAPI
        return Mockito.spy(new MasterAPI(SeProxyService.getInstance(), node, 10000,
                MasterAPI.PLUGIN_TYPE_DEFAULT, pluginName));
    }

    /**
     * Create a Spy SlaveAPI
     * 
     * @param node
     * @return
     */
    public static SlaveAPI createSpySlaveAPI(DtoNode node, String masterNodeId) {
        // Binds node for outgoing KeypleDto
        return Mockito.spy(new SlaveAPI(SeProxyService.getInstance(), node, masterNodeId));
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
            TransmissionMode transmissionMode) throws InterruptedException,
            KeypleReaderNotFoundException, KeyplePluginNotFoundException {

        StubPlugin stubPlugin = createStubPlugin();

        // register an stubPluginObserver to start the plugin monitoring thread
        // stubPlugin.addObserver(observer); //do not observe so the monitoring thread is not
        // created

        logger.debug("Stub plugin count observers : {}", stubPlugin.countObservers());

        logger.debug("Create a new StubReader : {}", stubReaderName);
        stubPlugin.plugStubReader(stubReaderName, transmissionMode, true);

        Thread.sleep(100);

        // Get the created proxy reader
        return (StubReader) stubPlugin.getReader(stubReaderName);
    }

    public static void unregisterAllPlugin(String remoteSePluginName) {
        SeProxyService.getInstance().unregisterPlugin(SLAVE_STUB);
        SeProxyService.getInstance().unregisterPlugin(SLAVE_POOL_STUB);
        SeProxyService.getInstance().unregisterPlugin(remoteSePluginName);

    }


    public static StubPlugin createStubPlugin() throws KeyplePluginNotFoundException {

        try {
            // get SeProxyService
            SeProxyService seProxyService = SeProxyService.getInstance();

            // register plugin
            seProxyService.registerPlugin(new StubPluginFactory(SLAVE_STUB));


            // get plugin
            StubPlugin stubPlugin = (StubPlugin) seProxyService.getPlugin(SLAVE_STUB);

            return stubPlugin;
        } catch (KeyplePluginInstanciationException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Create a Stub Reader Pool Plugin
     *
     * @return
     * @throws InterruptedException
     * @throws KeypleReaderNotFoundException
     */
    public static StubPoolPlugin createStubPoolPlugin() {

        SeProxyService seProxyService = SeProxyService.getInstance();

        StubPoolPluginFactory stubPoolPluginFactory = new StubPoolPluginFactory(SLAVE_POOL_STUB);

        try {
            seProxyService.registerPlugin(stubPoolPluginFactory);


            StubPoolPlugin poolPlugin = (StubPoolPlugin) seProxyService.getPlugin(SLAVE_POOL_STUB);

            return poolPlugin;
        } catch (KeyplePluginInstanciationException e) {
            e.printStackTrace();
        } catch (KeyplePluginNotFoundException e) {
            e.printStackTrace();
        }
        return null;


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
                return new LocalTransportDto(
                        KeypleDtoHelper.NoResponse(transportDto.getKeypleDTO().getId()), null);
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
