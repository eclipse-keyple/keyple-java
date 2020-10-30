/* **************************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.plugin;

import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.service.event.ObservablePlugin;
import org.eclipse.keyple.core.service.event.PluginEvent;
import org.eclipse.keyple.core.service.exception.KeypleReaderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractThreadedObservablePluginTest extends CoreBaseTest {

  private static final Logger logger =
      LoggerFactory.getLogger(AbstractThreadedObservablePluginTest.class);

  @Before
  public void setUp() {
    logger.info("------------------------------");
    logger.info("Test {}", name.getMethodName() + "");
    logger.info("------------------------------");
  }

  /**
   * An KeypleRuntimeException is thrown when building the plugin
   *
   * @throws Throwable
   */
  @Test(expected = KeypleReaderException.class)
  public void instantiatePlugin() {
    new BlankFailingPlugin("addObserverTest");
  }

  @Test
  public void addObserver() throws Throwable {
    MockAbstractThreadedPlugin plugin = new MockAbstractThreadedPlugin("addObserverTest");

    // add observer
    plugin.addObserver(getOneObserver());

    Assert.assertEquals(1, plugin.countObservers());
    // test if thread is activated
    Assert.assertTrue(plugin.isMonitoring());

    // shutdown thread
    plugin.finalize();
  }

  @Test
  public void removeObserver() throws Throwable {
    MockAbstractThreadedPlugin plugin = new MockAbstractThreadedPlugin("addObserverTest");

    ObservablePlugin.PluginObserver obs = getOneObserver();

    // add observer
    plugin.addObserver(obs);
    plugin.removeObserver(obs);

    Assert.assertEquals(0, plugin.countObservers());
    // test if thread is deactivated
    Assert.assertFalse(plugin.isMonitoring());

    // shutdown thread
    plugin.finalize();
  }

  @Test
  public void clearObserver() throws Throwable {
    MockAbstractThreadedPlugin plugin = new MockAbstractThreadedPlugin("addObserverTest");

    ObservablePlugin.PluginObserver obs = getOneObserver();

    // add observer
    plugin.addObserver(obs);
    plugin.clearObservers();

    Assert.assertEquals(0, plugin.countObservers());
    // test if thread is deactivated
    Assert.assertFalse(plugin.isMonitoring());

    // shutdown thread
    plugin.finalize();
  }

  /*
   * Helpers
   */
  ObservablePlugin.PluginObserver getOneObserver() {
    return new ObservablePlugin.PluginObserver() {
      @Override
      public void update(PluginEvent event) {}
    };
  }
}
