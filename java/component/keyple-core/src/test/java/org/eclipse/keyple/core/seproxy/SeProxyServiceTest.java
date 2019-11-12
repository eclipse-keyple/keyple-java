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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginInstanciationException;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.plugin.AbstractPlugin;
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

    AbstractPlugin plugin1 = new MockAbstractThreadedPlugin(PLUGIN_NAME_1);

    AbstractPlugin plugin2 = new MockAbstractThreadedPlugin(PLUGIN_NAME_2);;

    @Mock
    AbstractPluginFactory factory1;

    @Mock
    AbstractPluginFactory factory2;


    static String PLUGIN_NAME_1 = "plugin1";
    static String PLUGIN_NAME_2 = "plugin2";

    @Before
    public void setupBeforeEach() throws KeyplePluginInstanciationException {

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

    @Test(expected = KeyplePluginInstanciationException.class)
    public void testFailingPlugin() throws KeyplePluginInstanciationException {

        doThrow(new KeyplePluginInstanciationException("")).when(factory1).getPluginInstance();

        proxyService.registerPlugin(factory1);
    }



    @Test
    public void testRegisterPlugin()
            throws KeyplePluginNotFoundException, KeyplePluginInstanciationException {

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
    public void testRegisterTwicePlugin() throws KeyplePluginInstanciationException {

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
    public void testRegisterTwoPlugins() throws KeyplePluginInstanciationException {

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


    /**
     * Test that a plugin can not be added twice with multi thread
     * 
     * @throws Exception
     */
    @Test
    public void testRegister_MultiThread() throws Exception {

        final MockObservablePluginFactory factory = new MockObservablePluginFactory(PLUGIN_NAME_1);
        final CountDownLatch latch = new CountDownLatch(1);

        final AtomicBoolean running = new AtomicBoolean();
        final AtomicInteger overlaps = new AtomicInteger();

        int threads = 10;
        ExecutorService service = Executors.newFixedThreadPool(threads);
        Collection<Future> futures = new ArrayList(threads);

        for (int t = 0; t < threads; ++t) {
            futures.add(service.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        /*
                         * All thread wait for the countdown
                         */
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (running.get()) {
                        overlaps.incrementAndGet();
                    }
                    running.set(true);
                    try {
                        proxyService.registerPlugin(factory);
                    } catch (KeyplePluginInstanciationException e) {
                        e.printStackTrace();
                    }
                    running.set(false);

                }
            }));
        }
        /*
         * Release all thread at once
         */
        latch.countDown();
        /*
         * wait for execution
         */
        Thread.sleep(500);
        logger.info("Overlap {}", overlaps);
        assertEquals(1, proxyService.getPlugins().size());

        // unregister
        proxyService.unregisterPlugin(PLUGIN_NAME_1);


    }

    /**
     * Test that a plugin can not be added twice with multi thread
     *
     * @throws Exception
     */
    @Test
    public void unregisterMultiThread() throws Exception {

        final MockObservablePluginFactory factory = new MockObservablePluginFactory(PLUGIN_NAME_1);

        // add a plugin
        proxyService.registerPlugin(factory);


        final CountDownLatch latch = new CountDownLatch(1);

        final AtomicBoolean running = new AtomicBoolean();
        final AtomicInteger overlaps = new AtomicInteger();

        int threads = 10;
        ExecutorService service = Executors.newFixedThreadPool(threads);
        Collection<Future> futures = new ArrayList(threads);

        for (int t = 0; t < threads; ++t) {
            futures.add(service.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        /*
                         * All thread wait for the countdown
                         */
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (running.get()) {
                        overlaps.incrementAndGet();
                    }
                    running.set(true);
                    proxyService.unregisterPlugin(factory.getPluginName());
                    running.set(false);
                }
            }));
        }
        /*
         * Release all thread at once
         */
        latch.countDown();
        Thread.sleep(500);
        logger.info("Overlap {}", overlaps);
        assertEquals(0, proxyService.getPlugins().size());
        // unregister
        proxyService.unregisterPlugin(PLUGIN_NAME_1);


    }

}
