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
import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointClient;
import org.eclipse.keyple.plugin.remote.spi.SyncEndpointClient;
import org.eclipse.keyple.plugin.remote.AsyncNodeServer;
import org.eclipse.keyple.plugin.remote.RemotePoolClientPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>Remote Pool Client Plugin</b> Factory
 *
 * <p>This factory must be used in the use case of the <b>Remote Pool Client Plugin</b>.
 *
 * <p>To register a Remote Pool Client Plugin, use the method {@link
 * SmartCardService#registerPlugin(PluginFactory)} fed in with an instance of this factory. Invoke
 * the {@link #builder()} method to create and configure a factory instance.
 *
 * <p>Plugin name is defined by default in the factory. Access the Remote Pool Client Plugin with
 * the {@link RemotePoolClientUtils#getRemotePlugin()} ()}.
 */
public class RemotePoolClientPluginFactory implements PluginFactory {

  private static final Logger logger = LoggerFactory.getLogger(RemotePoolClientPluginFactory.class);
  /** default name of the RemotePoolClientPlugin : {@value} */
  static final String DEFAULT_PLUGIN_NAME = "DefaultRemotePoolServerPlugin";

  private static final int DEFAULT_TIMEOUT = 5;

  private final RemotePoolClientPlugin plugin;

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
  private RemotePoolClientPluginFactory(RemotePoolClientPlugin plugin) {
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
  public Plugin getPlugin() {
    return plugin;
  }

  public interface NodeStep {
    /**
     * Configure the plugin with an async server endpoint. Retrieve the created {@link
     * AsyncNodeServer} with the method {@code
     * RemotePoolClientUtils.getAsyncNode()}
     *
     * @param asyncEndpoint non nullable instance of an async server endpoint
     * @return next configuration step
     * @since 1.0
     */
    TimeoutStep withAsyncNode(AsyncEndpointClient asyncEndpoint);

    /**
     * Configure the plugin to be used with a sync node.
     *
     * @param syncEndpoint non nullable instance of an sync client endpoint*
     * @return next configuration step
     * @since 1.0
     */
    TimeoutStep withSyncNode(SyncEndpointClient syncEndpoint);
  }

  public interface TimeoutStep {
    /**
     * Use the default timeout of 5 seconds. This timeout defines how long the client waits for a
     * server order before cancelling the global transaction.
     *
     * @return next configuration step
     * @since 1.0
     */
    BuilderStep usingDefaultTimeout();

    /**
     * Configure the service with a custom timeout. This timeout defines how long the client waits
     * for a server order before cancelling the global transaction.
     *
     * @param timeoutInSeconds timeout in seconds
     * @return next configuration step
     * @since 1.0
     */
    BuilderStep usingCustomTimeout(int timeoutInSeconds);
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
    RemotePoolClientPluginFactory build();
  }

  /** The builder pattern to create the factory instance. */
  public static class Builder implements NodeStep, BuilderStep, TimeoutStep {

    private AsyncEndpointClient asyncEndpoint;
    private SyncEndpointClient syncEndpoint;
    private int timeoutInSec;

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public TimeoutStep withAsyncNode(AsyncEndpointClient asyncEndpoint) {
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
    public TimeoutStep withSyncNode(SyncEndpointClient syncEndpoint) {
      Assert.getInstance().notNull(syncEndpoint, "syncEndpoint");
      this.syncEndpoint = syncEndpoint;
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public BuilderStep usingDefaultTimeout() {
      timeoutInSec = DEFAULT_TIMEOUT;
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public BuilderStep usingCustomTimeout(int timeoutInSeconds) {
      timeoutInSec = timeoutInSeconds;
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public RemotePoolClientPluginFactory build() {

      RemotePoolClientPluginImpl plugin = new RemotePoolClientPluginImpl(DEFAULT_PLUGIN_NAME);

      if (asyncEndpoint != null) {
        logger.info("Create a new RemotePoolClientPlugin with a async client endpoint");
        plugin.bindAsyncNodeClient(asyncEndpoint, timeoutInSec);
      } else {
        logger.info("Create a new RemotePoolClientPlugin with a sync client endpoint");
        plugin.bindSyncNodeClient(syncEndpoint, null, null);
      }

      return new RemotePoolClientPluginFactory(plugin);
    }
  }
}
