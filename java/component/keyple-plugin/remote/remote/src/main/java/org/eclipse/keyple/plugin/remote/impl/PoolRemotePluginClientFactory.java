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

import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.PluginFactory;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.plugin.remote.PoolRemotePluginClient;
import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointClient;
import org.eclipse.keyple.plugin.remote.spi.SyncEndpointClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class of the {@link PoolRemotePluginClient}.
 *
 * @since 1.0
 */
public final class PoolRemotePluginClientFactory implements PluginFactory {

  private static final Logger logger = LoggerFactory.getLogger(PoolRemotePluginClientFactory.class);

  static final String DEFAULT_PLUGIN_NAME = "DefaultPoolRemotePluginClient";

  private static final int DEFAULT_TIMEOUT = 5;

  private final PoolRemotePluginClient plugin;

  /**
   * (private)<br>
   * Constructor
   *
   * @param plugin instance created from the builder process
   */
  private PoolRemotePluginClientFactory(PoolRemotePluginClient plugin) {
    this.plugin = plugin;
  }

  /**
   * Init the builder
   *
   * @return next configuration step
   * @since 1.0
   */
  public static NameStep builder() {
    return new Builder();
  }

  /** {@inheritDoc} */
  @Override
  public String getPluginName() {
    return plugin.getName();
  }

  /** {@inheritDoc} */
  @Override
  public Plugin getPlugin() {
    return plugin;
  }

  public interface NameStep {
    /**
     * Configures the plugin with a specific name.
     *
     * @param pluginName specific plugin name.
     * @return next configuration step
     * @since 1.0
     */
    NodeStep withPluginName(String pluginName);

    /**
     * Configures the plugin with the default name : {@value DEFAULT_PLUGIN_NAME} . Note that only
     * one plugin of this type with the default name can be register.
     *
     * @return next configuration step
     * @since 1.0
     */
    NodeStep withDefaultPluginName();
  }

  public interface NodeStep {
    /**
     * Configures the plugin with a {@link org.eclipse.keyple.plugin.remote.AsyncNodeClient} node.
     *
     * @param endpoint The {@link AsyncEndpointClient} network endpoint to use.
     * @return next configuration step
     * @since 1.0
     */
    TimeoutStep withAsyncNode(AsyncEndpointClient endpoint);

    /**
     * Configures the plugin with a {@link org.eclipse.keyple.plugin.remote.SyncNodeClient} node.
     *
     * @param endpoint The {@link SyncEndpointClient} network endpoint to use.
     * @return next configuration step
     * @since 1.0
     */
    BuilderStep withSyncNode(SyncEndpointClient endpoint);
  }

  public interface TimeoutStep {
    /**
     * Sets the default timeout of 5 seconds.
     *
     * <p>This timeout defines how long the async client waits for a server order before cancelling
     * the global transaction.
     *
     * @return next configuration step
     * @since 1.0
     */
    BuilderStep usingDefaultTimeout();

    /**
     * Sets the provided timeout.
     *
     * <p>This timeout defines how long the async client waits for a server order before cancelling
     * the global transaction.
     *
     * @param timeoutInSeconds timeout in seconds
     * @return next configuration step
     * @since 1.0
     */
    BuilderStep usingTimeout(int timeoutInSeconds);
  }

  public interface BuilderStep {
    /**
     * Build the plugin factory instance.
     *
     * <p>This instance should be passed to {@link SmartCardService#registerPlugin(PluginFactory)}
     * in order to register the plugin.
     *
     * @return instance of the plugin factory
     * @since 1.0
     */
    PoolRemotePluginClientFactory build();
  }

  /** The builder pattern to create the factory instance. */
  private static class Builder implements NameStep, NodeStep, BuilderStep, TimeoutStep {

    private AsyncEndpointClient asyncEndpoint;
    private SyncEndpointClient syncEndpoint;
    private int timeoutInSec;
    private String pluginName;

    /** {@inheritDoc} */
    @Override
    public NodeStep withPluginName(String pluginName) {
      Assert.getInstance().notNull(pluginName, "plugin name");
      this.pluginName = pluginName;
      return this;
    }

    /** {@inheritDoc} */
    @Override
    public NodeStep withDefaultPluginName() {
      this.pluginName = DEFAULT_PLUGIN_NAME;
      return this;
    }

    /** {@inheritDoc} */
    @Override
    public TimeoutStep withAsyncNode(AsyncEndpointClient endpoint) {
      Assert.getInstance().notNull(endpoint, "endpoint");
      this.asyncEndpoint = endpoint;
      return this;
    }

    /** {@inheritDoc} */
    @Override
    public BuilderStep withSyncNode(SyncEndpointClient endpoint) {
      Assert.getInstance().notNull(endpoint, "endpoint");
      this.syncEndpoint = endpoint;
      return this;
    }

    /** {@inheritDoc} */
    @Override
    public BuilderStep usingDefaultTimeout() {
      timeoutInSec = DEFAULT_TIMEOUT;
      return this;
    }

    /** {@inheritDoc} */
    @Override
    public BuilderStep usingTimeout(int timeoutInSeconds) {
      timeoutInSec = timeoutInSeconds;
      return this;
    }

    /** {@inheritDoc} */
    @Override
    public PoolRemotePluginClientFactory build() {

      PoolRemotePluginClientImpl plugin = new PoolRemotePluginClientImpl(pluginName);

      if (asyncEndpoint != null) {
        logger.info("Create a new PoolRemotePluginClient with a AsyncNodeClient");
        plugin.bindAsyncNodeClient(asyncEndpoint, timeoutInSec);
      } else {
        logger.info("Create a new PoolRemotePluginClient with a SyncNodeClient");
        plugin.bindSyncNodeClient(syncEndpoint, null, null);
      }

      return new PoolRemotePluginClientFactory(plugin);
    }
  }
}
