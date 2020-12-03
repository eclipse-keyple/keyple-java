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

import static org.eclipse.keyple.plugin.remote.impl.LocalServiceClientFactory.DEFAULT_SERVICE_NAME;

import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.plugin.remote.AsyncNodeClient;
import org.eclipse.keyple.plugin.remote.LocalServiceClient;

/**
 * Utility class of the {@link LocalServiceClient}.
 *
 * @since 1.0
 */
public final class LocalServiceClientUtils {

  /**
   * Gets the service having the default name.
   *
   * @return a not null reference
   * @throws IllegalStateException if the service is not initialized.
   * @since 1.0
   */
  public static LocalServiceClient getLocalService() {
    return getServiceImpl(DEFAULT_SERVICE_NAME);
  }

  /**
   * Gets the local service by its name.
   *
   * @param serviceName identifier of the local service
   * @return a not null reference
   * @throws IllegalStateException if the service is not initialized.
   * @since 1.0
   */
  public static LocalServiceClient getLocalService(String serviceName) {
    Assert.getInstance().notNull(serviceName, "service name");
    return getServiceImpl(serviceName);
  }

  /**
   * Gets the {@link AsyncNodeClient} node associated to the service having the default name.
   *
   * @return a not null reference
   * @throws IllegalStateException if the service is not initialized or is not configured with a
   *     {@link AsyncNodeClient} node.
   * @since 1.0
   */
  public static AsyncNodeClient getAsyncNode() {
    return getAsyncNode(DEFAULT_SERVICE_NAME);
  }

  /**
   * Gets the {@link AsyncNodeClient} node associated to a local service.
   *
   * @param serviceName identifier of the local service
   * @return a not null reference
   * @throws IllegalStateException if the service is not initialized or is not configured with a
   *     {@link AsyncNodeClient} node.
   * @since 1.0
   */
  public static AsyncNodeClient getAsyncNode(String serviceName) {
    Assert.getInstance().notNull(serviceName, "service name");
    LocalServiceClientImpl service = getServiceImpl(serviceName);
    if (service.node instanceof AsyncNodeClient) {
      return (AsyncNodeClient) service.node;
    }
    throw new IllegalStateException(
        "The LocalServiceClient is not configured with a AsyncNodeClient");
  }

  /**
   * (private)<br>
   * Gets the service implementation by its service name.
   *
   * @param serviceName identifier of the local service
   * @return a not null reference
   * @throws IllegalStateException If there's no service having the provided name
   */
  private static LocalServiceClientImpl getServiceImpl(String serviceName) {
    Assert.getInstance().notNull(serviceName, "service name");
    LocalServiceClientImpl service = LocalServiceClientImpl.getInstance(serviceName);
    return service;
  }
}
