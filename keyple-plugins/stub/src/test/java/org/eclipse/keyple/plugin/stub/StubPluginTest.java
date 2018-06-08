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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StubPluginTest {

    StubPlugin stubPlugin;

    @Before
    public void setUp() throws IOReaderException {
        stubPlugin = StubPlugin.getInstance();

    }

    @Test
    public void testGetReaders() throws IOReaderException {
        assert (stubPlugin.getReaders().size() == 1);
    }

    @Test
    public void testGetName() throws IOReaderException {
        assert (stubPlugin.getName() != null);
    }

}
