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
package org.eclipse.keyple.plugin.stub;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StubPluginTest extends BaseStubTest {

    Logger logger = LoggerFactory.getLogger(StubPluginTest.class);

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws InterruptedException, KeypleReaderException {
        super.tearDown();
    }

    /**
     * Plug one reader and count if created
     */
    @Test
    public void testA_PlugOneReaderCount() throws InterruptedException, KeypleReaderException {
        final String READER_NAME = "testA_PlugOneReaderCount";

        // connect reader
        stubPlugin.plugStubReader(READER_NAME, true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
    }


    /**
     * Plug one reader and wait for event
     */
    @Test
    public void testA_PlugOneReaderEvent() throws InterruptedException, KeypleReaderException {
        final CountDownLatch readerConnected = new CountDownLatch(1);
        final String READER_NAME = "testA_PlugReaders";

        // add READER_CONNECTED assert observer
        stubPlugin.addObserver(new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {
                Assert.assertEquals(PluginEvent.EventType.READER_CONNECTED, event.getEventType());
                Assert.assertEquals(1, event.getReaderNames().size());
                Assert.assertEquals(READER_NAME, event.getReaderNames().first());
                readerConnected.countDown();
            }
        });

        stubPlugin.plugStubReader(READER_NAME, false);
        readerConnected.await(2, TimeUnit.SECONDS);
        Assert.assertEquals(0, readerConnected.getCount());
    }

    /**
     * Plug one reader synchronously, an event is raised no matter what
     * 
     * @Test public void testA_PlugOneReaderEventSynchronous() throws InterruptedException,
     *       KeypleReaderException { final CountDownLatch readerConnected = new CountDownLatch(1);
     *       final String READER_NAME = "testA_PlugReaders";
     * 
     *       // add READER_CONNECTED assert observer stubPlugin.addObserver(new
     *       ObservablePlugin.PluginObserver() {
     * @Override public void update(PluginEvent event) {
     *           Assert.assertEquals(PluginEvent.EventType.READER_CONNECTED, event.getEventType());
     *           Assert.assertEquals(1, event.getReaderNames().size());
     *           Assert.assertEquals(READER_NAME, event.getReaderNames().first());
     *           readerConnected.countDown(); } });
     * 
     *           stubPlugin.plugStubReader(READER_NAME, true); readerConnected.await(2,
     *           TimeUnit.SECONDS);
     * 
     *           //TODO event should not be raised Assert.assertEquals(1,
     *           stubPlugin.getReaders().size()); Assert.assertEquals(1,
     *           readerConnected.getCount()); }
     */

    /**
     * Unplug one reader and count if removed
     */
    @Test
    public void testA_UnplugOneReaderCount() throws InterruptedException, KeypleReaderException {
        final String READER_NAME = "testA_UnplugOneReaderCount";
        // connect reader
        stubPlugin.plugStubReader(READER_NAME, true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        stubPlugin.unplugStubReader(READER_NAME, true);
        Assert.assertEquals(0, stubPlugin.getReaders().size());

    }

    /**
     * Unplug one reader and wait for event
     */
    @Test
    public void testB_UnplugOneReaderEvent() throws InterruptedException, KeypleReaderException {
        final CountDownLatch readerConnected = new CountDownLatch(1);
        final CountDownLatch readerDisconnected = new CountDownLatch(1);
        final String READER_NAME = "testB_PlugUnplugOneReaders";

        ObservablePlugin.PluginObserver disconnected_obs = new ObservablePlugin.PluginObserver() {
            int event_i = 1;

            @Override
            public void update(PluginEvent event) {
                logger.info("event {} {}", event.getEventType(), event.getReaderNames().size());
                if (event_i == 1) {
                    Assert.assertEquals(PluginEvent.EventType.READER_CONNECTED,
                            event.getEventType());
                    readerConnected.countDown();
                }
                // analyze the second event, should be a READER_DISCONNECTED
                if (event_i == 2) {
                    Assert.assertEquals(PluginEvent.EventType.READER_DISCONNECTED,
                            event.getEventType());
                    Assert.assertEquals(1, event.getReaderNames().size());
                    Assert.assertEquals(READER_NAME, event.getReaderNames().first());
                    readerDisconnected.countDown();
                }
                event_i++;
            }
        };

        // add READER_DISCONNECTED assert observer
        stubPlugin.addObserver(disconnected_obs);

        // plug a reader
        stubPlugin.plugStubReader(READER_NAME, false);

        readerConnected.await(2, TimeUnit.SECONDS);

        // unplug reader
        stubPlugin.unplugStubReader(READER_NAME, false);

        // wait for event to be raised
        readerDisconnected.await(2, TimeUnit.SECONDS);
        Assert.assertEquals(0, readerDisconnected.getCount());
    }

    /**
     * Plug same reader twice
     */
    @Test
    public void testC_PlugSameReaderTwice() throws InterruptedException, KeypleReaderException {
        final String READER_NAME = "testC_PlugSameReaderTwice";

        stubPlugin.plugStubReader(READER_NAME, true);
        stubPlugin.plugStubReader(READER_NAME, true);
        logger.debug("Stubplugin readers size {} ", stubPlugin.getReaders().size());

        Assert.assertEquals(1, stubPlugin.getReaders().size());
    }

    /**
     * Get name
     */
    @Test
    public void testD_GetName() {
        Assert.assertNotNull(stubPlugin.getName());
    }

    /**
     * Plug many readers at once and count
     */
    @Test
    public void testE_PlugMultiReadersCount() throws InterruptedException, KeypleReaderException {
        Set<String> newReaders =
                new HashSet<String>(Arrays.asList("EC_reader1", "EC_reader2", "EC_reader3"));
        // connect readers at once
        stubPlugin.plugStubReaders(newReaders, true);
        logger.info("Stub Readers connected {}", stubPlugin.getReaderNames());
        Assert.assertEquals(newReaders, stubPlugin.getReaderNames());
        Assert.assertEquals(3, stubPlugin.getReaders().size());
    }

    /**
     * Plug many readers at once and wait for event
     */
    @Test
    public void testE_PlugMultiReadersEvent() throws InterruptedException, KeypleReaderException {
        final Set<String> READERS =
                new HashSet<String>(Arrays.asList("E_Reader1", "E_Reader2", "E_Reader3"));

        // lock test until message is received
        final CountDownLatch readerConnected = new CountDownLatch(1);

        // add READER_CONNECTED assert observer
        stubPlugin.addObserver(new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {
                logger.info("event {} {}", event.getEventType(), event.getReaderNames().size());
                Assert.assertEquals(PluginEvent.EventType.READER_CONNECTED, event.getEventType());
                Assert.assertEquals(3, event.getReaderNames().size());
                Assert.assertEquals(READERS, event.getReaderNames());
                readerConnected.countDown();
            }

        });


        // connect readers
        stubPlugin.plugStubReaders(READERS, false);

        // wait for event to be raised
        readerConnected.await(2, TimeUnit.SECONDS);
        Assert.assertEquals(0, readerConnected.getCount());

    }

    /**
     * Plug and unplug many readers at once and count
     */
    @Test
    public void testF_PlugUnplugMultiReadersCount()
            throws InterruptedException, KeypleReaderException {
        final Set<String> READERS =
                new HashSet<String>(Arrays.asList("FC_Reader1", "FC_Reader2", "FC_Reader3"));
        // connect readers at once
        stubPlugin.plugStubReaders(READERS, true);
        Assert.assertEquals(3, stubPlugin.getReaders().size());
        stubPlugin.unplugStubReaders(READERS, true);
        Assert.assertEquals(0, stubPlugin.getReaders().size());
    }

    /**
     * Plug and unplug many readers at once and wait for events
     */
    @Test
    public void testF_PlugUnplugMultiReadersEvent()
            throws InterruptedException, KeypleReaderException {
        final Set<String> READERS =
                new HashSet<String>(Arrays.asList("F_Reader1", "F_Reader2", "F_Reader3"));

        // lock test until message is received
        final CountDownLatch readerConnected = new CountDownLatch(1);
        final CountDownLatch readerDisconnected = new CountDownLatch(1);

        ObservablePlugin.PluginObserver assertDisconnect = new ObservablePlugin.PluginObserver() {
            int event_i = 1;

            @Override
            public void update(PluginEvent event) {
                logger.info("event {} {}", event.getEventType(), event.getReaderNames().size());
                if (event_i == 1) {
                    Assert.assertEquals(PluginEvent.EventType.READER_CONNECTED,
                            event.getEventType());
                    readerConnected.countDown();
                }
                // analyze the second event, should be a READER_DISCONNECTED
                if (event_i == 2) {
                    Assert.assertEquals(PluginEvent.EventType.READER_DISCONNECTED,
                            event.getEventType());
                    Assert.assertEquals(3, event.getReaderNames().size());
                    Assert.assertEquals(READERS, event.getReaderNames());
                    readerDisconnected.countDown();
                }
                event_i++;
            }
        };
        // add assert DISCONNECT assert observer
        stubPlugin.addObserver(assertDisconnect);

        // connect reader
        stubPlugin.plugStubReaders(READERS, false);

        Assert.assertTrue(readerConnected.await(5, TimeUnit.SECONDS));

        stubPlugin.unplugStubReaders(READERS, false);

        Assert.assertTrue(readerDisconnected.await(5, TimeUnit.SECONDS));

        Thread.sleep(1000);// Todo fix me, should works without sleep
        logger.debug("Stub Readers connected {}", stubPlugin.getReaderNames());
        Assert.assertEquals(0, stubPlugin.getReaders().size());
        Assert.assertEquals(0, readerConnected.getCount());
        Assert.assertEquals(0, readerDisconnected.getCount());
    }
}
