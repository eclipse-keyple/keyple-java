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
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReader;
import org.eclipse.keyple.plugin.remotese.transport.factory.TransportFactory;
import org.eclipse.keyple.plugin.remotese.transport.impl.java.LocalClient;
import org.eclipse.keyple.plugin.remotese.transport.impl.java.LocalTransportFactory;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test Master API methods : connectReader and DisconnectReader
 */
@RunWith(MockitoJUnitRunner.class)
public class MasterAPITest {

    private static final Logger logger = LoggerFactory.getLogger(MasterAPITest.class);

    @Rule
    public TestName name = new TestName();

    // Real objects
    TransportFactory factory;
    MasterAPI masterAPI;

    StubReader nativeReader;
    VirtualReader virtualReader;

    final String NATIVE_READER_NAME = "testStubReader";
    final String CLIENT_NODE_ID = "testClientNodeId";
    final String SERVER_NODE_ID = "testServerNodeId";


    // Spy Object
    SlaveAPI nativeReaderSpy;



    @Before
    public void setTup() throws Exception {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName());
        logger.info("------------------------------");

        logger.info("*** Init LocalTransportFactory");
        // use a local transport factory for testing purposes (only java calls between client and
        // server)
        // only one client and one server
        factory = new LocalTransportFactory(SERVER_NODE_ID);

        logger.info("*** Bind Master Services");
        // bind Master services to server
        masterAPI = Integration.bindMaster(factory.getServer());

        logger.info("*** Bind Slave Services");
        // bind Slave services to client
        nativeReaderSpy =
                Integration.bindSlaveSpy(factory.getClient(CLIENT_NODE_ID), SERVER_NODE_ID);

        nativeReader = Integration.createStubReader(NATIVE_READER_NAME);

    }


    @After
    public void tearDown() throws Exception {

        logger.info("TearDown Test");

        StubPlugin stubPlugin = StubPlugin.getInstance();

        // delete stubReader
        stubPlugin.unplugStubReader(nativeReader.getName(), true);

        // Thread.sleep(500);

        // delete observer and monitor thread
        // stubPlugin.removeObserver(stubPluginObserver);

        nativeReader.clearObservers();

        // Thread.sleep(500);
    }



    /*
     * CONNECT METHOD
     */


    /**
     * Connect successfully a reader
     * 
     * @throws Exception
     */
    @Test
    public void testOKConnect() throws Exception {

        String sessionId = nativeReaderSpy.connectReader(nativeReader);

        // assert that a virtual reader has been created
        VirtualReader virtualReader =
                (VirtualReader) masterAPI.getPlugin().getReaderByRemoteName(NATIVE_READER_NAME);

        Assert.assertEquals(NATIVE_READER_NAME, virtualReader.getNativeReaderName());
        Assert.assertEquals(1, nativeReader.countObservers());
        Assert.assertEquals(0, virtualReader.countObservers());
        Assert.assertNotNull(sessionId);


    }

    /**
     * Connect error : reader already exists
     * 
     * @throws Exception
     */
    @Test(expected = KeypleReaderException.class)
    public void testKOConnectError() throws Exception {

        // first connectReader is successful
        String sessionId = nativeReaderSpy.connectReader(nativeReader);

        // assert an exception will be contained into keypleDto response
        // doAnswer(Integration.assertContainsException()).when(nativeReaderSpy)
        // .onDTO(ArgumentMatchers.<TransportDto>any());

        // should throw a DTO with an exception in master side KeypleReaderException
        nativeReaderSpy.connectReader(nativeReader);


    }

    /**
     * Connect error : impossible to send DTO
     * 
     * @throws Exception
     */
    @Test(expected = KeypleReaderException.class)
    public void testKOConnectServerError() throws Exception {

        // bind Slave to faulty client
        nativeReaderSpy =
                Integration.bindSlaveSpy(new LocalClient(CLIENT_NODE_ID, null), SERVER_NODE_ID);

        nativeReaderSpy.connectReader(nativeReader);
        // should throw a KeypleRemoteException in slave side
    }

    /*
     * DISCONNECT METHOD
     */

    /**
     * Disconnect successfully a reader
     * 
     * @throws Exception
     */
    @Test
    public void testOKConnectDisconnect() throws Exception {

        // connect
        String sessionId = nativeReaderSpy.connectReader(nativeReader);

        VirtualReader virtualReader =
                (VirtualReader) masterAPI.getPlugin().getReaderByRemoteName(NATIVE_READER_NAME);

        Assert.assertEquals(NATIVE_READER_NAME, virtualReader.getNativeReaderName());

        // disconnect
        nativeReaderSpy.disconnectReader(sessionId, nativeReader.getName());

        // assert that the virtual reader has been destroyed
        Assert.assertEquals(0, masterAPI.getPlugin().getReaders().size());
    }


    /**
     * Disconnect Error : reader not connected
     * 
     * @throws Exception
     */
    // @Test
    // public void testKODisconnectNotFoundError() throws Exception {
    //
    // // assert an exception will be contained into keypleDto response
    // doAnswer(Integration.assertContainsException()).when(nativeReaderSpy)
    // .onDTO(ArgumentMatchers.<TransportDto>any());
    //
    // // disconnect
    // nativeReaderSpy.disconnectReader(nativeReader, CLIENT_NODE_ID);
    // // should throw exception in master side KeypleNotFound
    //
    // }


    /**
     * Disconnect error : impossible to send DTO
     * 
     * @throws Exception
     */
    @Test(expected = KeypleReaderException.class)
    public void testKODisconnectServerError() throws Exception {

        // bind Slave to faulty client
        nativeReaderSpy =
                Integration.bindSlaveSpy(new LocalClient(CLIENT_NODE_ID, null), SERVER_NODE_ID);

        nativeReaderSpy.disconnectReader("null", nativeReader.getName());
        // should throw a KeypleRemoteException in slave side
    }


    /*
     * HELPERS
     */



}
