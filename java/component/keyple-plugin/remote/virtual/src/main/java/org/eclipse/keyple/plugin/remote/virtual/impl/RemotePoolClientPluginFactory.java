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
package org.eclipse.keyple.plugin.remote.virtual.impl;

import org.eclipse.keyple.core.seproxy.PluginFactory;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.plugin.remote.core.KeypleServerAsync;
import org.eclipse.keyple.plugin.remote.core.KeypleServerSyncNode;
import org.eclipse.keyple.plugin.remote.virtual.RemotePoolClientPlugin;
import org.eclipse.keyple.plugin.remote.virtual.RemoteServerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <b>Remote Pool Client Plugin</b> Factory
 *
 * <p>This factory must be used in the use case of the <b>Remote Pool Client Plugin</b>.
 *
 * <p>To register a Remote Pool Client Plugin, use the method {@link
 * org.eclipse.keyple.core.seproxy.SeProxyService#registerPlugin(PluginFactory)} fed in with an
 * instance of this factory. Invoke the {@link #builder()} method to create and configure a factory
 * instance.
 *
 * <p>Plugin name is defined by default in the factory. Access the Remote Pool Client Plugin with the
 * {@link RemotePoolClientUtils#getAsyncPlugin()} or {@link RemotePoolClientUtils#getSyncNode()} depending
 * on your node configuration.
 */
public class RemotePoolClientPluginFactory implements PluginFactory {

  private static final Logger logger = LoggerFactory.getLogger(RemotePoolClientPluginFactory.class);
  /** default name of the RemotePoolClientPlugin for a sync node : {@value} */
  static final String PLUGIN_NAME_SYNC = "RemotePoolClientPluginSync";
  /** default name of the RemotePoolClientPlugin for a async node : {@value} */
  static final String PLUGIN_NAME_ASYNC = "RemotePoolClientPluginAsync";

  private RemotePoolClientPlugin plugin;

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
  public ReaderPlugin getPlugin() {
    return plugin;
  }

  public interface NodeStep {
    /**
     * Configure the plugin with an async server endpoint. Retrieve the created {@link
     * org.eclipse.keyple.plugin.remote.core.KeypleServerAsyncNode} with the method {@code
     * RemotePoolClientUtils.getAsyncNode()}
     *
     * @param asyncEndpoint non nullable instance of an async server endpoint
     * @return next configuration step
     * @since 1.0
     */
    BuilderStep withAsyncNode(KeypleServerAsync asyncEndpoint);

    /**
     * Configure the plugin to be used with a sync node. Retrieve the created {@link
     * KeypleServerSyncNode} with the method {@link RemotePoolClientUtils#getSyncNode()}
     *
     * @return next configuration step
     * @since 1.0
     */
    BuilderStep withSyncNode();
  }


  public interface BuilderStep {
    /**
     * Build the plugin factory instance.
     *
     * <p>This instance should be passed to {@link
     * org.eclipse.keyple.core.seproxy.SeProxyService#registerPlugin(PluginFactory)} in order to
     * register the plugin.
     *
     * @return instance of the plugin factory
     * @since 1.0
     */
    RemotePoolClientPluginFactory build();
  }

  /** The builder pattern to create the factory instance. */
  public static class Builder
      implements NodeStep, BuilderStep {

    private KeypleServerAsync asyncEndpoint;
    private ExecutorService eventNotificationPool;
    private ObservablePlugin.PluginObserver observer;

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public BuilderStep withAsyncNode(KeypleServerAsync asyncEndpoint) {
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
    public BuilderStep withSyncNode() {
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public RemotePoolClientPluginFactory build() {

      RemotePoolClientPluginImpl plugin;

      if (asyncEndpoint != null) {
        plugin = new RemotePoolClientPluginImpl(PLUGIN_NAME_ASYNC);
        logger.info("Create a new RemotePoolClientPlugin with a async server endpoint");
        plugin.bindServerAsyncNode(asyncEndpoint);
      } else {
        plugin = new RemotePoolClientPluginImpl(PLUGIN_NAME_SYNC);
        logger.info("Create a new RemotePoolClientPlugin with a sync server endpoint");
        plugin.bindServerSyncNode();
      }

      return new RemotePoolClientPluginFactory(plugin);
    }
  }
}
