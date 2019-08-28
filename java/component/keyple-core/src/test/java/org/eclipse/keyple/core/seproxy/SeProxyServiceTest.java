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
package org.eclipse.keyple.core.seproxy;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import java.util.SortedSet;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservablePlugin;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.SignatureDeclareThrowsException")
@RunWith(MockitoJUnitRunner.class)
public class SeProxyServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(SeProxyServiceTest.class);


    // class to test
    SeProxyService proxyService;

    AbstractObservablePlugin plugin1 = new MockAbstractObservablePlugin(PLUGIN_NAME_1);

    AbstractObservablePlugin plugin2 = new MockAbstractObservablePlugin(PLUGIN_NAME_2);;

    @Mock
    PluginFactory factory1;

    @Mock
    PluginFactory factory2;


    static String PLUGIN_NAME_1 = "plugin1";
    static String PLUGIN_NAME_2 = "plugin2";

    @Before
    public void setupBeforeEach() {

        // init class to test
        proxyService = SeProxyService.getInstance();

        Assert.assertEquals(0, proxyService.getPlugins().size());

        when(factory1.getPluginInstance()).thenReturn(plugin1);
        when(factory2.getPluginInstance()).thenReturn(plugin2);

        when(factory1.getPluginName()).thenReturn(PLUGIN_NAME_1);
        when(factory2.getPluginName()).thenReturn(PLUGIN_NAME_2);

    }

    @Test
    public void testGetVersion() {
        // test that version follows semver guidelines
        String regex =
                "^([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z-]+)?$";
        String version = SeProxyService.getInstance().getVersion();
        logger.info("Version of SeProxyService {}", version);
        System.out.println("Version of SeProxyService " + version);
        assertTrue(version.matches(regex));
    }

    /*
     * @Test public void testGetSetPlugins() { // init ConcurrentSkipListSet<ReaderPlugin> plugins =
     * getPluginList();
     * 
     * // test proxyService.setPlugins(plugins); assertArrayEquals(plugins.toArray(),
     * proxyService.getPlugins().toArray()); }
     */
    @Test
    public void testRegisterPlugin() throws KeyplePluginNotFoundException {

        // register plugin1 by its factory
        proxyService.registerPlugin(factory1);

        // results
        ReaderPlugin testPlugin = proxyService.getPlugin(PLUGIN_NAME_1);
        SortedSet testPlugins = proxyService.getPlugins();

        Assert.assertNotNull(testPlugin);
        Assert.assertEquals(PLUGIN_NAME_1, testPlugin.getName());
        Assert.assertEquals(1, testPlugins.size());

        // unregister
        proxyService.unregisterPlugin(PLUGIN_NAME_1);


    }

    @Test
    public void testRegisterTwicePlugin() throws KeyplePluginNotFoundException {

        // register plugin1 by its factory
        proxyService.registerPlugin(factory1);
        proxyService.registerPlugin(factory1);

        // should not be added twice
        SortedSet testPlugins = proxyService.getPlugins();
        Assert.assertEquals(1, testPlugins.size());

        // unregister
        proxyService.unregisterPlugin(PLUGIN_NAME_1);

    }

    @Test
    public void testRegisterTwoPlugins() throws KeyplePluginNotFoundException {

        // register plugin1 by its factory
        proxyService.registerPlugin(factory1);
        proxyService.registerPlugin(factory2);

        // should not be added twice
        SortedSet testPlugins = proxyService.getPlugins();
        Assert.assertEquals(2, testPlugins.size());

        // unregister
        proxyService.unregisterPlugin(PLUGIN_NAME_1);
        proxyService.unregisterPlugin(PLUGIN_NAME_2);

    }



    @Test(expected = KeyplePluginNotFoundException.class)
    public void testGetPluginFail() throws Exception {
        proxyService.getPlugin("unknown");// Throw exception

    }



}
