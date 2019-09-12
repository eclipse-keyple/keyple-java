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


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.plugin.remotese.pluginse.RemoteSePlugin;
import org.eclipse.keyple.plugin.remotese.pluginse.VirtualReader;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test RemoteSePlugin event READER_CONNECTED and READER_DISCONNECTED
 */
public class RemoteSePluginEventTest extends VirtualReaderBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(RemoteSePluginEventTest.class);
    StubReader nativeReader;
    ObservablePlugin.PluginObserver obs;
    RemoteSePlugin remoteSePlugin;

    @Before
    public void setUp() throws Exception {
        initMasterNSlave();
        remoteSePlugin = masterAPI.getPlugin();
    }

    @After
    public void tearDown() throws Exception {
        // remove observer
        remoteSePlugin.removeObserver(obs);
        Assert.assertEquals(0, remoteSePlugin.countObservers());
        clearMasterNSlave();
        unregisterPlugins();
        Assert.assertEquals(0, seProxyService.getPlugins().size());
    }

    /**
     * Test READER_CONNECTED Plugin Event
     * 
     * @throws Exception
     */
    @Test
    public void testEvent_READER_CONNECTED() throws Exception {
        // lock test until message is received
        final CountDownLatch lock = new CountDownLatch(1);

        obs = new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {
                Assert.assertNotNull(event.getReaderNames().first());
                Assert.assertEquals(1, event.getReaderNames().size());
                Assert.assertEquals(remoteSePlugin.getName(), event.getPluginName());
                Assert.assertEquals(PluginEvent.EventType.READER_CONNECTED, event.getEventType());
                lock.countDown();
            }
        };

        // add observer
        remoteSePlugin.addObserver(obs);

        // connect a Stub Native reader
        nativeReader = this.connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID,
                TransmissionMode.CONTACTLESS);

        // wait 5 seconds
        lock.await(2, TimeUnit.SECONDS);

        remoteSePlugin.removeObserver(obs);

        Assert.assertEquals(0, lock.getCount());

        disconnectReader(NATIVE_READER_NAME);

    }


    /**
     * Test READER_DISCONNECTED Plugin Event
     *
     * @throws Exception
     */
    @Test
    public void testEvent_READER_DISCONNECTED() throws Exception {
        // lock test until two messages are received
        final CountDownLatch lock = new CountDownLatch(2);

        obs = new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {

                // we expect the first event to be READER_CONNECTED
                if (event.getEventType() == PluginEvent.EventType.READER_CONNECTED) {
                    Assert.assertEquals(2, lock.getCount());
                    lock.countDown();
                } else {
                    // second event should be a READER_DISCONNECTED
                    Assert.assertNotNull(event.getReaderNames().first());
                    Assert.assertEquals(1, event.getReaderNames().size());
                    Assert.assertEquals(remoteSePlugin.getName(), event.getPluginName());
                    Assert.assertEquals(PluginEvent.EventType.READER_DISCONNECTED,
                            event.getEventType());
                    lock.countDown();
                }
            }
        };

        // add observer
        remoteSePlugin.addObserver(obs);

        // connect a Stub Native reader
        nativeReader = this.connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID,
                TransmissionMode.CONTACTLESS);

        // wait 1 second
        Thread.sleep(1000);

        this.disconnectStubReader("anysession", NATIVE_READER_NAME, CLIENT_NODE_ID);

        // wait 5 seconds
        lock.await(5, TimeUnit.SECONDS);

        Assert.assertEquals(0, lock.getCount());
    }

    /**
     * Test connect reader twice
     *
     * @throws Exception
     * 
     * @Test(expected = KeypleReaderException.class) public void testConnectTwiceSameReader() throws
     *                Exception { // lock test until message is received final CountDownLatch lock =
     *                new CountDownLatch(1);
     * 
     *                obs = new ObservablePlugin.PluginObserver() {
     * @Override public void update(PluginEvent event) { // READER_CONNECTED should be raised only
     *           once, so lock.getCount() should be equals // to 1 if (1 != lock.getCount()) { throw
     *           new IllegalStateException(); //Assert.assertTrue(false); }
     *           Assert.assertNotNull(event.getReaderNames().first()); Assert.assertEquals(1,
     *           event.getReaderNames().size()); Assert.assertEquals(remoteSePlugin.getName(),
     *           event.getPluginName()); Assert.assertEquals(PluginEvent.EventType.READER_CONNECTED,
     *           event.getEventType()); lock.countDown(); } }; //add observer
     *           remoteSePlugin.addObserver(obs);
     * 
     *           // connect a Stub Native reader try { this.connectStubReader(NATIVE_READER_NAME,
     *           CLIENT_NODE_ID, TransmissionMode.CONTACTLESS); } catch (Exception e) {
     *           e.printStackTrace(); }
     * 
     *           // wait 2 seconds lock.await(2, TimeUnit.SECONDS);
     * 
     *           // connect twice try { this.connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID,
     *           TransmissionMode.CONTACTLESS); //should throw exception } catch (Exception e) {
     *           //Disconnect reader to clean environment disconnectReader(NATIVE_READER_NAME);
     *           //propagate exception throw e; } // Expect a KeypleReaderException exception to be
     *           thrown }
     */

    /**
     * Test disconnect a not connected reader
     * 
     * @throws Exception
     */
    @Test(expected = KeypleReaderException.class)
    public void testDisconnectUnknownReader() throws Exception {

        obs = new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {
                // READER_CONNECTED should not be called
                throw new IllegalStateException();
            }
        };

        remoteSePlugin.addObserver(obs);

        this.disconnectStubReader("anysession", "A_NOT_CONNECTED_READER", CLIENT_NODE_ID);

        // Expect a KeypleReaderException exception to be thrown
    }


    /**
     * Test MasterAPI disconnect method
     *
     * @throws Exception
     */
    @Test
    public void testMaster_DisconnectReaderSucess() throws Exception {
        // lock test until two messages are received
        final CountDownLatch lock = new CountDownLatch(2);

        obs = new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {

                // we expect the first event to be READER_CONNECTED
                if (event.getEventType() == PluginEvent.EventType.READER_CONNECTED) {
                    Assert.assertEquals(2, lock.getCount());
                    lock.countDown();
                } else {
                    // second event should be a READER_DISCONNECTED
                    Assert.assertNotNull(event.getReaderNames().first());
                    Assert.assertEquals(1, event.getReaderNames().size());
                    Assert.assertEquals(remoteSePlugin.getName(), event.getPluginName());
                    Assert.assertEquals(PluginEvent.EventType.READER_DISCONNECTED,
                            event.getEventType());
                    lock.countDown();
                }
            }
        };

        // add observer
        remoteSePlugin.addObserver(obs);

        // connect a Stub Native reader
        nativeReader = this.connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID,
                TransmissionMode.CONTACTLESS);

        // wait 1 second
        Thread.sleep(1000);

        VirtualReader virtualReader = getVirtualReader();

        masterAPI.getPlugin().disconnectVirtualReader(
                ((VirtualReader) virtualReader).getNativeReaderName(),
                virtualReader.getSession().getSlaveNodeId());

        // wait 5 seconds
        lock.await(5, TimeUnit.SECONDS);

        Assert.assertEquals(0, lock.getCount());
    }

    /**
     * Test MasterAPI disconnect method fail
     *
     * @throws Exception
     */
    @Test(expected = KeypleReaderNotFoundException.class)
    public void testMaster_DisconnectReaderNotFound() throws Exception {
        // connect a Stub Native reader
        nativeReader = this.connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID,
                TransmissionMode.CONTACTLESS);
        // wait 1 second
        Thread.sleep(1000);
        VirtualReader virtualReader = getVirtualReader();
        masterAPI.getPlugin().disconnectVirtualReader(
                ((VirtualReader) virtualReader).getNativeReaderName(), "none");
    }

    /**
     * Test MasterAPI disconnect method fail
     *
     * @throws Exception
     */
    @Test(expected = KeypleReaderNotFoundException.class)
    public void testMaster_DisconnectReaderNotFound2() throws Exception {
        // connect a Stub Native reader
        nativeReader = this.connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID,
                TransmissionMode.CONTACTLESS);
        // wait 1 second
        Thread.sleep(1000);
        VirtualReader virtualReader = getVirtualReader();
        masterAPI.getPlugin().disconnectVirtualReader("none",
                virtualReader.getSession().getSlaveNodeId());
    }

}
