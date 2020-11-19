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
import org.eclipse.keyple.core.service.event.PluginObservationExceptionHandler;
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

  /** Observation exception handler */
  class PluginExceptionHandler implements PluginObservationExceptionHandler {

    @Override
    public void onPluginObservationError(String pluginName, Throwable e) {}
  }

  /**
   * An KeypleRuntimeException is thrown when registering the plugin
   *
   * @throws Throwable
   */
  @Test(expected = KeypleReaderException.class)
  public void instantiatePlugin() {
    BlankFailingPlugin plugin = new BlankFailingPlugin("addObserverTest");
    plugin.register();
  }

  @Test(expected = IllegalStateException.class)
  public void addObserver_without_exception_handler() throws Throwable {
    MockAbstractThreadedPlugin plugin = new MockAbstractThreadedPlugin("addObserverTest");

    // add observer
    plugin.addObserver(getOneObserver());
  }

  @Test
  public void addObserver() throws Throwable {
    MockAbstractThreadedPlugin plugin = new MockAbstractThreadedPlugin("addObserverTest");

    // add exception handler
    plugin.setPluginObservationExceptionHandler(new PluginExceptionHandler());

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

    // add exception handler
    plugin.setPluginObservationExceptionHandler(new PluginExceptionHandler());

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

    // add exception handler
    plugin.setPluginObservationExceptionHandler(new PluginExceptionHandler());

    // add observer
    plugin.addObserver(obs);
    plugin.clearObservers();

    Assert.assertEquals(0, plugin.countObservers());
    // test if thread is deactivated
    Assert.assertFalse(plugin.isMonitoring());

    // shutdown thread
    plugin.finalize();
  }

  @Test
  public void trigExceptionHandler() throws Throwable {
    MockAbstractThreadedPlugin plugin = new MockAbstractThreadedPlugin("addObserverTest");

    plugin.register();

    // add exception handler
    plugin.setPluginObservationExceptionHandler(new PluginExceptionHandler());

    // add observer
    plugin.addObserver(getOneFailingObserver());

    Assert.assertEquals(1, plugin.countObservers());

    // test if thread is activated
    Assert.assertTrue(plugin.isMonitoring());

    // insert a reader to trig the monitoring thread
    plugin.addNativeReaderName("READER1");

    // TODO add a delay and check that the exception handle is invoked
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

  ObservablePlugin.PluginObserver getOneFailingObserver() {
    return new ObservablePlugin.PluginObserver() {
      @Override
      public void update(PluginEvent event) {
        throw new IllegalStateException("Exception from update");
      }
    };
  }
}
