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
   * Gets the {@link AsyncNodeServer} node associated to the service having the default name.
   *
   * @return a not null reference
   * @throws IllegalStateException if the service is not initialized or is not configured with a
   *     {@link AsyncNodeServer} node.
   * @since 1.0
   */
  public static AsyncNodeServer getAsyncNode() {
    PoolLocalServiceServerImpl service = getServiceImpl();
    if (service.node instanceof AsyncNodeServer) {
      return (AsyncNodeServer) service.node;
    }
    throw new IllegalStateException(
        "The PoolLocalServiceServer is not configured with a AsyncNodeServer");
  }

  /**
   * Gets the {@link SyncNodeServer} node associated to the service having the default name.
   *
   * @return a not null reference
   * @throws IllegalStateException if the service is not initialized or is not configured with a
   *     {@link SyncNodeServer} node.
   * @since 1.0
   */
  public static SyncNodeServer getSyncNode() {
    PoolLocalServiceServerImpl service = getServiceImpl();
    if (service.node instanceof SyncNodeServer) {
      return (SyncNodeServer) service.node;
    }
    throw new IllegalStateException(
        "The PoolLocalServiceServer is not configured with a SyncNodeServer");
  }

  /**
   * (private)<br>
   * Gets the service implementation.
   *
   * @return a not null reference
   * @throws IllegalStateException if the service is not initialized.
   */
  private static PoolLocalServiceServerImpl getServiceImpl() {
    PoolLocalServiceServerImpl service = PoolLocalServiceServerImpl.getInstance();
    if (service == null) {
      throw new IllegalStateException("The PoolLocalServiceServer is not initialized");
    }
    return service;
  }
}
