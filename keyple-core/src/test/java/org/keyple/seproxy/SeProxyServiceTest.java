/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import static org.junit.Assert.*;
import java.util.ArrayList;
import org.junit.Test;

public class SeProxyServiceTest {

    @Test
    public void testGetInstance() {
        SeProxyService proxyService = SeProxyService.getInstance();
        assertNotNull(proxyService);
    }

    @Test
    public void testGetVersion() {
        SeProxyService proxyService = SeProxyService.getInstance();
        assertEquals(1, proxyService.getVersion().intValue());
    }

    @Test
    public void testSetPlugins() {
        SeProxyService proxyService = SeProxyService.getInstance();
        proxyService.setPlugins(new ArrayList<ReadersPlugin>());
        assertArrayEquals(new ArrayList<ReadersPlugin>().toArray(),
                proxyService.getPlugins().toArray());
    }

    @Test
    public void testGetPlugins() {
        SeProxyService proxyService = SeProxyService.getInstance();

        assertArrayEquals(new ArrayList<ReadersPlugin>().toArray(),
                proxyService.getPlugins().toArray());

    }

}
