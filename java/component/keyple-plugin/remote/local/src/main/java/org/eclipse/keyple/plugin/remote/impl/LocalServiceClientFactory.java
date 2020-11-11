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

import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.plugin.remote.LocalServiceClient;
import org.eclipse.keyple.plugin.remote.spi.AsyncEndpointClient;
import org.eclipse.keyple.plugin.remote.ObservableReaderEventFilter;
import org.eclipse.keyple.plugin.remote.spi.SyncEndpointClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This factory must be used to initialize a {@link LocalServiceClient}
 *
 * @since 1.0
 */
public class LocalServiceClientFactory {

  private static final Logger logger = LoggerFactory.getLogger(LocalServiceClientFactory.class);
  private static final int DEFAULT_TIMEOUT = 5;

  /**
   * Init the builder
   *
   * @return next configuration step
   * @since 1.0
   */
  public NodeStep builder() {
    return new Step();
  }

  public interface BuilderStep {
    /**
     * Build the service
     *
     * @return singleton instance of the service
     * @since 1.0
     */
    LocalServiceClient getService();
  }

  public interface TimeoutStep {
    /**
     * Use the default timeout of 5 seconds. This timeout defines how long the client waits for a
     * server order before cancelling the global transaction.
     *
     * @return next configuration step
     * @since 1.0
     */
    ReaderStep usingDefaultTimeout();

    /**
     * Configure the service with a custom timeout. This timeout defines how long the client waits
     * for a server order before cancelling the global transaction.
     *
     * @param timeoutInSeconds timeout in seconds
     * @return next configuration step
     * @since 1.0
     */
    ReaderStep usingCustomTimeout(int timeoutInSeconds);
  }

  public interface NodeStep {
    /**
     * Configure the service with an async Client
     *
     * @param asyncClient non nullable instance of an async client
     * @return next configuration step
     * @since 1.0
     */
    TimeoutStep withAsyncNode(AsyncEndpointClient asyncClient);

    /**
     * Configure the service with a sync Client
     *
     * @param syncClient non nullable instance of a sync client
     * @return next configuration step
     * @since 1.0
     */
    ReaderStep withSyncNode(SyncEndpointClient syncClient);
  }

  public interface ReaderStep {
    /**
     * Configure the service to observe the local reader
     *
     * @param eventFilter non-nullable event filter
     * @return next configuration step
     * @since 1.0
     */
    BuilderStep withReaderObservation(ObservableReaderEventFilter eventFilter);

    /**
     * Configure the service without observation
     *
     * @return next configuration step
     * @since 1.0
     */
    BuilderStep withoutReaderObservation();
  }

  private static class Step implements NodeStep, ReaderStep, BuilderStep, TimeoutStep {

    private AsyncEndpointClient asyncEndpoint;
    private SyncEndpointClient syncEndpoint;
    private Boolean withReaderObservation;
    private ObservableReaderEventFilter eventFilter;
    private int timeoutInSec;

    private Step() {}

    @Override
    public TimeoutStep withAsyncNode(AsyncEndpointClient endpoint) {
      Assert.getInstance().notNull(endpoint, "endpoint");
      this.asyncEndpoint = endpoint;
      return this;
    }

    @Override
    public ReaderStep withSyncNode(SyncEndpointClient endpoint) {
      Assert.getInstance().notNull(endpoint, "endpoint");
      this.syncEndpoint = endpoint;
      return this;
    }

    @Override
    public BuilderStep withoutReaderObservation() {
      this.withReaderObservation = false;
      return this;
    }

    @Override
    public BuilderStep withReaderObservation(ObservableReaderEventFilter eventFilter) {
      Assert.getInstance().notNull(eventFilter, "eventFilter");
      this.withReaderObservation = true;
      this.eventFilter = eventFilter;
      return this;
    }

    @Override
    public LocalServiceClient getService() {

      // create the service
      LocalServiceClientImpl service =
          LocalServiceClientImpl.createInstance(withReaderObservation, eventFilter);

      // bind the service to the node
      if (asyncEndpoint != null) {
        logger.info(
            "Create a new LocalServiceClientImpl with a async client and params withReaderObservation:{}",
            withReaderObservation);
        service.bindClientAsyncNode(asyncEndpoint, timeoutInSec);
      } else {
        logger.info(
            "Create a new LocalServiceClientImpl with a sync client and params withReaderObservation:{}",
            withReaderObservation);
        service.bindClientSyncNode(syncEndpoint, null, null);
      }
      return service;
    }

    @Override
    public ReaderStep usingDefaultTimeout() {
      timeoutInSec = DEFAULT_TIMEOUT;
      return this;
    }

    @Override
    public ReaderStep usingCustomTimeout(int timeoutInSeconds) {
      timeoutInSec = timeoutInSeconds;
      return this;
    }
  }
}
