/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.remotese.virtualse.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.core.seproxy.PluginFactory;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.plugin.remotese.core.KeypleServerAsync;
import org.eclipse.keyple.plugin.remotese.virtualse.RemoteSeServerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>Remote Se Server Plugin</b> Factory
 *
 * <p>This factory must be used in the use case of the <b>Remote SE Server Plugin</b>.
 *
 * <p>To register a Remote Se Server Plugin, use the method {@link
 * org.eclipse.keyple.core.seproxy.SeProxyService#registerPlugin(PluginFactory)} fed in with
 * an instance of this factory. Invoke the {@link #builder()} method to create and configure a
 * factory instance.</p>
 *
 * <p>Plugin name is defined by default in the factory. Access the Remote Se Server Plugin with the {@link
 * RemoteSeServerUtils#getAsyncPlugin()} or {@link RemoteSeServerUtils#getSyncNode()} depending on
 * your node configuration.</p>
 */
public class RemoteSeServerPluginFactory implements PluginFactory {

  private static final Logger logger = LoggerFactory.getLogger(RemoteSeServerPluginFactory.class);
  /** default name of the RemoteSeServerPlugin for a sync node : {@value} */
  static final String PLUGIN_NAME_SYNC = "RemoteSeServerPluginSync";
  /** default name of the RemoteSeServerPlugin for a async node : {@value} */
  static final String PLUGIN_NAME_ASYNC = "RemoteSeServerPluginAsync";

  private RemoteSeServerPlugin plugin;

  /**
   * Create a builder process for this factory
   *
   * @return next configuration step
   * @since 1.0
   */
  public static NodeStep builder() {
    return new Builder();
  }

  /**
   * (private)<br>
   * constructor
   *
   * @param plugin instance created from the builder process
   */
  private RemoteSeServerPluginFactory(RemoteSeServerPlugin plugin) {
    this.plugin = plugin;
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public String getPluginName() {
    return plugin.getName();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public ReaderPlugin getPlugin() {
    return plugin;
  }

  public interface NodeStep {
    /**
     * Configure the plugin with an async server endpoint
     *
     * @param asyncEndpoint non nullable instance of an async server endpoint
     * @return next configuration step
     * @since 1.0
     */
    PluginObserverStep withAsyncNode(KeypleServerAsync asyncEndpoint);

    /**
     * Configure the plugin with a sync server endpoint
     *
     * @return next configuration step
     * @since 1.0
     */
    PluginObserverStep withSyncNode();
  }

  public interface PluginObserverStep {
    /**
     * Configure the observer of the plugin
     *
     * @param observer
     * @return next configuration step
     * @since 1.0
     */
    EventNotificationPoolStep withPluginObserver(ObservablePlugin.PluginObserver observer);
  }

  public interface EventNotificationPoolStep {
    /**
     * Configure the plugin to use a default pool for events notification
     *
     * @return next configuration step
     * @since 1.0
     */
    BuilderStep usingDefaultEventNotificationPool();

    /**
     * Configure the plugin to use a custom pool for events notification
     *
     * @param eventNotificationPool
     * @return next configuration step
     * @since 1.0
     */
    BuilderStep usingEventNotificationPool(ExecutorService eventNotificationPool);
  }

  public interface BuilderStep {
    /**
     * Build the plugin factory
     *
     * @return instance of the plugin factory
     * @since 1.0
     */
    RemoteSeServerPluginFactory build();
  }

  /** The builder pattern */
  public static class Builder
      implements NodeStep, PluginObserverStep, EventNotificationPoolStep, BuilderStep {

    private KeypleServerAsync asyncEndpoint;
    private ExecutorService eventNotificationPool;
    private ObservablePlugin.PluginObserver observer;

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public PluginObserverStep withAsyncNode(KeypleServerAsync asyncEndpoint) {
      Assert.getInstance().notNull(asyncEndpoint, "asyncEndpoint");
      this.asyncEndpoint = asyncEndpoint;
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public PluginObserverStep withSyncNode() {
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public EventNotificationPoolStep withPluginObserver(ObservablePlugin.PluginObserver observer) {
      Assert.getInstance().notNull(observer, "observer");
      this.observer = observer;
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public BuilderStep usingDefaultEventNotificationPool() {
      this.eventNotificationPool = Executors.newCachedThreadPool();
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public BuilderStep usingEventNotificationPool(ExecutorService eventNotificationPool) {
      Assert.getInstance().notNull(eventNotificationPool, "eventNotificationPool");
      this.eventNotificationPool = eventNotificationPool;
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public RemoteSeServerPluginFactory build() {

      RemoteSeServerPluginImpl plugin;

      if (asyncEndpoint != null) {
        plugin = new RemoteSeServerPluginImpl(PLUGIN_NAME_ASYNC, eventNotificationPool);
        logger.info("Create a new RemoteSeServerPlugin with a async server endpoint");
        plugin.bindServerAsyncNode(asyncEndpoint);
      } else {
        plugin = new RemoteSeServerPluginImpl(PLUGIN_NAME_SYNC, eventNotificationPool);
        logger.info("Create a new RemoteSeServerPlugin with a sync server endpoint");
        plugin.bindServerSyncNode();
      }

      plugin.addObserver(observer);

      return new RemoteSeServerPluginFactory(plugin);
    }
  }
}
