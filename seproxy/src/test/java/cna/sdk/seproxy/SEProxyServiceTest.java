package cna.sdk.seproxy;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class SEProxyServiceTest {

    @Test
    public void testGetInstance() {
        SEProxyService proxyService = SEProxyService.getInstance();
        assertNotNull(proxyService);
    }

    @Test
    public void testGetVersion() {
        SEProxyService proxyService = SEProxyService.getInstance();
        assertEquals(1, proxyService.getVersion().intValue());
    }

    @Test
    public void testSetPlugins() {
        SEProxyService proxyService = SEProxyService.getInstance();
        proxyService.setPlugins(new ArrayList<ReadersPlugin>());
        assertArrayEquals(new ArrayList<ReadersPlugin>().toArray(), proxyService.getPlugins().toArray());
    }

    @Test
    public void testGetPlugins() {
        SEProxyService proxyService = SEProxyService.getInstance();

        assertArrayEquals(new ArrayList<ReadersPlugin>().toArray(), proxyService.getPlugins().toArray());

    }

}
