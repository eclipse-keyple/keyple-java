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
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StubPluginTest {

    StubPlugin stubPlugin;
    Integer test_event; // 0 if READER_CONNECTED, 1 if READER_DISCONNECTED

    @Before
    public void setUp() throws IOReaderException {
        stubPlugin = StubPlugin.getInstance(); // singleton

        stubPlugin.clearObservers();
        // add one observer for all tests
        stubPlugin.addObserver(new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {
                switch (test_event) {
                    case 0:
                        Assert.assertEquals(PluginEvent.EventType.READER_CONNECTED,
                                event.getEventType());
                        break;
                    case 1:
                        Assert.assertEquals(PluginEvent.EventType.READER_DISCONNECTED,
                                event.getEventType());
                        break;

                }
            }
        });
    }

    @Test
    public void testA_PlugReaders() throws IOReaderException {

        test_event = 0;
        stubPlugin.plugStubReader("test");
        assert (stubPlugin.getReaders().size() == 1);

    }

    @Test
    public void testB_UnplugReaders() throws IOReaderException {
        test_event = 1;
        stubPlugin.unplugReader("test");
        assert (stubPlugin.getReaders().size() == 0);
    }

    @Test
    public void testC_PlugSameReaderTwice() throws IOReaderException {
        test_event = 0;
        stubPlugin.plugStubReader("test");
        stubPlugin.plugStubReader("test");
        assert (stubPlugin.getReaders().size() == 1);
        test_event = 1;
        stubPlugin.unplugReader("test");
        assert (stubPlugin.getReaders().size() == 0);
    }



    @Test
    public void testD_GetName() throws IOReaderException {
        assert (stubPlugin.getName() != null);
    }

}
