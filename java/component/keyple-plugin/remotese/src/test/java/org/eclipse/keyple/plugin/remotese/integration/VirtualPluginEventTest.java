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
import org.eclipse.keyple.plugin.remotese.pluginse.RemoteSePlugin;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.util.Observable;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test VirtualPlugin event READER_CONNECTED and READER_DISCONNECTED
 */
public class VirtualPluginEventTest extends VirtualReaderBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(VirtualPluginEventTest.class);

    @Before
    public void setUp() throws Exception {
        initKeypleServices();
    }

    @After
    public void tearDown() throws Exception {
        clearStubpluginReaders();
    }

    /**
     * Test READER_CONNECTED Plugin Event
     * 
     * @throws Exception
     */
    @Test
    public void testConnectOneReader() throws Exception {
        // lock test until message is received
        final CountDownLatch lock = new CountDownLatch(1);

        final RemoteSePlugin remoteSePlugin = virtualReaderService.getPlugin();

        remoteSePlugin.addObserver(new Observable.Observer<PluginEvent>() {
            @Override
            public void update(PluginEvent event) {
                Assert.assertNotNull(event.getReaderNames().first());
                Assert.assertEquals(1, event.getReaderNames().size());
                Assert.assertEquals(remoteSePlugin.getName(), event.getPluginName());
                Assert.assertEquals(PluginEvent.EventType.READER_CONNECTED, event.getEventType());
                lock.countDown();
            }
        });

        // connect a Stub Native reader
        nativeReader = this.connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID);

        // wait 5 seconds
        lock.await(5, TimeUnit.SECONDS);

        Assert.assertEquals(0, lock.getCount());
    }


    /**
     * Test READER_DISCONNECTED Plugin Event
     *
     * @throws Exception
     */
    @Test
    public void testDisconnectEvent() throws Exception {
        // lock test until two messages are received
        final CountDownLatch lock = new CountDownLatch(2);

        final RemoteSePlugin remoteSePlugin = virtualReaderService.getPlugin();

        remoteSePlugin.addObserver(new Observable.Observer<PluginEvent>() {
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
        });

        // connect a Stub Native reader
        nativeReader = this.connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID);

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
     */
    @Test(expected = KeypleReaderException.class)
    public void testConnectTwiceSameReader() throws Exception {
        // lock test until message is received
        final CountDownLatch lock = new CountDownLatch(1);

        final RemoteSePlugin remoteSePlugin = virtualReaderService.getPlugin();

        remoteSePlugin.addObserver(new Observable.Observer<PluginEvent>() {
            @Override
            public void update(PluginEvent event) {
                // READER_CONNECTED should be raised only once, so lock.getCount() should be equals
                // to 1
                if (1 != lock.getCount()) {
                    throw new IllegalStateException();
                } ;
                Assert.assertNotNull(event.getReaderNames().first());
                Assert.assertEquals(1, event.getReaderNames().size());
                Assert.assertEquals(remoteSePlugin.getName(), event.getPluginName());
                Assert.assertEquals(PluginEvent.EventType.READER_CONNECTED, event.getEventType());
                lock.countDown();
            }
        });

        // connect a Stub Native reader
        nativeReader = this.connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID);

        // wait 2 seconds
        lock.await(2, TimeUnit.SECONDS);

        // connect twice
        nativeReader = this.connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID);

        // a KeypleReaderException exception is thrown
    }

    /**
     * Test disconnect a not connected reader
     * 
     * @throws Exception
     */
    @Test(expected = KeypleReaderException.class)
    public void testDisconnectUnknownReader() throws Exception {
        final RemoteSePlugin remoteSePlugin = virtualReaderService.getPlugin();

        remoteSePlugin.addObserver(new Observable.Observer<PluginEvent>() {
            @Override
            public void update(PluginEvent event) {
                // READER_CONNECTED should not be called
                throw new IllegalStateException();
            }
        });
        this.disconnectStubReader("anysession", "A_NOT_CONNECTED_READER", CLIENT_NODE_ID);

        // a KeypleReaderException exception is thrown
    }



}
