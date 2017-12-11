package org.keyple.seproxy;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;
import org.keyple.seproxy.ReadersPlugin;
import org.keyple.seproxy.SeProxyService;

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
        assertArrayEquals(new ArrayList<ReadersPlugin>().toArray(), proxyService.getPlugins().toArray());
    }

    @Test
    public void testGetPlugins() {
        SeProxyService proxyService = SeProxyService.getInstance();

        assertArrayEquals(new ArrayList<ReadersPlugin>().toArray(), proxyService.getPlugins().toArray());

    }

}
