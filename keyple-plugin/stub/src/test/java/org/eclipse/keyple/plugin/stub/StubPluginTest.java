/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.stub;


import org.eclipse.keyple.seproxy.exception.IOReaderException;
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

    @Before
    public void setUp() throws IOReaderException {
        stubPlugin = StubPlugin.getInstance();
    }

    @Test
    public void testA_PlugReaders() throws IOReaderException {
        stubPlugin.plugStubReader("test");
        assert (stubPlugin.getReaders().size() == 1);
    }

    @Test(expected = AssertionError.class)
    public void testB_PlugSameReaderTwice() throws IOReaderException {
        stubPlugin.plugStubReader("test");
        stubPlugin.plugStubReader("test");
        assert (stubPlugin.getReaders().size() == 1);
    }

    @Test
    public void testC_UnPlugReaders() throws IOReaderException {
        stubPlugin.unplugReader("test");
        assert (stubPlugin.getReaders().size() == 0);
    }

    @Test
    public void testD_GetName() throws IOReaderException {
        assert (stubPlugin.getName() != null);
    }
}
