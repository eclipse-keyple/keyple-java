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
import org.eclipse.keyple.plugin.remote.AsyncNodeServer;
import org.eclipse.keyple.plugin.remote.PoolLocalServiceServer;
import org.eclipse.keyple.plugin.remote.SyncNodeServer;

/**
 * Utility class of the {@link PoolLocalServiceServer}.
 *
 * @since 1.0
 */
public final class PoolLocalServiceServerUtils {

  /**
   * Gets the {@link AsyncNodeServer} node associated to the local service having the default name.
   *
   * @return A not null reference.
   * @throws IllegalStateException If the service is not initialized or is not configured with a
   *     {@link AsyncNodeServer} node.
   * @since 1.0
   */
  public static AsyncNodeServer getAsyncNode() {
    return getAsyncNode(PoolLocalServiceServerFactory.DEFAULT_SERVICE_NAME);
  }

  /**
   * Gets the {@link AsyncNodeServer} node associated to the local service having the provided name.
   *
   * @param serviceName The identifier of the local service.
   * @return A not null reference.
   * @throws IllegalArgumentException If the service name is null.
   * @throws IllegalStateException If the service is not initialized or is not configured with a
   *     {@link AsyncNodeServer} node.
   * @since 1.0
   */
  public static AsyncNodeServer getAsyncNode(String serviceName) {
    Assert.getInstance().notNull(serviceName, "service name");
    PoolLocalServiceServerImpl service = getServiceImpl(serviceName);
    if (service.node instanceof AsyncNodeServer) {
      return (AsyncNodeServer) service.node;
    }
    throw new IllegalStateException(
        "The PoolLocalServiceServer is not configured with a AsyncNodeServer");
  }

  /**
   * Gets the {@link SyncNodeServer} node associated to the local service having the provided name.
   *
   * @return A not null reference.
   * @throws IllegalStateException If the service is not initialized or is not configured with a
   *     {@link SyncNodeServer} node.
   * @since 1.0
   */
  public static SyncNodeServer getSyncNode() {
    return getSyncNode(PoolLocalServiceServerFactory.DEFAULT_SERVICE_NAME);
  }

  /**
   * Gets the {@link SyncNodeServer} node associated to the local service having the provided name.
   *
   * @param serviceName The identifier of the local service.
   * @return A not null reference.
   * @throws IllegalArgumentException If the service name is null.
   * @throws IllegalStateException If the service is not initialized or is not configured with a
   *     {@link SyncNodeServer} node.
   * @since 1.0
   */
  public static SyncNodeServer getSyncNode(String serviceName) {
    Assert.getInstance().notNull(serviceName, "service name");
    PoolLocalServiceServerImpl service = getServiceImpl(serviceName);
    if (service.node instanceof SyncNodeServer) {
      return (SyncNodeServer) service.node;
    }
    throw new IllegalStateException(
        "The PoolLocalServiceServer is not configured with a SyncNodeServer");
  }

  /**
   * (private)<br>
   * Gets the service implementation having the provided name.
   *
   * @param serviceName The identifier of the local service.
   * @return A not null reference.
   * @throws IllegalStateException If there's no service having the provided name.
   */
  private static PoolLocalServiceServerImpl getServiceImpl(String serviceName) {
    PoolLocalServiceServerImpl service = PoolLocalServiceServerImpl.getInstance(serviceName);
    return service;
  }
}
