/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.stub;


import org.eclipse.keyple.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.plugin.AbstractObservableReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class StubPluginTest {

    StubPlugin stubPlugin;
    Logger logger = LoggerFactory.getLogger(StubPluginTest.class);

    @Before
    public void setUp() throws KeypleReaderException, KeypleReaderException {
        logger.info("Reset Stubplugin readers and stubplugin observers");

        stubPlugin = StubPlugin.getInstance(); // singleton

        // delete all observers
        stubPlugin.clearObservers();

        // unplug all readers
        for (AbstractObservableReader reader : stubPlugin.getReaders()) {
            stubPlugin.unplugReader(reader.getName());
        }
        logger.info("Stubplugin readers size {}", stubPlugin.getReaders().size());
        Assert.assertEquals(stubPlugin.getNativeReaders().size(), 0);
        logger.info("Stubplugin observers size {}", stubPlugin.countObservers());
        Assert.assertEquals(stubPlugin.countObservers(), 0);

    }

    @Test
    public void testA_PlugReaders() throws KeypleReaderException {

        // add READER_CONNECTED assert observer
        stubPlugin.addObserver(new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {
                Assert.assertEquals(PluginEvent.EventType.READER_CONNECTED, event.getEventType());
            }
        });

        stubPlugin.plugStubReader("test");
        logger.debug("Stubplugin readers size {} ", stubPlugin.getReaders().size());

        assert (stubPlugin.getReaders().size() == 1);

    }

    @Test
    public void testB_UnplugReaders() throws KeypleReaderException, KeypleReaderException {
        // add a reader
        stubPlugin.plugStubReader("test");
        assert (stubPlugin.getReaders().size() == 1);

        // add READER_DISCONNECTED assert observer
        stubPlugin.addObserver(new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {
                Assert.assertEquals(PluginEvent.EventType.READER_DISCONNECTED,
                        event.getEventType());
            }
        });

        // unplug reader
        stubPlugin.unplugReader("test");
        logger.debug("Stubplugin readers size {} ", stubPlugin.getReaders().size());
        assert (stubPlugin.getReaders().size() == 0);
    }

    @Test
    public void testC_PlugSameReaderTwice() throws KeypleReaderException, KeypleReaderException {
        stubPlugin.plugStubReader("test");
        stubPlugin.plugStubReader("test");
        logger.debug("Stubplugin readers size {} ", stubPlugin.getReaders().size());
        assert (stubPlugin.getReaders().size() == 1);
        stubPlugin.unplugReader("test");
        logger.debug("Stubplugin readers size {} ", stubPlugin.getReaders().size());
        assert (stubPlugin.getReaders().size() == 0);
    }



    @Test
    public void testD_GetName() throws KeypleReaderException {
        assert (stubPlugin.getName() != null);
    }

}
