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

import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.plugin.remote.core.KeypleClientAsync;
import org.eclipse.keyple.plugin.remote.core.KeypleClientReaderEventFilter;
import org.eclipse.keyple.plugin.remote.core.KeypleClientSync;
import org.eclipse.keyple.plugin.remote.nativ.NativeClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This factory must be used to initialize a {@link NativeClientService}
 *
 * @since 1.0
 */
public class NativeClientServiceFactory {

  private static final Logger logger = LoggerFactory.getLogger(NativeClientServiceFactory.class);
  private static final int DEFAULT_TIMEOUT = 5;

  /**
   * Init the builder
   *
   * @return next configuration step
   */
  public NodeStep builder() {
    return new Step();
  }

  public interface BuilderStep {
    /**
     * Build the service
     *
     * @return singleton instance of the service
     */
    NativeClientService getService();
  }

  public interface TimeoutStep {
    /**
     * Use the default timeout of 5 seconds. This timeout defines how long the client waits for a
     * server order before cancelling the global transaction.
     *
     * @return next configuration step
     */
    ReaderStep usingDefaultTimeout();

    /**
     * Configure the service with a custom timeout. This timeout defines how long the client waits
     * for a server order before cancelling the global transaction.
     *
     * @param timeoutInSeconds timeout in seconds
     * @return next configuration step
     */
    ReaderStep usingCustomTimeout(int timeoutInSeconds);
  }

  public interface NodeStep {
    /**
     * Configure the service with an async Client
     *
     * @param asyncClient non nullable instance of an async client
     * @return next configuration step
     */
    TimeoutStep withAsyncNode(KeypleClientAsync asyncClient);

    /**
     * Configure the service with a sync Client
     *
     * @param syncClient non nullable instance of a sync client
     * @return next configuration step
     */
    ReaderStep withSyncNode(KeypleClientSync syncClient);
  }

  public interface ReaderStep {
    /**
     * Configure the service to observe the local reader
     *
     * @param eventFilter non-nullable event filter
     * @return next configuration step
     */
    BuilderStep withReaderObservation(KeypleClientReaderEventFilter eventFilter);

    /**
     * Configure the service without observation
     *
     * @return next configuration step
     */
    BuilderStep withoutReaderObservation();
  }

  private static class Step implements NodeStep, ReaderStep, BuilderStep, TimeoutStep {

    private KeypleClientAsync asyncEndpoint;
    private KeypleClientSync syncEndpoint;
    private Boolean withReaderObservation;
    private KeypleClientReaderEventFilter eventFilter;
    private int timeoutInSec;

    private Step() {}

    @Override
    public TimeoutStep withAsyncNode(KeypleClientAsync endpoint) {
      Assert.getInstance().notNull(endpoint, "endpoint");
      this.asyncEndpoint = endpoint;
      return this;
    }

    @Override
    public ReaderStep withSyncNode(KeypleClientSync endpoint) {
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
    public BuilderStep withReaderObservation(KeypleClientReaderEventFilter eventFilter) {
      Assert.getInstance().notNull(eventFilter, "eventFilter");
      this.withReaderObservation = true;
      this.eventFilter = eventFilter;
      return this;
    }

    @Override
    public NativeClientService getService() {

      // create the service
      NativeClientServiceImpl service =
          NativeClientServiceImpl.createInstance(withReaderObservation, eventFilter);

      // bind the service to the node
      if (asyncEndpoint != null) {
        logger.info(
            "Create a new NativeClientServiceImpl with a async client and params withReaderObservation:{}",
            withReaderObservation);
        service.bindClientAsyncNode(asyncEndpoint, timeoutInSec);
      } else {
        logger.info(
            "Create a new NativeClientServiceImpl with a sync client and params withReaderObservation:{}",
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
