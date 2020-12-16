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
package org.eclipse.keyple.distributed.impl;

import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.PoolPlugin;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.distributed.PoolLocalServiceServer;
import org.eclipse.keyple.distributed.spi.AsyncEndpointServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class of the {@link PoolLocalServiceServer}.
 *
 * @since 1.0
 */
public final class PoolLocalServiceServerFactory {

  private static final Logger logger = LoggerFactory.getLogger(PoolLocalServiceServerFactory.class);

  static final String DEFAULT_SERVICE_NAME = "defaultPoolLocalServiceServer";

  /**
   * (private)<br>
   * Constructor
   */
  private PoolLocalServiceServerFactory() {}

  /**
   * Init the builder
   *
   * @return next configuration step
   * @since 1.0
   */
  public static NameStep builder() {
    return new PoolLocalServiceServerFactory.Step();
  }

  /**
   * Last step : builds the service.
   *
   * @since 1.0
   */
  public interface BuilderStep {
    /**
     * Builds and gets the service.
     *
     * @return singleton instance of the service
     * @throws IllegalArgumentException if a service already exists with the same name.
     * @since 1.0
     */
    PoolLocalServiceServer getService();
  }

  /**
   * Step to configure the service name.
   *
   * @since 1.0
   */
  public interface NameStep {
    /**
     * Configures the service with a specific name.
     *
     * @param serviceName The identifier of the local service.
     * @return next configuration step
     * @throws IllegalArgumentException If the service name is null or if a service already exists
     *     with the same name.
     * @since 1.0
     */
    NodeStep withServiceName(String serviceName);

    /**
     * Configures the service with the default service name : {@value DEFAULT_SERVICE_NAME}.
     *
     * @return next configuration step
     * @throws IllegalArgumentException If a service already exists with the default name.
     * @since 1.0
     */
    NodeStep withDefaultServiceName();
  }

  /**
   * Step to configure the node associated with the service.
   *
   * @since 1.0
   */
  public interface NodeStep {
    /**
     * Configures the service with a {@link org.eclipse.keyple.distributed.AsyncNodeServer} node.
     *
     * @param endpoint The {@link AsyncEndpointServer} network endpoint to use.
     * @return next configuration step
     * @since 1.0
     */
    PluginStep withAsyncNode(AsyncEndpointServer endpoint);

    /**
     * Configures the service with a {@link org.eclipse.keyple.distributed.SyncNodeServer} node.
     *
     * @return next configuration step
     * @since 1.0
     */
    PluginStep withSyncNode();
  }

  /**
   * Step to select the target plugins to manage.
   *
   * @since 1.0
   */
  public interface PluginStep {
    /**
     * Configures the service with one or more {@link PoolPlugin} plugin(s).
     *
     * @param poolPluginNames One or more plugin names of PoolPlugin
     * @return next configuration step
     */
    BuilderStep withPoolPlugins(String... poolPluginNames);
  }

  private static class Step implements NameStep, NodeStep, BuilderStep, PluginStep {

    private AsyncEndpointServer asyncEndpoint;
    private String[] poolPluginNames;
    private String serviceName;

    private Step() {}

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public NodeStep withServiceName(String serviceName) {
      Assert.getInstance().notNull(serviceName, "serviceName");
      this.serviceName = serviceName;
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public NodeStep withDefaultServiceName() {
      this.serviceName = DEFAULT_SERVICE_NAME;
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public PluginStep withAsyncNode(AsyncEndpointServer endpoint) {
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
    public PluginStep withSyncNode() {
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public BuilderStep withPoolPlugins(String... poolPluginNames) {
      Assert.getInstance().notNull(poolPluginNames, "poolPluginNames");
      // verify that each plugin is instance of ReaderPoolPlugin
      for (String poolPluginName : poolPluginNames) {
        Plugin plugin = SmartCardService.getInstance().getPlugin(poolPluginName);
        if (!(plugin instanceof PoolPlugin)) {
          throw new IllegalArgumentException(
              "Invalid plugin type for plugin "
                  + poolPluginName
                  + ", only ReaderPoolPlugin are valid");
        }
      }
      this.poolPluginNames = poolPluginNames;
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public PoolLocalServiceServer getService() {
      PoolLocalServiceServerImpl poolLocalServiceServerImpl =
          PoolLocalServiceServerImpl.createInstance(serviceName, poolPluginNames);
      if (asyncEndpoint != null) {
        poolLocalServiceServerImpl.bindAsyncNodeServer(asyncEndpoint);
        logger.info("Create a new PoolLocalServiceServer with a AsyncNodeServer");
      } else {
        poolLocalServiceServerImpl.bindSyncNodeServer();
        logger.info("Create a new PoolLocalServiceServer with a SyncNodeServer");
      }

      return poolLocalServiceServerImpl;
    }
  }
}
