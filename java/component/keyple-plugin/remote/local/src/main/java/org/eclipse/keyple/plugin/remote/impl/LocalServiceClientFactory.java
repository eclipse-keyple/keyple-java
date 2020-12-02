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
import org.eclipse.keyple.plugin.remote.spi.ObservableReaderEventFilter;
import org.eclipse.keyple.plugin.remote.spi.SyncEndpointClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class of the {@link LocalServiceClient}.
 *
 * @since 1.0
 */
public final class LocalServiceClientFactory {

  private static final Logger logger = LoggerFactory.getLogger(LocalServiceClientFactory.class);
  private static final int DEFAULT_TIMEOUT = 5;

  private static final String DEFAULT_SERVICE_NAME = "defaultLocalServiceClient";

  /**
   * (private)<br>
   * Constructor
   */
  private LocalServiceClientFactory() {}

  /**
   * Init the builder
   *
   * @return next configuration step
   * @since 1.0
   */
  public static NameStep builder() {
    return new Step();
  }

  public interface BuilderStep {
    /**
     * Builds and gets the service.
     *
     * @return singleton instance of the service
     * @since 1.0
     */
    LocalServiceClient getService();
  }

  public interface NameStep {
    /**
     * Configures the service with a specific name.
     *
     * @param serviceName identifier of the local service
     * @return next configuration step
     * @since 1.0
     */
    NodeStep withServiceName(String serviceName);

    /**
     * Configures the service with the a specific service name. Note that if the service already
     * exists, it will be overridden
     *
     * @return next configuration step
     * @since 1.0
     */
    NodeStep withDefaultServiceName();
  }

  public interface NodeStep {
    /**
     * Configures the service with a {@link org.eclipse.keyple.plugin.remote.AsyncNodeClient} node.
     *
     * @param endpoint The {@link AsyncEndpointClient} network endpoint to use.
     * @return next configuration step
     * @since 1.0
     */
    TimeoutStep withAsyncNode(AsyncEndpointClient endpoint);

    /**
     * Configures the service with a {@link org.eclipse.keyple.plugin.remote.SyncNodeClient} node.
     *
     * @param endpoint The {@link SyncEndpointClient} network endpoint to use.
     * @return next configuration step
     * @since 1.0
     */
    ReaderStep withSyncNode(SyncEndpointClient endpoint);
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
    ReaderStep usingDefaultTimeout();

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
    ReaderStep usingTimeout(int timeoutInSeconds);
  }

  public interface ReaderStep {
    /**
     * Activates the observation of the local reader events.
     *
     * @param eventFilter The {@link ObservableReaderEventFilter} event filter to use.
     * @return next configuration step
     * @since 1.0
     */
    BuilderStep withReaderObservation(ObservableReaderEventFilter eventFilter);

    /**
     * Do not activates the observation of the local reader events.
     *
     * @return next configuration step
     * @since 1.0
     */
    BuilderStep withoutReaderObservation();
  }

  private static class Step implements NameStep, NodeStep, ReaderStep, BuilderStep, TimeoutStep {

    private AsyncEndpointClient asyncEndpoint;
    private SyncEndpointClient syncEndpoint;
    private Boolean withReaderObservation;
    private ObservableReaderEventFilter eventFilter;
    private int timeoutInSec;
    private String serviceName;

    private Step() {}

    /** {@inheritDoc} */
    @Override
    public NodeStep withServiceName(String serviceName) {
      Assert.getInstance().notNull(serviceName, "service name");
      this.serviceName = serviceName;
      return this;
    }

    /** {@inheritDoc} */
    @Override
    public NodeStep withDefaultServiceName() {
      this.serviceName = DEFAULT_SERVICE_NAME;
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
    public ReaderStep withSyncNode(SyncEndpointClient endpoint) {
      Assert.getInstance().notNull(endpoint, "endpoint");
      this.syncEndpoint = endpoint;
      return this;
    }

    /** {@inheritDoc} */
    @Override
    public BuilderStep withoutReaderObservation() {
      this.withReaderObservation = false;
      return this;
    }

    /** {@inheritDoc} */
    @Override
    public BuilderStep withReaderObservation(ObservableReaderEventFilter eventFilter) {
      Assert.getInstance().notNull(eventFilter, "eventFilter");
      this.withReaderObservation = true;
      this.eventFilter = eventFilter;
      return this;
    }

    /** {@inheritDoc} */
    @Override
    public LocalServiceClient getService() {

      // create the service
      LocalServiceClientImpl service =
          LocalServiceClientImpl.createInstance(serviceName, withReaderObservation, eventFilter);

      // bind the service to the node
      if (asyncEndpoint != null) {
        logger.info(
            "Create a new LocalServiceClientImpl with a AsyncNodeClient and withReaderObservation={}",
            withReaderObservation);
        service.bindAsyncNodeClient(asyncEndpoint, timeoutInSec);
      } else {
        logger.info(
            "Create a new LocalServiceClientImpl with a SyncNodeClient and withReaderObservation={}",
            withReaderObservation);
        service.bindSyncNodeClient(syncEndpoint, null, null);
      }
      return service;
    }

    /** {@inheritDoc} */
    @Override
    public ReaderStep usingDefaultTimeout() {
      timeoutInSec = DEFAULT_TIMEOUT;
      return this;
    }

    /** {@inheritDoc} */
    @Override
    public ReaderStep usingTimeout(int timeoutInSeconds) {
      timeoutInSec = timeoutInSeconds;
      return this;
    }
  }
}
