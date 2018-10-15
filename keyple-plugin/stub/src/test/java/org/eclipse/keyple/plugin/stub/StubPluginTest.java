/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.plugin.stub;


import org.eclipse.keyple.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.util.Observable;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StubPluginTest {

    StubPlugin stubPlugin;
    Logger logger = LoggerFactory.getLogger(StubPluginTest.class);

    @Before
    public void setUp() throws InterruptedException, KeypleReaderException {
        logger.info("setUp, assert stubplugin is empty");
        stubPlugin = StubPlugin.getInstance(); // singleton

        logger.info("Stubplugin readers size {}", stubPlugin.getReaders().size());
        Assert.assertEquals(0, stubPlugin.getReaders().size());

        logger.info("Stubplugin observers size {}", stubPlugin.countObservers());
        Assert.assertEquals(0, stubPlugin.countObservers());

        Thread.sleep(100);

    }

    @After
    public void tearDown() throws InterruptedException {
        stubPlugin = StubPlugin.getInstance(); // singleton
        stubPlugin.clearObservers();
        Thread.sleep(100);

    }


    @Test
    public void testA_PlugReaders() throws InterruptedException, KeypleReaderException {

        Observable.Observer connected_obs = new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {
                Assert.assertEquals(PluginEvent.EventType.READER_CONNECTED, event.getEventType());
            }
        };

        // add READER_CONNECTED assert observer
        stubPlugin.addObserver(connected_obs);

        // connect reader
        stubPlugin.plugStubReader("testA_PlugReaders");

        Thread.sleep(200);
        logger.debug("Stubplugin readers size {} ", stubPlugin.getReaders().size());

        assert (stubPlugin.getReaders().size() == 1);

        // clean
        stubPlugin.removeObserver(connected_obs);
        stubPlugin.unplugReader("testA_PlugReaders");

        Thread.sleep(100);


    }

    @Test
    public void testB_UnplugReaders() throws InterruptedException, KeypleReaderException {

        Observable.Observer disconnected_obs = new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {
                Assert.assertEquals(PluginEvent.EventType.READER_DISCONNECTED,
                        event.getEventType());
            }
        };


        // add a reader
        stubPlugin.plugStubReader("testB_UnplugReaders");

        // let the monitor thread work
        Thread.sleep(100);


        logger.debug("Stubplugin readers size {} ", stubPlugin.getReaders().size());
        assert (stubPlugin.getReaders().size() == 1);


        stubPlugin.addObserver(disconnected_obs);
        stubPlugin.unplugReader("testB_UnplugReaders");

        Thread.sleep(100);

        Assert.assertEquals(0, stubPlugin.getReaders().size());

        // clean
        stubPlugin.removeObserver(disconnected_obs);

    }

    @Test
    public void testC_PlugSameReaderTwice() throws InterruptedException, KeypleReaderException {
        stubPlugin.plugStubReader("testC_PlugSameReaderTwice");
        stubPlugin.plugStubReader("testC_PlugSameReaderTwice");
        logger.debug("Stubplugin readers size {} ", stubPlugin.getReaders().size());

        // let the monitor thread work
        Thread.sleep(100);

        assert (stubPlugin.getReaders().size() == 1);
        stubPlugin.unplugReader("testC_PlugSameReaderTwice");

        // let the monitor thread work
        Thread.sleep(100);

        logger.debug("Stubplugin readers size {} ", stubPlugin.getReaders().size());
        assert (stubPlugin.getReaders().size() == 0);
    }

    @Test
    public void testD_GetName() {
        assert (stubPlugin.getName() != null);
    }
}
