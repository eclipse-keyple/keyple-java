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

import static org.eclipse.keyple.distributed.impl.LocalServiceClientFactory.DEFAULT_SERVICE_NAME;

import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.distributed.AsyncNodeClient;
import org.eclipse.keyple.distributed.LocalServiceClient;

/**
 * Utility class of the {@link LocalServiceClient}.
 *
 * @since 1.0
 */
public final class LocalServiceClientUtils {

  private LocalServiceClientUtils() {}

  /**
   * Gets the local service having the default name.
   *
   * @return A not null reference.
   * @throws IllegalStateException If the service is not initialized.
   * @since 1.0
   */
  public static LocalServiceClient getLocalService() {
    return getServiceImpl(DEFAULT_SERVICE_NAME);
  }

  /**
   * Gets the local service having the provided name.
   *
   * @param serviceName The identifier of the local service.
   * @return A not null reference.
   * @throws IllegalArgumentException If the service name is null.
   * @throws IllegalStateException If the service is not initialized.
   * @since 1.0
   */
  public static LocalServiceClient getLocalService(String serviceName) {
    Assert.getInstance().notNull(serviceName, "serviceName");
    return getServiceImpl(serviceName);
  }

  /**
   * Gets the {@link AsyncNodeClient} node associated to the local service having the default name.
   *
   * @return A not null reference.
   * @throws IllegalStateException If the service is not initialized or is not configured with a
   *     {@link AsyncNodeClient} node.
   * @since 1.0
   */
  public static AsyncNodeClient getAsyncNode() {
    return getAsyncNode(DEFAULT_SERVICE_NAME);
  }

  /**
   * Gets the {@link AsyncNodeClient} node associated to the local service having the provided name.
   *
   * @param serviceName The identifier of the local service.
   * @return A not null reference.
   * @throws IllegalArgumentException If the service name is null.
   * @throws IllegalStateException If the service is not initialized or is not configured with a
   *     {@link AsyncNodeClient} node.
   * @since 1.0
   */
  public static AsyncNodeClient getAsyncNode(String serviceName) {
    Assert.getInstance().notNull(serviceName, "serviceName");
    LocalServiceClientImpl service = getServiceImpl(serviceName);
    if (service.node instanceof AsyncNodeClient) {
      return (AsyncNodeClient) service.node;
    }
    throw new IllegalStateException(
        "The LocalServiceClient is not configured with a AsyncNodeClient");
  }

  /**
   * (private)<br>
   * Gets the service implementation having the provided name.
   *
   * @param serviceName The identifier of the local service.
   * @return A not null reference.
   * @throws IllegalStateException If there's no service having the provided name.
   */
  private static LocalServiceClientImpl getServiceImpl(String serviceName) {
    return LocalServiceClientImpl.getInstance(serviceName);
  }
}
