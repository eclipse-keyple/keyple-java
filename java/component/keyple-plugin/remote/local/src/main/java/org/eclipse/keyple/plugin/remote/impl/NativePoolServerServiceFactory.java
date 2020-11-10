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
import org.eclipse.keyple.core.service.ReaderPoolPlugin;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointServer;
import org.eclipse.keyple.plugin.remote.NativePoolServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This factory must be used to initialize a {@link NativePoolServerService}
 *
 * @since 1.0
 */
public final class NativePoolServerServiceFactory {

  private static final Logger logger =
      LoggerFactory.getLogger(NativePoolServerServiceFactory.class);

  /**
   * Init the builder
   *
   * @return next configuration step
   * @since 1.0
   */
  public NativePoolServerServiceFactory.NodeStep builder() {
    return new NativePoolServerServiceFactory.Step();
  }

  public interface BuilderStep {
    /**
     * Build the service
     *
     * @return singleton instance of the service
     * @since 1.0
     */
    NativePoolServerService getService();
  }

  public interface NodeStep {
    /**
     * Configure the service with an async server
     *
     * @param endpoint non nullable instance of an async client
     * @return next configuration step
     * @since 1.0
     */
    PluginStep withAsyncNode(AsyncEndpointServer endpoint);

    /**
     * Configure the service with a sync server
     *
     * @return next configuration step
     * @since 1.0
     */
    PluginStep withSyncNode();
  }

  public interface PluginStep {

    /**
     * Configure the service with one or more {@link ReaderPoolPlugin} plugin(s).
     *
     * @param poolPluginNames one or more reader plugin names of ReaderPoolPlugin
     * @return next configuration step
     */
    BuilderStep withPoolPlugins(String... poolPluginNames);
  }

  private static class Step implements NodeStep, BuilderStep, PluginStep {

    private AsyncEndpointServer asyncEndpoint;
    private String[] poolPluginNames;

    private Step() {}

    @Override
    public PluginStep withAsyncNode(AsyncEndpointServer endpoint) {
      Assert.getInstance().notNull(endpoint, "endpoint");
      this.asyncEndpoint = endpoint;
      return this;
    }

    @Override
    public PluginStep withSyncNode() {
      return this;
    }

    @Override
    public BuilderStep withPoolPlugins(String... poolPluginNames) {
      Assert.getInstance().notNull(poolPluginNames, "poolPluginNames");
      // verify that each plugin is instance of ReaderPoolPlugin
      for (String poolPluginName : poolPluginNames) {
        Plugin plugin = SmartCardService.getInstance().getPlugin(poolPluginName);
        if (!(plugin instanceof ReaderPoolPlugin)) {
          throw new IllegalArgumentException(
              "Invalid plugin type for plugin "
                  + poolPluginName
                  + ", only ReaderPoolPlugin are valid");
        }
      }
      this.poolPluginNames = poolPluginNames;
      return this;
    }

    @Override
    public NativePoolServerService getService() {
      NativePoolServerServiceImpl nativePoolServerServiceImpl =
          NativePoolServerServiceImpl.createInstance(poolPluginNames);
      if (asyncEndpoint != null) {
        nativePoolServerServiceImpl.bindServerAsyncNode(asyncEndpoint);
        logger.info("Create a new NativePoolServerService with a async server");
      } else {
        nativePoolServerServiceImpl.bindServerSyncNode();
        logger.info("Create a new NativePoolServerService with a sync server");
      }

      return nativePoolServerServiceImpl;
    }
  }
}
