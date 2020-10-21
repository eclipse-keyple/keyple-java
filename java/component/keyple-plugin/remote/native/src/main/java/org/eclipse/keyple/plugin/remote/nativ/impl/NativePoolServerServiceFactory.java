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
package org.eclipse.keyple.plugin.remote.nativ.impl;

import org.eclipse.keyple.core.seproxy.ReaderPoolPlugin;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.plugin.remote.core.KeypleServerAsync;
import org.eclipse.keyple.plugin.remote.nativ.NativePoolServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This factory must be used to initialize a {@link NativePoolServerService}
 *
 * @since 1.0
 */
public class NativePoolServerServiceFactory {

  private static final Logger logger =
      LoggerFactory.getLogger(NativePoolServerServiceFactory.class);

  /**
   * Init the builder
   *
   * @return next configuration step
   */
  public NativePoolServerServiceFactory.PluginStep builder() {
    return new NativePoolServerServiceFactory.Step();
  }

  public interface BuilderStep {
    /**
     * Build the service
     *
     * @return singleton instance of the service
     */
    NativePoolServerService getService();
  }

  public interface NodeStep {
    /**
     * Configure the service with an async server
     *
     * @param asyncServer non nullable instance of an async client
     * @return next configuration step
     */
    BuilderStep withAsyncNode(KeypleServerAsync asyncServer);

    /**
     * Configure the service with a sync server
     *
     * @return next configuration step
     */
    BuilderStep withSyncNode();
  }

  public interface PluginStep {
    /**
     * Configure the service with a local reader pool plugin
     *
     * @param poolPlugin non nullable instance of a reader pool plugin
     * @return next configuration step
     */
    NodeStep withReaderPoolPlugin(ReaderPoolPlugin poolPlugin);
  }

  private static class Step implements NodeStep, BuilderStep, PluginStep {

    private KeypleServerAsync asyncEndpoint;
    private ReaderPoolPlugin readerPoolPlugin;

    private Step() {}

    @Override
    public BuilderStep withAsyncNode(KeypleServerAsync endpoint) {
      Assert.getInstance().notNull(endpoint, "endpoint");
      this.asyncEndpoint = endpoint;
      return this;
    }

    @Override
    public BuilderStep withSyncNode() {
      return this;
    }

    @Override
    public NativePoolServerService getService() {
      NativePoolServerServiceImpl nativePoolServerServiceImpl =
          NativePoolServerServiceImpl.createInstance(readerPoolPlugin);

      if (asyncEndpoint != null) {
        nativePoolServerServiceImpl.bindServerAsyncNode(asyncEndpoint);
        logger.info(
            "Create a new NativePoolServerService with a async server and plugin {}",
            readerPoolPlugin.getName());
      } else {
        nativePoolServerServiceImpl.bindServerSyncNode();
        logger.info(
            "Create a new NativePoolServerService with a sync server and plugin {}",
            readerPoolPlugin.getName());
      }

      return nativePoolServerServiceImpl;
    }

    @Override
    public NodeStep withReaderPoolPlugin(ReaderPoolPlugin readerPoolPlugin) {
      Assert.getInstance().notNull(readerPoolPlugin, "readerPoolPlugin");
      this.readerPoolPlugin = readerPoolPlugin;
      return this;
    }
  }
}
