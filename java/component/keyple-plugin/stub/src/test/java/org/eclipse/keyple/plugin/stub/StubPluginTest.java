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

import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
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
    public void setupStub() throws Exception {
        super.setupStub();
    }

    @After
    public void clearStub() throws InterruptedException, KeypleReaderException, KeyplePluginNotFoundException {
        super.clearStub();
    }

/*
    @Test
    public void instanciatePlugin() throws InterruptedException, KeypleReaderException, KeyplePluginNotFoundException {
        final String READER_NAME = "plugOneReaderSync_sucess";
        SeProxyService seProxyService = SeProxyService.getInstance();

        seProxyService.registerPlugin(new StubPluginFactory());

        ReaderPlugin stubPlugin = seProxyService.getPlugin(StubPlugin.PLUGIN_NAME);

        ((StubPlugin) stubPlugin).plugStubReader(READER_NAME, TransmissionMode.CONTACTLESS, true);

    }
*/





        /**
         * Plug one reader synchronously Check: Count if created
         */
    @Test
    public void plugOneReaderSync_success() throws InterruptedException, KeypleReaderException {
        final String READER_NAME = "plugOneReaderSync_sucess";

        // connect reader
        stubPlugin.plugStubReader(READER_NAME, TransmissionMode.CONTACTLESS, true);
        StubReaderImpl stubReader = (StubReaderImpl) stubPlugin.getReaders().first();

        Assert.assertEquals(1, stubPlugin.getReaders().size());
        Assert.assertEquals(READER_NAME, stubReader.getName());
        Assert.assertEquals(TransmissionMode.CONTACTLESS, stubReader.getTransmissionMode());

    }



    /**
     * Plug one reader synchronously Check: Event thrown
     */

    @Test
    public void plugOneReaderSyncEvent_success()
            throws InterruptedException, KeypleReaderException {
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

        stubPlugin.plugStubReader(READER_NAME, true);
        readerConnected.await(2, TimeUnit.SECONDS);

        Assert.assertEquals(1, stubPlugin.getReaders().size());
        Assert.assertEquals(0, readerConnected.getCount());
    }


    /**
     * Unplug one reader synchronously Check: Count if removed
     */
    @Test
    public void unplugOneReader_success() throws InterruptedException, KeypleReaderException {
        final String READER_NAME = "unplugOneReader_success";
        // connect reader
        stubPlugin.plugStubReader(READER_NAME, true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        stubPlugin.unplugStubReader(READER_NAME, true);
        Assert.assertEquals(0, stubPlugin.getReaders().size());

    }



    /**
     * Plug same reader twice Check : only one reader
     */
    @Test
    public void plugSameReaderTwice_fail() throws InterruptedException, KeypleReaderException {
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
    public void getName_success() {
        Assert.assertNotNull(stubPlugin.getName());
    }

    /**
     * Plug many readers at once sync
     *
     * Check : count readers
     */
    @Test
    public void plugMultiReadersSync_success() throws InterruptedException, KeypleReaderException {
        Set<String> newReaders =
                new HashSet<String>(Arrays.asList("EC_reader1", "EC_reader2", "EC_reader3"));
        // connect readers at once
        stubPlugin.plugStubReaders(newReaders, true);
        logger.info("Stub Readers connected {}", stubPlugin.getReaderNames());
        Assert.assertEquals(newReaders, stubPlugin.getReaderNames());
        Assert.assertEquals(3, stubPlugin.getReaders().size());
    }



    /**
     * Plug and unplug many readers at once synchronously Check : count
     */
    @Test
    public void plugUnplugMultiReadersSync_success()
            throws InterruptedException, KeypleReaderException {
        final Set<String> READERS =
                new HashSet<String>(Arrays.asList("FC_Reader1", "FC_Reader2", "FC_Reader3"));
        // connect readers at once
        stubPlugin.plugStubReaders(READERS, true);
        Assert.assertEquals(3, stubPlugin.getReaders().size());
        stubPlugin.unplugStubReaders(READERS, true);
        Assert.assertEquals(0, stubPlugin.getReaders().size());
    }


}
