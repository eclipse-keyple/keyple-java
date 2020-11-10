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
import org.eclipse.keyple.plugin.remote.SyncNodeServer;
import org.eclipse.keyple.plugin.remote.NativePoolServerService;

/**
 * Utility class associated to a {@link NativePoolServerService}
 *
 * @since 1.0
 */
public final class NativePoolServerUtils {

  /**
   * Get the async node associated to the Native Pool Server Service.
   *
   * @return a not null reference
   * @throws IllegalStateException if the service is not initialized or is not bounded to an async
   *     node.
   * @since 1.0
   */
  public static AsyncNodeServer getAsyncNode() {
    NativePoolServerServiceImpl service = getNativePoolServerService();
    if (service.getNode() instanceof AsyncNodeServer) {
      return (AsyncNodeServer) service.getNode();
    }
    throw new IllegalStateException(
        "The Native Pool Server Service is not bounded to an async node");
  }

  /**
   * Get the sync node associated to the Native Pool Server Service.
   *
   * @return a not null reference
   * @throws IllegalStateException if the service is not initialized or is not bounded to a sync
   *     node.
   * @since 1.0
   */
  public static SyncNodeServer getSyncNode() {
    NativePoolServerServiceImpl service = getNativePoolServerService();
    if (service.getNode() instanceof SyncNodeServer) {
      return (SyncNodeServer) service.getNode();
    }
    throw new IllegalStateException(
        "The Native Pool Server Service is not bounded to an sync node");
  }

  /**
   * (private)<br>
   * Get the Native Pool Server Service implementation
   *
   * @return a not null reference
   * @throws IllegalStateException if the service is not initialized.
   */
  private static NativePoolServerServiceImpl getNativePoolServerService() {
    NativePoolServerServiceImpl service = NativePoolServerServiceImpl.getInstance();
    if (service == null) {
      throw new IllegalStateException("The Native Pool Server Service is not initialized");
    }
    return service;
  }
}
