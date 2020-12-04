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
package org.eclipse.keyple.plugin.remote.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.PluginFactory;
import org.eclipse.keyple.core.service.event.ObservablePlugin;
import org.eclipse.keyple.core.service.event.PluginEvent;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.plugin.remote.RemotePluginServer;
import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class of the {@link RemotePluginServer}.
 *
 * @since 1.0
 */
public final class RemotePluginServerFactory implements PluginFactory {

  private static final Logger logger = LoggerFactory.getLogger(RemotePluginServerFactory.class);

  static final String DEFAULT_PLUGIN_NAME = "DefaultRemotePluginServer";

  private RemotePluginServer plugin;

  /**
   * (private)<br>
   * Constructor
   *
   * @param plugin instance created from the builder process
   */
  private RemotePluginServerFactory(RemotePluginServer plugin) {
    this.plugin = plugin;
  }

  /**
   * Create a builder process for this factory
   *
   * @return next configuration step
   * @since 1.0
   */
  public static NameStep builder() {
    return new Builder();
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
  public Plugin getPlugin() {
    return plugin;
  }

  /**
   * Step to configure the plugin name.
   *
   * @since 1.0
   */
  public interface NameStep {
    /**
     * Configures the plugin with a specific name.
     *
     * @param pluginName The specific plugin name.
     * @return next configuration step
     * @since 1.0
     */
    NodeStep withPluginName(String pluginName);

    /**
     * Configures the plugin with the default name : {@value DEFAULT_PLUGIN_NAME}.
     *
     * @return next configuration step
     * @since 1.0
     */
    NodeStep withDefaultPluginName();
  }

  /**
   * Step to configure the node associated with the plugin.
   *
   * @since 1.0
   */
  public interface NodeStep {
    /**
     * Configures the plugin with a {@link org.eclipse.keyple.plugin.remote.AsyncNodeServer} node.
     *
     * @param endpoint The {@link AsyncEndpointServer} network endpoint to use.
     * @return next configuration step
     * @since 1.0
     */
    PluginObserverStep withAsyncNode(AsyncEndpointServer endpoint);

    /**
     * Configures the service with a {@link org.eclipse.keyple.plugin.remote.SyncNodeServer} node.
     *
     * @return next configuration step
     * @since 1.0
     */
    PluginObserverStep withSyncNode();
  }

  /**
   * Step to register the observer of the plugin.
   *
   * @since 1.0
   */
  public interface PluginObserverStep {
    /**
     * Sets the main observer of the plugin.
     *
     * <p>More observers can be added later with the method {@link
     * RemotePluginServer#addObserver(ObservablePlugin.PluginObserver)}.
     *
     * @param observer The plugin observer.
     * @return next configuration step
     * @since 1.0
     */
    EventNotificationPoolStep withPluginObserver(ObservablePlugin.PluginObserver observer);
  }

  /**
   * Step to choose the thread pool to use for events notification.
   *
   * @since 1.0
   */
  public interface EventNotificationPoolStep {
    /**
     * Configures the plugin to use the default pool for events notification.
     *
     * <p>The thread pool used by default is a {@link Executors#newCachedThreadPool()}. From the
     * documentation, "it creates new threads as needed, but will reuse previously constructed
     * threads when they are available."
     *
     * @return next configuration step
     * @since 1.0
     */
    BuilderStep usingDefaultEventNotificationPool();

    /**
     * Configures the plugin to use a custom thread pool for events notification.
     *
     * <p>The custom pool should be flexible enough to handle many concurrent tasks as each {@link
     * ReaderEvent} and {@link PluginEvent} are executed asynchronously.
     *
     * @param eventNotificationPool non nullable instance of a executor service
     * @return next configuration step
     * @since 1.0
     */
    BuilderStep usingEventNotificationPool(ExecutorService eventNotificationPool);
  }

  /**
   * Last step : builds the factory.
   *
   * @since 1.0
   */
  public interface BuilderStep {
    /**
     * Builds the plugin factory instance.
     *
     * @return instance of the plugin factory
     * @since 1.0
     */
    RemotePluginServerFactory build();
  }

  /** The builder pattern to create the factory instance. */
  private static class Builder
      implements NameStep, NodeStep, PluginObserverStep, EventNotificationPoolStep, BuilderStep {

    private AsyncEndpointServer asyncEndpoint;
    private ExecutorService eventNotificationPool;
    private ObservablePlugin.PluginObserver observer;
    private String pluginName;

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public NodeStep withPluginName(String pluginName) {
      Assert.getInstance().notNull(pluginName, "pluginName");
      this.pluginName = pluginName;
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public NodeStep withDefaultPluginName() {
      this.pluginName = DEFAULT_PLUGIN_NAME;
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public PluginObserverStep withAsyncNode(AsyncEndpointServer endpoint) {
      Assert.getInstance().notNull(endpoint, "endpoint");
      this.asyncEndpoint = endpoint;
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
    public RemotePluginServerFactory build() {

      RemotePluginServerImpl plugin = new RemotePluginServerImpl(pluginName, eventNotificationPool);

      if (asyncEndpoint != null) {
        logger.info("Create a new RemotePluginServer with a AsyncNodeServer");
        plugin.bindAsyncNodeServer(asyncEndpoint);
      } else {
        logger.info("Create a new RemotePluginServer with a SyncNodeServer");
        plugin.bindSyncNodeServer();
      }

      plugin.addObserver(observer);

      return new RemotePluginServerFactory(plugin);
    }
  }
}
